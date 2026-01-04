package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

public class TurretSubsystem {
    private final DcMotorEx turretMotor;
    private final Servo turretAngleServo;

    private double rotationInput = 0.0;
    private double maxPower = 0.6; // clamp power

    private double angleInput = 0.5; // -1..1 mapped to 0..1 position

    public TurretSubsystem(HardwareMap hardwareMap, String turretMotorName, String turretAngleServoName) {
        this.turretMotor = hardwareMap.get(DcMotorEx.class, turretMotorName);
        this.turretMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        this.turretMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        this.turretMotor.setDirection(DcMotorSimple.Direction.FORWARD);

        this.turretAngleServo = hardwareMap.get(Servo.class, turretAngleServoName);
        this.turretAngleServo.setPosition(0.5);
    }

    public void setMaxPower(double maxPower) {
        this.maxPower = Math.max(0.0, Math.min(1.0, maxPower));
    }

    /** Manual rotate turret by motor (gamepad2.right_stick_x) */
    public void setManualInput(double joystick) {
        this.rotationInput = joystick;
    }

    /** Adjust turret angle by servo (e.g., gamepad2.left_stick_y) */
    public void setAngleInput(double joystick) {
        // map -1..1 to 0..1 safely
        double pos = (Math.max(-1.0, Math.min(1.0, joystick)) * -0.5) + 0.5; // up is -y
        angleInput = Math.max(0.0, Math.min(1.0, pos));
    }

    public void update() {
        double p = Math.max(-maxPower, Math.min(maxPower, rotationInput));
        turretMotor.setPower(p);
        turretAngleServo.setPosition(angleInput);
    }

    public String getStatus() {
        return String.format("turretPower=%.2f angle=%.2f", turretMotor.getPower(), turretAngleServo.getPosition());
    }
}
