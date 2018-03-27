package edu.tamu.ecen.capstone.patientmd.activity.fragment;

import android.app.Activity;
import android.content.Context;
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
import edu.tamu.ecen.capstone.patientmd.util.Util;


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
                final Context context = getContext();
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle(R.string.record_input_type);/*
                        .setCancelable(true)
                        .setPositiveButton(R.string.picture, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                dispatchTakePictureIntent();

                            }
                        })
                        .setNeutralButton(R.string.record_in_storage, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //todo get picture from storage
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();    */
                builder.setItems(new CharSequence[]
                                {"Take a picture", "Select an existing File"},
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // The 'which' argument contains the index position
                                // of the selected item
                                switch (which) {
                                    case 0:
                                        Log.d(TAG, "Record Button:: take a picture");
                                        dispatchTakePictureIntent();
                                        break;
                                    case 1:
                                        Log.d(TAG, "Record Button:: use existing");
                                        Toast.makeText(context, "clicked 'existing record'", Toast.LENGTH_SHORT).show();
                                        break;
                                }
                            }
                        }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.show();

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
            Log.d(TAG,"onActivityResult:: Filesize: "+ (Util.getFileSize(mCurrentPhotoPath)) + " bytes");

            Toast.makeText(getActivity().getApplicationContext(), "Filename: "+toastMessage, Toast.LENGTH_LONG)
                    .show();

            //ToDo: anything that requires the picture as soon as it is taken
        }
    }


    String mCurrentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        Util.permissionExternalWrite(getActivity());
        String imageFileName = Util.getImgFilepath() + "/" + Util.dateForFile(System.currentTimeMillis()) + ".jpg";
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
