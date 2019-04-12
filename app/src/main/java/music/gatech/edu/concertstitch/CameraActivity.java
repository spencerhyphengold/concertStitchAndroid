package music.gatech.edu.concertstitch;

import android.content.Intent;
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
import com.otaliastudios.cameraview.VideoResult;

import java.io.File;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class CameraActivity extends AppCompatActivity {

    private CameraView camera;
    private Button recordBtn;
    private TrackingFragment trackingFragment;

    private boolean isRecording = false;
    public File videoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        videoFile = getVideoFile();
        if (videoFile == null) {
            Toast.makeText(this, "Cannot record video without write permissions.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            return;
        }

        trackingFragment = (TrackingFragment) getSupportFragmentManager()
                .findFragmentById(R.id.trackingFragment);
        recordBtn = findViewById(R.id.recordBtn);
        recordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isRecording) {
                    onFinishVideo();
                } else {
                    isRecording = true;
                    recordBtn.setText("Recording");
                    camera.takeVideo(videoFile);
                    trackingFragment.onStartTracking();
                }
            }
        });

        camera = findViewById(R.id.camera);
        setUpCamera();
    }

    private void onFinishVideo() {
        isRecording = false;
        recordBtn.setText("Record");
        camera.stopVideo();
    }

    private File getVideoFile() {
        String folderPath = Environment.getExternalStorageDirectory() +
                File.separator + "ConcertStitch";
        File folder = new File(folderPath);
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdirs();
        }

        if (success) {
            return new File(folderPath + File.separator + "demo.mp4");
        } else {
            return null;
        }
    }

    private void setUpCamera() {
        camera.setLifecycleOwner(this);
        camera.setMode(Mode.VIDEO);
        camera.addCameraListener(new CameraListener() {
            @Override
            public void onVideoTaken(@NonNull VideoResult result) {
            ArrayList<TrackingFrame> trackingFrames = trackingFragment.onFinishTracking();
            // toast is a proxy for creating xml with the points, sending to server
            Toast.makeText(CameraActivity.this, String.format("%d frames(s) ready to send to server", trackingFrames.size()), Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(getApplicationContext(), ClassifyActivity.class);
            Bundle args = new Bundle();
            args.putSerializable("trackingFrames", trackingFrames);
            args.putSerializable("videoPath", videoFile.getAbsolutePath());
            Toast.makeText(getApplicationContext(), videoFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
            intent.putExtra("bundle", args);
            startActivity(intent);
            }
        });
    }
}
