package music.gatech.edu.concertstitch;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.Map;

/**
 * @author mcw0805
 */

public class ReadAnnoTask extends AsyncTask<Void, Void, Map<?, ?>> {

    private long startTime, endTime;


    @Override
    protected Map<?, ?> doInBackground(Void... voids) {
        startTime = System.currentTimeMillis();
        Log.e("ReadAnnoTask", "Parse annotation async task executing...");


            ParseMedia.getSyncTimes();
            return ParseMedia.getAnnotations();

    }

    @Override
    protected void onPreExecute() {

    }

    @Override
    protected void onPostExecute(Map<?, ?> m) {
        endTime = System.currentTimeMillis();
        long timeTaken = endTime - startTime;

        Log.e("ReadAnnoTask", "Parse annotation async task finished...");
        Log.e("ReadAnnoTask", "Time taken to parse: " + timeTaken / 1000 + " sec");



        //Toast.makeText(, "Finished parsing annotations in " + timeTaken / 1000 + " sec. ", Toast.LENGTH_SHORT).show();

    }

}