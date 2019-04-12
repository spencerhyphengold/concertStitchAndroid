package music.gatech.edu.concertstitch;

import android.content.Context;
import android.gesture.GestureOverlayView;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;


public class TrackingFragment extends Fragment implements View.OnTouchListener {

    private int READING_DEBOUNCE_TIME = 50;

    private long startRecordingTime;
    private long lastMeasuredTime;
    private TrackingFrame currTrackingFrame;
    private ArrayList<TrackingFrame> trackingFrames;
    private Paint paint;
    private Path path;
    private TrackingCanvas trackingCanvas;

    private boolean isActive;

    public static class TrackingCanvas extends GestureOverlayView {
        Paint paint;
        Path path;

        public TrackingCanvas(android.content.Context context, android.util.AttributeSet attributes) {
            super(context, attributes);
        }

        public void initialize(Path path) {
            this.path = path;
            this.paint = new Paint();
            paint.setAntiAlias(true);
            paint.setColor(Color.WHITE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(10f);
        }

        @Override
        public void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawPath(path, paint);
        }
    }

    public TrackingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tracking, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        trackingFrames = new ArrayList<>();
        currTrackingFrame = null;

        path = new Path();

        trackingCanvas = getView().findViewById(R.id.trackingCanvas);
        trackingCanvas.initialize(path);
        trackingCanvas.setOnTouchListener(this);
    }

    void onStartTracking() {
        isActive = true;
        startRecordingTime = System.currentTimeMillis();
    }

    ArrayList<TrackingFrame> onFinishTracking() {
        Toast.makeText(getContext(), "done recording", Toast.LENGTH_SHORT).show();
        if (currTrackingFrame != null) {
            currTrackingFrame.endTime = System.currentTimeMillis() - startRecordingTime;
        }
        isActive = false;
        return trackingFrames;
    }

    public boolean onTouch(View view, MotionEvent event) {
        if (!isActive) {
            return false;
        }

        float xPos = event.getX();
        float yPos = event.getY();
        long time = event.getEventTime() - startRecordingTime;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                path.moveTo(xPos, yPos);
                if (currTrackingFrame != null) {
                    currTrackingFrame.endTime = time;
                }
                currTrackingFrame = new TrackingFrame(time, xPos, yPos);
                trackingFrames.add(currTrackingFrame);

            case MotionEvent.ACTION_MOVE:
                path.lineTo(xPos, yPos);
                long timeSinceLastPoint = time - lastMeasuredTime;
                if (timeSinceLastPoint < READING_DEBOUNCE_TIME) {
                    return false;
                }
                lastMeasuredTime = time;
                currTrackingFrame.addPoint(xPos, yPos);
                break;

            case MotionEvent.ACTION_UP:
                path = new Path();
                trackingCanvas.path = path;

                float xish = event.getX();
                float yish = event.getY();
                Toast.makeText(getContext(), String.format("x: %f\n y: %f", xish, yish), Toast.LENGTH_SHORT).show();
                break;

            default:
                return false;
        }

        trackingCanvas.invalidate();
        return true;
    }
}
