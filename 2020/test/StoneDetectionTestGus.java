package test;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import team25core.Robot;
import team25core.RobotEvent;

@Autonomous(name = "Stones Detection Test", group = "Team 25")
public class StoneDetectionTestGus extends Robot {

    private final static String TAG = "Jerry";
    private Telemetry.Item stonePositionTlm;
    private Telemetry.Item stoneTlm;
    private Telemetry.Item stoneConfidenceTlm;
    private double confidence;
    private double left;

    StoneDetectionTaskJerry mdTask;

    @Override
    public void handleEvent(RobotEvent e)
    {
    }

    @Override
        public void init()
        {
            mdTask = new StoneDetectionTaskJerry(this, "Webcam1") {
                @Override
                public void handleEvent(RobotEvent e) {
                    StoneDetectionEvent event = (StoneDetectionEvent)e;
                    //0 gives you the first stone on list of stones
                    confidence = event.stones.get(0).getConfidence();
                    left = event.stones.get(0).getLeft();
                    RobotLog.ii(TAG, "Saw: " + event.kind + " Confidence: " + confidence);

                    stonePositionTlm = telemetry.addData("LeftOrigin", left);
                    stoneConfidenceTlm = telemetry.addData("Confidence", confidence);
                }
            };

            mdTask.init(telemetry, hardwareMap);
            mdTask.setDetectionKind(StoneDetectionTaskJerry.DetectionKind.EVERYTHING);

        }
    @Override
    public void start()
    {
        addTask(mdTask);
    }
}
