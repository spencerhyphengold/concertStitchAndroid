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

        camera = findViewById(R.id.camera);
        camera.setLifecycleOwner(this);
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
                    String videoPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                            .getAbsolutePath() + "/concertStitch/testVideo.mp4";
                    videoFile = new File(videoPath);
                    camera.takeVideo(videoFile);
                    trackingFragment.onStartTracking();
                }
            }
        });

        setUpCamera();
    }

    private void onFinishVideo() {
        isRecording = false;
        recordBtn.setText("Record");
        camera.stopVideo();

//        Toast.makeText(getApplicationContext(), "onVideoTaken was called.", Toast.LENGTH_SHORT).show();
        ArrayList<TrackingFrame> trackingFrames = trackingFragment.onFinishTracking();
        // toast is a proxy for creating xml with the points, sending to server
        Toast.makeText(CameraActivity.this, String.format("%d frames(s) ready to send to server", trackingFrames.size()), Toast.LENGTH_SHORT).show();


        Intent intent = new Intent(this, ClassifyActivity.class);
        Bundle args = new Bundle();
        args.putSerializable("trackingFrames", trackingFrames);
        args.putSerializable("videoPath", videoFile.getAbsolutePath());
        intent.putExtra("bundle", args);
        startActivity(intent);
////                    Fragment classifyFragment = ClassifyPlayersFragment.newInstance(trackingFrames, result.getFile().getAbsolutePath());
//        Fragment classifyFragment = ClassifyPlayersFragment.newInstance(trackingFrames, videoFile.getAbsolutePath());
//        FragmentManager manager = getSupportFragmentManager();
//        FragmentTransaction transaction = manager.beginTransaction();
//        transaction.replace(R.id.cameraLayout,classifyFragment);
//        transaction.addToBackStack(null);
//        transaction.commit();
    }


    private void setUpCamera() {
        camera.setMode(Mode.VIDEO);
        camera.addCameraListener(new CameraListener() {
            @Override
            public void onVideoTaken(@NonNull VideoResult result) {
                Toast.makeText(getApplicationContext(), "Done recording", Toast.LENGTH_SHORT).show();

//                Toast.makeText(getApplicationContext(), "onVideoTaken was called.", Toast.LENGTH_SHORT).show();
//                ArrayList<TrackingFragment.TrackingFrame> trackingFrames = trackingFragment.onFinishTracking();
//                // toast is a proxy for creating xml with the points, sending to server
//                Toast.makeText(CameraActivity.this, String.format("%d frames(s) ready to send to server", trackingFrames.size()), Toast.LENGTH_SHORT).show();
//
//                Fragment classifyFragment = ClassifyPlayersFragment.newInstance(trackingFrames, result.getFile().getAbsolutePath());
//                FragmentManager manager = getSupportFragmentManager();
//                FragmentTransaction transaction = manager.beginTransaction();
//                transaction.replace(R.id.cameraLayout,classifyFragment);
//                transaction.addToBackStack(null);
//                transaction.commit();
            }
        });
    }

//    public void onFinishTracking(List<TrackingFragment.TrackingPoint> trackingPoints) {}
}
