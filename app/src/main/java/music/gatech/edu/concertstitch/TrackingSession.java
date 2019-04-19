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


    TrackingSession() {
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

    void addVideoResult(VideoResult result) {
        this.source = result.getFile();
        this.width = result.getSize().getWidth();
        this.height = result.getSize().getHeight();
        this.frameRate = result.getVideoFrameRate();
    }

    void addPlayerLabel(int index, String player) {
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

    TrackingFrame createTrackingFrame(long time, float xPos, float yPos) {
        time = time - startRecordingTime;
        currTrackingFrame = new TrackingFrame(time, xPos, yPos);
        return currTrackingFrame;
    }

    void addTrackingFrame(long time, TrackingFrame trackingFrame) {
        time = time - startRecordingTime;
        currTrackingFrame.endTime = time;
        trackingFrames.add(currTrackingFrame);
    }

    List<TrackingFrame> getTrackingFrames() {
        return trackingFrames;
    }

    class TrackingFrame implements Serializable, Comparable<TrackingFrame> {
        long startTime, endTime;
        Coordinate coordinate;
        String player;

        TrackingFrame(long startTime, float x, float y) {
            this.startTime = startTime;
            this.coordinate = new Coordinate(x, y);
        }

        @Override
        public int compareTo(TrackingFrame other) {
            long diff = this.startTime - other.startTime;
            if (diff < 0) {
                return -1;
            } else if (diff == 0) {
                return 0;
            } else {
                return 1;
            }
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

        boolean contains(float x, float y) {
            return x > minX && x < maxX && y > minY && y < maxY;
        }

        @Override
        public String toString() {
            return String.format(Locale.ENGLISH,
                    "<box keyframe=\"%d\" occluded=\"%d\" outside=\"%d\" ybr=\"%f\" xbr=\"%f\" ytl=\"%f\" xtl=\"%f\" frame=\"%d\"/>",
                    keyframe, occluded, outside, minY, maxX, maxY, minX, frame);
        }
    }
}
