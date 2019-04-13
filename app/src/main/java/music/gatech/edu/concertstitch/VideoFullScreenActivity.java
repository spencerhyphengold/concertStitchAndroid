package music.gatech.edu.concertstitch;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.IOException;

import static music.gatech.edu.concertstitch.ParseMedia.annotationsMap;
import static music.gatech.edu.concertstitch.ParseMedia.syncMap;
import static music.gatech.edu.concertstitch.ResourceConstants.AUDIO_URI;
import static music.gatech.edu.concertstitch.ResourceConstants.BASE_VIDEO_URI;
import static music.gatech.edu.concertstitch.ResourceConstants.FPS;
import static music.gatech.edu.concertstitch.ResourceConstants.INSTRUMENT_LABELS;

public class VideoFullScreenActivity extends AppCompatActivity implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener, CustomMediaControlView.MediaPlayerControl {

    public final static int EXIT_FULL_SCREEN_REQUEST_CODE = 1;

    public final int SCREEN_WIDTH = Resources.getSystem().getDisplayMetrics().widthPixels;
    public final int SCREEN_HEIGHT = Resources.getSystem().getDisplayMetrics().heightPixels;

    private String currentVideoName;
    private String currentVideoSrc;
    private String audioSrc;

    private SurfaceView videoSurface;
    private SurfaceHolder videoHolder;

    private SurfaceView videoSurface2;
    private SurfaceHolder videoHolder2;

    private MediaPlayer videoPlayer;
    private MediaPlayer audioPlayer;
    private MediaPlayer videoPlayer2;

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

        videoSurface2 = findViewById(R.id.video_surface2);
        videoHolder2 = videoSurface2.getHolder();
        videoHolder2.addCallback(this); // this activity
        videoSurface2.setVisibility(View.GONE);

        videoPlayer = new MediaPlayer();
        audioPlayer = new MediaPlayer();
        controller = new CustomMediaControlView(this);

        videoPlayer2 = new MediaPlayer();

        try {
            videoPlayer.setDataSource(BASE_VIDEO_URI);
            videoPlayer.setVolume(0f, 0f);
            videoPlayer.setLooping(false);
            videoPlayer.setOnPreparedListener(this);

            audioPlayer.setDataSource(AUDIO_URI);
            audioPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            audioPlayer.setLooping(false);
            audioPlayer.setOnPreparedListener(this);

            videoPlayer2.setVolume(0f, 0f);
            videoPlayer2.setLooping(false);

        } catch (IOException e) {
            e.printStackTrace();
        }

