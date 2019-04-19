package music.gatech.edu.concertstitch;

/**
 * @author mcw0805
 */
public final class ResourceConstants {
    public final static String BASE_VIDEO_URI = "https://s3.amazonaws.com/concert-stitch-webapp/ISO_2_480p.mp4";
    public final static String AUDIO_URI = "https://s3.amazonaws.com/concert-stitch-webapp/HQ_Audio.mp3";

    public static final String[] VIDEO_NAMES = {"ISO_2", "ISO_1", "ISO_3", "ISO_4", "01", "02", "00461", "00462",
            "00463", "00464", "00465", "00466", "00467", "Video_1", "00434", "00434_1c",
            "00435", "00436", "00436_1", "00442", "00443", "00444", "00444_1c", "00444_2c",
            "00471", "00473", "00479", "00480", "00482", "00482_1"};

    // public final static String[] INSTRUMENT_LABELS = {"Shimon", "Trumpet", "Trombone", "Drums", "Bass", "Guitar", "Saxophone"};
    // in alphabetical order
    public static final String[] INSTRUMENT_LABELS = {"Bass", "Drums", "Guitar", "Saxophone", "Shimon", "Trombone", "Trumpet"};
    public static final String[] DEMO_LABELS = {"Bass", "Drums", "Guitar", "Keyboard", "Singer"};

    public static final String[] SHOT_BY = {"House", "Mike A.", "Chris H.", "Kai R.", "Ryan R.",
            "Raghav S.", "Gil W.", "Ning Y.", "Richard S.", "Keshav B.",
            "Tejas R.", "Veronica P.", "Jason S.", "Lorna R.", "Mike A.",
            "Chris H.", "Kai R.", "Ryan R.", "Raghav S.", "Gil W.", "Ning Y.",
            "Richard S.", "Keshav B.", "Tejas R.", "Veronica P.", "Jason S.",
            "Lorna R.", "Mike A.", "Chris H.", "Kai R."};

    // JSON
    public static final String SYNC_TIME_URL = "https://concertstitch.com/sync_time.json";

    // XML
    public static final String ANNOTATION_BASE_URL = "https://concertstitch.com/annotations/";
    public static final String MEDIA_SRC_URL = "https://concertstitch.com/mediaSources.xml";

    // frames per second
    public static final int FPS = 25;


    private ResourceConstants() {
        throw new AssertionError();
    }


}
