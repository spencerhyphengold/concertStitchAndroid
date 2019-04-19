package music.gatech.edu.concertstitch;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static music.gatech.edu.concertstitch.FullScreenActivityOld.EXIT_FULL_SCREEN_REQUEST_CODE;
import static music.gatech.edu.concertstitch.ResourceConstants.AUDIO_URI;
import static music.gatech.edu.concertstitch.ResourceConstants.BASE_VIDEO_URI;
import static music.gatech.edu.concertstitch.ResourceConstants.VIDEO_NAMES;

public class VideoListActivity extends AppCompatActivity {

    private ListView videoListView;
    public static String TAG = "VideoListActivity";
    private String videoPath;
    private String audioPath;
    private String annotationPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);

        videoListView = findViewById(R.id.video_lv);

        final String internalConcertStitchPathStr = Environment.getExternalStorageDirectory() +
                File.separator + "ConcertStitch";
        File concertStitchDir = new File(internalConcertStitchPathStr);

        File[] mp4Files = concertStitchDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".mp4");
            }
        });

        List<String> videoNameList = new ArrayList<>();
        videoNameList.add("Oh Oh");
        if (mp4Files != null) {
            for (File f : mp4Files) {
                Log.e("Video ", f.getName());
                videoNameList.add(f.getName());
            }
        }


        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,videoNameList);
        videoListView.setAdapter(adapter);

        videoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String  itemName    = (String) videoListView.getItemAtPosition(position);
                Log.e(TAG, "pos " + position + " " + itemName);

                // hardcoding for now
                if ("demo.mp4".equals(itemName)) {
                    videoPath = internalConcertStitchPathStr + File.separator + itemName;
                    annotationPath = internalConcertStitchPathStr + File.separator + "testOutput.xml";
                    audioPath = internalConcertStitchPathStr + File.separator + itemName.split("\\.")[0] + ".mp3";

                    final ReadingTask rTask = new ReadingTask();
                    rTask.execute();
//                    Map<String, HashMap<Integer, double[][]>> la = ParseMedia.getAnnotationsLocal(annotationPath);
//
//                    Intent localFileParsingIntent = new Intent(getApplicationContext(), VideoFullScreenActivity.class);
//
//                    localFileParsingIntent.putExtra("currentVideoSrc", videoPath);
//                    localFileParsingIntent.putExtra("currentVideoName", "demo");
//                    localFileParsingIntent.putExtra("currentTime", 0);
//                    localFileParsingIntent.putExtra("audioSrc", audioPath);
//
//                    startActivityForResult(localFileParsingIntent, EXIT_FULL_SCREEN_REQUEST_CODE);

                } else {
                    Intent localFileParsingIntent = new Intent(getApplicationContext(), VideoPageActivity.class);

                    startActivityForResult(localFileParsingIntent, EXIT_FULL_SCREEN_REQUEST_CODE);
//                    final ReadingTask rTask = new ReadingTask();
//                    rTask.execute();

                }

            }
        });

    }

    // callback from when activity from startActivityResult is finished (from FullScreenActivityOld)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == EXIT_FULL_SCREEN_REQUEST_CODE) {
            if (data.hasExtra("currentPlayPos")) {
                Log.e(TAG, "received currentPlayPos: " + data.getExtras().getInt("currentPlayPos"));

            }
        }
    }

    public class ReadingTask extends AsyncTask<Void, Void, Map<?, ?>> {

        private long startTime, endTime;
        private ProgressDialog dialog = new ProgressDialog(VideoListActivity.this);

        @Override
        protected Map<?, ?> doInBackground(Void... voids) {
            startTime = System.currentTimeMillis();
            Log.e("ReadAnnoTask", "Parse annotation async task executing...");
            return ParseMedia.getAnnotationsLocal(annotationPath);

//            ParseMedia.getSyncTimes();
//            return ParseMedia.getAnnotations();

        }

        @Override
        protected void onPreExecute() {


            this.dialog.setMessage("Loading annotations.");
            this.dialog.show();

        }

        @Override
        protected void onPostExecute(Map<?, ?> m) {
            endTime = System.currentTimeMillis();
            long timeTaken = endTime - startTime;

            Log.e("ReadAnnoTask", "Parse annotation async task finished...");
            Log.e("ReadAnnoTask", "Time taken to parse: " + timeTaken / 1000 + " sec");

            if (dialog.isShowing()) {
                dialog.dismiss();
            }


            Intent localFileParsingIntent = new Intent(getApplicationContext(), VideoFullScreenActivity.class);

            localFileParsingIntent.putExtra("currentVideoSrc", videoPath);
            localFileParsingIntent.putExtra("currentVideoName", "demo");
            localFileParsingIntent.putExtra("currentTime", 0);
            localFileParsingIntent.putExtra("audioSrc", audioPath);

            startActivityForResult(localFileParsingIntent, EXIT_FULL_SCREEN_REQUEST_CODE);
            Toast.makeText(VideoListActivity.this, "Finished parsing annotations in " + timeTaken / 1000 + " sec. ", Toast.LENGTH_SHORT).show();

        }

    }
}
