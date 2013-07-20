package com.vmx.vmxapp;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;

public class DisplayCameraActivity extends Activity {
	CameraPreview mPreview;
	Camera mCamera;
    SurfaceView mSurfaceView;
	SurfaceHolder mHolder;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display_camera);
		
	    Log.d("vmxdebug","inside oncreate of display camera activity");
		if (mCamera == null){
			Log.d("vmxerror","camera is null in oncreate of displaycam activity - TOP");
		}
	    // Create an instance of Camera
		
		mCamera = getCameraInstance();

		if (mCamera == null){
			Log.d("vmxerror","camera is null in oncreate of displaycam activity");
		}
		else{
			if(mCamera != null){
				Log.d("vmxdebug",mCamera.getParameters().flatten());
			}	
		}
	
	    // Create our Preview view and set it as the content of our activity.
	    mPreview = new CameraPreview(this, mCamera);
	    FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
	    preview.addView(mPreview);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.display_camera, menu);
		return true;
	}

	public static Camera getCameraInstance(){
		   Camera c = null;
		   try {
			   c = Camera.open(0); // attempt to get a Camera instance
		   }
		   catch (Exception e){
			   Log.d("vmxerror", "camera didn't open:" + e.getMessage());
		       // Camera is not available (in use or does not exist)
		   }
		   if(c==null){
			   Log.d("vmxerror", "we have a null camera in getcamerainstance");
		   }
		   return c; // returns null if camera is unavailable
	}

}
