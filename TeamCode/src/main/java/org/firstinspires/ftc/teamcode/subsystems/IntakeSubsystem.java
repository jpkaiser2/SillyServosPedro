package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

public class IntakeSubsystem {
    private final DcMotorEx intakeMotor; // Core Hex motor
    private final Servo intakeAngleServo; // rotates intake

    private boolean requestStageFlag = false;
    private boolean prevStageButton = false;

    public IntakeSubsystem(HardwareMap hardwareMap, String intakeMotorName, String intakeAngleServoName) {
        this.intakeMotor = hardwareMap.get(DcMotorEx.class, intakeMotorName);
        this.intakeMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        this.intakeMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        this.intakeMotor.setDirection(DcMotorSimple.Direction.FORWARD);

        this.intakeAngleServo = hardwareMap.get(Servo.class, intakeAngleServoName);
        this.intakeAngleServo.setPosition(0.5);
    }

    /**
     * Update intake motor based on trigger inputs.
     * @param inTrigger right trigger (intake forward)
     * @param outTrigger left trigger (reverse)
     */
    public void setTriggers(double inTrigger, double outTrigger) {
        double power = 0.0;
        if (inTrigger > 0.05 && outTrigger < 0.05) {
            power = Math.min(1.0, inTrigger);
        } else if (outTrigger > 0.05 && inTrigger < 0.05) {
            power = -Math.min(1.0, outTrigger);
        } else {
            power = 0.0;
        }
        intakeMotor.setPower(power);
    }

    /** Adjust intake rotation servo (e.g., gamepad2.left_stick_x) */
    public void setRotationInput(double joystick) {
        double pos = (Math.max(-1.0, Math.min(1.0, joystick)) * 0.5) + 0.5; // -1..1 -> 0..1
        intakeAngleServo.setPosition(Math.max(0.0, Math.min(1.0, pos)));
    }


    /** Edge-detect stage request button (e.g., gamepad2.a) */
    public void handleStageButton(boolean pressed) {
        if (pressed && !prevStageButton) {
            requestStageFlag = true;
        }
        prevStageButton = pressed;
    }

    /** Whether a stage request is active. */
    public boolean requestStage() {
        return requestStageFlag;
    }

    /** Clear the stage request flag once consumed. */
    public void clearRequestStage() {
        requestStageFlag = false;
    }

    public String getStatus() {
        return String.format(
            "intakePower=%.2f, intakeAngle=%.2f, requestStage=%s",
            intakeMotor.getPower(), intakeAngleServo.getPosition(), requestStageFlag);
    }
}
