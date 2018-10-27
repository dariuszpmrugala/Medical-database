package edu.tamu.ecen.capstone.patientmd.activity.fragment;

import android.app.Activity;
import android.app.FragmentManager;
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
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import edu.tamu.ecen.capstone.patientmd.R;
import edu.tamu.ecen.capstone.patientmd.database.DatabaseEntry;
import edu.tamu.ecen.capstone.patientmd.database.DatabaseHelper;
import edu.tamu.ecen.capstone.patientmd.util.Const;
import edu.tamu.ecen.capstone.patientmd.util.FileUtil;
import edu.tamu.ecen.capstone.patientmd.util.NetworkUtil;
import edu.tamu.ecen.capstone.patientmd.util.Util;


/**
 * Created by Reese on 3/21/2018.
 *
 * This Fragment is for showing the home application
 *
 * TODO allow other applcations to share images with this application
 */

public class HomeFragment extends Fragment {

    private DatabaseHelper myDb;
    private Spinner spinner_tests;
    private EditText editText_date;
    private EditText editText_result;
    private EditText editText_units;
    private TextView error_date;
    private TextView error_result;
    private TextView error_units;

    private Button cameraButton;
    private Button uploadButton;
    private Button deleteButton;
    private Button editButton;
    private String item;
    private String[] listItems;

    private List<DatabaseEntry> entries = new ArrayList<>();

    private DatabaseEntry entry;

    private final String TAG = "HomeFragment:";

    public static Fragment newInstance(String text) {
        Fragment frag = new HomeFragment();

        return frag;
    }

    /*
    Handles the view whenever the fragment is created
     */
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
        myDb = new DatabaseHelper(getContext());

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


