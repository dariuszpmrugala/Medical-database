package edu.tamu.ecen.capstone.patientmd.util;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;

import static edu.tamu.ecen.capstone.patientmd.util.Const.RECORD_VIEW_SCALE;


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
    private static final String TAG = "Util:";

    private static int DEVICE_HEIGHT;
    private static int DEVICE_WIDTH;


    public static Hashtable<String, Bitmap> recordImageTable;

    public static void setDeviceDimensions(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Service.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point point = new Point();
        display.getRealSize(point);
        DEVICE_HEIGHT = point.y;
        DEVICE_WIDTH = point.x;
    }
    public static int getDeviceHeight() {
        return DEVICE_HEIGHT;
    }

    public static int getDeviceWidth() {
        return DEVICE_WIDTH;
    }


    /* Filepath to containing all images for this app */
    private static String IMG_FILEPATH=null;

    public static void setImgFilepath(String path) {
        if(IMG_FILEPATH==null)
            IMG_FILEPATH=path;
    }
    public static String getImgFilepath() {
        return IMG_FILEPATH;
    }

    public static String dateForFile(long time) {
        Date date = new Date(time);
        DateFormat formatter = new SimpleDateFormat("MM-dd-yy_HHmmss");

        return formatter.format(date);
    }

    public static long getFileSize(String filePath) {
        return (new File(filePath)).length();
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

    /*
    Copy file from out location to another
     */
    public static void copyFile(File src, File dst) throws IOException {
        Log.d(TAG, "CopyFile:: src has size in bytes: " + src.length());
        try (InputStream in = new FileInputStream(src)) {
            try (OutputStream out = new FileOutputStream(dst)) {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }

                Log.d(TAG, "CopyFile:: dest has size in bytes: " + dst.length());
            }
        }
    }

    public static String getLastPathComponent(File file) {
        String[] segments = file.getAbsolutePath().split("/");
        if (segments.length == 0)
            return "";
        String lastPathComponent = segments[segments.length - 1];
        Log.d(TAG, "getLastPathComponent: " + lastPathComponent);
        return lastPathComponent;
    }

    /*
    Run this in a background thread
     */
    public static void initRecordTable() {
        File[] allImages = (new File(getImgFilepath())).listFiles();
        recordImageTable = new Hashtable<>(allImages.length);

        updateRecordTable();
    }

    public static void updateRecordTable() {
        File[] allImages = (new File(getImgFilepath())).listFiles();

        for (File image : allImages) {
            String fileName = image.getName();
            Log.d(TAG, "updateRecordTable: file name is "+fileName);
            if (!(fileName.contains(".jpg") || fileName.contains(".jpeg") || fileName.contains(".png")))
                continue;

            //if key does not exist within the bitmap, put a new one in
            if(!recordImageTable.containsKey(fileName)) {
                try {
                    Bitmap bm = BitmapFactory.decodeFile(image.getAbsolutePath());
                    if (bm!=null) {
                        bm = Bitmap.createScaledBitmap(bm, Util.DEVICE_WIDTH / 2,
                                (int) (Util.getDeviceHeight() / RECORD_VIEW_SCALE), true);
                        recordImageTable.put(fileName, bm);
                    }
                    else    //in this case, bitmap is null and the file is bad... delete it.
                        image.delete();
                } catch (Exception e) {
                    Log.e(TAG, "Error Updating Record Table",e);
                }


            }
        }
    }

    public static Runnable runnableUpdateTable = new Runnable() {
        @Override
        public void run() {
            Util.updateRecordTable();
        }
    };

    public static void replaceInTable(String old, String newer) {
        Bitmap bm = recordImageTable.get(old);
        if(bm!=null) {
            recordImageTable.remove(old);
            recordImageTable.put(newer, bm);
        }
    }


}
