package edu.tamu.ecen.capstone.patientmd.input;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;

/**
 * Created by reesul on 2/27/2018.
 */

public class RecordPhoto  {

    protected static CameraManager mCameraManager = null;
    protected static CameraDevice mCameraDevice;
    protected static String mCameraId;

    private static Handler mBackgroundHandler;
    private static HandlerThread mBackgroundThread;
    private static CameraCaptureSession mCameraCaptureSession;

    private static Size previewsize;
    private static Size jpegSizes[]=null;
    private TextureView textureView;
    private CaptureRequest.Builder previewBuilder;
    private CameraCaptureSession previewSession;
    private static final SparseIntArray ORIENTATIONS=new SparseIntArray();
    static
    {
        ORIENTATIONS.append(Surface.ROTATION_0,90);
        ORIENTATIONS.append(Surface.ROTATION_90,0);
        ORIENTATIONS.append(Surface.ROTATION_180,270);
        ORIENTATIONS.append(Surface.ROTATION_270,180);
    }

    public static boolean isRunning = false;

    private static String TAG  = "RecordPhoto: ";

    protected void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }
    protected void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public RecordPhoto() {
    }

    /*
        get a camera manager, and setup so a photo can be taken
        returns some camera characteristics
     */
    private static String getManager(Context context) {
        mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        String camInfo=null;

        try {
            mCameraId = mCameraManager.getCameraIdList()[0];
            CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(mCameraId);
            camInfo = "Has Flash: " + characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) + " \n";
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

        if(mCameraDevice == null) {
            Log.d(TAG, "Camera device does not exist");
            return;
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            try {
                mCameraManager.openCamera(mCameraId, mStateCallback, mBackgroundHandler);


            }
            catch (CameraAccessException e) {
                Log.w(TAG, "Error accessing camera: ", e);
            }
        }
        else {
            Log.d(TAG, "takePhoto: permission to camera not granted");
            return;
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
    private static CameraManager.AvailabilityCallback availabilityCallback = new CameraManager.AvailabilityCallback() {
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


    private static final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            //This is called when the camera is open
            Log.e(TAG, "onOpened");
            mCameraDevice = camera;
            //createCameraPreview();
        }
        @Override
        public void onDisconnected(CameraDevice camera) {
            mCameraDevice.close();
        }
        @Override
        public void onError(CameraDevice camera, int error) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    };

}
