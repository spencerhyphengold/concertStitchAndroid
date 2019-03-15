package music.gatech.edu.concertstitch;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.IOException;

import static music.gatech.edu.concertstitch.ResourceConstants.AUDIO_URI;
import static music.gatech.edu.concertstitch.ResourceConstants.BASE_VIDEO_URI;

public class VideoFullScreenActivity extends AppCompatActivity implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener, CustomMediaControlView.MediaPlayerControl {

    public final static int EXIT_FULL_SCREEN_REQUEST_CODE = 1;

    public static boolean fullScreenActive = false;

    private String currentVideoName;
    private String currentVideoSrc;
    private String audioSrc;

    private SurfaceView videoSurface;
    private SurfaceHolder videoHolder;
    private MediaPlayer videoPlayer;
    private MediaPlayer audioPlayer;

    private FrameLayout shapeFrame;

    private CustomMediaControlView controller;
    private int currFrame = 0;
    private int prevFrame = 0;

    boolean audioReady = false;
    boolean videoReady = false;

    private int currentTime = 0;
    private final static int BUFFER_CHECK_INTERVAL = 100; // ms

    private int currentPlayPos = 0;
    private int lastPlayPos = 0;
    private boolean bufferingDetected = false;
    private boolean changingVideoSrc = false;

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

        setContentView(R.layout.activity_video_full_screen);

        videoSurface = findViewById(R.id.video_surface);
        videoHolder = videoSurface.getHolder();
        videoHolder.addCallback(this); // this activity

        videoPlayer = new MediaPlayer();
        audioPlayer = new MediaPlayer();
        controller = new CustomMediaControlView(this);

        try {
            videoPlayer.setDataSource(BASE_VIDEO_URI);
            videoPlayer.setVolume(0f, 0f);
            videoPlayer.setLooping(false);
            videoPlayer.setOnPreparedListener(this);


        } catch (IOException e) {
            e.printStackTrace();
        }

        shapeFrame = findViewById(R.id.shape_fl);
        //shapeFrame.setVisibility(View.GONE);


    }

    // label display test
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.e("TOUCH-playing", isPlaying() + "");
        Log.e("TOUCH-audioPos", getCurrentPosition() + "");
        TextView tv = new TextView(this);
        tv.setText("Bass");
        tv.setTextColor(Color.WHITE);

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.topMargin = 650; // margin in pixels, not dps
        layoutParams.leftMargin = 650; // margin in pixels, not dps
        tv.setLayoutParams(layoutParams);
        shapeFrame.addView(tv);
        setUpFadeAnimation(shapeFrame);

        controller.show();
        return false;
    }

    private void setUpFadeAnimation(final FrameLayout textView) {

        final Animation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(1000);
        fadeIn.setStartOffset(3000);

        final Animation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setDuration(500);
        fadeOut.setStartOffset(3000);

        fadeIn.setAnimationListener(new Animation.AnimationListener(){
            @Override
            public void onAnimationEnd(Animation arg0) {
                textView.startAnimation(fadeOut);
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
            }

            @Override
            public void onAnimationStart(Animation arg0) {
            }
        });

        fadeOut.setAnimationListener(new Animation.AnimationListener(){
            @Override
            public void onAnimationEnd(Animation arg0) {
                if (isPlaying())
                    textView.removeAllViews();
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
            }

            @Override
            public void onAnimationStart(Animation arg0) {
            }
        });

        textView.startAnimation(fadeOut);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        videoPlayer.setDisplay(surfaceHolder);
        videoPlayer.prepareAsync();

        try {
            audioPlayer.setDataSource(AUDIO_URI);
            audioPlayer.prepareAsync();
            audioPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            audioPlayer.setLooping(false);
            audioPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    audioReady = true;
                    audioPlayer.start();
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }


    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        controller.setMediaPlayer(this);
        controller.setAnchorView((FrameLayout) findViewById(R.id.video_surface_container_frame));
        videoReady = true;
        videoPlayer.start();
    }

    @Override
    public void start() {
        audioPlayer.start();
        videoPlayer.start();
    }

    @Override
    public void pause() {
        audioPlayer.pause();
        videoPlayer.pause();
    }

    @Override
    public int getDuration() {
        return audioPlayer.getDuration();
        //return videoPlayer.getDuration();
    }

    @Override
    public int getCurrentPosition() {
        return audioPlayer.getCurrentPosition();
    }

    @Override
    public void seekTo(int pos) {
        audioPlayer.seekTo(pos);
        videoPlayer.seekTo(pos);
    }

    @Override
    public boolean isPlaying() {
        return videoPlayer.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return false;
    }

    @Override
    public boolean canSeekForward() {
        return false;
    }

    @Override
    public boolean isFullScreen() {
        return false;
    }

    @Override
    public void toggleFullScreen() {

    }
}