                //when the user clicks the button, they are prompted with how they want to provide a new record
                final Context context = getContext();
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(R.string.record_input_type);
                builder.setItems(new CharSequence[]
                                {"Take a picture", "Select an existing image", "For testing purposes"},
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
                                    case 2:
                                        startDatabaseFragment();
                                        break;

                                }
                            }
                        });
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                });
                builder.show();

            }
        });

        uploadButton = view.findViewById(R.id.upload_button);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Record Button:: insert manual record");
                AlertDialog.Builder builder_add_existing = new AlertDialog.Builder(getContext());
                builder_add_existing.setTitle("ADD NEW ENTRY");

                View mView = getLayoutInflater().inflate(R.layout.dialog_add, null);
                spinner_tests = mView.findViewById(R.id.spinner_tests);
                editText_result = mView.findViewById(R.id.editText_result);
                editText_date = mView.findViewById(R.id.editText_date);
                TextWatcher tw = new TextWatcher() {
                    private String current = "";
                    private String ddmmyyyy = "MMDDYYYY";
                    private Calendar cal = Calendar.getInstance();
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (!s.toString().equals(current)) {
                            String clean = s.toString().replaceAll("[^\\d.]|\\.", "");
                            String cleanC = current.replaceAll("[^\\d.]|\\.", "");

                            int cl = clean.length();
                            int sel = cl;
                            for (int i = 2; i <= cl && i < 6; i += 2) {
                                sel++;
                            }
                            //Fix for pressing delete next to a forward slash
                            if (clean.equals(cleanC)) sel--;

                            if (clean.length() < 8){
                                clean = clean + ddmmyyyy.substring(clean.length());
                            }else{
                                //This part makes sure that when we finish entering numbers
                                //the date is correct, fixing it otherwise
                                int day  = Integer.parseInt(clean.substring(2,4));
                                int mon  = Integer.parseInt(clean.substring(0,2));
                                int year = Integer.parseInt(clean.substring(4,8));

                                mon = mon < 1 ? 1 : mon > 12 ? 12 : mon;
                                cal.set(Calendar.MONTH, mon-1);
                                year = (year<1900)?1900:(year>2100)?2100:year;
                                cal.set(Calendar.YEAR, year);
                                // ^ first set year for the line below to work correctly
                                //with leap years - otherwise, date e.g. 29/02/2012
                                //would be automatically corrected to 28/02/2012

                                day = (day > cal.getActualMaximum(Calendar.DATE))? cal.getActualMaximum(Calendar.DATE):day;
                                clean = String.format("%02d%02d%02d",mon, day, year);
                            }

                            clean = String.format("%s/%s/%s", clean.substring(0, 2),
                                    clean.substring(2, 4),
                                    clean.substring(4, 8));

                            sel = sel < 0 ? 0 : sel;
                            current = clean;
                            editText_date.setText(current);
                            editText_date.setSelection(sel < current.length() ? sel : current.length());
                        }
                    }
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void afterTextChanged(Editable s) {}

                };

                editText_date.addTextChangedListener(tw);
                error_result = mView.findViewById(R.id.error_result);
                error_date = mView.findViewById(R.id.error_date);

                builder_add_existing.setPositiveButton("OK", null);
                builder_add_existing.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });


                builder_add_existing.setView(mView);
                AlertDialog my_dialog = builder_add_existing.create();

                my_dialog.setOnShowListener(new DialogInterface.OnShowListener() {

                    @Override
                    public void onShow(DialogInterface dialog) {

                        Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                        button.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {
                                if (editText_result.getText().toString().equals("") || editText_result.getText().toString().equals("NUMERICAL VALUE")) {
                                    error_result.setText(R.string.error_result_string);
                                }
                                else {
                                    error_result.setText("");
                                }
                                if (editText_date.getText().toString().equals("")
                                        || editText_date.getText().toString().contains("M") || editText_date.getText().toString().contains("D")
                                        || editText_date.getText().toString().contains("Y")) {
                                    error_date.setText("Input must be a date in the MM/DD/YYYY format.");
                                }
                                else {
                                    error_date.setText("");
                                }

                            }
                        });
                    }
                });

                my_dialog.show();

                SpinnerTests();
            }
        });

        deleteButton = view.findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Record Button:: insert manual record");
                AlertDialog.Builder builder_delete = new AlertDialog.Builder(getContext());
                builder_delete.setTitle("DELETE ENTRY");

                View mView = getLayoutInflater().inflate(R.layout.dialog_delete, null);
                spinner_tests = mView.findViewById(R.id.spinner_tests);


                builder_delete.setPositiveButton("OK", null);
                builder_delete.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });


                builder_delete.setView(mView);
                final AlertDialog my_dialog = builder_delete.create();
                SpinnerTests();

                my_dialog.setOnShowListener(new DialogInterface.OnShowListener() {

                    @Override
                    public void onShow(DialogInterface dialog) {

                        Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                        button.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {
                                Cursor res = myDb.getAllData();
                                if(res.getCount() == 0) {
                                    my_dialog.dismiss();
                                }
                                else {
                                    entries.clear();
                                    entries = myDb.queryData("Tests", item);
                                    entries = OrderEntries(entries);
                                    final ArrayList<Integer> mUserItems = new ArrayList<>();
                                    listItems = new String[entries.size()];

                                    for(int i = 0; i < entries.size(); ++i) {
                                        listItems[i] = entries.get(i).getDate() + ", " + entries.get(i).getResult() + " " + entries.get(i).getUnits();
                                    }

                                    boolean[] checkedItems = new boolean[listItems.length];
                                    my_dialog.dismiss();

                                    AlertDialog.Builder mBuilder = new AlertDialog.Builder(getContext());
                                    mBuilder.setTitle("DELETE ENTRY");
                                    mBuilder.setMultiChoiceItems(listItems, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int position, boolean isChecked) {
                                            if(isChecked) {
                                                if(!mUserItems.contains(position)) {
                                                    mUserItems.add(position);
                                                }
                                            }
                                            else if (mUserItems.contains(position)) {
                                                mUserItems.remove((Integer)position);
                                            }
                                        }
                                    });

                                    mBuilder.setPositiveButton("OK", null);
                                    mBuilder.setNegativeButton("CANCEL", null);

                                    AlertDialog mDialog = mBuilder.create();

                                    mDialog.setOnShowListener(new DialogInterface.OnShowListener() {

                                        @Override
                                        public void onShow(DialogInterface dialog) {

                                            Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                                            button.setOnClickListener(new View.OnClickListener() {

                                                @Override
                                                public void onClick(View view) {
                                                    if (mUserItems.size() == 0) {
                                                        ShowMessage("INPUT ERROR", "Please select at least one entry to delete.");
                                                    }

                                                }
                                            });
                                        }
                                    });
                                    mDialog.show();

                                }

                            }
                        });
                    }
                });

                my_dialog.show();

            }


        });

        editButton = view.findViewById(R.id.edit_button);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Record Button:: insert manual record");
                AlertDialog.Builder builder_edit = new AlertDialog.Builder(getContext());
                builder_edit.setTitle("EDIT ENTRY");

                View mView = getLayoutInflater().inflate(R.layout.dialog_delete, null);
                spinner_tests = mView.findViewById(R.id.spinner_tests);


                builder_edit.setPositiveButton("OK", null);
                builder_edit.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });


                builder_edit.setView(mView);
                final AlertDialog my_dialog = builder_edit.create();
                SpinnerTests();

                my_dialog.setOnShowListener(new DialogInterface.OnShowListener() {

                    @Override
                    public void onShow(DialogInterface dialog) {

                        Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                        button.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {
                                Cursor res = myDb.getAllData();
                                if(res.getCount() == 0) {
                                    my_dialog.dismiss();
                                }
                                else {
                                    entries.clear();
                                    entries = myDb.queryData("Tests", item);
                                    entries = OrderEntries(entries);
                                    listItems = new String[entries.size()];

                                    for(int i = 0; i < entries.size(); ++i) {
                                        listItems[i] = entries.get(i).getDate() + ", " + entries.get(i).getResult() + " " + entries.get(i).getUnits();
                                    }

                                    my_dialog.dismiss();

                                    AlertDialog.Builder mBuilder = new AlertDialog.Builder(getContext());
                                    mBuilder.setTitle("EDIT ENTRY");
                                    mBuilder.setItems(listItems, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialogInterface, int position) {
                                            String selected = listItems[position];
                                            String date =  selected.substring(0, selected.indexOf(','));
                                            String value = selected.substring(selected.indexOf(',')+2, selected.indexOf(' ', selected.indexOf(',')+2));

                                            for (int i = 0; i < entries.size(); ++i) {
                                                if (entries.get(i).getDate().equals(date) && entries.get(i).getResult().equals(value))
                                                    entry = entries.get(i);
                                            }

                                            AlertDialog.Builder builder_edit = new AlertDialog.Builder(getContext());
                                            builder_edit.setTitle("EDIT ENTRY");

                                            View mView = getLayoutInflater().inflate(R.layout.dialog_edit, null);
                                            editText_result = mView.findViewById(R.id.editText_result);
                                            editText_result.setText(entry.getResult());

                                            editText_date = mView.findViewById(R.id.editText_date);
                                            TextWatcher tw = new TextWatcher() {
                                                private String current = "";
                                                private String ddmmyyyy = "MMDDYYYY";
                                                private Calendar cal = Calendar.getInstance();
                                                @Override
                                                public void onTextChanged(CharSequence s, int start, int before, int count) {
                                                    if (!s.toString().equals(current)) {
                                                        String clean = s.toString().replaceAll("[^\\d.]|\\.", "");
                                                        String cleanC = current.replaceAll("[^\\d.]|\\.", "");

                                                        int cl = clean.length();
                                                        int sel = cl;
                                                        for (int i = 2; i <= cl && i < 6; i += 2) {
                                                            sel++;
                                                        }
                                                        //Fix for pressing delete next to a forward slash
                                                        if (clean.equals(cleanC)) sel--;

                                                        if (clean.length() < 8){
                                                            clean = clean + ddmmyyyy.substring(clean.length());
                                                        }else{
                                                            //This part makes sure that when we finish entering numbers
                                                            //the date is correct, fixing it otherwise
                                                            int day  = Integer.parseInt(clean.substring(2,4));
                                                            int mon  = Integer.parseInt(clean.substring(0,2));
                                                            int year = Integer.parseInt(clean.substring(4,8));

                                                            mon = mon < 1 ? 1 : mon > 12 ? 12 : mon;
                                                            cal.set(Calendar.MONTH, mon-1);
                                                            year = (year<1900)?1900:(year>2100)?2100:year;
                                                            cal.set(Calendar.YEAR, year);
                                                            // ^ first set year for the line below to work correctly
                                                            //with leap years - otherwise, date e.g. 29/02/2012
                                                            //would be automatically corrected to 28/02/2012

                                                            day = (day > cal.getActualMaximum(Calendar.DATE))? cal.getActualMaximum(Calendar.DATE):day;
                                                            clean = String.format("%02d%02d%02d",mon, day, year);
                                                        }

                                                        clean = String.format("%s/%s/%s", clean.substring(0, 2),
                                                                clean.substring(2, 4),
                                                                clean.substring(4, 8));

                                                        sel = sel < 0 ? 0 : sel;
                                                        current = clean;
                                                        editText_date.setText(current);
                                                        editText_date.setSelection(sel < current.length() ? sel : current.length());
                                                    }
                                                }
                                                @Override
                                                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                                                @Override
                                                public void afterTextChanged(Editable s) {}

                                            };

                                            editText_date.addTextChangedListener(tw);
                                            if (date.indexOf('/') < 2)
                                                date = "0" + date;
                                            if (date.indexOf('/', 3) < 5)
                                                date = date.substring(0, 3) + "0" + date.substring(3, date.length());

                                            editText_date.setText(date);

                                            editText_units = mView.findViewById(R.id.editText_units);
                                            editText_units.setText(entry.getUnits());

                                            error_result = mView.findViewById(R.id.error_result);
                                            error_date = mView.findViewById(R.id.error_date);
                                            error_units = mView.findViewById(R.id.error_units);

                                            builder_edit.setPositiveButton("OK", null);
                                            builder_edit.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {

                                                }
                                            });


                                            builder_edit.setView(mView);
                                            final AlertDialog my_dialog = builder_edit.create();

                                            my_dialog.setOnShowListener(new DialogInterface.OnShowListener() {

                                                @Override
                                                public void onShow(DialogInterface dialog) {

                                                    Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                                                    button.setOnClickListener(new View.OnClickListener() {

                                                        @Override
                                                        public void onClick(View view) {
                                                            if (editText_result.getText().toString().equals("") || editText_result.getText().toString().equals("NUMERICAL VALUE")) {
                                                                error_result.setText(R.string.error_result_string);
                                                            }
                                                            else {
                                                                error_result.setText("");
                                                            }
                                                            if (editText_date.getText().toString().equals("")
                                                                    || editText_date.getText().toString().contains("M") || editText_date.getText().toString().contains("D")
                                                                    || editText_date.getText().toString().contains("Y")) {
                                                                error_date.setText("Input must be a date in the MM/DD/YYYY format.");
                                                            }
                                                            else {
                                                                error_date.setText("");
                                                            }
                                                            if (editText_units.getText().toString().equals(""))
                                                                error_units.setText("Input must be a unit.");
                                                            else
                                                                error_units.setText("");

                                                        }
                                                    });
                                                }
                                            });

                                            my_dialog.show();
                                        }
                                    });

                                    mBuilder.setNegativeButton("CANCEL", null);

                                    AlertDialog mDialog = mBuilder.create();

                                    mDialog.show();

                                }

                            }
                        });
                    }
                });

                my_dialog.show();

            }


        });



    }

    static final int REQUEST_TAKE_PHOTO = 1559;

    /*
    Starts the stock camera application for taking a picture
        Provides a file for the picture to actually go to
     */
    private void dispatchTakePictureIntent() {
        //todo make sure permission is not causing app to crash
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


    /*
    Function to handle the result of either taking a picture or selecting an existing one
        Takes the returned image (in both cases), and puts it into application specific storage
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.d(TAG, "onActivityResult: requestCode, resultCode: "+requestCode+","+resultCode);
        //Handle the result of activity used for taking a picture
        if(requestCode == REQUEST_TAKE_PHOTO) {
             if (resultCode == Activity.RESULT_OK) {
                 int index = mCurrentPhotoPath.lastIndexOf("/");
                 String toastMessage = mCurrentPhotoPath.substring(index + 1, mCurrentPhotoPath.length());
                 Log.d(TAG, "onActivityResult:: new Image file: " + mCurrentPhotoPath);
                 Log.d(TAG, "onActivityResult:: Filesize: " + (Util.getFileSize(mCurrentPhotoPath)) + " bytes");

                 Toast.makeText(getActivity().getApplicationContext(), "Filename: " + toastMessage, Toast.LENGTH_LONG)
                         .show();

                 //ToDo: anything that requires the picture as soon as it is taken
                 NetworkUtil.POST(Const.ADDRESS, Const.PORT, new File(mCurrentPhotoPath), getContext());
                 AsyncTask.execute(Util.runnableUpdateTable);
             }
            else if (resultCode == Activity.RESULT_CANCELED){
                Log.d(TAG, "Cancelled taking photo, deleting empty file " + mCurrentPhotoPath);
                File image = new File(mCurrentPhotoPath);
                image.delete();
            }
        }

        /*
        Handle result of selecting an existing image on the phone
        Makes a copy of the existing image
         */
        if(requestCode == REQUEST_EXISTING_IMAGE  && resultCode == Activity.RESULT_OK) {
            //First get the filepath of the file we selected from the uri returned by the activity
            Uri uri = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getActivity().getContentResolver().query(uri,
                    filePathColumn, null, null, null);
            // Move to first row(?)
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String filePath = cursor.getString(columnIndex);
            Log.d(TAG, "onActivityResult:: existing file at "+filePath);

            //check if the file is of valid type; ignore it if it is not
            boolean validExtension = filePath.endsWith(".jpg") || filePath.endsWith(".jpeg") || filePath.endsWith(".png");
            if(!validExtension) {
                Log.d(TAG, "onActivityResult:: file " + filePath + " is not a valid file type. Ignoring.");
                Toast.makeText(getContext(), "Invalid file type, please choose another file", Toast.LENGTH_LONG).show();
                return;
            }


            //save a copy of the file into app storage
            File dest = createImageFile();
            File src = new File(filePath);
            if(dest == null || src.length()==0) {
                Log.w(TAG, "Existing file:: null file");
                return;
            }
            try {
                if(Util.copyFile(src, dest))
                    Toast.makeText(getContext(), "Filename: "+dest.getName(), Toast.LENGTH_LONG).show();
            } catch (IOException ex) {
                Log.e(TAG, "Existing file:: error copying to "+filePath, ex);
            }

            //TODO anything needed whenever new record is created
            NetworkUtil.POST(Const.ADDRESS, Const.PORT, dest, getContext());
            //update the hash table that holds all bitmaps for the files in app storage
            AsyncTask.execute(Util.runnableUpdateTable);




        }
    }


    String mCurrentPhotoPath;

    /*
    Creates a file for new images/records
     */
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
            Log.d(TAG, "createImageFile:: IOException", ex);
            return null;
        }
        Log.d(TAG, "createImageFile:: path is "+mCurrentPhotoPath);
        return image;
    }

    private int REQUEST_EXISTING_IMAGE = 1560;

    /*
    Starts the activity for selecting an existing image from the phone's gallery app
     */
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


    private void startDatabaseFragment() {

        int viewId;
        String tag = "DatabaseFragment";

        Fragment fragment = DatabaseFragment.newInstance();

        android.support.v4.app.FragmentManager fragmentManager = getFragmentManager();

        FragmentTransaction ft = fragmentManager.beginTransaction();
        //ft.remove(homeFrag);
        //ft.replace(viewId, fragment);
        ft.replace(R.id.fragment_container, fragment, tag);
        ft.addToBackStack(null);
        ft.show(fragment);
        ft.commit();
    }

    public void SpinnerTests() {
        if (myDb==null)
            Log.d(TAG, "FUCK DB");

        Set<String> items = myDb.getAllField("tests");

        if (items.size() != 0) {
            String[] items_strings = new String[items.size()];

            Iterator it = items.iterator();
            int i = 0;
            while (it.hasNext()) {
                items_strings[i] = it.next().toString();
                ++i;
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                    android.R.layout.simple_spinner_item, items_strings);


            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            spinner_tests.setAdapter(adapter);
            spinner_tests.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view,
                                           int position, long id) {
                    ((TextView) parent.getChildAt(0)).setTextSize(20);
                    item = parent.getItemAtPosition(position).toString();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // TODO Auto-generated method stub
                }
            });

        }
    }

    public void ShowMessage(String title,String Message){
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(Message);
        builder.show();
    }

    public List<DatabaseEntry> OrderEntries(List<DatabaseEntry> entries) {

        DatabaseEntry temp;
        Date date1 = new Date();
        Date date2 = new Date();

        for (int i = 0; i < entries.size() - 1; ++i) {
            for (int j = 0; j < entries.size() - 1 - i; ++j) {
                SimpleDateFormat format = new SimpleDateFormat("M/dd/yyyy", Locale.US);
                try {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(format.parse(entries.get(j).getDate()));
                    date1 = calendar.getTime();

                    calendar.setTime(format.parse(entries.get(j + 1).getDate()));
                    date2 = calendar.getTime();

                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if (date2.getTime() < date1.getTime()) {
                    temp = entries.get(j + 1);
                    entries.set(j + 1, entries.get(j));
                    entries.set(j, temp);
                }
            }
        }

        return entries;
    }


}