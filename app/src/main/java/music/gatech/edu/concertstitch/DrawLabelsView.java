package music.gatech.edu.concertstitch;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import static music.gatech.edu.concertstitch.ResourceConstants.INSTRUMENT_LABELS;

/**
 * @author mcw0805
 */
public class DrawLabelsView extends View {

    List<InstrumentInfoAtFrame> instrumentInfoAtFrameList = new ArrayList<>();
    Paint paint = new Paint();
    int screenWidth, screenHeight;
    boolean showRectangle = true;

    public DrawLabelsView(Context context) {
        super(context);
    }

    @Override
    public void onDraw(Canvas canvas) {

        if (instrumentInfoAtFrameList.size() > 0) {
            paint.setColor(Color.WHITE);
            paint.setStyle(Paint.Style.STROKE);
            for (InstrumentInfoAtFrame info : instrumentInfoAtFrameList) {
                canvas.drawRect(
                        info.rectF,
                        paint);

                canvas.drawText(info.label, info.x, info.y, paint);
            }

        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                float x = event.getX();
                float y = event.getY();

                VideoFullScreenActivity fullScreenActivity = (VideoFullScreenActivity) getContext();

                for (InstrumentInfoAtFrame info : instrumentInfoAtFrameList) {
                    if (x > info.x && x < info.x + info.boxWidth && y > info.y && y < info.y + info.boxHeight) {
                        Log.e("XXXX", "touched " + info.label);
                        fullScreenActivity.changeSource("https://s3.amazonaws.com/concert-stitch-webapp/02_480p.mp4");
                    } else {

                        fullScreenActivity.showController();
                    }
                }

                return true;
        }

        return true;
    }

    public void setDimensions(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    public void disableShowRectangle() {
        this.showRectangle = false;
    }

    public void fillLabels(double[][] boxInfo) {

        for (int i = 0; i < boxInfo.length; i++) {
            double[] infoForInstrument = boxInfo[i];
            String label = INSTRUMENT_LABELS[i];
            double x = infoForInstrument[0] * this.screenWidth;
            double y = infoForInstrument[1] * this.screenHeight;
            double boxWidth = infoForInstrument[2] * this.screenWidth;
            double boxHeight = infoForInstrument[3] * this.screenHeight;

            // if no instrument is labeled at this frame, x, y, boxWidth, boxHeight will all be -1.0
            if (x > 0) {
                InstrumentInfoAtFrame infoAtFrame = new InstrumentInfoAtFrame(label, x, y, boxWidth, boxHeight);
                instrumentInfoAtFrameList.add(infoAtFrame);

            }
        }

    }

    private class InstrumentInfoAtFrame {
        String label;
        float x;
        float y;
        float boxWidth;
        float boxHeight;
        RectF rectF;

        public InstrumentInfoAtFrame(String label, double x, double y, double boxWidth, double boxHeight) {
            this.label = label;
            this.x = (float) x;
            this.y = (float) y;
            this.boxWidth = (float) boxWidth;
            this.boxHeight = (float) boxHeight;
            this.rectF = new RectF(this.x, this.y, this.x + this.boxWidth, this.y + this.boxHeight);
        }

    }
}
