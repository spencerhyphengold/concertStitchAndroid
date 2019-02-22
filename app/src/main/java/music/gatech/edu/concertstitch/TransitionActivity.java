package music.gatech.edu.concertstitch;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class TransitionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transition);

        final Button uploadOrRecordBtn = findViewById(R.id.upload_or_record_btn);
        final Button viewVideoBtn = findViewById(R.id.view_video_btn);


        uploadOrRecordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent testIntent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(testIntent);
            }
        });

        viewVideoBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent loginToVidIntent = new Intent(getApplicationContext(), VideoPageActivity.class);
                startActivity(loginToVidIntent);

            }
        });


    }
}
