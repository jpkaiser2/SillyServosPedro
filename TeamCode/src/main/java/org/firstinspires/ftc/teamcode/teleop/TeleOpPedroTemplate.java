package org.firstinspires.ftc.teamcode.teleop;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.bylazar.configurables.PanelsConfigurables;
/*
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;
import com.qualcomm.robotcore.hardware.NormalizedRGBA;
import android.graphics.Color;
*/

import org.firstinspires.ftc.teamcode.subsystems.IntakeSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.IndexerSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.FlywheelSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.TurretSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.drive.DriveBase;
import org.firstinspires.ftc.teamcode.subsystems.drive.PedroDrive;

/**
 * TeleOpPedroTemplate
 *
 * How to swap RawMecanumDrive to Pedro later:
 * - Create a class PedroDrive implements DriveBase with the same methods.
 * - In init(), replace `drive = new RawMecanumDrive(...)` with `drive = new PedroDrive(...)`.
 * - Keep calls to drive.setDriverInput(...) and drive.update() the same.
 */
@TeleOp(name = "TeleOpMainPedro", group = "TeleOp")
public class TeleOpPedroTemplate extends OpMode {

    // HardwareMap names (edit these to match your configuration)
    private static final String FRONT_LEFT = "frontLeft";
    private static final String FRONT_RIGHT = "frontRight";
    private static final String BACK_LEFT = "backLeft";
    private static final String BACK_RIGHT = "backRight";
    private static final String TURRET = "turret";           // motor
    private static final String TURRET_ANGLE = "turretAngle"; // servo
    private static final String INTAKE = "intake";            // core hex motor
    private static final String INTAKE_ANGLE = "intakeAngle"; // servo
    private static final String FEED_LEVER = "feedLever";     // servo
    private static final String INDEXER = "indexer";          // motor
    private static final String FLYWHEEL = "flywheel";        // motor
    private static final String IMU = "imu"; // optional
    // private static final String COLOR_SENSOR = "sensor_color"; // color sensor at shooting position

    private DriveBase drive;
    private TurretSubsystem turret;
    private IntakeSubsystem intake;
    private IndexerSubsystem indexer;
    private FlywheelSubsystem flywheel;
    // Indexer preset control
    
    private boolean prevUp = false, prevRight = false, prevDown = false;
    private boolean prevX = false, prevB = false, prevIndex = false;

    // Software indexing state (disabled: color sensor-based indexing)
    /*
    private enum BallColor { BLUE, PURPLE, UNKNOWN }
    private BallColor[] slots = new BallColor[] { BallColor.UNKNOWN, BallColor.UNKNOWN, BallColor.UNKNOWN };
    private int head = 0; // slot at shooting/color sensor position
    private int pendingSteps = 0; // queued forward steps
    private boolean wasMovingLastUpdate = false;
    private NormalizedColorSensor colorSensor;
    */


    @Override
    public void init() {
        HardwareMap hw = hardwareMap;
        // Use Pedro Pathing Follower for teleop drive
        drive = new PedroDrive(hw);
        turret = new TurretSubsystem(hw, TURRET, TURRET_ANGLE);
        intake = new IntakeSubsystem(hw, INTAKE, INTAKE_ANGLE);
        indexer = new IndexerSubsystem(hw, INDEXER, FEED_LEVER);
        flywheel = new FlywheelSubsystem(hw, FLYWHEEL);
        // Enable dashboard configurables for indexer presets
        try { PanelsConfigurables.INSTANCE.refreshClass(indexer); } catch (Exception ignore) {}
        // Color sensor disabled
        // try {
        //     colorSensor = hw.get(NormalizedColorSensor.class, COLOR_SENSOR);
        // } catch (Exception ignore) { colorSensor = null; }
    }

