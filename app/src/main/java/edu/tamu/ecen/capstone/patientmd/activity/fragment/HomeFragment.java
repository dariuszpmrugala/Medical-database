package edu.tamu.ecen.capstone.patientmd.activity.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
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
        View view = getView();
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null) {
                parent.removeView(view);
            }
        }
        return inflater.inflate(R.layout.home_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView(view);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy:: ");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach:: ");
    }

    @Override
    public void onResume() {
        super.onResume();
        //highlight relevant tab on nav menu
    }

    /*
    Function sets the initial view whenever the app is opened
     */
    private void initView(View view) {
        Log.d(TAG, "initRecordView");
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
                builder.setTitle(R.string.record_input_type);
                builder.setItems(new CharSequence[]
                                {"Take a picture", "Select an existing image", /*"Select other existing file"*/},
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
                                        Log.d(TAG, "Record Button:: use image");
                                        getExistingImage();
                                        break;
                                        /*  todo get file from other part of storage like pdf
                                    case 2:
                                        Log.d(TAG, "Record Button:: use existing file (other)");
                                        getExistingFile();
                                        break;
                                        */
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
            photoFile = createImageFile();
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

        //Handle the result of activity used for taking a picture
        if(requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            int index = mCurrentPhotoPath.lastIndexOf("/");
            String toastMessage = mCurrentPhotoPath.substring(index+1, mCurrentPhotoPath.length());
            Log.d(TAG,"onActivityResult:: new Image file: "+mCurrentPhotoPath);
            Log.d(TAG,"onActivityResult:: Filesize: "+ (Util.getFileSize(mCurrentPhotoPath)) + " bytes");

            Toast.makeText(getActivity().getApplicationContext(), "Filename: "+toastMessage, Toast.LENGTH_LONG)
                    .show();

            //ToDo: anything that requires the picture as soon as it is taken
            AsyncTask.execute(Util.runnableUpdateTable);
        }

        if(requestCode == REQUEST_EXISTING_IMAGE && resultCode == Activity.RESULT_OK) {
            //First get the filepath of the file we selected
            Uri uri = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getActivity().getContentResolver().query(uri,
                    filePathColumn, null, null, null);
            // Move to first row
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String filePath = cursor.getString(columnIndex);
            Log.d(TAG, "onActivityResult:: existing file at "+filePath);

            //Now save a copy of the file into app storage
            File dest = createImageFile();
            File src = new File(filePath);
            if(dest == null || src.length()==0) {
                Log.w(TAG, "Existing file:: null file");
                return;
            }
            try {
                Util.copyFile(src, dest);
            } catch (IOException ex) {
                Log.e(TAG, "Existing file:: error copying to "+filePath, ex);
            }
            //TODO anything that requires updating due to new record
            AsyncTask.execute(Util.runnableUpdateTable);

        }
    }


    String mCurrentPhotoPath;

    private File createImageFile() {
        // Create an image file name

        //todo necessary to get this permission?? Should only need to read
        //Util.permissionExternalWrite(getActivity());
        String imageFileName = Util.getImgFilepath() + "/" + Util.dateForFile(System.currentTimeMillis()) + ".jpg";
        File image;
        try {
            image = new File(imageFileName);
            image.getParentFile().mkdirs();
            image.createNewFile();

            if (image.exists())
                Log.d(TAG, "createImageFile:: file created successfully");

            // Save a file: path for use with ACTION_VIEW intents
            mCurrentPhotoPath = image.getAbsolutePath();
        } catch (IOException ex) {
            Log.d(TAG, "CreateImageFile:: IOException", ex);
            return null;
        }
        Log.d(TAG, "CreateImageFile:: path is "+mCurrentPhotoPath);
        return image;
    }

    private int REQUEST_EXISTING_IMAGE = 1560;

    private void getExistingImage() {
        Util.permissionExternalRead(getActivity());
        try {
            Intent intent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

            startActivityForResult(intent, REQUEST_EXISTING_IMAGE);
        } catch (Exception e) {
            Log.e(TAG, "getExistingImage:: error", e);
        }
    }




}
