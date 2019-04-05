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

//    public class TrackingFrame implements Serializable {
//        long time;
//        float minX, minY, maxX, maxY;
//
//        TrackingFrame(long time, float x, float y) {
//            this.time = time;
//            this.minX = x;
//            this.maxX = x;
//            this.minY = y;
//            this.maxY = y;
//        }
//
//        private TrackingFrame(Parcel parcel) {
//            this.time = parcel.readLong();
//            this.minX = parcel.readFloat();
//            this.maxX = parcel.readFloat();
//            this.minY = parcel.readFloat();
//            this.maxY = parcel.readFloat();
//        }
//
//        public void addPoint(float newX, float newY) {
//            if (newX < minX) {
//                minX = newX;
//            } else if (newX > maxX) {
//                maxX = newX;
//            }
//            if (newY < minY) {
//                minY = newY;
//            } else if (newY > maxY) {
//                maxY = newY;
//            }
//        }
//
//        public String toString() {
//            return String.format("time: %d \nx: %f \ny: %f", time, minX, maxY);
//        }
//
////        @Override
////        public int describeContents() {
////            return 0;
////        }
////
////        @Override
////        public void writeToParcel(Parcel parcel, int i) {
////            parcel.writeLong(time);
////            parcel.writeFloat(minX);
////            parcel.writeFloat(maxX);
////            parcel.writeFloat(minY);
////            parcel.writeFloat(maxY);
////        }
////        public final Parcelable.Creator<TrackingFrame> CREATOR
////                = new Parcelable.Creator<TrackingFrame>() {
////
////            // This simply calls our new constructor (typically private) and
////            // passes along the unmarshalled `Parcel`, and then returns the new object!
////            @Override
////            public TrackingFrame createFromParcel(Parcel in) {
////                return new TrackingFrame(in);
////            }
////
////            // We just need to copy this and change the type to match our class.
////            @Override
////            public TrackingFrame[] newArray(int size) {
////                return new TrackingFrame[size];
////            }
////        };
//    }



    public static class TrackingCanvas extends GestureOverlayView {
        Paint paint;
        Path path;

        public TrackingCanvas(android.content.Context context, android.util.AttributeSet attributes) {
            super(context, attributes);
            setBackgroundColor(0x000000);
        }

        public void initialize(Paint paint, Path path) {
            this.paint = paint;
            this.path = path;
        }

        @Override
        public void draw(Canvas canvas) {
            super.draw(canvas);
            canvas.drawPath(path, paint);
        }
    }

    public TrackingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        trackingFrames = new ArrayList<>();
        paint = new Paint();
        path = new Path();
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10f);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tracking, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        trackingCanvas = getView().findViewById(R.id.trackingCanvas);
        trackingCanvas.initialize(paint, path);
        trackingCanvas.setOnTouchListener(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        mListener = null;
    }


    void onStartTracking() {
        isActive = true;
        startRecordingTime = System.currentTimeMillis();
    }

    ArrayList<TrackingFrame> onFinishTracking() {
        Toast.makeText(getContext(), "done recording", Toast.LENGTH_SHORT).show();
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
                currTrackingFrame = new TrackingFrame(time, xPos, yPos);

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
                trackingFrames.add(currTrackingFrame);
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
