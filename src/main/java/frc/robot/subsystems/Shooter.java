package frc.robot.subsystems;

import com.ctre.phoenix6.hardware.TalonFX;

import edu.wpi.first.math.controller.BangBangController;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.wpilibj.DataLogManager;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import frc.robot.Robot;

import edu.wpi.first.util.datalog.*;

enum ShootingStates {
    SHOOTING,
    OFF,
    FEEDING,
    REVERSING
}

public class Shooter extends SubsystemBase {
    ShootingStates states = ShootingStates.OFF;
    Robot robot = null;
    public TalonFX shooterMotor1 = new TalonFX(14);
    public TalonFX shooterMotor2 = new TalonFX(15);
    BangBangController bangController = new BangBangController();
    String stateString = "";

    StringLogEntry stateStringLog;

    public Shooter(Robot robot) {
        this.robot = robot;
        shooterMotor2.setInverted(true);
        shooterMotor1.setInverted(false);

        DataLog dataLog = DataLogManager.getLog();
        stateStringLog = new StringLogEntry(dataLog, "/shooter/stateString");
    }

    public boolean atSetPoint() {
        double motor1Vel = shooterMotor1.getVelocity().getValueAsDouble();
        double motor2Vel = shooterMotor2.getVelocity().getValueAsDouble();
        return motor1Vel >= Constants.Shooter.SPEED && motor2Vel >= Constants.Shooter.SPEED;
    }

    public void setState(ShootingStates state) {
        this.states = state;
    }

    public void shooterFaults() {
        SmartDashboard.putBoolean("Shooter motor 1 good", shooterMotor1.getFaultField().getValue() == 0);
        SmartDashboard.putBoolean("Shooter motor 2 good", shooterMotor2.getFaultField().getValue() == 0);
    }

    public void periodic() {
        if (states == ShootingStates.OFF) {
            shooterMotor1.set(0);
            shooterMotor2.set(0);
            stateString = "Off";
        } else if (states == ShootingStates.SHOOTING) {
            shooterMotor1
                    .set(bangController.calculate(shooterMotor1.getVelocity().getValueAsDouble(), Constants.Shooter.SPEED));
            shooterMotor2
                    .set(bangController.calculate(shooterMotor2.getVelocity().getValueAsDouble(), Constants.Shooter.SPEED));
            stateString = "Shooting";
        } else if (states == ShootingStates.FEEDING) {
            shooterMotor1.set(Constants.Shooter.SLOW_SPEED);
            shooterMotor2.set(Constants.Shooter.SLOW_SPEED);
            stateString = "Feeding";
        } else if (states == ShootingStates.REVERSING) {
            shooterMotor1.set(-Constants.Shooter.SLOW_SPEED);
            shooterMotor2.set(-Constants.Shooter.SLOW_SPEED);
            stateString = "Reversing";
        }

        stateStringLog.append(stateString);
    }

    public void putSmartDashValues() {
        SmartDashboard.putNumber("Motor 1 velocity", shooterMotor1.getVelocity().getValueAsDouble());
        SmartDashboard.putNumber("Motor 2 velocity", shooterMotor2.getVelocity().getValueAsDouble());
        SmartDashboard.putString("Shooting States", stateString);
    }

    public void checkFaults() {
        SmartDashboard.putBoolean("Shooter Motor 1 working", shooterMotor1.getDeviceTemp().getValueAsDouble() > 0);
        SmartDashboard.putBoolean("Shooter Motor 2 working", shooterMotor2.getDeviceTemp().getValueAsDouble() > 0);
    }
}
