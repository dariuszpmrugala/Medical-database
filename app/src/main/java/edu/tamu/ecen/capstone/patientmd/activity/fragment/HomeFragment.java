package edu.tamu.ecen.capstone.patientmd.activity.fragment;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import edu.tamu.ecen.capstone.patientmd.R;
import edu.tamu.ecen.capstone.patientmd.activity.MainActivity;
import edu.tamu.ecen.capstone.patientmd.util.Util;

import static edu.tamu.ecen.capstone.patientmd.util.Const.IMG_FILEPATH;

/**
 * Created by Reese on 3/21/2018.
 *
 * This Fragment is for showing the home application
 */

public class HomeFragment extends Fragment {


    private Button cameraButton;

    private final String TAG = "HomeFragment:";

    public static Fragment newInstance(String text) {
        Fragment frag = new HomeFragment();

        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.home_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView(view);

    }

    /*
    Function sets the initial view whenever the app is opened
     */
    private void initView(View view) {
        Log.d(TAG, "initView");
        //setup the button for accessing the camera
        cameraButton = (Button) view.findViewById(R.id.new_record_button);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "CameraButton: onClick");

                /*
                if (MainActivity.this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "CameraButton: User must provide storage write access");
                }*/

                //when the user clicks the button, they should be prompted to confirm they want to take a picture
                new AlertDialog.Builder(getContext())
                        .setMessage(R.string.camera_confirmation)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //begin process to taking a picture
                                dispatchTakePictureIntent();
                                /*Intent intent = new Intent(MainActivity.this, PhotoActivity.class);
                                startActivity(intent);
                                */
                            }
                        })
                        .show();

            }
        });


    }

    static final int REQUEST_TAKE_PHOTO = 1559;

    private void dispatchTakePictureIntent() {
        Util.permissionCamera(getActivity());
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e(TAG, "dispatchTakePictureIntent:: error", ex);
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getContext(),
                        "edu.tamu.ecen.capstone.input.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            int index = mCurrentPhotoPath.lastIndexOf("/");
            String toastMessage = mCurrentPhotoPath.substring(index+1, mCurrentPhotoPath.length());
            Log.d(TAG,"onActivityResult:: File: "+mCurrentPhotoPath);
            Toast.makeText(getActivity().getApplicationContext(), "Filename: "+toastMessage, Toast.LENGTH_LONG)
                    .show();

            //ToDo: anything that requires the picture as soon as it is taken
        }
    }


    String mCurrentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        Util.permissionExternalWrite(getActivity());
        String imageFileName = IMG_FILEPATH + "/" + Util.dateForFile(System.currentTimeMillis()) + ".jpg";
        File image = new File(imageFileName);
        image.getParentFile().mkdirs();
        image.createNewFile();

        if(image.exists())
            Log.d(TAG,"createImageFile:: file created successfully");

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        Log.d(TAG, "CreateImageFile:: path is "+mCurrentPhotoPath);
        return image;
    }

}
