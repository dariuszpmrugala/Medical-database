package edu.tamu.ecen.capstone.patientmd.util;


import android.os.Environment;

/**
 * Created by reesul on 2/27/2018.
 * Store constant values here
 */

public class Const {

    //File path to store images taken by this app
    public static final String IMG_FILEPATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/patientMD";

    public static final int PERMISSION_CAMERA_CODE = 1;
    public static final int PERMISSION_READ_EXT_STORAGE_CODE = 2;
    public static final int PERMISSION_WRITE_EXT_STORAGE_CODE = 3;

}
