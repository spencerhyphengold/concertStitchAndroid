package music.gatech.edu.concertstitch;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.widget.VideoView;

import java.util.Map;

import static music.gatech.edu.concertstitch.FullScreenActivityOld.EXIT_FULL_SCREEN_REQUEST_CODE;
import static music.gatech.edu.concertstitch.ResourceConstants.AUDIO_URI;
import static music.gatech.edu.concertstitch.ResourceConstants.BASE_VIDEO_URI;
import static music.gatech.edu.concertstitch.ResourceConstants.VIDEO_NAMES;

public class VideoPageActivity extends AppCompatActivity {

    private final static String TAG = "VideoPageActivity";

    private VideoView mainVideoView;
    private String currentVideoSrc;
    private String currentVideoName;

    private int currentTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_page);

        mainVideoView = findViewById(R.id.main_video_view);
        currentVideoSrc = BASE_VIDEO_URI;
        currentVideoName = VIDEO_NAMES[0]; // this is the house video

        final ReadAnnotationsTask readAnnotationsTask = new ReadAnnotationsTask();
        readAnnotationsTask.execute();

//        final ReadMediaSourceTask readMediaSourceTask = new ReadMediaSourceTask();
//        readMediaSourceTask.execute();


        // clicking on the VideoView should take one to full screen view
        mainVideoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (readAnnotationsTask.getStatus() == AsyncTask.Status.FINISHED
                        ) {

                    Intent fullVideoIntent = new Intent(getApplicationContext(), VideoFullScreenActivity.class);
                    fullVideoIntent.putExtra("currentVideoSrc", currentVideoSrc);
                    fullVideoIntent.putExtra("currentVideoName", currentVideoName);
                    fullVideoIntent.putExtra("audioSrc", AUDIO_URI);
                    fullVideoIntent.putExtra("currentTime", currentTime);

                    startActivityForResult(fullVideoIntent, EXIT_FULL_SCREEN_REQUEST_CODE);
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

    public class ReadAnnotationsTask extends AsyncTask<Void, Void, Map<?, ?>> {

        private ProgressDialog dialog = new ProgressDialog(VideoPageActivity.this);
        private long startTime, endTime;

        @Override
        protected Map<?, ?> doInBackground(Void... voids) {
            startTime = System.currentTimeMillis();
            Log.e(TAG, "Parse annotation async task executing...");

            ParseMedia.getSyncTimes();
            return ParseMedia.getAnnotations();
        }

        @Override
        protected void onPreExecute() {


            this.dialog.setMessage("Loading annotations.");
            //this.dialog.show();
        }

        @Override
        protected void onPostExecute(Map<?, ?> m) {
            endTime = System.currentTimeMillis();
            long timeTaken = endTime - startTime;

            Log.e(TAG, "Parse annotation async task finished...");
            Log.e(TAG, "Time taken to parse: " + timeTaken / 1000 + " sec");

            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            Toast.makeText(VideoPageActivity.this, "Finished parsing annotations in " + timeTaken / 1000 + " sec. ", Toast.LENGTH_SHORT).show();

        }

    }

    public class ReadMediaSourceTask extends AsyncTask<Void, Void, Map<String, String>> {

        private long startTime, endTime;

        @Override
        protected Map<String, String> doInBackground(Void... voids) {
            startTime = System.currentTimeMillis();
            Log.e(TAG, "Parse async read video source task executing...");

            return ParseMedia.getMediaSrc();
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onPostExecute(Map<String, String> m) {
            endTime = System.currentTimeMillis();
            long timeTaken = endTime - startTime;
            Log.e(TAG, "Parse video source async task finished...");
            Log.e(TAG, "Finished parsing in " + timeTaken / 1000 + " sec. ");

        }

    }
}
