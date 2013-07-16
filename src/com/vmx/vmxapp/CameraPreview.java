package com.vmx.vmxapp;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/** A basic Camera preview class */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
	private String TAG;
    private SurfaceHolder mHolder;
    private SurfaceView mSurfaceView;
    private Camera mCamera;
    private List<Size> mSupportedPreviewSizes;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = this.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        
        
    }

    
    public void setCamera(Camera camera) {
        if (mCamera == camera) { return; }
        
        stopPreviewAndFreeCamera();

        
        mCamera = camera;

        
        if (mCamera != null) {
            List<Size> localSizes = mCamera.getParameters().getSupportedPreviewSizes();
            Log.d("vmxerror",mCamera.getParameters().flatten());
            mSupportedPreviewSizes = localSizes;
            requestLayout();
          
            try {
                mCamera.setPreviewDisplay(mHolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
          
            /*
              Important: Call startPreview() to start updating the preview surface. Preview must 
              be started before you can take a picture.
              */
            mCamera.startPreview();
        }
    }
    
    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview (in surface created): " + e.getMessage());
        }

    }


    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null){
          // preview surface does not exist
          return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
        	
          // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here
        Camera.Parameters camParams = mCamera.getParameters();
        Size pSize = camParams.getPreferredPreviewSizeForVideo();
        camParams.setPreviewSize(pSize.width,pSize.height);
        mCamera.setParameters(camParams);

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e){
        	TAG = "vmxapp";
            Log.d(TAG, "Error starting camera preview (in surface changed): " + e.getMessage());
        }
    }
    
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return, so stop the preview.
        if (mCamera != null) {
            /*
              Call stopPreview() to stop updating the preview surface.
            */
            mCamera.stopPreview();
        }
    }

    /**
      * When this function returns, mCamera will be null.
      */
    private void stopPreviewAndFreeCamera() {

        if (mCamera != null) {
            /*
              Call stopPreview() to stop updating the preview surface.
            */
            mCamera.stopPreview();
        
            /*
              Important: Call release() to release the camera for use by other applications. 
              Applications should release the camera immediately in onPause() (and re-open() it in
              onResume()).
            */
            mCamera.release();
        
            mCamera = null;
        }
    }
}