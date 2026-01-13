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

    // Manual mode removed

    // Preset positions in encoder ticks (user-provided; tune as needed)
    public static int POSITION_1 = 0;
    public static int POSITION_2 = 97;
    public static int POSITION_3 = 190;

    // Secondary collection positions in encoder ticks
    public static int COLLECTION_1 = 332;
    public static int COLLECTION_2 = 241;
    public static int COLLECTION_3 = 429;

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

    /** Choose which preset aligns with the turret. */
    public void setSelection(Selection sel) {
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

    /** Choose which collection preset to move to. */
    public void setCollectionSelection(Selection sel) {
        int target;
        switch (sel) {
            case POSITION_1:
                target = COLLECTION_1; break;
            case POSITION_2:
                target = COLLECTION_2; break;
            case POSITION_3:
            default:
                target = COLLECTION_3; break;
        }
        indexerMotor.setTargetPosition(target);
        indexerMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        indexerMotor.setPower(0.6);
    }

    public Selection getSelection() { return selection; }

    // Manual mode APIs removed

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
        boolean busy = indexerMotor.getMode() == DcMotor.RunMode.RUN_TO_POSITION && indexerMotor.isBusy();
        return String.format("indexerSel=%s mode=%s busy=%s leverPulsing=%s",
            selection, indexerMotor.getMode(), busy, leverPulsing);
    }

    /** Whether the indexer is currently moving toward a target position. */
    public boolean isMoving() {
        return indexerMotor.getMode() == DcMotor.RunMode.RUN_TO_POSITION && indexerMotor.isBusy();
    }

    // Tuning helpers removed
}