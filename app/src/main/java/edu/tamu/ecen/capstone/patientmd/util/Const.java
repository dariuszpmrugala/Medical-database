package edu.tamu.ecen.capstone.patientmd.util;


import android.content.Context;
import android.os.Environment;

import java.io.File;

 /**
 * Created by reesul on 2/27/2018.
 * Store constant values here
 */

public class Const {

    //File path to store images taken by this app
    public static String IMG_FILEPATH; //= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/patientMD";

    public static final int PERMISSION_CAMERA_CODE = 1;
    public static final int PERMISSION_READ_EXT_STORAGE_CODE = 2;
    public static final int PERMISSION_WRITE_EXT_STORAGE_CODE = 3;
    public static final int PERMISSION_INTERNET_CODE = 4;

    public static final double RECORD_VIEW_SCALE = 2.5;


    public static final String ADDRESS = "http://10.236.35.219";
    public static final String PORT = "8000";






}
