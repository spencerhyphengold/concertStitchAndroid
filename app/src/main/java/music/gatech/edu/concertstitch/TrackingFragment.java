package music.gatech.edu.concertstitch;

import android.gesture.GestureOverlayView;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


public class TrackingFragment extends Fragment implements View.OnTouchListener {

    private TrackingSession trackingSession;
    private Path path;
    private TrackingCanvas trackingCanvas;
    private TrackingSession.TrackingFrame currentFrame;
    private Map<TrackingSession.TrackingFrame, RectF> frameToRectMap;
    private Set<TrackingSession.TrackingFrame> activeFrames;

    private boolean isActive, isDrawing;
    private float downEventXPos;

    public static class TrackingCanvas extends GestureOverlayView {
        Paint paint;
        Path path;
        Set<RectF> rectangles;

        public TrackingCanvas(android.content.Context context, android.util.AttributeSet attributes) {
            super(context, attributes);
            this.setWillNotDraw(true);
            rectangles = new HashSet<>();
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
            for (RectF rect : rectangles) {
                canvas.drawRect(rect, paint);
            }
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
        frameToRectMap = new HashMap<>();
        activeFrames = new HashSet<>();
        path = new Path();

        trackingCanvas = getView().findViewById(R.id.trackingCanvas);
        trackingCanvas.initialize(path);
        trackingCanvas.setOnTouchListener(this);

        trackingSession = new TrackingSession();
    }

    void onStartTracking() {
        isActive = true;
        trackingCanvas.setWillNotDraw(false);
    }

    TrackingSession onFinishTracking() {
        long time = System.currentTimeMillis();
        for (TrackingSession.TrackingFrame trackingFrame : activeFrames) {
            trackingSession.addTrackingFrame(time, trackingFrame);
        }
        isActive = false;
        return trackingSession;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (!isActive) {
            return false;
        }

        float xPos = event.getX();
        float yPos = event.getY();
        long time = System.currentTimeMillis();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                currentFrame = trackingSession.createTrackingFrame(time, xPos, yPos);
                path.moveTo(xPos, yPos);
                isDrawing = false;
                downEventXPos = xPos;
                break;

            case MotionEvent.ACTION_MOVE:
                if (isDrawing && activeFrames.size() < 5) {
                    path.lineTo(xPos, yPos);
                    currentFrame.coordinate.addPoint(xPos, yPos);
                }
                isDrawing = Math.abs(xPos - downEventXPos) > 5;
                break;

            case MotionEvent.ACTION_UP:
                if (isDrawing) {
                    if (activeFrames.size() > 5) {
                        Toast.makeText(getContext(), "Cannot draw more than 5 boxes. Tap to erase.", Toast.LENGTH_SHORT).show();
                    } else {
                        TrackingSession.Coordinate coord = currentFrame.coordinate;
                        RectF rect = new RectF(coord.minX, coord.maxY, coord.maxX, coord.minY);
                        trackingCanvas.rectangles.add(rect);
                        activeFrames.add(currentFrame);
                        frameToRectMap.put(currentFrame, rect);
                        path = new Path();
                        trackingCanvas.path = path;
                    }
                } else {
                    Iterator<TrackingSession.TrackingFrame> iterator = activeFrames.iterator();
                    while (iterator.hasNext()) {
                        TrackingSession.TrackingFrame frame = iterator.next();
                        if (frame.coordinate.contains(xPos, yPos)) {
                            iterator.remove();
                            trackingSession.addTrackingFrame(time, frame);
                            RectF rectToDelete = frameToRectMap.get(frame);
                            trackingCanvas.rectangles.remove(rectToDelete);
                        }
                    }
                }
                break;

            default:
                return false;
        }

        trackingCanvas.invalidate();
        return true;
    }
}
