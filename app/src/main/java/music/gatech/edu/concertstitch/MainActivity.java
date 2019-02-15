package music.gatech.edu.concertstitch;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int PERMISSIONS_REQUEST = 0;
    private static final int FILE_READ_PERMISSION_REQUEST = 1;
    private static final int LOAD_VIDEO_REQUEST = 2;

    private static final int MINIMUM_HOLD_DURATION = 2000;

    private static final String[] PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
    };

    private static boolean videoIsPaused = false;

    // asynchronous helpers for hold-to-track functionality
    private final Handler videoHoldHandler = new Handler();
    private final Runnable pauseVideoRunnable = new Runnable() {
        @Override
        public void run() {
            videoIsPaused = true;
            videoView.pause();
        }
    };

    private View.OnTouchListener videoViewTouchListener  = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            int eventAction = motionEvent.getAction();
            if (eventAction == MotionEvent.ACTION_DOWN) {
                Log.e("sg", "it's a down");
                videoHoldHandler.postDelayed(pauseVideoRunnable, MINIMUM_HOLD_DURATION);
            } else if (eventAction == MotionEvent.ACTION_UP) {
                Log.e("sg", "it's a up");
                if (videoIsPaused) {
                    float releasedXCoordinate = motionEvent.getX();
                    float releasedYCoordinate = motionEvent.getY();
                    String message = String.format("X: %f | Y: %f", releasedXCoordinate, releasedYCoordinate);
                    videoView.start();
                    videoIsPaused = false;
                    // for now, the toast is a proxy for sending touch/video data to in the server
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                } else {
                    videoHoldHandler.removeCallbacks(pauseVideoRunnable);
                }
            }
            Log.e("sg", Integer.toString(eventAction));
            return true;
        }
    };

    VideoView videoView;
    Button uploadBtn, launchCameraBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.app_name);

        requestPermissions();

        uploadBtn = (Button) findViewById(R.id.uploadBtn);
        uploadBtn.setOnClickListener(this);
        launchCameraBtn = (Button) findViewById(R.id.launchCameraBtn);
        launchCameraBtn.setOnClickListener(this);
        videoView = findViewById(R.id.videoView);
        videoView.setOnTouchListener(videoViewTouchListener);

    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.uploadBtn:
                startVideoUpload();
                break;
            case R.id.launchCameraBtn:
                launchCamera();
                break;
        }
    }

    private void startVideoUpload() {
        Intent videoPickerIntent = new Intent(Intent.ACTION_PICK);
        File videoDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        String videoDirectoryPath = videoDirectory.getPath();
        Uri data = Uri.parse(videoDirectoryPath);

        videoPickerIntent.setDataAndType(data, "video/*");
        startActivityForResult(videoPickerIntent, LOAD_VIDEO_REQUEST);
    }

    private void launchCamera() {
        Intent launchCameraIntent = new Intent(getApplicationContext(), CameraActivity.class);
        startActivity(launchCameraIntent);
    }

    private boolean handleVideoTouch(MotionEvent motionEvent) {
        int eventAction = motionEvent.getAction();
        if (eventAction == MotionEvent.ACTION_DOWN) {
            Log.e("sg","it's a down");
            videoHoldHandler.postDelayed(pauseVideoRunnable, MINIMUM_HOLD_DURATION);
        } else if (eventAction == MotionEvent.ACTION_UP) {
//            Toast.makeText(getApplicationContext(), "ACTION_UP", Toast.LENGTH_SHORT).show();
            Log.e("sg","it's a up");
            if (videoIsPaused) {
                float releasedXCoordinate = motionEvent.getX();
                float releasedYCoordinate = motionEvent.getY();
                String message = String.format("X: %f | Y: %f", releasedXCoordinate, releasedYCoordinate);
                videoView.start();
                videoIsPaused = false;
                // for now, the toast is a proxy for sending touch/video data to in the server
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            } else {
                videoHoldHandler.removeCallbacks(pauseVideoRunnable);
            }
        }
        Log.e("sg", Integer.toString(eventAction));
        return true;
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!hasPermissions(this, PERMISSIONS)) {
                ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSIONS_REQUEST);
            }
        }
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case FILE_READ_PERMISSION_REQUEST:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case LOAD_VIDEO_REQUEST:
                if (resultCode == RESULT_OK) {
                    Uri videoUri = data.getData();
                    videoView.setVideoURI(videoUri);
                    videoView.setVisibility(View.VISIBLE);
                    uploadBtn.setVisibility(View.GONE);
                    launchCameraBtn.setVisibility(View.GONE);
                    videoView.start();
                }
                break;

            case PERMISSIONS_REQUEST:
                if (resultCode == RESULT_OK) {
                    Uri videoUri = data.getData();
                    videoView.setVideoURI(videoUri);
                    videoView.setVisibility(View.VISIBLE);
                    uploadBtn.setVisibility(View.GONE);
                    launchCameraBtn.setVisibility(View.GONE);
                    videoView.start();
                }
        }
    }
}
