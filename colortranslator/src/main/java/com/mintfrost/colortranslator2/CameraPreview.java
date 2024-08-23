package com.mintfrost.colortranslator2;

import android.content.Context;
import android.content.res.ColorStateList;
import android.hardware.Camera;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static android.content.ContentValues.TAG;
import static com.mintfrost.colortranslator2.GraphicalTools.decodeYUV420SP;
import static com.mintfrost.colortranslator2.GraphicalTools.getHexText;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {
    private static final String LOG_TAG = CameraPreview.class.getSimpleName();
    private final FloatingActionButton fab;
    private final ImageView leftColor;
    private final ImageView rightColor;
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private Camera.Size previewSize;
    private int[] pixels;
    private int capturedColor = -1;

    public CameraPreview(Context context, FloatingActionButton fab, ImageView leftColor, ImageView rightColor) {
        super(context);

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        this.fab = fab;
        this.leftColor = leftColor;
        this.rightColor = rightColor;
    }

    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    public int captureColor() {
        capturedColor = pixels[0];
        Log.d(LOG_TAG, "Captured color: " + getHexText(capturedColor));
        return capturedColor;
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        initCamera();
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();

            Camera.Parameters parameters = mCamera.getParameters();

            previewSize = parameters.getPreviewSize();
            pixels = new int[previewSize.width * previewSize.height];
            Log.d(TAG, "Surface created " + previewSize.width + "x" + previewSize.height);

        } catch (IOException e) {
            Log.e(TAG, "Error setting camera preview", e);
        }
    }

    public void setFlashlightMode(boolean enabled) {
        Camera.Parameters parameters = mCamera.getParameters();
        boolean hasTorchMode = Optional.ofNullable(parameters.getSupportedFlashModes())
                .map(x -> x.contains(Camera.Parameters.FLASH_MODE_TORCH))
                .isPresent();
        if (!hasTorchMode) {
            Log.e(TAG, "Flash torch mode not supported");
            return;
        }
        if (enabled) {
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        } else {
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        }
        mCamera.setParameters(parameters);
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
        releaseCamera();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
            mCamera.setPreviewCallback(this);

        } catch (Exception e) {
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    public void onPreviewFrame(byte[] data, Camera camera) {
        decodeYUV420SP(pixels, data, previewSize.width, previewSize.height);
//        Log.i("Pixels", "The top right pixel has the following RGB (hexadecimal) values:" + Integer.toHexString(pixels[0]));
//        fab.setBackgroundTintList(ColorStateList.valueOf((pixels[(previewSize.width * previewSize.height / 2) + (previewSize.width / 2)])));
        if (capturedColor == -1) {
            fab.setBackgroundTintList(ColorStateList.valueOf((pixels[0])));
        }
        leftColor.setBackgroundColor(pixels[0]);
        rightColor.setBackgroundColor(pixels[0]);
    }

    private void initCamera() {
        mCamera = getCameraInstance();
        if (mCamera != null) {
            mCamera.setDisplayOrientation(90);
        }
    }

    /**
     * Check if this device has a camera
     */


    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }
}
