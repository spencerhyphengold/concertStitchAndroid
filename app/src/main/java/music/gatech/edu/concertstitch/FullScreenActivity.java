package music.gatech.edu.concertstitch;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;

import java.util.HashMap;
import java.util.Map;

import static music.gatech.edu.concertstitch.ParseMedia.syncMap;
import static music.gatech.edu.concertstitch.ResourceConstants.AUDIO_URI;

public class FullScreenActivity extends AppCompatActivity {

    public static boolean fullScreenActive = false;

    private MediaController mediaController;
    private MediaPlayer audioPlayer;
    private VideoView fullScreenVideoView;

    private String currentVideoSrc;
    private String audioSrc;


    private int currentTime = 0;
    private double bufferCheckInterval = 50.0; // ms

    private int currentPlayPos = 0;
    private int lastPlayPos = 0;
    private boolean bufferingDetected = false;
    private boolean changingVideoSrc = false;

    // TODO: play audio

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            currentVideoSrc = extras.getString("currentVideoSrc");
            audioSrc = extras.getString("audioSrc");
            currentTime = extras.getInt("currentTime");
        }

        setContentView(R.layout.activity_full_screen);

        mediaController = new FullScreenMediaController(this, false);
        Uri audio = Uri.parse(AUDIO_URI);
        audioPlayer = new MediaPlayer();
        audioPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        Uri video = Uri.parse(currentVideoSrc);
        fullScreenVideoView = findViewById(R.id.full_screen_video_view);
        fullScreenVideoView.setMediaController(mediaController);
        fullScreenVideoView.setVideoURI(video);

        // VideoView internally wraps MediaPlayer and calls prepareAsync
        // https://stackoverflow.com/questions/18598675/calling-mediaplayer-prepareasync-from-videoview
        fullScreenVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

                mp.setVolume(0f, 0f);
                mp.setLooping(false);
                fullScreenVideoView.seekTo(currentTime);
                fullScreenVideoView.start();
            }
        });
        // ^ help from: https://stackoverflow.com/questions/11310764/videoview-full-screen-in-android-application/15718881

    }

    @Override
    public void onStart() {
        super.onStart();
        fullScreenActive = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        fullScreenActive = false;
    }

    // TODO: implement checkBufferingAndVideoEnd -> sync mechanism for audio/video that runs every 50 ms

    public class CheckBufferingAndVideoEndTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            currentPlayPos = audioPlayer.getCurrentPosition();

            double offset = (bufferCheckInterval - 20) / 1000;

            if (!bufferingDetected && currentPlayPos < (lastPlayPos + offset) && fullScreenVideoView.isPlaying()) {
                bufferingDetected = true;

            }

            if (!bufferingDetected && currentPlayPos > (lastPlayPos + offset) && fullScreenVideoView.isPlaying()) {
                bufferingDetected = false;

            }

            // syncMap -> currVideo: [index, duration]
            fullScreenVideoView.seekTo(Math.max(0, (audioPlayer.getCurrentPosition()) - (int) syncMap.get(currentVideoSrc)[0]));

            if (audioPlayer.isPlaying() && !fullScreenVideoView.isPlaying() && !changingVideoSrc) {
                fullScreenVideoView.start();
            }

            if (currentPlayPos > (int)(syncMap.get(currentVideoSrc)[0] + syncMap.get(currentVideoSrc)[1]) ){
                // call goHome
            }

            lastPlayPos = currentPlayPos;
            return null;
        }
    }

}
