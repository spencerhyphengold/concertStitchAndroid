package music.gatech.edu.concertstitch;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.util.List;

public class ClassifyActivity extends AppCompatActivity {

    private MediaMetadataRetriever media;
    private TrackingSession trackingSession;
    private List<TrackingSession.TrackingFrame> trackingFrames;
    private TrackingSession.TrackingFrame currTrackingFrame;
    private String videoPath;
    private Bitmap currImage;
    private int currIndex;

    private ImageView framePreview;
    private Button nextFrameBtn;
    private Spinner instrumentSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classify);

        Bundle args = getIntent().getBundleExtra("bundle");
        trackingSession = (TrackingSession) args.getSerializable("trackingSession");
        videoPath = args.getString("videoPath");

        framePreview = findViewById(R.id.framePreview);
        nextFrameBtn = findViewById(R.id.nextFrameBtn);
        media = new MediaMetadataRetriever();
        media.setDataSource(videoPath);

        trackingFrames = trackingSession.getTrackingFrames();
        if (trackingFrames.size() == 0) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            return;
        }
        currIndex = 0;
        updatePage();

        nextFrameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currIndex++;
                updatePage();
            }
        });

        instrumentSpinner = findViewById(R.id.instrumentSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.instruments_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        instrumentSpinner.setAdapter(adapter);
        instrumentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int index, long id) {
                String playerLabel = adapterView.getItemAtPosition(index).toString();
                trackingSession.addPlayerLabel(currIndex, playerLabel);
                Toast.makeText(ClassifyActivity.this, Integer.toString(trackingSession.playerMap.get(playerLabel).size()), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void updatePage() {
        if (currIndex >= trackingFrames.size()) {
            String xmlTargetPath = Environment.getExternalStorageDirectory() +
                    File.separator + "ConcertStitch" + File.separator + "testOutput.xml";
            File xmlTarget = new File(xmlTargetPath);
            XmlEncoder.saveXmlFromTrackingSession(trackingSession, xmlTarget);
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            return;
        } else if (currIndex == trackingFrames.size() - 1) {
            nextFrameBtn.setText("Submit");
        }
        currTrackingFrame = trackingFrames.get(currIndex);
        Toast.makeText(this, Float.toString(currTrackingFrame.startTime), Toast.LENGTH_SHORT).show();
        currImage = media.getFrameAtTime(currTrackingFrame.startTime * 1000, MediaMetadataRetriever.OPTION_CLOSEST);
        framePreview.setImageBitmap(currImage);
    }
}
