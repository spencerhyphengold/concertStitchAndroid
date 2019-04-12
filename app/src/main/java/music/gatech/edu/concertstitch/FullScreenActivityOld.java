package music.gatech.edu.concertstitch;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.MediaController;
import android.widget.VideoView;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static music.gatech.edu.concertstitch.ParseMedia.syncMap;
import static music.gatech.edu.concertstitch.ResourceConstants.AUDIO_URI;

public class FullScreenActivityOld extends AppCompatActivity {
    public final static int EXIT_FULL_SCREEN_REQUEST_CODE = 1;

    public static boolean fullScreenActive = false;

    private MediaController mediaController;
    private MediaPlayer audioPlayer;
    private VideoView fullScreenVideoView;

    private String currentVideoName;
    private String currentVideoSrc;
    private String audioSrc;


    private int currentTime = 0;
    private final static int BUFFER_CHECK_INTERVAL = 100; // ms

    private int currentPlayPos = 0;
    private int lastPlayPos = 0;
    private boolean bufferingDetected = false;
    private boolean changingVideoSrc = false;

    // TODO: play audio

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras != null) { // data from VideoPageActivity
            currentVideoSrc = extras.getString("currentVideoSrc");
            currentVideoName = extras.getString("currentVideoName");
            audioSrc = extras.getString("audioSrc");
            currentTime = extras.getInt("currentTime");
        }

        setContentView(R.layout.activity_full_screen);


        mediaController = new FullScreenMediaController(this, false);
        Uri audio = Uri.parse(AUDIO_URI);
        audioPlayer = new MediaPlayer();
        audioPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            audioPlayer.setDataSource(this, audio);
        } catch (IOException e) {
            e.printStackTrace();
        }

        audioPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                audioPlayer.seekTo(currentTime);
                audioPlayer.start();
            }
        });
        audioPlayer.prepareAsync();


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


        Log.e("FULLSCREEN", "" + (syncMap == null)  );

        if (syncMap != null) {
            final Handler videoAudioSyncHandler = new Handler();
            videoAudioSyncHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    synchronizeVideoAudio();
                    videoAudioSyncHandler.postDelayed(this, BUFFER_CHECK_INTERVAL);
                }
            }, BUFFER_CHECK_INTERVAL);
        }

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

    public int getCurrentPlayPos() {
        return this.currentPlayPos;
    }

    public void pauseAudio() {
        if (audioPlayer != null) {
            audioPlayer.pause();
        }
    }

    public void synchronizeVideoAudio() {
        CheckBufferingAndVideoEndTask checkBufferingAndVideoEndTask = new CheckBufferingAndVideoEndTask();
        checkBufferingAndVideoEndTask.execute();
    }

    public class CheckBufferingAndVideoEndTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            Log.e("background", "in background");
            currentPlayPos = audioPlayer.getCurrentPosition(); // ms

            int offset = (BUFFER_CHECK_INTERVAL - 20);

            if (!bufferingDetected && currentPlayPos < (lastPlayPos + offset) && fullScreenVideoView.isPlaying()) {
                bufferingDetected = true;

            }

            if (!bufferingDetected && currentPlayPos > (lastPlayPos + offset) && fullScreenVideoView.isPlaying()) {
                bufferingDetected = false;
            }

            // syncMap -> currVideo: [index, duration]
            double posDiff = audioPlayer.getCurrentPosition()  - syncMap.get(currentVideoName)[0] * 1000; // seconds
            Log.e("background audio", audioPlayer.getCurrentPosition() + "");
            Log.e("background diff", posDiff + "");
            Log.e("background vid", fullScreenVideoView.getCurrentPosition() + "");
            if (posDiff - fullScreenVideoView.getCurrentPosition() >= 101)
            fullScreenVideoView.seekTo( Math.max(0, (int)(posDiff)) );

            if (audioPlayer.isPlaying() && !fullScreenVideoView.isPlaying() && !changingVideoSrc) {
                //fullScreenVideoView.start();
            }

            double endOfClipTime = (syncMap.get(currentVideoName)[0] + syncMap.get(currentVideoName)[1]) * 1000; // ms
            if (currentPlayPos > (int) endOfClipTime ) {
                // call goHome
            }

            lastPlayPos = currentPlayPos;
            return null;
        }


    }

}
