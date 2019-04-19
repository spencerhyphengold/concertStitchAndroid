package music.gatech.edu.concertstitch;

import android.gesture.GestureOverlayView;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
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
    private Map<TrackingSession.TrackingFrame, Path> frameToPathMap;
    private Set<TrackingSession.TrackingFrame> activeFrames;

    private boolean isActive, isDrawing;
    private float downEventXPos;

    public static class TrackingCanvas extends GestureOverlayView {
        Paint paint;
        Path path;
        Set<Path> paths;

        public TrackingCanvas(android.content.Context context, android.util.AttributeSet attributes) {
            super(context, attributes);
            this.setWillNotDraw(true);
            paths = new HashSet<>();
        }

        public void initialize(Path path) {
            this.path = path;
            paths.add(path);
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
            for (Path currPath : paths) {
                canvas.drawPath(currPath, paint);
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
        trackingSession = new TrackingSession();
        frameToPathMap = new HashMap<>();
        activeFrames = new HashSet<>();
        path = new Path();

        trackingCanvas = getView().findViewById(R.id.trackingCanvas);
        trackingCanvas.initialize(path);
        trackingCanvas.setOnTouchListener(this);
    }

    void onStartTracking() {
        isActive = true;
        trackingCanvas.setWillNotDraw(false);
        trackingSession = new TrackingSession();
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
                        Toast.makeText(getContext(), "Cannot draw more than 5 circles. Tap to erase.", Toast.LENGTH_SHORT).show();
                    } else {
                        activeFrames.add(currentFrame);
                        frameToPathMap.put(currentFrame, path);
                        path = new Path();
                        trackingCanvas.paths.add(path);
                    }
                } else {
                    Iterator<TrackingSession.TrackingFrame> iterator = activeFrames.iterator();
                    while (iterator.hasNext()) {
                        TrackingSession.TrackingFrame frame = iterator.next();
                        if (frame.coordinate.contains(xPos, yPos)) {
                            iterator.remove();
                            trackingSession.addTrackingFrame(time, frame);
                            Path pathToDelete = frameToPathMap.get(frame);
                            trackingCanvas.paths.remove(pathToDelete);
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
