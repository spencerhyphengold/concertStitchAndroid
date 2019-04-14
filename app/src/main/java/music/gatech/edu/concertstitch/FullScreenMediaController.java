package music.gatech.edu.concertstitch;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;

/**
 * @author mcw0805
 */
public class FullScreenMediaController extends MediaController {
    private ImageButton exitFullScreenImgBtn;
    private ImageButton homeVideoImgBtn;
    private String isFullScreen;

    // TODO: add home button

    public FullScreenMediaController(Context context) {
        super(context);
    }

    // enter false for useFastForward if you don't want to see the >> and << buttons
    public FullScreenMediaController(Context context, boolean useFastForward) {
        super(context, useFastForward);
    }

    @Override
    public void setAnchorView(View view) {

        super.setAnchorView(view);

        // image button for full screen to be added to media controller
        exitFullScreenImgBtn = new ImageButton(super.getContext());
        homeVideoImgBtn = new ImageButton(super.getContext());

        FrameLayout.LayoutParams params =
                new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT);
//        FrameLayout.LayoutParams params =
//                new FrameLayout.LayoutParams(120, 120);
        params.gravity = Gravity.RIGHT;
        params.rightMargin = 20;
        addView(exitFullScreenImgBtn, params);

        exitFullScreenImgBtn.setImageResource(R.drawable.exit_full_screen_icon);
        exitFullScreenImgBtn.setScaleType(ImageView.ScaleType.CENTER_INSIDE);


        FrameLayout.LayoutParams params2 =
                new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT);
//        FrameLayout.LayoutParams params =
//                new FrameLayout.LayoutParams(120, 120);
        params.gravity = Gravity.RIGHT;
        params.topMargin = 30;
        params.rightMargin = 120;
        // https://www.flaticon.com/free-icon/home_25694
        homeVideoImgBtn.setImageResource(R.drawable.home_icon);
        homeVideoImgBtn.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        addView(homeVideoImgBtn, params2);


        //fullscreen indicator from intent
        isFullScreen = ((Activity) getContext()).getIntent().
                getStringExtra("fullScreenInd");


        // add listener to image button to handle full screen and exit full screen events
        exitFullScreenImgBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                // go back to previous activity, which is VideoPageActivity
                if (getContext() instanceof FullScreenActivityOld) {

                    FullScreenActivityOld fullScreenActivity = (FullScreenActivityOld) getContext();

                    Intent intent = new Intent();
                    intent.putExtra("currentPlayPos", fullScreenActivity.getCurrentPlayPos());
                    fullScreenActivity.setResult(Activity.RESULT_OK, intent);

                    ((Activity) getContext()).finish();
                }
//                Intent intent = new Intent(getContext(),VideoPageActivity.class);
//
//                if("y".equals(isFullScreen)){
//                    intent.putExtra("fullScreenInd", "");
//                }else{
//                    intent.putExtra("fullScreenInd", "y");
//                }
//                getContext().startActivity(intent);
            }
        });

    }
//
//    @Override
//    public void show(int timeout) {
//
//    }
}
