package test;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import team25core.DeadReckonPath;
import team25core.DeadReckonTask;
import team25core.GamepadTask;
import team25core.MechanumGearedDrivetrain;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.StoneDetectionTaskJerry;

@Autonomous(name = "Avalon SkyStone Autonomous2", group = "Team 25")
public class StoneAutonomousBuildSite extends Robot {


    private final static String TAG = "Margarita";

    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor rearLeft;
    private DcMotor rearRight;
    //for mechanism
    //private DcMotor intakeR
    //private DcMotor intakeL
    //private DcMotor
    //Private DcMotor

    private MechanumGearedDrivetrain drivetrain;

    private Telemetry.Item stonePositionTlm;
    private Telemetry.Item stoneTlm;
    private Telemetry.Item stoneConfidTlm;
    private Telemetry.Item stoneTypeTlm;
    private Telemetry.Item stoneMidpointTlm;
    private Telemetry.Item imageMidpointTlm;
    private Telemetry.Item loggingTlm;
    private Telemetry.Item handleEvntTlm;

    private double confidence;
    private double left;
    private double type;
    private double imageMidpoint;
    private double stoneMidpoint;
    private double margin;
    private boolean inCenter;

    DeadReckonPath path;

    StoneDetectionTaskJerry sdTask;

    private enum AllianceColor {
        BLUE, // Button X
        RED, // Button B
        DEFAULT
    }

    private enum RobotPosition {
        BUILD_SITE, // Button Y
        DEPOT, // Button A
        DEFAULT
    }

    // declaring gamepad variables
    //variables declarations have lowercase then uppercase
    private GamepadTask gamepad;
    protected AllianceColor allianceColor;
    protected RobotPosition robotPosition;

    //declaring telemetry item
    private Telemetry.Item allianceTlm;
    private Telemetry.Item positionTlm;

    @Override
    public void handleEvent(RobotEvent e)
    {
        if (e instanceof DeadReckonTask.DeadReckonEvent) {
            RobotLog.i("Completed path segment %d", ((DeadReckonTask.DeadReckonEvent)e).segment_num);
        }
        //decide what alliance and position of robot
        if (e instanceof GamepadTask.GamepadEvent) {
            GamepadTask.GamepadEvent event = (GamepadTask.GamepadEvent) e;
            switch (event.kind) {
                case BUTTON_X_DOWN:
                    allianceColor = AllianceColor.BLUE;
                    allianceTlm.setValue("BLUE");
                    break;
                case BUTTON_B_DOWN:
                    allianceColor = AllianceColor.RED;
                    allianceTlm.setValue("RED");
                    break;
                case BUTTON_Y_DOWN:
                    robotPosition = RobotPosition.BUILD_SITE;
                    positionTlm.setValue("BUILD SITE");
                    break;
                case BUTTON_A_DOWN:
                    robotPosition = RobotPosition.DEPOT;
                    positionTlm.setValue("DEPOT");
                    break;
            }
        }
    }

    public void goPickupSkystone()
    {
        //FIXME
        RobotLog.i("Go Pick Up Skystone");
    }

    public void setStoneDetection()
    {
        //caption: what appears on the phone
        stonePositionTlm = telemetry.addData("LeftOrigin", "unknown");
        stoneConfidTlm = telemetry.addData("Confidence", "N/A");
        stoneTypeTlm = telemetry.addData("StoneType","unknown");
        imageMidpointTlm = telemetry.addData("Image_Mdpt", "unknown");
        stoneMidpointTlm = telemetry.addData("Stone Mdpt", "unknown");

        sdTask = new StoneDetectionTaskJerry(this, "Webcam1") {
            //starts when you find a skystone
            @Override
            public void handleEvent(RobotEvent e) {
                StoneDetectionTaskJerry.StoneDetectionEvent event = (StoneDetectionTaskJerry.StoneDetectionEvent) e;
                //0 gives you the first stone on list of stones
                confidence = event.stones.get(0).getConfidence();
                left = event.stones.get(0).getLeft();

                RobotLog.ii(TAG, "Saw: " + event.kind + " Confidence: " + confidence);
                RobotLog.i("startHandleEvent");

                imageMidpoint = event.stones.get(0).getImageWidth() / 2.0;
                stoneMidpoint = (event.stones.get(0).getWidth() / 2.0) + left;

                stonePositionTlm.setValue(left);
                stoneConfidTlm.setValue(confidence);
                imageMidpointTlm.setValue(imageMidpoint);
                stoneMidpointTlm.setValue(stoneMidpoint);

                if (event.kind == StoneDetectionTaskJerry.EventKind.OBJECTS_DETECTED) {
                    if (Math.abs(imageMidpoint - stoneMidpoint) < margin) {
                        inCenter = true;
                        RobotLog.i("506 Found gold");
                        sdTask.stop();
                        drivetrain.stop();
                        goPickupSkystone();
                    }
                }
            }
        };

        sdTask.init(telemetry, hardwareMap);
        //later will find skystone
        sdTask.setDetectionKind(StoneDetectionTaskJerry.DetectionKind.SKY_STONE_DETECTED);

    }
    @Override
    public void init()
    {
        frontLeft = hardwareMap.get(DcMotor.class, "frontLeft");
        frontRight = hardwareMap.get(DcMotor.class, "frontRight");
        rearLeft = hardwareMap.get(DcMotor.class, "rearLeft");
        rearRight = hardwareMap.get(DcMotor.class, "rearRight");

        drivetrain = new MechanumGearedDrivetrain(360, frontRight, rearRight, frontLeft, rearLeft);
        drivetrain.encodersOn();
        drivetrain.resetEncoders();

        path = new DeadReckonPath();
        path.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 30,.75);

        //initializing gamepad variables
        allianceColor = allianceColor.DEFAULT;
        gamepad = new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_1);
        addTask(gamepad);

        //telemetry setup
        telemetry.setAutoClear(false);
        allianceTlm = telemetry.addData("ALLIANCE", "Unselected (X-blue /B-red)");
        positionTlm = telemetry.addData("POSITION", "Unselected (Y-build/A-depot)");
        handleEvntTlm = telemetry.addData("detecting","unknown");
        setStoneDetection();
    }

    public void startStrafing()
    {
        //start looking for Skystones
        RobotLog.i("startStrafing");
        addTask(sdTask);
        drivetrain.strafe(SkyStoneConstants25.STRAFE_SPEED);
    }

    public void parkUnderBridge(DeadReckonPath path)
    {
        //go park under skybridge
        this.addTask(new DeadReckonTask(this, path, drivetrain));

    }

    @Override
    public void start()
    {

        //this.addTask(new DeadReckonTask(this, path, drivetrain));




        /**
         * Alternatively, this could be an anonymous class declaration that implements
         * handleEvent() for task specific event handlers.
         */
        //this.addTask(new DeadReckonTask(this, path, drivetrain));

        parkUnderBridge(path);

        /*

        if (robotPosition == RobotPosition.BUILD_SITE)
        {


        }
        else if (robotPosition == RobotPosition.DEPOT)
        {
            RobotLog.i("start: before startStrafing");
            loggingTlm = telemetry.addData("log","unknown");
            startStrafing();
        } */


    }
}
