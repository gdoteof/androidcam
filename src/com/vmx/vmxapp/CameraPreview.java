package com.vmx.vmxapp;

import java.util.List;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

/** A basic Camera preview class */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
	private String TAG;
    private SurfaceHolder mHolder;
    private SurfaceView mSurfaceView;
    public Camera mCamera;
    private List<Size> mSupportedPreviewSizes;
    
    private Context mContext;
    private Camera.CameraInfo mCameraInfo;
    private Size mOptimalSizeLandscape = null;
    private Size mOptimalSizePortrait = null;
    private int mCameraOpenedOrientation;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        mContext = context;
        mCamera = camera;
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = this.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        
        
    }

        
    public void surfaceCreated(SurfaceHolder holder) {
    	Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        int numberOfCameras = Camera.getNumberOfCameras();

        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                if (mCamera != null) {
                    mCamera.stopPreview();
                    mCamera.release();
                    mCamera = null;
                }
                mCamera = Camera.open(i);
                mCameraInfo = cameraInfo;
            }
        }
    }


    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null){
          // preview surface does not exist
          return;
        }

        // start preview with new settings
        
        if(mCamera != null){
	        mCamera.stopPreview();
        }
        Display display = ((WindowManager) mContext.getSystemService(mContext.WINDOW_SERVICE)).getDefaultDisplay();

        int rotation = display.getRotation();
        int degrees = 0;
        int result;

        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        if (mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (mCameraInfo.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (mCameraInfo.orientation - degrees + 360) % 360;
        }

        mCamera.setDisplayOrientation(result);
        if (mCameraOpenedOrientation == Configuration.ORIENTATION_PORTRAIT) {
            setCameraOrientationOnOpen(width, height); //sets correct ratio
        }
        new Thread(new Runnable() {
        	public void run() {
		        try{
		        	mCamera.setPreviewDisplay(mHolder);
					mCamera.startPreview();
		        }	
		        catch(Exception e){
		        	Log.d("vmxerror", e.getMessage());
		        }
        	}
        }).start();

    }
    
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return, so stop the preview.
        if (mCamera != null) {
            /*
              Call stopPreview() to stop updating the preview surface.
            */
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    public void setCameraOrientationOnOpen(int w, int h) {
        Size cameraSize;
        mCamera.stopPreview();
        final float rotation = getRotation();
        Camera.Parameters currentCameraParameters = mCamera.getParameters();
        List<Camera.Size> previewSizes = currentCameraParameters.getSupportedPreviewSizes();
        cameraSize = getOptimalPreviewSize(previewSizes, h, w);

        currentCameraParameters.setPreviewSize(cameraSize.width, cameraSize.height);
        mCamera.setParameters(currentCameraParameters);
        if (rotation == 90 || rotation == 270) {
            float ratio = 100;
            if (cameraSize.width < mSurfaceView.getLayoutParams().height) {
                ratio = (mSurfaceView.getLayoutParams().height * 100) / cameraSize.width;
            }
            if (cameraSize.height < mSurfaceView.getLayoutParams().width) {
                ratio = (mSurfaceView.getLayoutParams().width * 100) / cameraSize.height;
            }

            mSurfaceView.getLayoutParams().height = (int) ((cameraSize.width * ratio) / 100);
            mSurfaceView.getLayoutParams().width = (int) ((cameraSize.height * ratio) / 100);
        }
        new Thread(new Runnable() {
        	public void run() {
		        try{
			        mCamera.setDisplayOrientation((int)rotation);
			        mCamera.startPreview();
		        }	
		        catch(Exception e){
		        	Log.d("vmxerror", e.getMessage());
		        }
        	}
        }).start();
    }
    
    public float getRotation() {
        Display display = ((WindowManager) mContext.getSystemService(mContext.WINDOW_SERVICE)).getDefaultDisplay();

        int rotation = display.getRotation();
        int degrees = 0;
        int result;

        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        result = (mCameraInfo.orientation + degrees) % 360;
        result = (360 - result) % 360;  // compensate the mirror
        return result;
    }
    
    
    public Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
        boolean isLanscape = false;
        if (getRotation() == 0 || getRotation() == 180)
            isLanscape = true;

        if (isLanscape && mOptimalSizeLandscape != null) //landscape
        {
            return mOptimalSizeLandscape;
        } else if (!isLanscape && mOptimalSizePortrait != null) //portrait
        {
            return mOptimalSizePortrait;
        }

        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h + 500;

        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        if (isLanscape) {
            mOptimalSizeLandscape = optimalSize;
        } else if (!isLanscape) {
            mOptimalSizePortrait = optimalSize;
        }
        return optimalSize;
    }
}