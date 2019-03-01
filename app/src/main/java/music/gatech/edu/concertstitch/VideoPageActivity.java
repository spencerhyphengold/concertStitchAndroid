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

import static music.gatech.edu.concertstitch.ResourceConstants.AUDIO_URI;
import static music.gatech.edu.concertstitch.ResourceConstants.BASE_VIDEO_URI;

public class VideoPageActivity extends AppCompatActivity {
    final static int FULL_SCREEN_REQUEST_CODE = 1;
    private final static String TAG = "VideoPageActivity";

    private VideoView mainVideoView;
    private String currentVideoSrc;

    private int currentTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_page);

        mainVideoView = findViewById(R.id.main_video_view);
        currentVideoSrc = BASE_VIDEO_URI;

        Uri video = Uri.parse(currentVideoSrc);

        mainVideoView.setVideoURI(video); // this sets the screen to proper size

        final ReadMediaInfoTask readMediaInfoTask = new ReadMediaInfoTask();
        readMediaInfoTask.execute();

        // clicking on the VideoView should take one to full screen view
        mainVideoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (readMediaInfoTask.getStatus() == AsyncTask.Status.FINISHED) {

                    Intent fullVideoIntent = new Intent(getApplicationContext(), FullScreenActivity.class);
                    fullVideoIntent.putExtra("currentVideoSrc", currentVideoSrc);
                    fullVideoIntent.putExtra("audioSrc", AUDIO_URI);
                    fullVideoIntent.putExtra("currentTime", currentTime);

                    startActivityForResult(fullVideoIntent, FULL_SCREEN_REQUEST_CODE);
                }
            }
        });


    }

    // callback from when activity from startActivityResult is finished
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == FULL_SCREEN_REQUEST_CODE) {
            if (data.hasExtra("currentVideoSrc")) {
                if (mainVideoView != null) {
                    mainVideoView.start();
                }

            }
        }
    }

    // TODO: write a task that gets the annotation info on the background.
    // make sure this is done before the user is able to click on the video

    public class ReadMediaInfoTask extends AsyncTask<Void, Void, Map<?, ?>> {

        private ProgressDialog dialog = new ProgressDialog(VideoPageActivity.this);
        private long startTime, endTime;

        @Override
        protected Map<?, ?> doInBackground(Void... voids) {
            startTime = System.currentTimeMillis();
            Log.e(TAG, "Parse async task executing...");
//            syncMap = ParseMedia.getSyncTimes();
//            annotationsMap = ParseMedia.getAnnotations();

            ParseMedia.getSyncTimes();
            return ParseMedia.getAnnotations();

            //return annotationsMap;
        }

        @Override
        protected void onPreExecute() {
            //super.onPreExecute();
            this.dialog.setMessage("Loading annotations.");
            this.dialog.show();
        }

        @Override
        protected void onPostExecute(Map<?, ?> m) {
            endTime = System.currentTimeMillis();
            long timeTaken = endTime - startTime;

            Log.e(TAG, "Parse async task finished...");
            Log.e(TAG, "Time taken to parse: " + timeTaken / 1000 + " sec");

            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            Toast.makeText(VideoPageActivity.this, "Finished parsing in " + timeTaken / 1000 + " sec", Toast.LENGTH_SHORT).show();


        }


    }
}
