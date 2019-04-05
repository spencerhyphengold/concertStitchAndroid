package music.gatech.edu.concertstitch;

import java.io.Serializable;

public class TrackingFrame implements Serializable {
    long time;
    float minX, minY, maxX, maxY;

    TrackingFrame(long time, float x, float y) {
        this.time = time;
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

    public String toString() {
        return String.format("time: %d \nx: %f \ny: %f", time, minX, maxY);
    }
}
