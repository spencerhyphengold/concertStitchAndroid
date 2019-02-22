package music.gatech.edu.concertstitch;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.VideoView;

public class VideoPageActivity extends AppCompatActivity {
    final static String VIDEO_URI = "https://s3.amazonaws.com/concert-stitch-webapp/ISO_2_480p.mp4";
    final static String AUDIO_URI = "https://s3.amazonaws.com/concert-stitch-webapp/HQ_Audio.mp3";
    final static int FULL_SCREEN_REQUEST_CODE = 1;

    private VideoView mainVideoView;

    int currentTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_page);

        mainVideoView = findViewById(R.id.main_video_view);

        Uri video = Uri.parse(VIDEO_URI);

        mainVideoView.setVideoURI(video);
        mainVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setVolume(0f, 0f);
                mp.setLooping(false);
            }
        });

        mainVideoView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent fullVideoIntent = new Intent(getApplicationContext(), FullScreenActivity.class);
                fullVideoIntent.putExtra("videoUrl", VIDEO_URI);
                fullVideoIntent.putExtra("audioUrl", AUDIO_URI);
                fullVideoIntent.putExtra("currentTime", currentTime);
                startActivityForResult(fullVideoIntent, FULL_SCREEN_REQUEST_CODE);
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == FULL_SCREEN_REQUEST_CODE) {
            if (data.hasExtra("videoUrl")) {
                if (null != mainVideoView) {
                    mainVideoView.start();
                }

            }
        }
    }
}
