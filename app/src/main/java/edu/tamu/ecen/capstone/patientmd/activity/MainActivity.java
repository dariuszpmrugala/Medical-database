package edu.tamu.ecen.capstone.patientmd.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.util.Log;

import com.google.android.gms.common.oob.SignUp;

import java.io.File;

import edu.tamu.ecen.capstone.patientmd.R;
import edu.tamu.ecen.capstone.patientmd.activity.fragment.HomeFragment;
import edu.tamu.ecen.capstone.patientmd.activity.fragment.PlotFragment;
import edu.tamu.ecen.capstone.patientmd.activity.fragment.RecordFragment;
import edu.tamu.ecen.capstone.patientmd.database.DatabaseHelper;
import edu.tamu.ecen.capstone.patientmd.util.Util;


public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;

    private final String TAG = "MainActivity: ";

    DatabaseHelper myDb;

    private int mSelectedItem;

    //app specific permission codes; to add more permissions, include in setPermissions and AndroidManifest.xml
    private final int PERMISSION_READ_EXT_STORAGE_CODE=2;
    private final int PERMISSION_WRITE_EXT_STORAGE_CODE=3;

    private Button cameraButton;

    private FragmentManager fragmentManager;
    private Fragment currentFragment;


    //todo use a selector to make the icons change
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            return selectFragment(item);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myDb = new DatabaseHelper(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate");

        Util.setDeviceDimensions(this);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        initView();

        //initialize the path that we save images and data
        Util.setImgFilepath(this.getFilesDir().getAbsolutePath()+"/patientMD/records");
        new File(Util.getImgFilepath()).mkdirs();
        Util.setDataFilepath(this.getFilesDir().getAbsolutePath()+"/patientMD/data");
        new File(Util.getDataFilepath()).mkdirs();

        //instantiate a table to create a table for the bitmaps; should be easier to access in the future
        AsyncTask.execute(new Runnable(){
            @Override
            public void run(){
                Util.initRecordTable();
            }
        });


        //todo get dropbox working;
        //FileUtil.initDropbox();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.miHelp:
                ShowMessage("Help", "Taking a Picture: \n\n" +
                        "-  Use a 4:3 perspective\n" +
                        "-  Ensure good overhead lighting\n" +
                        "-  No papers underneath the record\n" +
                        "-  No wrinkled papers\n" +
                        "-  Landscape images are acceptable\n\n");
                return true;

            case R.id.miProfile:
                myDb.deleteAllData();
                Intent myIntent = new Intent(this, SignUpActivity.class);
                this.startActivity(myIntent);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    /*
    setup home fragment when the app is opened
     */
    private void initView() {

        Fragment homeFrag = HomeFragment.newInstance(null);
        fragmentManager = getSupportFragmentManager();/*
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.add(R.id.fragment_container, homeFrag, "HomeFragment init");
        ft.commit();
        */

        startFragment(homeFrag, "HomeFragment init");
    }


    private boolean selectFragment(MenuItem item) {
        Fragment fragment = null;
        int viewId;
        String tag;

        // Special case, this is the default for when the app starts
        if (item==null) {
            Log.d(TAG, "SelectFragment:: Home");
            fragment = HomeFragment.newInstance(null);
            viewId = R.layout.home_fragment;
            tag = "HomeFragment";
        }

        else {
            //todo add all fragments here
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Log.d(TAG, "SelectFragment:: Home");
                    item.setChecked(true);
                    fragment = HomeFragment.newInstance(null);
                    viewId = R.layout.home_fragment;
                    tag = "HomeFragment";
                    // mTextMessage.setText(R.string.title_home);
                    break;
                case R.id.navigation_records:
                    Log.d(TAG, "SelectFragment:: Records");
                    item.setChecked(true);
                    fragment = RecordFragment.newInstance();
                    viewId = R.layout.record_fragment_layout;
                    tag = "RecordFragment";
                    //mTextMessage.setText(R.string.title_archive);
                    break;
                case R.id.navigation_plots:
                    Log.d(TAG, "SelectFragment:: Plots");
                    item.setChecked(true);
                    fragment = PlotFragment.newInstance();
                    viewId = R.layout.plot_fragment_layout;
                    tag = "PlotFragment";
                    //mTextMessage.setText(R.string.title_plots);
                    break;
                default:
                    return false;
            }

        }


        //Fragment homeFrag = fragmentManager.findFragmentByTag("HomeFragment init");

        return startFragment(fragment, tag);
    }

    private boolean startFragment(Fragment fragment, String tag) {
        if(fragment != null || fragment==currentFragment) {

            try {
                FragmentTransaction ft = fragmentManager.beginTransaction();
                //ft.remove(homeFrag);
                //ft.replace(viewId, fragment);
                ft.replace(R.id.fragment_container, fragment, tag);
                ft.addToBackStack(null);
                ft.show(fragment);
                ft.commit();
                currentFragment = fragment;
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    public void ShowMessage(String title,String Message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(Message);
        builder.show();
    }


}
