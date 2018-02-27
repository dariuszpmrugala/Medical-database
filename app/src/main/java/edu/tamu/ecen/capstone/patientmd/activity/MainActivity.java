package edu.tamu.ecen.capstone.patientmd.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import edu.tamu.ecen.capstone.patientmd.R;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;

    //app specific permission codes; to add more permissions, include in setPermissions and AndroidManifest.xml
    private final int PERMISSION_CAMERA_CODE=1;
    private final int PERMISSION_READ_EXT_STORAGE_CODE=2;
    private final int PERMISSION_WRITE_EXT_STORAGE_CODE=3;

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
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        //todo add this from R, and create an onClickListener; should call to RecordPhoto
        Button cameraButton;

        //get set of permissions from the user
        requestPermissionCamera();

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
