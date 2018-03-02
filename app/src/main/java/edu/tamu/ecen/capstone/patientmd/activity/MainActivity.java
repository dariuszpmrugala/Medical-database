package edu.tamu.ecen.capstone.patientmd.activity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.util.Log;

import edu.tamu.ecen.capstone.patientmd.R;
import edu.tamu.ecen.capstone.patientmd.input.RecordPhoto;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;

    private final String TAG = "MainActivity: ";

    //app specific permission codes; to add more permissions, include in setPermissions and AndroidManifest.xml
    private final int PERMISSION_CAMERA_CODE=1;
    private final int PERMISSION_READ_EXT_STORAGE_CODE=2;
    private final int PERMISSION_WRITE_EXT_STORAGE_CODE=3;

    private Button cameraButton;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            //TODO use setContentView to show different screens
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_archive:
                    mTextMessage.setText(R.string.title_archive);
                    return true;
                case R.id.navigation_plots:
                    mTextMessage.setText(R.string.title_plots);
                    return true;
            }
            Log.d(TAG, "NavigationMenuListener");
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate");

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        //todo add this from R, and create an onClickListener; should call to RecordPhoto
        initView();

        //get set of permissions from the user
        requestPermissionCamera();

    }


/*
Function sets the initial view whenever the app is opened
 */
    private void initView() {
        //setup the button for accessing the camera
        cameraButton = (Button) findViewById(R.id.photo_button);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!RecordPhoto.isRunning) {
                    //Maybe not use this type of context??
                    RecordPhoto.takePhoto(getApplicationContext());
                }
                else Log.d(TAG, "Cam button click; unavailable");
            }
        });


    }


    /*
    Here we ask the user to provide permissions if they had not before
    Returned String contains the permissions NOT granted yet
        This lets user be aware of which features will not be accessible to them
     */
    protected void requestPermissionCamera() {

        //permission for camera access
        if (this.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            //Use alert dialog if you want to add additional dialog to a permission request
            /*final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("This app needs camera access");
            builder.setMessage("Please grant location access so this app can detect peripherals.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, );
                }
            });*/
            shouldShowRequestPermissionRationale("Camera access is required so you can take images of your health records within the app");
            requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSION_CAMERA_CODE);
        }

    }

    protected void requestPermissionStorage() {
        if (this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            shouldShowRequestPermissionRationale("Camera access is required so you can take images of your health records within the app");
            requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSION_READ_EXT_STORAGE_CODE);
        }

        if (this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            shouldShowRequestPermissionRationale("Camera access is required so you can take images of your health records within the app");
            requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSION_WRITE_EXT_STORAGE_CODE);
        }

    }

}
