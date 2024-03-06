package frc.robot.subsystems;
import java.beans.Encoder;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import com.revrobotics.AbsoluteEncoder;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.wpilibj.DutyCycleEncoder;
import edu.wpi.first.wpilibj.PneumaticsModuleType;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.Constants;
import swervelib.motors.TalonSRXSwerve;

public class AmpBar {
    enum AmpBarStates {
        IN,
        OUT
    }
    PIDController pivotController = new PIDController(1.5, 0, 0);
    private AmpBarStates state = AmpBarStates.OUT;
    private final WPI_TalonSRX leftMotor = new WPI_TalonSRX(40);
    private final WPI_TalonSRX rightMotor = new WPI_TalonSRX(45);
    private DutyCycleEncoder pivotEncoder = new DutyCycleEncoder(9);
    double pivotMotorSetpoint = Constants.AmpBar.IN;
    
    public AmpBar() {
        rightMotor.follow(leftMotor);
        leftMotor.setInverted(true);
        pivotEncoder.reset();
    }

    public void periodic() {
        if (state == AmpBarStates.OUT) {
            pivotMotorSetpoint = Constants.AmpBar.OUT;
        } else if (state == AmpBarStates.IN) {
            pivotMotorSetpoint = Constants.AmpBar.IN;
        }
        
        leftMotor.set(pivotController.calculate(pivotEncoder.getAbsolutePosition(), pivotMotorSetpoint));
        SmartDashboard.putNumber("Amp motor setpoint", pivotMotorSetpoint);
        SmartDashboard.putNumber("Current Amp motor postition", pivotEncoder.getAbsolutePosition());

    }

    public boolean nearSetpoint() {
        return Math.abs(pivotEncoder.getAbsolutePosition() - pivotMotorSetpoint) < 0.1;
    }

	public void setState(AmpBarStates state) {
        this.state = state;
    }
}