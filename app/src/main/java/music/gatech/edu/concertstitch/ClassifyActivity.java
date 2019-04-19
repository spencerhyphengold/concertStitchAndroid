package music.gatech.edu.concertstitch;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
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
import java.util.Iterator;
import java.util.SortedSet;

public class ClassifyActivity extends AppCompatActivity {

    private MediaMetadataRetriever media;
    private TrackingSession trackingSession;
    private SortedSet<TrackingSession.TrackingFrame> trackingFrames;
    Iterator<TrackingSession.TrackingFrame> iterator;
    private TrackingSession.TrackingFrame currTrackingFrame;
    private String videoPath;
    private Bitmap currBitmap;
    private Paint paint;
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
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10f);

        trackingFrames = trackingSession.getTrackingFrames();
        if (trackingFrames.size() == 0) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            return;
        }

        currIndex = 0;
        iterator = trackingFrames.iterator();
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
                trackingSession.addPlayerLabel(currTrackingFrame, playerLabel);
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
        currTrackingFrame = iterator.next();
        currBitmap = media.getFrameAtTime(currTrackingFrame.startTime * 1000, MediaMetadataRetriever.OPTION_CLOSEST);
        framePreview.setImageBitmap(currBitmap);

        TrackingSession.Coordinate coord = currTrackingFrame.coordinate;
        Bitmap tempBitmap = Bitmap.createBitmap(currBitmap.getWidth(), currBitmap.getHeight(), Bitmap.Config.RGB_565);
        Canvas tempCanvas = new Canvas(tempBitmap);
        tempCanvas.drawBitmap(currBitmap, 0, 0, null);
        tempCanvas.drawRect(new RectF(coord.minX, coord.maxY, coord.maxX, coord.minY), paint);
        framePreview.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));
    }
}
