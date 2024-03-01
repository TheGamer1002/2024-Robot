// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.XboxController;
import frc.robot.commands.AutoCommands;
import frc.robot.commands.Shooting;
import frc.robot.subsystems.Climber;
import frc.robot.subsystems.Drive;
import frc.robot.subsystems.RGB;
import frc.robot.subsystems.Vision;
import frc.robot.subsystems.Manager;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.auto.NamedCommands;
import com.pathplanner.lib.commands.PathPlannerAuto;
import com.pathplanner.lib.path.PathPlannerPath;
import com.revrobotics.CANSparkBase.IdleMode;

import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;

import edu.wpi.first.wpilibj.DataLogManager;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.util.datalog.DataLog;

public class Robot extends TimedRobot {
    public XboxController controller = new XboxController(0);
    public XboxController secondaryController = new XboxController(1);
    //Vision vision = new Vision();
    Drive drive = new Drive(this);
    Vision vision = new Vision();
    RGB rgb = new RGB(this);
    //Climber climber = new Climber(this);
    AutoCommands autoCommands = new AutoCommands(this);
    public Manager manager = new Manager(this);
    private final SendableChooser<String> chooser = new SendableChooser<>();
    

    public Command getAutonomousCommand(String autoName) {
        return new PathPlannerAuto(autoName);
    }

    @Override
    public void robotInit() {
        DataLogManager.start();
        DriverStation.startDataLog(DataLogManager.getLog());

        // climber.zeroClimber();
        CameraServer.startAutomaticCapture();

        NamedCommands.registerCommand("Intaking",  autoCommands.intaking());
        NamedCommands.registerCommand("Shooting", new Shooting(this));
        NamedCommands.registerCommand("Return To Idle", autoCommands.returnToIdle());
        NamedCommands.registerCommand("Speeding Up", autoCommands.startSpinningUp());

        // TODO: Score Drive Backwards (2-17)
        // TODO: 2 note autos (score any close note) (2-21)
        // TODO: 3 note autos (score any 2 close notes) (2-23)
        // TODO: 3 note autos (score 1 close, 1 far) (2-24)
        // TODO: 4 note auto (all 3 close) (2-28)
        // TODO: 4 note auto (2 close and 1 far on the left) (2-28)
        // TODO: 4 note auto (2 close and 1 far on the right) (2-28)
        // TODO: 5 note auto (all 3 close and 1 far) (3-2)
        
        // Misc Autos
        chooser.addOption("Drive Forwards", "Drive Forwards");
        chooser.addOption("Do Nothing", "Do Nothing");
        chooser.addOption("Drive backwards, score preload", "Drive Backwards + Score");
        chooser.addOption("PID Tuning Auto", "PID Tuning Auto");
        //Choreo Autos
        chooser.addOption("4 Note Choreo", "Optimized 4 Note");
        // 2 Note Autos
        chooser.addOption("Preload + Left Note", "Left Note");
        chooser.addOption("Preload + Middle Note", "Middle Note");
        chooser.addOption("Preload + Right Note", "Right Note");
        // 3 Note Autos
        chooser.addOption("Left + Mid", "Left + Mid");
        chooser.addOption("Mid + Right", "Mid + Right");
        chooser.addOption("Center Left + Left", "Center Left + Left");
        chooser.addOption("Right + Center Right", "Right + Center Right");
        chooser.addOption("Mid + Center Left", "Mid + Center Left");
        // 4 Note Autos
        chooser.addOption("All Close", "All Close");
        chooser.addOption("2 Close + Right Far", "2 Close + Right Far");
        chooser.addOption("2 Close + Left Far", "2 Close + Left Far");
        //5 Note Auto
        chooser.addOption("5 Note Auto", "5 Note Auto");
        
        
        SmartDashboard.putData("Path Chooser", chooser);
    }
    
    @Override
    public void robotPeriodic() {
        rgb.periodic();
        manager.periodic();
        // climber.periodic();
        CommandScheduler.getInstance().run();
        /* 
        vision.periodic();
        intake.putSmartDashValues();
        if (vision.getPose2d().isPresent()) {
            drive.addVisionMeasurement(vision.getPose2d().get(), Timer.getFPGATimestamp());
        } 
        */
   }

    @Override
    public void autonomousInit() {
        drive.setHeadingCorrection(false);
        System.out.println("Scheduling Auto");
        CommandScheduler.getInstance().cancelAll();
        drive.zeroGyro();
        drive.resetOdometry();
        getAutonomousCommand((chooser.getSelected() != null) ? chooser.getSelected() : "Do Nothing").schedule();
    }

    @Override
    public void autonomousPeriodic() {
    }

    @Override
    public void teleopInit() {
        // climber.zeroClimber();
        drive.setHeadingCorrection(true);
        
        manager.intake.setPivotMotorMode(IdleMode.kBrake);
    }

    @Override
    public void teleopPeriodic() {
        drive.periodic();
    }

    @Override
    public void disabledInit() {
        manager.intake.setPivotMotorMode(IdleMode.kCoast);
    }

    @Override
    public void disabledPeriodic() {
    }

    @Override
    public void testInit() {
    }

    @Override
    public void testPeriodic() {
    }

    @Override
    public void simulationInit() {
    }

    @Override
    public void simulationPeriodic() {
    }
}
