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
import java.util.SortedSet;
import java.util.TreeSet;

public class TrackingSession implements Serializable {
    private SortedSet<TrackingFrame> trackingFrames;
    int id, name, size, overlap, width, height, frameRate, rotation;
    String mode, bugTracker;
    boolean flipped, horizontal;
    Date created, updated, dumped;
    File source;
    Map<String, List<Coordinate>> playerMap;
    long startRecordingTime;


    TrackingSession() {
        this.trackingFrames = new TreeSet<>();
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

        this.rotation = result.getRotation();
        for (TrackingFrame frame : this.trackingFrames) {
            frame.coordinate.rotate(this.rotation, this.width, this.height);
        }
    }

    void addPlayerLabel(TrackingFrame trackingFrame, String player) {
        List<Coordinate> coordinates = playerMap.get(player);
        if (coordinates == null) {
            coordinates = new ArrayList<>();
            playerMap.put(player, coordinates);
        }

        long startFrame = Math.round(trackingFrame.startTime / 1000. * this.frameRate);
        long endFrame = Math.round(trackingFrame.endTime  / 1000. * this.frameRate);

        Coordinate originalCoordinate = trackingFrame.coordinate;

        for (long i = startFrame; i < endFrame; i++) {
            coordinates.add(new Coordinate(originalCoordinate, i));
        }
    }

    TrackingFrame createTrackingFrame(long time, float xPos, float yPos) {
        time = time - startRecordingTime;
        return new TrackingFrame(time, xPos, yPos);
    }

    void addTrackingFrame(long time, TrackingFrame trackingFrame) {
        time = time - startRecordingTime;
        trackingFrame.endTime = time;
        trackingFrames.add(trackingFrame);
    }

    SortedSet<TrackingFrame> getTrackingFrames() {
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

        Coordinate(float minX, float minY, float maxX, float maxY) {
            this.minX = minX;
            this.minY = minY;
            this.maxX = maxX;
            this.maxY = maxY;
        }

        Coordinate(float x, float y) {
            this(x, y, x, y);
        }

        Coordinate(Coordinate other, long frame) {
            this(other.minX, other.minY, other.maxX, other.maxY);
            this.frame = frame;
        }

        Coordinate(Coordinate other) {
            this(other.minX, other.minY, other.maxX, other.maxY);
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

        void rotate(int rotation, int width, int height) {
            Coordinate temp = new Coordinate(this);

            switch (rotation % 360) {
                // phone is rotated to the left
                case 0:
                    this.minX = temp.minY;
                    this.maxX = temp.maxY;
                    this.minY = height - temp.minX;
                    this.maxY = height - temp.maxX;
                    break;
                // phone is in normal, vertical orientation
                case 90:
                    break;
                // phone is rotated to the right
                case 180:
                    this.minX = width - temp.minY;
                    this.maxX = width - temp.maxY;
                    this.minY = temp.maxX;
                    this.maxY = temp.minX;
                    break;
                // phone is upside-down
                case 270:
                    this.minX = width - temp.minY;
                    this.maxX = width - temp.maxY;
                    this.minY = height - temp.maxX;
                    this.maxY = height - temp.minX;
                    break;
                default:
                    Log.e("MY_TAG", "Coordinate.rotate: Rotate value is not a multiple of 90");
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
