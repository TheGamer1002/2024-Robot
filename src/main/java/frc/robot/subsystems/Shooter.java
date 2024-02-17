package frc.robot.subsystems;

import com.ctre.phoenix6.hardware.TalonFX;

import edu.wpi.first.math.controller.BangBangController;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import frc.robot.Robot;

enum ShootingStates {
    SHOOTING,
    OFF
}

public class Shooter extends SubsystemBase {
    ShootingStates states = ShootingStates.OFF;
    Robot robot = null;
    public TalonFX shooterMotor1 = new TalonFX(14);
    public TalonFX shooterMotor2 = new TalonFX(15);
    BangBangController bangController = new BangBangController();
    String stateString;

    public Shooter(Robot robot) {
        this.robot = robot;
        shooterMotor2.setInverted(true);
        shooterMotor1.setInverted(false);
    }

    public void setState(ShootingStates state) {
        this.states = state;
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
        }
    }

    public void putSmartDashValues() {
        SmartDashboard.putNumber("Motor 1 velocity", shooterMotor1.getVelocity().getValueAsDouble());
        SmartDashboard.putNumber("Motor 2 velocity", shooterMotor2.getVelocity().getValueAsDouble());
        SmartDashboard.putString("Shooting States", stateString);
    }

}
