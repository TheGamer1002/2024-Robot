package frc.robot.subsystems;

import frc.robot.Constants;

import java.io.File;
import java.io.IOException;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.util.HolonomicPathFollowerConfig;
import com.pathplanner.lib.util.ReplanningConfig;
import com.revrobotics.CANSparkMax;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.DataLogManager;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Robot;
import swervelib.SwerveDrive;
import swervelib.math.SwerveMath;
import swervelib.parser.SwerveParser;
import swervelib.telemetry.SwerveDriveTelemetry;
import swervelib.SwerveModule;

import edu.wpi.first.util.datalog.*;

enum DriveStates {
    FIELD_ABSOLUTE,
    FIELD_RELATIVE
}

public class Drive extends SubsystemBase {
    SwerveDrive swerveDrive;
    DriveStates driveStates = DriveStates.FIELD_ABSOLUTE;
    Robot robot = null;
    boolean fieldRelative = false;
   
    StringLogEntry stateStringLog;
    BooleanLogEntry fieldRelativeLog;

    DoubleLogEntry robotPoseX;
    DoubleLogEntry robotPoseY;
    DoubleLogEntry robotPoseRotation;

    SwerveModule[] modules;

    public Drive (Robot robot) {
        SwerveDriveTelemetry.verbosity = SwerveDriveTelemetry.TelemetryVerbosity.LOW;
        this.robot = robot;

        double driveConversionFactor = SwerveMath.calculateMetersPerRotation(Units.inchesToMeters(Constants.Drive.wheelDiameter),
        Constants.Drive.driveGearRatio, 1);
        double angleConversionFactor = SwerveMath.calculateDegreesPerSteeringRotation(Constants.Drive.angleGearRatio, 1);

        try {
            SwerveParser swerveParser = new SwerveParser(new File(Filesystem.getDeployDirectory(), Constants.Drive.pathPlannerFile));
            swerveDrive = swerveParser.createSwerveDrive(Constants.Drive.maxSpeed, angleConversionFactor, driveConversionFactor);
            modules = swerveDrive.getModules();
            pathPlannerInit();

            // UNTESTED (with changes)
            //swerveDrive.setHeadingCorrection(true, 0.01);

        } catch (IOException e) {
            e.printStackTrace();
        }

        DataLog dataLog = DataLogManager.getLog();
        stateStringLog = new StringLogEntry(dataLog, "/drive/stateString");
        fieldRelativeLog = new BooleanLogEntry(dataLog, "/drive/fieldRelative");

        robotPoseX = new DoubleLogEntry(dataLog, "/drive/pose/x");
        robotPoseY = new DoubleLogEntry(dataLog, "/drive/pose/y");
        robotPoseRotation = new DoubleLogEntry(dataLog, "/drive/pose/rotation");
    }

    public void setHeadingCorrection(boolean headingCorrection) {
        swerveDrive.setHeadingCorrection(headingCorrection, 0.01);
    }

    public void zeroGyro() {
        swerveDrive.zeroGyro();
    }
    
    public void resetOdometry() {
        swerveDrive.resetOdometry(new Pose2d(new Translation2d(0, 0), new Rotation2d()));
    }

    public void pathPlannerInit() {
        AutoBuilder.configureHolonomic(
            swerveDrive::getPose, // Robot pose supplier
            swerveDrive::resetOdometry, // Method to reset odometry (will be called if your auto has a starting pose)
            swerveDrive::getRobotVelocity, // ChassisSpeeds supplier. MUST BE ROBOT RELATIVE
            swerveDrive::drive, // Method that will drive the robot given ROBOT RELATIVE ChassisSpeeds
            new HolonomicPathFollowerConfig( // HolonomicPathFollowerConfig, this should likely live in your Constants class
                // More PID tuning would be nice
                Constants.Drive.translationPID, // Translation PID constants
                Constants.Drive.rotationPID, // Rotation PID constants
                Constants.Drive.maxModuleSpeed, // Max module speed, in m/s
                swerveDrive.swerveDriveConfiguration.getDriveBaseRadiusMeters(), // Drive base radius in meters. Distance from robot center to furthest module.
                new ReplanningConfig(true, true) // Default path replanning config.
            ),
            () -> {
                // Boolean supplier that controls when the path will be mirrored for the red alliance
                // This will flip the path being followed to the red side of the field.
                // THE ORIGIN WILL REMAIN ON THE BLUE SIDE
                var alliance = DriverStation.getAlliance();
                return alliance.isPresent() ? alliance.get() == DriverStation.Alliance.Red : false; 
            },
            this // Reference to this subsystem to set requirements
        );
    }

    public void checkFaults() {
        for (int i = 0; i < modules.length; i++) {
            CANSparkMax driveMotor = (CANSparkMax) modules[i].getDriveMotor().getMotor();
            CANSparkMax angleMotor = (CANSparkMax) modules[i].getAngleMotor().getMotor();

            SmartDashboard.putBoolean("Encoder " + i + " Good", !modules[i].getAbsoluteEncoderReadIssue());
            SmartDashboard.putBoolean("Drive motor " + i + " reachable", driveMotor.getFirmwareVersion() > 0 && driveMotor.getFaults() == 0);
            SmartDashboard.putBoolean("Angle motor " + i + " reachable", angleMotor.getFirmwareVersion() > 0 && angleMotor.getFaults() == 0);
        }
    }

    public void periodic() {
        String state = "";
        
        double xMovement = MathUtil.applyDeadband(-robot.controller.getLeftY(), Constants.STICK_DEADBAND);
        double rotation = MathUtil.applyDeadband(-robot.controller.getRightX(), Constants.STICK_DEADBAND);
        double yMovement = MathUtil.applyDeadband(robot.controller.getLeftX() * Constants.Drive.leftXSign, Constants.STICK_DEADBAND);


        if (driveStates == DriveStates.FIELD_ABSOLUTE) {
            state = "Field Absolute";
            fieldRelative = false;

            if (robot.controller.getXButtonPressed()) {
                driveStates = DriveStates.FIELD_RELATIVE;
                System.out.println("FIELD relative ON");
            }
        } else if (driveStates == DriveStates.FIELD_RELATIVE) {
            state = "Field Relative";
            fieldRelative = true;

            if (robot.controller.getXButtonPressed()) {
                driveStates = DriveStates.FIELD_ABSOLUTE;
                System.out.println("FIELD Relative OFF");
            }
        }

        if (robot.controller.getStartButtonPressed()) {
            swerveDrive.zeroGyro();
            System.out.println("Gyro Zeroed");
        } else if (robot.controller.getYButton()) {
            swerveDrive.lockPose();
        } else if (robot.controller.getLeftBumper()) {
            xMovement *= Constants.Drive.slowSpeedMultiplier;
            rotation *= Constants.Drive.slowRotationMultiplier;
            yMovement *= Constants.Drive.slowSpeedMultiplier;
        }

        swerveDrive.drive(new Translation2d(xMovement, yMovement), rotation, fieldRelative, false);
        SmartDashboard.putString("Drive State", state);
        
        fieldRelativeLog.append(fieldRelative);
        stateStringLog.append(state);

        Pose2d robotPose = swerveDrive.field.getRobotPose();
        robotPoseX.append(robotPose.getX());
        robotPoseY.append(robotPose.getY());
        robotPoseRotation.append(robotPose.getRotation().getDegrees());
    }

    public void addVisionMeasurement(Pose2d pose, double timestamp) {
        swerveDrive.addVisionMeasurement(pose, timestamp);
    }
}
