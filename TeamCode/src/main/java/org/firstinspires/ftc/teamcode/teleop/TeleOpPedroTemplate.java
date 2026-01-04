package org.firstinspires.ftc.teamcode.teleop;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.subsystems.IntakeSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.IndexerSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.FlywheelSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.TurretSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.drive.DriveBase;
import org.firstinspires.ftc.teamcode.subsystems.drive.RawMecanumDrive;

/**
 * TeleOpPedroTemplate
 *
 * How to swap RawMecanumDrive to Pedro later:
 * - Create a class PedroDrive implements DriveBase with the same methods.
 * - In init(), replace `drive = new RawMecanumDrive(...)` with `drive = new PedroDrive(...)`.
 * - Keep calls to drive.setDriverInput(...) and drive.update() the same.
 */
@TeleOp(name = "Pedro Template", group = "TeleOp")
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
    private static final String INDEXER = "indexer";          // servo
    private static final String FLYWHEEL = "flywheel";        // motor
    private static final String IMU = "imu"; // optional

    private DriveBase drive;
    private TurretSubsystem turret;
    private IntakeSubsystem intake;
    private IndexerSubsystem indexer;
    private FlywheelSubsystem flywheel;


    @Override
    public void init() {
        HardwareMap hw = hardwareMap;
        drive = new RawMecanumDrive(hw, FRONT_LEFT, FRONT_RIGHT, BACK_LEFT, BACK_RIGHT, IMU);
        turret = new TurretSubsystem(hw, TURRET, TURRET_ANGLE);
        intake = new IntakeSubsystem(hw, INTAKE, INTAKE_ANGLE);
        indexer = new IndexerSubsystem(hw, INDEXER, FEED_LEVER);
        flywheel = new FlywheelSubsystem(hw, FLYWHEEL);
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

        // Always robot-centric
        drive.setDriverInput(x, y, rx, false);
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

        // Indexer selection: choose which ball aligns with turret
        if (gamepad2.dpad_left) {
            indexer.setSelection(IndexerSubsystem.Selection.POSITION_1);
        } else if (gamepad2.dpad_up) {
            indexer.setSelection(IndexerSubsystem.Selection.POSITION_2);
        } else if (gamepad2.dpad_right) {
            indexer.setSelection(IndexerSubsystem.Selection.POSITION_3);
        }
        // Maintain lever timing
        indexer.update();

        // Flywheel: run while gamepad2.right_bumper held
        flywheel.setPower(gamepad2.right_bumper ? 1.0 : 0.0);

        // Telemetry
        telemetry.addData("slowModeHeld", slowModeHeld);
        telemetry.addData("Drive", "x=%.2f y=%.2f rx=%.2f", x, y, rx);
        telemetry.addData("Turret", turret.getStatus());
        telemetry.addData("Intake", intake.getStatus());
        telemetry.addData("Indexer", indexer.getStatus());
        telemetry.addData("Flywheel", flywheel.getStatus());
        telemetry.update();
    }
}
