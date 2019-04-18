package music.gatech.edu.concertstitch;

import android.util.Log;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class XmlEncoder {
    public static void saveXmlFromTrackingSession(TrackingSession trackingSession, File saveFile) {

        Annotations annotations = new Annotations();
        annotations.version = 1.1;
        annotations.meta = new Meta();
        annotations.meta.task = new Task();

        annotations.meta.task.labels = new ArrayList<>();
        for (String player : trackingSession.playerMap.keySet()) {
            annotations.meta.task.labels.add(new Label(player));
        }

        try {
            Serializer serializer = new Persister();
            serializer.write(annotations, saveFile);
        } catch (Exception e) {
            Log.e("ME_TAG", "saveXmlFromTrackingSession: could not write initial XML to file");
        }

        // hard code writing tracks because SimpleXML doesn't support lists with attributes
        try {
            // remove closing </annotations> tag
            RandomAccessFile raf = new RandomAccessFile(saveFile.getPath(), "rw");
            raf.setLength(raf.length() - "</annotations>".length());
            raf.close();

            // write player coordinates to xml file
            BufferedWriter writer = new BufferedWriter(new FileWriter(saveFile, true));
            StringBuilder builder = new StringBuilder();
            int id = 0;
            for (String player : trackingSession.playerMap.keySet()) {
                builder.append("\t<track label=\"");
                builder.append(player);
                builder.append("\" id=\"");
                builder.append(id++);
                builder.append("\">\n");
                List<TrackingSession.Coordinate> coordinates = trackingSession.playerMap.get(player);
                for (TrackingSession.Coordinate coordinate : coordinates) {
                    builder.append("\t\t");
                    builder.append(coordinate.toString());
                    builder.append("\n");
                }
                builder.append("\t</track>\n");
            }
            builder.append("</annotations>");

            writer.write(new String(builder));
            writer.close();
        } catch (Exception e) {
            Log.e("ME_TAG", "saveXmlFromTrackingSession: could not write tracking boxes to file");
        }
    }

    @Root
    private static class Annotations {
        @Element
        double version;
        @Element
        Meta meta;
//        @ElementList
        List<Track> tracks;

        public double getVersion() {
            return version;
        }

        public Meta getMeta() {
            return meta;
        }

        public List<Track> getTracks() {
            return tracks;
        }
    }

    private static class Meta {
        @Element
        Task task;
//        @Element
        Date dumped;

        public Task getTask() {
            return task;
        }

        public Date getDumped() {
            return dumped;
        }
    }

    private static class Task {
//        @Element
        int id, name, size, overlap;
//        @Element
        String mode, bugTracker, source;
//        @Element
        boolean flipped;
//        @Element
        Date created, updated;
        @ElementList
        List<Label> labels;
//        @ElementList
        List<Segment> segments;
//        @Element
        Owner owner;
//        @Element
        OriginalSize originalSize;

        public int getId() {
            return id;
        }

        public int getName() {
            return name;
        }

        public int getSize() {
            return size;
        }

        public String getMode() {
            return mode;
        }

        public int getOverlap() {
            return overlap;
        }

        public String getBugTracker() {
            return bugTracker;
        }

        public boolean isFlipped() {
            return flipped;
        }

        public Date getCreated() {
            return created;
        }

        public Date getUpdated() {
            return updated;
        }

        public String getSource() {
            return source;
        }

        public List<Label> getLabels() {
            return labels;
        }

        public List<Segment> getSegments() {
            return segments;
        }

        public Owner getOwner() {
            return owner;
        }

        public OriginalSize getOriginalSize() {
            return originalSize;
        }
    }

    private static class Label {
        @Element
        String name;
        @ElementList
        List<String> attributes;

        Label(String name) {
            this.name = name;
            this.attributes = new ArrayList<>();
        }

        public String getName() {
            return name;
        }

        public List<String> getAttributes() {
            return attributes;
        }
    }

    private static class Segment {
        @Element
        int i;
        @Element
        int start;
        @Element
        int stop;
        @Element
        String url;

        public int getI() {
            return i;
        }

        public int getStart() {
            return start;
        }

        public int getStop() {
            return stop;
        }

        public String getUrl() {
            return url;
        }
    }

    private static class Owner {
        @Element
        String username;
        @Element
        String email;

        public String getUsername() {
            return username;
        }

        public String getEmail() {
            return email;
        }
    }

    private static class OriginalSize {
        @Element
        int width;
        @Element
        int height;

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }

    private static class Track {
        @Attribute
        String label;
        @Attribute
        int id;
        @ElementList
        List<Box> boxes;

        Track(String label, int id, List<Box> boxes) {
            this.label = label;
            this.id = id;
            this.boxes = boxes;
        }

        public String getLabel() {
            return label;
        }

        public int getId() {
            return id;
        }

        public List<Box> getBoxes() {
            return boxes;
        }
    }

    private static class Box {
        @Attribute
        int keyframe;
        @Attribute
        int occluded;
        @Attribute
        int outside;
        @Attribute
        double ybr;
        @Attribute
        double xbr;
        @Attribute
        double ytl;
        @Attribute
        double xtl;
        @Attribute
        long frame;

        public int getKeyframe() {
            return keyframe;
        }

        public int getOccluded() {
            return occluded;
        }

        public int getOutside() {
            return outside;
        }

        public double getYbr() {
            return ybr;
        }

        public double getXbr() {
            return xbr;
        }

        public double getYtl() {
            return ytl;
        }

        public double getXtl() {
            return xtl;
        }

        public long getFrame() {
            return frame;
        }

        public Box(TrackingSession.Coordinate coordinate) {
            this.keyframe = coordinate.keyframe;
            this.occluded = coordinate.occluded;
            this.outside = coordinate.outside;
            this.ybr = coordinate.minY;
            this.xbr = coordinate.maxX;
            this.ytl = coordinate.maxY;
            this.xtl = coordinate.minX;
            this.frame = coordinate.frame;
        }
    }
}