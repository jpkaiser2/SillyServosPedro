package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

public class TurretSubsystem {
    private final DcMotorEx turretMotor;
    private final Servo turretAngleServo;

    // ===== Tuning constants MUST be set for turret =====
    private static final double MOTOR_TICKS_PER_REV = 537.7; // goBILDA 5203 312RPM output ticks/rev
    private static final double TURRET_GEAR_RATIO = 4.0;     // 50T -> 200T
    private static final double TICKS_PER_TURRET_REV = MOTOR_TICKS_PER_REV * TURRET_GEAR_RATIO;


    private double rotationPowerCmd = 0.0;
    private double maxPower = 0.6;

    private double angleInput = 0.5;

    // "Soft zero" offset so angle=0 when turret faces forward
    private int zeroTicks = 0;

    // Optional soft limits (degrees). Set to null behavior by making wide.
    private double minDeg = -180.0;
    private double maxDeg = 180.0;

    public TurretSubsystem(HardwareMap hardwareMap, String turretMotorName, String turretAngleServoName) {
        this.turretMotor = hardwareMap.get(DcMotorEx.class, turretMotorName);
        this.turretMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        this.turretMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        this.turretMotor.setDirection(DcMotorSimple.Direction.FORWARD);

        this.turretAngleServo = hardwareMap.get(Servo.class, turretAngleServoName);
        this.turretAngleServo.setPosition(0.5);

        // Soft-zero at init: turret angle will be 0 at whatever position you are in during init.
        zeroTurretHere();
    }

    /** Call this when turret is physically pointing "forward" to define 0° */
    public void zeroTurretHere() {
        zeroTicks = turretMotor.getCurrentPosition();
    }

    /** Optional: if you want to reset encoder counts (not required) */
    public void resetEncoderHard() {
        turretMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        turretMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        zeroTicks = 0;
    }

    public void setSoftLimitsDeg(double minDeg, double maxDeg) {
        this.minDeg = minDeg;
        this.maxDeg = maxDeg;
    }

    public void setMaxPower(double maxPower) {
        this.maxPower = clip(maxPower, 0.0, 1.0);
    }

    // ---- Motor / Servo inputs ----

    /** For auto aim: set actual motor command power directly */
    public void setTurretPower(double power) {
        rotationPowerCmd = clip(power, -maxPower, maxPower);
    }

    /** Manual rotate turret by motor (gamepad2.right_stick_x) */
    public void setManualInput(double joystick) {
        rotationPowerCmd = clip(joystick, -maxPower, maxPower);
    }

    /** Adjust hood/angle servo by joystick -1..1 mapped to 0..1 */
    public void setAngleInput(double joystick) {
        double pos = (clip(joystick, -1.0, 1.0) * -0.5) + 0.5; // up is -y
        angleInput = clip(pos, 0.0, 1.0);
    }

    // ---- State access (what PID needs) ----

    public int getTurretTicks() {
        return turretMotor.getCurrentPosition() - zeroTicks;
    }

    /** Turret yaw angle in degrees (0° at zeroTurretHere()) */
    public double getTurretAngleDeg() {
        return (getTurretTicks() * 360.0) / TICKS_PER_TURRET_REV;
    }

    /** Turret yaw angle in radians */
    public double getTurretAngleRad() {
        return Math.toRadians(getTurretAngleDeg());
    }

    // ---- Update ----
    public void update() {
        // Optional soft limit clamp: prevent driving further into limits
        double deg = getTurretAngleDeg();
        double p = rotationPowerCmd;

        if ((deg <= minDeg && p < 0) || (deg >= maxDeg && p > 0)) {
            p = 0.0;
        }

        turretMotor.setPower(p);
        turretAngleServo.setPosition(angleInput);
    }

    public String getStatus() {
        return String.format("turretPower=%.2f angleServo=%.2f yawDeg=%.1f ticks=%d",
                turretMotor.getPower(), turretAngleServo.getPosition(), getTurretAngleDeg(), getTurretTicks());
    }

    private static double clip(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
