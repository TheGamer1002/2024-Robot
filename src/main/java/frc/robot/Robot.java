// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.XboxController;
import frc.robot.commands.AutoCommands;
import frc.robot.commands.Intaking;
import frc.robot.commands.ReturnRobotToIdle;
import frc.robot.commands.Shooting;
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

public class Robot extends TimedRobot {
    public XboxController controller = new XboxController(0);
    //Vision vision = new Vision();
    Drive drive = new Drive(this);
    Vision vision = new Vision();
    RGB rgb = new RGB(this);
    AutoCommands autoCommands = new AutoCommands(this);
    public Manager manager = new Manager(this);
    private final SendableChooser<String> chooser = new SendableChooser<>();

    public Command getAutonomousCommand(String autoName) {
        return new PathPlannerAuto(autoName);
    }

    @Override
    public void robotInit() {
        CameraServer.startAutomaticCapture();

        NamedCommands.registerCommand("Intaking", new Intaking(this));
        NamedCommands.registerCommand("Shoooting", new Shooting(this));
        NamedCommands.registerCommand("Return To Idle", new ReturnRobotToIdle(this));

        // TODO: Score Drive Backwards (2-17)
        // TODO: 2 note autos (score any close note) (2-21)
        // TODO: 3 note autos (score any 2 close notes) (2-23)
        // TODO: 3 note autos (score 1 close, 1 far) (2-24)
        // TODO: 4 note auto (all 3 close) (2-28)
        // TODO: 4 note auto (2 close and 1 far on the left) (2-28)
        // TODO: 4 note auto (2 close and 1 far on the right) (2-28)
        // TODO: 5 note auto (all 3 close and 1 far) (3-2)

        chooser.addOption("Drive Forwards", "Drive Forwards");
        chooser.addOption("Drive Backwards", "Drive Backwards");
        chooser.addOption("Do Nothing", "Do Nothing");
        chooser.addOption("Drive backwards, score preload", "Drive Backwards + Score");
        SmartDashboard.putData("Path Chooser", chooser);
    }

    @Override
    public void robotPeriodic() {
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
        System.out.println("Scheduling Auto");
        CommandScheduler.getInstance().cancelAll();
        drive.zeroGyro();
        drive.resetOdometry();
        getAutonomousCommand((chooser.getSelected() != null) ? chooser.getSelected() : "Do Nothing").schedule();
    }

    @Override
    public void autonomousPeriodic() {
        manager.periodic();
    }

    @Override
    public void teleopInit() {
        manager.intake.pivotMotor.setIdleMode(IdleMode.kBrake);

    }

    @Override
    public void teleopPeriodic() {
        drive.periodic();
        rgb.periodic();
        manager.periodic();
    }

    @Override
    public void disabledInit() {
        manager.intake.pivotMotor.setIdleMode(IdleMode.kCoast);
    }

    @Override
    public void disabledPeriodic() {
        manager.intake.putSmartDashValues();

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