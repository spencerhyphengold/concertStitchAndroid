package music.gatech.edu.concertstitch;

import android.util.Log;
import android.widget.Toast;

import com.otaliastudios.cameraview.VideoResult;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TrackingSession implements Serializable {
    private List<TrackingFrame> trackingFrames;
    private TrackingFrame currTrackingFrame;
    int id;
    int name;
    int size;
    String mode;
    int overlap;
    String bugTracker;
    boolean flipped;
    Date created;
    Date updated;
    File source;
    int width;
    int height;
    Date dumped;
    Map<String, List<Coordinate>> playerMap;
    long startRecordingTime;
    int frameRate;


    public TrackingSession() {
        this.trackingFrames = new ArrayList<>();
        this.currTrackingFrame = null;
        this.id = 0;
        this.name = 0;
        this.mode = "interpolation";
        this.overlap = 5;
        this.bugTracker = "";
        this.flipped = false;
        this.created = new Date();
        this.startRecordingTime = System.currentTimeMillis();
        this.playerMap = new HashMap<>();
    }

    public void addVideoResult(VideoResult result) {
        this.source = result.getFile();
        this.width = result.getSize().getWidth();
        this.height = result.getSize().getHeight();
        this.frameRate = result.getVideoFrameRate();
    }

    public void addPlayerLabel(int index, String player) {
        currTrackingFrame = trackingFrames.get(index);
        List<Coordinate> coordinates = playerMap.get(player);
        if (coordinates == null) {
            coordinates = new ArrayList<>();
            playerMap.put(player, coordinates);
        }

        long startFrame = Math.round(currTrackingFrame.startTime / 1000. * this.frameRate);
        long endFrame = Math.round(currTrackingFrame.endTime  / 1000. * this.frameRate);

        Coordinate originalCoordinate = currTrackingFrame.coordinate;
        for (long i = startFrame; i < endFrame; i += 1) {
            Coordinate coordinate = new Coordinate(originalCoordinate, i);
            coordinates.add(coordinate);
        }
    }

    public void finishTracking() {
        if (this.currTrackingFrame != null) {
            currTrackingFrame.endTime = System.currentTimeMillis() - startRecordingTime;
        }
    }

    public void addTrackingFrame(long time, float xPos, float yPos) {
        time = time - startRecordingTime;
        if (currTrackingFrame != null) {
            currTrackingFrame.endTime = time;
        }
        currTrackingFrame = new TrackingFrame(time, xPos, yPos);
        trackingFrames.add(currTrackingFrame);
    }

    public void addTrackingPoint(float xPos, float yPos) {
        currTrackingFrame.coordinate.addPoint(xPos, yPos);
    }

    public List<TrackingFrame> getTrackingFrames() {
        return trackingFrames;
    }

    class TrackingFrame implements Serializable {
        long startTime, endTime;
        Coordinate coordinate;
        String player;

        TrackingFrame(long startTime, float x, float y) {
            this.startTime = startTime;
            this.coordinate = new Coordinate(x, y);
        }
    }

    class Coordinate implements Serializable {
        float minX, minY, maxX, maxY;
        int keyframe, occluded, outside;
        long frame;

        Coordinate(float minX, float minY, float maxX, float maxY, long frame) {
            this.minX = minX;
            this.minY = minY;
            this.maxX = maxX;
            this.maxY = maxY;
            this.frame = frame;
        }

        Coordinate(float x, float y) {
            this(x, y, x, y, 0);
        }

        Coordinate(Coordinate other, long frame) {
            this(other.minX, other.minY, other.maxX, other.maxY, frame);
        }

        void addPoint(float newX, float newY) {
            if (newX < minX) {
                minX = newX;
            } else if (newX > maxX) {
                maxX = newX;
            }
                    if (newY < minY) {
                minY = newY;
            } else if (newY > maxY) {
                maxY = newY;
            }
        }

        @Override
        public String toString() {
            return String.format(Locale.ENGLISH,
                    "<box keyframe=\"%d\" occluded=\"%d\" outside=\"%d\" ybr=\"%f\" xbr=\"%f\" ytl=\"%f\" xtl=\"%f\" frame=\"%d\"/>",
                    keyframe, occluded, outside, minY, maxX, maxY, minX, frame);
        }
    }
}
