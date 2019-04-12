package music.gatech.edu.concertstitch;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
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
    private ArrayList<TrackingFrame> trackingFrames;
    private String videoPath;
    private BitmapDrawable currImage;
    private long currTime;
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
        currTime = trackingFrames.get(0).startTime;
        // TODO: fix permissions errors thrown by MediaMetadataRetriever
        media.setDataSource(videoPath);
        currImage = new BitmapDrawable(getResources(), media.getFrameAtTime(currTime));
        framePreview.setBackground(currImage);

        nextFrameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currIndex++;
                if (currIndex < trackingFrames.size() - 1) {
                    currTime = trackingFrames.get(currIndex).startTime;
                    currImage = new BitmapDrawable(getResources(), media.getFrameAtTime(currTime));
                    framePreview.setBackground(currImage);
                } else if (currIndex == trackingFrames.size() - 1) {
                    nextFrameBtn.setText("Submit");
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

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }
}
