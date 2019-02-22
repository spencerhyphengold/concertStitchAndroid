package music.gatech.edu.concertstitch;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

public class FullScreenActivity extends AppCompatActivity {

    private MediaController mediaController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String videoUrl = "";

        Bundle extras = getIntent().getExtras();
        if (null != extras) {
            videoUrl = extras.getString("videoUrl");
        }

        setContentView(R.layout.activity_full_screen);

        mediaController = new FullScreenMediaController(this, false);

        Uri video = Uri.parse(videoUrl);
        final VideoView fullScreenVideoView = findViewById(R.id.full_screen_video_view);
        fullScreenVideoView.setMediaController(mediaController);
        fullScreenVideoView.setVideoURI(video);
        fullScreenVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                mp.setVolume(0f, 0f);
                mp.setLooping(false);
                fullScreenVideoView.start();
            }
        });
        // ^ help from: https://stackoverflow.com/questions/11310764/videoview-full-screen-in-android-application/15718881


    }
}
