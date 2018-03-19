package edu.tamu.ecen.capstone.patientmd.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.File;

import edu.tamu.ecen.capstone.patientmd.R;

public class PhotoActivity extends AppCompatActivity {

    private final String TAG = "PhotoActivity: ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);


    }

}