        shapeFrame = findViewById(R.id.shape_fl);
        //shapeFrame.setVisibility(View.GONE);

    }

    // label display test
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        Log.e("x", event.getX() + "");
        Log.e("y", event.getY() + "");

        int currPos = getCurrentPosition();
        int currVidFrame = currPos / 1000 * FPS;
        Log.e("TOUCH-playing", isPlaying() + "");
        Log.e("TOUCH-audioPos", (currPos) + "");
        Log.e("TOUCH-Frame", currentVideoName + " " + (currFrame) + "");

        Log.e("width", SCREEN_WIDTH + "");
        Log.e("height", SCREEN_HEIGHT + "");


        if (controller.isShowing()) {
            shapeFrame.removeAllViews();
        }

        //addOneTextView();

        //drawInstrumentLabels(currVidFrame);
        addLabelView(currVidFrame);
        setUpFadeAnimation(shapeFrame);

        controller.show();
        return false;
    }

    private void addOneTextView() {
        TextView tv = new TextView(this);
        tv.setText("Bass");
        tv.setTextColor(Color.WHITE);
        //tv.setX(0);
        //tv.setY(-10);
        tv.setPadding(0, 0, 0, 0);


        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.leftMargin = 250; // margin in pixels, not dps
        layoutParams.topMargin = 350; // margin in pixels, not dps
        tv.setLayoutParams(layoutParams);

        shapeFrame.addView(tv);
    }

    private void addLabelView(int currVidFrame){
        DrawLabelsView drawLabelsView = new DrawLabelsView(this);
        double[][]boxInfo = annotationsMap.get(currentVideoName).get(currVidFrame);
        drawLabelsView.setDimensions(SCREEN_WIDTH, SCREEN_HEIGHT);
        drawLabelsView.fillLabels(boxInfo);
        shapeFrame.addView(drawLabelsView);

    }

    public void showController() {
        controller.show();
    }

    // https://stackoverflow.com/questions/8294732/android-media-player-how-to-switch-between-videos
    public void changeSource(String s) {
        try {
            videoPlayer2.setDataSource(s);
            videoPlayer2.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        videoPlayer2.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                videoPlayer2.start();
                videoPlayer.setDisplay(null);
                videoPlayer2.setDisplay(videoHolder);
            }
        });
    }

    private void drawInstrumentLabels(int currVidFrame) {
        double[][] boxInfo = annotationsMap.get(currentVideoName).get(currVidFrame);
        for (int i = 0; i < boxInfo.length; i++) {
            double[] instrument = boxInfo[i];

            double x = instrument[0];
            double y = instrument[1];
            double boxWidth = instrument[2];
            double boxHeight = instrument[3];

            // if no instrument is labeled at this frame, x, y, boxWidth, boxHeight will all be -1.0
            if (x > 0) {
                final TextView instrumentLabelTextView = new TextView(this);
                instrumentLabelTextView.setText(INSTRUMENT_LABELS[i]);
                instrumentLabelTextView.setTextColor(Color.WHITE);

                FrameLayout.LayoutParams instrumentLabelLayoutParams = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT);

                instrumentLabelLayoutParams.leftMargin = (int) (x * SCREEN_WIDTH - 50);
                instrumentLabelLayoutParams.topMargin = (int) (y * SCREEN_HEIGHT + 50);

                Log.e("text-views-currinstr", INSTRUMENT_LABELS[i]);
                Log.e("real-x", +x + "");
                Log.e("real-y", +y + "");
                Log.e("real-boxw", +boxWidth + "");
                Log.e("real-boxh", +boxHeight + "");
                Log.e("textview-x", "" + (x * SCREEN_WIDTH));
                Log.e("textview-y", "" + (y * SCREEN_HEIGHT));
                Log.e("textview-w", "" + (boxWidth * SCREEN_WIDTH));
                Log.e("textview-h", "" + (boxHeight * SCREEN_HEIGHT));

                instrumentLabelTextView.setLayoutParams(instrumentLabelLayoutParams);

                LayerDrawable bottomBorder = getBorders(
                        Color.LTGRAY, // Background color
                        Color.WHITE, // Border color
                        5, // Left border in pixels
                        5, // Top border in pixels
                        5, // Right border in pixels
                        5 // Bottom border in pixels
                );

                instrumentLabelTextView.setBackground(bottomBorder);

                instrumentLabelTextView.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        Log.e("clicked: ", instrumentLabelTextView.getText() + "");
                    }
                });

                shapeFrame.addView(instrumentLabelTextView);

            }
        }
    }

    // Custom method to generate one or multi side border for a view
    protected LayerDrawable getBorders(int bgColor, int borderColor,
                                       int left, int top, int right, int bottom) {
        // Initialize new color drawables
        ColorDrawable borderColorDrawable = new ColorDrawable(borderColor);
        ColorDrawable backgroundColorDrawable = new ColorDrawable(bgColor);

        // Initialize a new array of drawable objects
        Drawable[] drawables = new Drawable[]{
                borderColorDrawable,
                backgroundColorDrawable
        };

        // Initialize a new layer drawable instance from drawables array
        LayerDrawable layerDrawable = new LayerDrawable(drawables);

        // Set padding for background color layer
        layerDrawable.setLayerInset(
                1, // Index of the drawable to adjust [background color layer]
                left, // Number of pixels to add to the left bound [left border]
                top, // Number of pixels to add to the top bound [top border]
                right, // Number of pixels to add to the right bound [right border]
                bottom // Number of pixels to add to the bottom bound [bottom border]
        );

        // Finally, return the one or more sided bordered background drawable
        return layerDrawable;
    }

    private void setUpFadeAnimation(final FrameLayout textView) {

        final Animation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(1000);
        fadeIn.setStartOffset(3000);

        final Animation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setDuration(500);
        fadeOut.setStartOffset(3000);

        fadeIn.setAnimationListener(new Animation.AnimationListener() {
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

        fadeOut.setAnimationListener(new Animation.AnimationListener() {
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
        audioPlayer.prepareAsync();

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
        audioReady = true;
        videoPlayer.start();
        audioPlayer.start();

    }

    @Override
    public void start() {
        audioPlayer.start();
        videoPlayer.start();
    }

    @Override
    public void pause() {
        if (isPlaying()) {
            audioPlayer.pause();
            videoPlayer.pause();
        }
    }

    @Override
    public int getDuration() {
        return audioPlayer.getDuration();
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
        return videoPlayer.isPlaying() || audioPlayer.isPlaying();
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
