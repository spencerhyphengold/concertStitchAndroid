package music.gatech.edu.concertstitch;

import android.os.Environment;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.Mode;
import com.otaliastudios.cameraview.PictureResult;
import com.otaliastudios.cameraview.VideoResult;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

public class CameraActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {

    private long TIME_BETWEEN_TOUCH_READINGS = 50;

    private CameraView camera;
    private Button recordBtn;

    private boolean isRecording = false;
    private long startRecordingTime;
    private long lastTrackingPointTime;
    private List<TrackingPoint> trackingPoints;
    private List<List<TrackingPoint>> trackingPointSequences = new ArrayList();
    File videoFile;

    private class TrackingPoint {
        long time;
        float x;
        float y;

        TrackingPoint(long time, float x, float y) {
            this.time = time;
            this.x = x;
            this.y = y;
        }

        public String toString() {
            return String.format("time: %d \nx: %f \ny: %f", time, x, y);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        camera = findViewById(R.id.camera);
        camera.setLifecycleOwner(this);
        camera.setOnTouchListener(this);
        recordBtn = findViewById(R.id.recordBtn);
        recordBtn.setOnClickListener(this);

        setUpCamera();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.recordBtn:
                handleRecordBtnClick();
                break;
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (view.getId()) {
            case R.id.camera:
                handleCameraKitTouch(motionEvent);
                break;
        }
        return false;
    }

    public void handleRecordBtnClick() {
        if (isRecording) {
            isRecording = false;
            recordBtn.setText("Record");
            camera.stopVideo();
        } else {
            String videoPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath()
                    + "/concertStitch/testVideo.mp4";
            videoFile = new File(videoPath);
            camera.takeVideo(videoFile);
            isRecording = true;
            recordBtn.setText("Recording");
        }
    }

    public boolean handleCameraKitTouch(MotionEvent motionEvent) {
        if (!isRecording) {
            return false;
        }

        switch (motionEvent.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                trackingPoints = new ArrayList();

            case MotionEvent.ACTION_MOVE:
                long timeSinceLastPoint = motionEvent.getEventTime() - lastTrackingPointTime;
                if (timeSinceLastPoint < TIME_BETWEEN_TOUCH_READINGS) {
                    return false;
                } else {
                    lastTrackingPointTime = motionEvent.getEventTime();
                }
                long time = motionEvent.getEventTime() - startRecordingTime;
                float x = motionEvent.getX();
                float y = motionEvent.getY();
                TrackingPoint trackingPoint = new TrackingPoint(time, x, y);
                trackingPoints.add(trackingPoint);
                break;

            case MotionEvent.ACTION_UP:
                Toast.makeText(CameraActivity.this, Integer.toString(trackingPoints.size()), Toast.LENGTH_SHORT).show();
                trackingPointSequences.add(trackingPoints);
                break;
        }
        return true;
    }

    private void setUpCamera() {
        camera.setMode(Mode.VIDEO);
        camera.addCameraListener(new CameraListener() {
            @Override
            public void onPictureTaken(PictureResult result) {
                // A Picture was taken!
            }
            @Override
            public void onVideoTaken(VideoResult result) {
                System.out.println(result.getFile().toString());
            }
        });
    }
}