    @Override
    public void loop() {
        // Slow mode held while LB
        // held via LB
        boolean slowModeHeld = gamepad1.left_bumper;

        // Read drive inputs (FTC sticks: up is -y)
        double y = -gamepad1.left_stick_y;  // forward
        double x = gamepad1.left_stick_x;   // strafe
        double rx = gamepad1.right_stick_x; // rotation

        if (slowModeHeld) {
            // scale while slow mode
            double slowFactor = 0.4;
            y *= slowFactor;
            x *= slowFactor;
            rx *= slowFactor;
        }

        // Hold RB to use robot-centric, else field-centric
        boolean robotCentricHeld = gamepad1.right_bumper;
        drive.setDriverInput(x, y, rx, !robotCentricHeld);
        drive.update();

        // Mechanisms
        // Turret: rotate with right_stick_x, angle with left_stick_y
        turret.setManualInput(gamepad2.right_stick_x);
        turret.setAngleInput(gamepad2.left_stick_y);
        turret.update();

        // Intake: motor with triggers, rotation servo with left_stick_x
        intake.setTriggers(gamepad2.right_trigger, gamepad2.left_trigger);
        intake.setRotationInput(gamepad2.left_stick_x);

        // Feed lever pulse (in Indexer) on gamepad2.y
        indexer.handleLeverButton(gamepad2.y);

        // Indexing disabled: manual presets only
        // if (gamepad1.a && !prevIndex) {
        //     intake.setHoldUp(true);
        //     pendingSteps += 1;
        // }
        // prevIndex = gamepad1.a;

        // Color selection disabled
        // if (gamepad1.x && !prevX) {
        //     intake.setHoldUp(true);
        //     int targetIndex = findColorIndex(BallColor.BLUE);
        //     if (targetIndex >= 0) {
        //         int deltaForward = (targetIndex - head + 3) % 3;
        //         pendingSteps += deltaForward;
        //     }
        // } else if (gamepad1.b && !prevB) {
        //     intake.setHoldUp(true);
        //     int targetIndex = findColorIndex(BallColor.PURPLE);
        //     if (targetIndex >= 0) {
        //         int deltaForward = (targetIndex - head + 3) % 3;
        //         pendingSteps += deltaForward;
        //     }
        // }
        // prevX = gamepad1.x;
        // prevB = gamepad1.b;

        // Presets via D-Pad, hold LB for collection presets
        boolean collectionMod = gamepad2.left_bumper; // modifier for collection positions
        if (gamepad2.dpad_up && !prevUp) {
            // Ensure intake is up before moving indexer to avoid interference
            intake.setHoldUp(true);
            if (collectionMod) {
                indexer.setCollectionSelection(IndexerSubsystem.Selection.POSITION_1);
            } else {
                indexer.setSelection(IndexerSubsystem.Selection.POSITION_1);
            }
        } else if (gamepad2.dpad_right && !prevRight) {
            intake.setHoldUp(true);
            if (collectionMod) {
                indexer.setCollectionSelection(IndexerSubsystem.Selection.POSITION_2);
            } else {
                indexer.setSelection(IndexerSubsystem.Selection.POSITION_2);
            }
        } else if (gamepad2.dpad_down && !prevDown) {
            intake.setHoldUp(true);
            if (collectionMod) {
                indexer.setCollectionSelection(IndexerSubsystem.Selection.POSITION_3);
            } else {
                indexer.setSelection(IndexerSubsystem.Selection.POSITION_3);
            }
        }

        prevUp = gamepad2.dpad_up;
        prevRight = gamepad2.dpad_right;
        prevDown = gamepad2.dpad_down;
        // Maintain lever timing only; indexing queue disabled
        indexer.update();
        // boolean moving = indexer.isMoving();
        // if (wasMovingLastUpdate && !moving) {
        //     // Just arrived: sample color at head
        //     sampleAndStoreColorAtHead();
        // }
        // wasMovingLastUpdate = moving;
        // if (!moving && pendingSteps > 0) {
        //     // Execute a single forward step through POSITION_1 -> POSITION_2 -> POSITION_3 -> POSITION_1
        //     IndexerSubsystem.Selection sel = indexer.getSelection();
        //     IndexerSubsystem.Selection nextSel;
        //     switch (sel) {
        //         case POSITION_1:
        //             nextSel = IndexerSubsystem.Selection.POSITION_2; break;
        //         case POSITION_2:
        //             nextSel = IndexerSubsystem.Selection.POSITION_3; break;
        //         case POSITION_3:
        //         default:
        //             nextSel = IndexerSubsystem.Selection.POSITION_1; break;
        //     }
        //     indexer.setSelection(nextSel);
        //     head = (head + 1) % 3;
        //     pendingSteps--;
        // }

        // Release intake hold once indexer finishes moving
        if (!indexer.isMoving() && intake.isHoldUp()) {
            intake.setHoldUp(false);
        }

        // Flywheel: run while gamepad2.right_bumper held
        flywheel.setPower(gamepad2.right_bumper ? 1.0 : 0.0);

        // Telemetry
        telemetry.addData("slowModeHeld", slowModeHeld);
        telemetry.addData("robotCentricHeld", robotCentricHeld);
        telemetry.addData("Drive", "x=%.2f y=%.2f rx=%.2f", x, y, rx);
        telemetry.addData("Turret", turret.getStatus());
        telemetry.addData("Intake", intake.getStatus());
        telemetry.addData("Indexer", indexer.getStatus());
        telemetry.addData("Indexer Presets", String.format("P1=%d P2=%d P3=%d",
            IndexerSubsystem.POSITION_1,
            IndexerSubsystem.POSITION_2,
            IndexerSubsystem.POSITION_3));
        telemetry.addData("Collection Presets", String.format("C1=%d C2=%d C3=%d",
            IndexerSubsystem.COLLECTION_1,
            IndexerSubsystem.COLLECTION_2,
            IndexerSubsystem.COLLECTION_3));
        telemetry.addData("Indexer Enc", String.format("cur=%d tgt=%d",
            indexer.getCurrentPosition(),
            indexer.getTargetPosition()));
        // telemetry.addData("Buffer", String.format("head=%d slots=[%s,%s,%s]", head, slots[0], slots[1], slots[2]));
        telemetry.addData("Flywheel", flywheel.getStatus());
        telemetry.update();
    }
    // private int findColorIndex(BallColor desired) {
    //     for (int i = 0; i < 3; i++) {
    //         if (slots[i] == desired) return i;
    //     }
    //     return -1;
    // }

    // private void sampleAndStoreColorAtHead() {
    //     if (colorSensor == null) return;
    //     try {
    //         NormalizedRGBA colors = colorSensor.getNormalizedColors();
    //         final float[] hsv = new float[3];
    //         Color.colorToHSV(colors.toColor(), hsv);
    //         float hue = hsv[0];
    //         BallColor detected;
    //         if (hue >= 200 && hue < 260) {
    //             detected = BallColor.BLUE;
    //         } else if (hue >= 260 && hue < 340) {
    //             detected = BallColor.PURPLE;
    //         } else {
    //             detected = BallColor.UNKNOWN;
    //         }
    //         slots[head] = detected;
    //     } catch (Exception ignore) { /* leave UNKNOWN */ }
    // }
}
