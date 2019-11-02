package test;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import team25core.DeadReckonPath;
import team25core.DeadReckonTask;
import team25core.MechanumGearedDrivetrain;
import team25core.Robot;
import team25core.RobotEvent;

@Autonomous(name = "Stones Detection Test", group = "Team 25")
public class RobotAutonomous extends Robot {

    private final static String TAG = "Jerry";

    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor rearLeft;
    private DcMotor rearRight;


    private MechanumGearedDrivetrain drivetrain;

    private Telemetry.Item stonePositionTlm;
    private Telemetry.Item stoneTlm;
    private Telemetry.Item stoneConfidenceTlm;
    private Telemetry.Item imageMidpointTlm;
    private Telemetry.Item stoneMidpointTlm;

    private double confidence;
    private double left;
    private double type;
    private double left1;
    private double imageMidpoint;
    private double stoneMidpoint;
    boolean inCenter;
    private double margin;
    private double initPos;

    StoneDetectionTaskJerry sdTask;

    @Override
    public void handleEvent(RobotEvent e)
    {
        if (e instanceof DeadReckonTask.DeadReckonEvent) {
            RobotLog.i("Completed path segment %d", ((DeadReckonTask.DeadReckonEvent)e).segment_num);
        }
    }
    public void goPickupSkystone()
    {
        //FIXME
    }

    public void setStoneDetection()

    {
        //caption: what appears on the phone
        stonePositionTlm = telemetry.addData("LeftOrigin", "unknown");
        stoneConfidenceTlm = telemetry.addData("Confidence", "N/A");
        stoneTlm = telemetry.addData("StoneType", "unknown");
        imageMidpointTlm = telemetry.addData("Image Mdpt", "unknown");
        stoneMidpointTlm = telemetry.addData("Stone Mdpt", "unknown");

        sdTask = new StoneDetectionTaskJerry(this, "Webcam1") {
            //starts when you find a skystone
            @Override
            public void handleEvent(RobotEvent e) {
              StoneDetectionEvent event = (StoneDetectionEvent)e;
                //0 gives you the first stone on list of stones
                confidence = event.stones.get(0).getConfidence();
                left = event.stones.get(0).getLeft();
                RobotLog.ii(TAG, "Saw: " + event.kind + " Confidence: " + confidence);

                stonePositionTlm.setValue(left);
                stoneConfidenceTlm.setValue(confidence);
                imageMidpoint = event.stones.get(0).getImageWidth() / 2.0;
                stoneMidpoint = (event.stones.get(0).getWidth() / 2.0) +left1;

                RobotLog.i("506 Current Position: " + initPos);
                if (event.kind == EventKind.OBJECTS_DETECTED) {
                    if (Math.abs(imageMidpoint - stoneMidpoint) < margin) {
                        inCenter = true;
                        RobotLog.i("506 Found gold");
                        sdTask.stop();
                        drivetrain.stop();
                    }
                }

            }
        };

        sdTask.init(telemetry, hardwareMap);
        // later will find skystone
        sdTask.setDetectionKind(StoneDetectionTaskJerry.DetectionKind.SKY_STONE_DETECTED);

    }
    @Override
        public void init()
        {
            frontLeft = hardwareMap.get(DcMotor.class, "frontLeft");
            frontRight = hardwareMap.get(DcMotor.class, "frontRight");
            rearLeft = hardwareMap.get(DcMotor.class, "rearLeft");
            rearRight = hardwareMap.get(DcMotor.class,"rearRight");

            drivetrain = new MechanumGearedDrivetrain(360,frontRight, rearRight, frontLeft, rearLeft);

            setStoneDetection();

        }
    //@Override

    public void startStrafing()
    {
        // start looking for stone
        addTask(sdTask);
        drivetrain.strafe(SkyStoneConstants25.STRAFE_SPEED);
    }
    public void start()
    {
        DeadReckonPath path = new DeadReckonPath();

        //path.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 10,1.0);
        //path.addSegment(DeadReckonPath.SegmentType.TURN, 90,1.0);
        //path.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 10,1.0);
        //path.addSegment(DeadReckonPath.SegmentType.TURN, 90,1.0);
        //path.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 10,1.0);
        //path.addSegment(DeadReckonPath.SegmentType.TURN, 90,1.0);
        //path.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 10,1.0);
        //path.addSegment(DeadReckonPath.SegmentType.TURN, 90,1.0);
        //path.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 10,1.0);
        //path.addSegment(DeadReckonPath.SegmentType.TURN, 90,1.0);
        //path.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 10,1.0);
        //path.addSegment(DeadReckonPath.SegmentType.TURN, 90,1.0);
        //path.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 10,1.0);


        //this.addTask(new DeadReckonTask(this, path, drivetrain));


        startStrafing();
    }
}
