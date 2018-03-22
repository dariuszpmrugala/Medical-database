package edu.tamu.ecen.capstone.patientmd.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import edu.tamu.ecen.capstone.patientmd.util.Const;



/**
 * Created by Reese on 3/9/2018.
 *
 * Contains some simple utility methods
 */

public class Util {

    /*
        Returns a date using a long for system time in milliseconds
            To use this for current time, call with System.currentTimeMillis()
     */
    public static String dateForFile(long time) {
        Date date = new Date(time);
        DateFormat formatter = new SimpleDateFormat("MM-dd-yy_HHmmss");

        return formatter.format(date);
    }

    /*
    get permission for reading from external storage
    @return: true if permission granted
     */
    public static boolean permissionExternalRead(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_EXTERNAL_STORAGE));
            else {
                ActivityCompat.requestPermissions(activity, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, Const.PERMISSION_READ_EXT_STORAGE_CODE);
            }
            return ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
        else return true;
    }

    /*
    get permission for writing to external storage
    @return: true if permission granted
     */
    public static boolean permissionExternalWrite(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE));
            else {
                ActivityCompat.requestPermissions(activity, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, Const.PERMISSION_WRITE_EXT_STORAGE_CODE);
            }
            return ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
        else return true;
    }

    /*
    get permission for camera
    @return: true if permission granted
     */
    public static boolean permissionCamera(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(activity, new String[] {Manifest.permission.CAMERA}, Const.PERMISSION_CAMERA_CODE);

            return ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        }
        else return true;
    }

}
