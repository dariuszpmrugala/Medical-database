package edu.tamu.ecen.capstone.patientmd.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.File;

import edu.tamu.ecen.capstone.patientmd.R;
import edu.tamu.ecen.capstone.patientmd.input.Camera2BasicFragment;
import edu.tamu.ecen.capstone.patientmd.input.ViewImageFragment;

public class PhotoActivity extends AppCompatActivity {

    private final String TAG = "PhotoActivity: ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        Intent i = getIntent();
        if(i.getBooleanExtra("image", false)){
            //todo start fragment that shows the image
            Log.d(TAG, "onCreate:: image intent found");

            String filePath = i.getStringExtra("filepath");
            if(filePath!=null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, ViewImageFragment.newInstance(new File(filePath)))
                        .commit();
            }
        }

        else if (null == savedInstanceState) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, Camera2BasicFragment.newInstance())
                    .commit();
        }
    }

}
