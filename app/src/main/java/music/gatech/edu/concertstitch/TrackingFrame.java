package music.gatech.edu.concertstitch;

import java.io.Serializable;

public class TrackingFrame implements Serializable {
    long startTime, endTime;
    float minX, minY, maxX, maxY;
    String player;

    TrackingFrame(long startTime, float x, float y) {
        this.startTime = startTime;
        this.minX = x;
        this.maxX = x;
        this.minY = y;
        this.maxY = y;
    }

    public void addPoint(float newX, float newY) {
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
        return String.format("time: %d \nx: %f \ny: %f", startTime, minX, maxY);
    }
}
