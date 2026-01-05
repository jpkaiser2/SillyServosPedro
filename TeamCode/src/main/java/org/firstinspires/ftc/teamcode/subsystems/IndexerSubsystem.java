package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

public class IndexerSubsystem {
    public enum Selection { POSITION_1, POSITION_2, POSITION_3 }

    private final DcMotorEx indexerMotor;    // motor with encoder controlling indexer
    private final Servo feedLeverServo;      // lever that feeds balls into intake

    // Temporary manual mode for free rotation/tuning (power control)
    private boolean manualMode = false;
    private static final double JOYSTICK_DEADBAND = 0.10;  // ignore tiny stick noise
    private double manualPower = 0.0;       // processed input after deadband

    // Preset positions in encoder ticks (user-provided; tune as needed)
    public static int POSITION_1 = 0;
    public static int POSITION_2 = 97;
    public static int POSITION_3 = 190;

    private Selection selection = Selection.POSITION_2; // default to middle

    // Lever pulse config
    private final ElapsedTime leverTimer = new ElapsedTime();
    private boolean leverPulsing = false;
    private long leverPulseMs = 200;
    private double leverIdlePos = 0.2;
    private double leverEngagedPos = 0.8;

    public IndexerSubsystem(HardwareMap hardwareMap, String indexerMotorName, String feedLeverServoName) {
        this.indexerMotor = hardwareMap.get(DcMotorEx.class, indexerMotorName);
        this.indexerMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        this.indexerMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        this.indexerMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        this.indexerMotor.setPower(0.0);

        this.feedLeverServo = hardwareMap.get(Servo.class, feedLeverServoName);
        this.feedLeverServo.setPosition(leverIdlePos);
    }

    public void setLeverConfig(long pulseMs, double idle, double engaged) {
        leverPulseMs = pulseMs; leverIdlePos = idle; leverEngagedPos = engaged;
    }

    /** Choose which preset aligns with the turret. Ignored when in manual mode. */
    public void setSelection(Selection sel) {
        if (manualMode) return;
        this.selection = sel;
        int target;
        switch (sel) {
            case POSITION_1:
                target = POSITION_1; break;
            case POSITION_2:
                target = POSITION_2; break;
            case POSITION_3:
            default:
                target = POSITION_3; break;
        }
        indexerMotor.setTargetPosition(target);
        indexerMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        indexerMotor.setPower(0.6); // move power; tune as needed
    }

    public Selection getSelection() { return selection; }

    /** Enable/disable temporary manual mode. */
    public void setManualMode(boolean enabled) {
        manualMode = enabled;
        // Reset power when switching modes
        manualPower = 0.0;
        // Ensure encoder is active
        indexerMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        indexerMotor.setPower(0.0);
    }
    public boolean isManualMode() { return manualMode; }

    /** While in manual mode, map joystick Y (-1..1) to motor power for free rotation. */
    public void setManualInput(double joystickY) {
        if (!manualMode) { manualPower = 0.0; return; }
        double p = -joystickY; // FTC sticks: up is -y, invert so up moves forward
        manualPower = (Math.abs(p) < JOYSTICK_DEADBAND) ? 0.0 : p;
        indexerMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        indexerMotor.setPower(manualPower);
    }

    /** Call once per loop to maintain lever pulse timing. */
    public void update() {
        updateLever();
    }

    /** Trigger feed lever pulse on button press (e.g., gamepad2.y). */
    public void handleLeverButton(boolean pressed) {
        if (pressed && !leverPulsing) {
            leverPulsing = true;
            leverTimer.reset();
            feedLeverServo.setPosition(leverEngagedPos);
        }
    }

    /** Non-blocking lever update, return to idle after pulse. */
    public void updateLever() {
        if (leverPulsing && leverTimer.milliseconds() >= leverPulseMs) {
            feedLeverServo.setPosition(leverIdlePos);
            leverPulsing = false;
        }
    }

    public String getStatus() {
        int ticks = indexerMotor.getCurrentPosition();
        boolean busy = indexerMotor.getMode() == DcMotor.RunMode.RUN_TO_POSITION && indexerMotor.isBusy();
        return String.format("indexerSel=%s ticks=%d mode=%s busy=%s manual=%s leverPulsing=%s",
            selection, ticks, indexerMotor.getMode(), busy, manualMode, leverPulsing);
    }

    /** Current encoder ticks (useful for finding presets). */
    public int getEncoder() { return indexerMotor.getCurrentPosition(); }

    /** Update preset ticks at runtime for tuning. */
    public void setPresets(int p1, int p2, int p3) { POSITION_1 = p1; POSITION_2 = p2; POSITION_3 = p3; }
}