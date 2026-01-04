package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

public class IndexerSubsystem {
    public enum Selection { POSITION_1, POSITION_2, POSITION_3 }

    private final Servo indexerServo;   // full-range positional servo controlling indexer
    private final Servo feedLeverServo; // lever that feeds balls into intake

    // Preset positions (user-specified)
    public static final double POSITION_1 = 1.00; // pos1
    public static final double POSITION_2 = 0.51; // pos2
    public static final double POSITION_3 = 0.05; // pos3

    private Selection selection = Selection.POSITION_2; // default to middle
    private boolean locked = false;     // when true, selection changes disabled

    // Lever pulse config
    private final ElapsedTime leverTimer = new ElapsedTime();
    private boolean leverPulsing = false;
    private long leverPulseMs = 200;
    private double leverIdlePos = 0.2;
    private double leverEngagedPos = 0.8;

    public IndexerSubsystem(HardwareMap hardwareMap, String indexerServoName, String feedLeverServoName) {
        this.indexerServo = hardwareMap.get(Servo.class, indexerServoName);
        this.indexerServo.setPosition(POSITION_2);

        this.feedLeverServo = hardwareMap.get(Servo.class, feedLeverServoName);
        this.feedLeverServo.setPosition(leverIdlePos);
    }

    public void setLeverConfig(long pulseMs, double idle, double engaged) {
        leverPulseMs = pulseMs; leverIdlePos = idle; leverEngagedPos = engaged;
    }

    /** Choose which preset aligns with the turret. Ignored if locked. */
    public void setSelection(Selection sel) {
        if (locked) return;
        this.selection = sel;
        switch (sel) {
            case POSITION_1:
                indexerServo.setPosition(POSITION_1);
                break;
            case POSITION_2:
                indexerServo.setPosition(POSITION_2);
                break;
            case POSITION_3:
                indexerServo.setPosition(POSITION_3);
                break;
        }
    }

    public Selection getSelection() { return selection; }

    /** Lock prevents selection changes. */
    public void setLocked(boolean locked) { this.locked = locked; }
    public boolean isLocked() { return locked; }

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
        double pos = indexerServo.getPosition();
        return String.format("indexerSel=%s pos=%.2f locked=%s leverPulsing=%s",
            selection, pos, locked, leverPulsing);
    }
}