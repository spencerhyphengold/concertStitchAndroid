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

    private long lastMeasuredTime;
    private TrackingSession trackingSession;
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
        trackingSession = new TrackingSession();
        path = new Path();

        trackingCanvas = getView().findViewById(R.id.trackingCanvas);
        trackingCanvas.initialize(path);
        trackingCanvas.setOnTouchListener(this);
    }

    void onStartTracking() {
        isActive = true;
        trackingSession = new TrackingSession();
    }

    TrackingSession onFinishTracking() {
        trackingSession.finishTracking();
        isActive = false;
        return trackingSession;
    }

    public boolean onTouch(View view, MotionEvent event) {
        if (!isActive) {
            return false;
        }

        float xPos = event.getX();
        float yPos = event.getY();
        long time = System.currentTimeMillis();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                path.moveTo(xPos, yPos);
                trackingSession.addTrackingFrame(time, xPos, yPos);

            case MotionEvent.ACTION_MOVE:
                path.lineTo(xPos, yPos);
                long timeSinceLastPoint = time - lastMeasuredTime;
                if (timeSinceLastPoint < READING_DEBOUNCE_TIME) {
                    return false;
                }
                lastMeasuredTime = time;
                trackingSession.addTrackingPoint(xPos, yPos);
                break;

            case MotionEvent.ACTION_UP:
                path = new Path();
                trackingCanvas.path = path;

                break;

            default:
                return false;
        }

        trackingCanvas.invalidate();
        return true;
    }
}
