package edu.tamu.ecen.capstone.patientmd.input;


import android.content.Context;
import android.graphics.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.Image;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;

/**
 * Created by reesul on 2/27/2018.
 */

public class RecordPhoto  {

    protected static CameraManager mCameraManager = null;
    public static boolean isRunning = false;

    private static String TAG  = "RecordPhoto: ";

    public RecordPhoto() {
    }

    /*
        get a camera manager, and setup so a photo can be taken
        returns some camera characteristics
     */
    private static String getManager(Context context) {
        mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        String camInfo = new String();

        try {
            String camId = mCameraManager.getCameraIdList()[0];
            CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(camId);
            camInfo += "Has Flash: " + characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) + " \n";
            Integer facingDir = characteristics.get(CameraCharacteristics.LENS_FACING);
            if (facingDir==CameraCharacteristics.LENS_FACING_FRONT)
                camInfo+="Camera facing forward\n";
            else
                camInfo+="Camera facing facing elsewhere\n";


        }
        catch (CameraAccessException e) {
            Log.e(TAG, "Error getting camera information\n", e);
        }

        return camInfo;
    }

    public static void takePhoto(Context context) {

        isRunning = true;

        if(mCameraManager==null) {
            String camInfo = getManager(context);
            Log.d(TAG, camInfo);
        }

        //TODO!!!  make handler (may need to do in the main activity, create additional thread
        //todo      register this availability callback
        //todo      register a stateCallback for a camera device used for mCameraManager.openCamera()
        //https://developer.android.com/samples/Camera2Basic/src/com.example.android.camera2basic/Camera2BasicFragment.html
        //mCameraManager.registerAvailabilityCallback(availabilityCallback, );


        isRunning = false;
    }


    /*
    This will notify us if the camera is currently unavailable
     */
    static CameraManager.AvailabilityCallback availabilityCallback = new CameraManager.AvailabilityCallback() {
        @Override
        public void onCameraAvailable(@NonNull String cameraId) {
            super.onCameraAvailable(cameraId);
            //Todo make this a message the user can see
            Log.d(TAG, "Camera is now available");
        }
/*  probably don't need to override this method
        @Override
        public void onCameraUnavaialble(@NonNull String cameraId) {
            super.onCameraUnavailable(cameraId);
            //todo make this a message the user can see
            Log.d(TAG, "Camera unavailable, sorry champ.");
        }*/
    };

}
