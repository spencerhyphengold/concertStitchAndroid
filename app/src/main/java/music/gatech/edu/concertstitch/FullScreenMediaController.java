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
    private ImageButton fullScreen;
    private String isFullScreen;

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
        fullScreen = new ImageButton (super.getContext());

        FrameLayout.LayoutParams params =
                new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT);
//        FrameLayout.LayoutParams params =
//                new FrameLayout.LayoutParams(120, 120);
        params.gravity = Gravity.RIGHT;
        params.topMargin = 30;
        params.rightMargin = 80;
        fullScreen.setImageResource(R.drawable.exit_full_screen_sm);
        fullScreen.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        addView(fullScreen, params);

        //fullscreen indicator from intent
        isFullScreen =  ((Activity)getContext()).getIntent().
                getStringExtra("fullScreenInd");


        // add listener to image button to handle full screen and exit full screen events
        fullScreen.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getContext(),VideoPageActivity.class);

                if("y".equals(isFullScreen)){
                    intent.putExtra("fullScreenInd", "");
                }else{
                    intent.putExtra("fullScreenInd", "y");
                }
                getContext().startActivity(intent);
            }
        });

    }
}
