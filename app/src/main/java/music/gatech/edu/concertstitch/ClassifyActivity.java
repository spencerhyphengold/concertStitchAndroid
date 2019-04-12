package music.gatech.edu.concertstitch;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;

import java.util.ArrayList;

public class ClassifyActivity extends AppCompatActivity {

    private MediaMetadataRetriever media = new MediaMetadataRetriever();
    private TrackingFrame currTrackingFrame;
    private ArrayList<TrackingFrame> trackingFrames;
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
        trackingFrames = (ArrayList<TrackingFrame>) args.getSerializable("trackingFrames");
        videoPath = args.getString("videoPath");

        framePreview = findViewById(R.id.framePreview);
        nextFrameBtn = findViewById(R.id.nextFrameBtn);

        if (trackingFrames.size() == 0) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            return;
        }
        currIndex = 0;
        currTrackingFrame = trackingFrames.get(0);
        media.setDataSource(videoPath);
        currImage = media.getFrameAtTime(currTrackingFrame.startTime, MediaMetadataRetriever.OPTION_CLOSEST);
        framePreview.setImageBitmap(currImage);

        nextFrameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currIndex++;
                if (currIndex == trackingFrames.size() - 1) {
                    nextFrameBtn.setText("Submit");
                } else if (currIndex < trackingFrames.size() - 1) {
                    currTrackingFrame = trackingFrames.get(currIndex);
                    currImage = media.getFrameAtTime(currTrackingFrame.startTime, MediaMetadataRetriever.OPTION_CLOSEST);
                    framePreview.setImageBitmap(currImage);
                } else {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                }
            }
        });

        instrumentSpinner = findViewById(R.id.instrumentSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.instruments_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        instrumentSpinner.setAdapter(adapter);
        instrumentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                currTrackingFrame.player = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }
}
