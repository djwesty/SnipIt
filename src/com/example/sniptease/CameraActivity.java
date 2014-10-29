/**@author David Westgate
 * SnipTease LLC.
 * 17 April 2014
 * */
package com.example.sniptease;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

public class CameraActivity extends Activity {
	Parameters p;
	boolean flashOn = false;
	boolean onFrontCam = false;
	private static int camId = Camera.CameraInfo.CAMERA_FACING_BACK;
	Button buttonClear;
	ProgressBar mProgressbar;
	Button buttonFlash;
	Button buttonFlipCam;
	Button buttonSave;
	Button buttonSendPic;
	Button buttonSendSnip;
	Button buttonTakePic;
	Button buttonTakeVideo;
	Button buttonUpload;
	public SurfaceHolder mHolder;
	Boolean snipIsAPic;

	public class CameraPreview extends SurfaceView implements
			SurfaceHolder.Callback {

		public CameraPreview(Context context, Camera camera) {
			super(context);
			mCamera = camera;

			// Install a SurfaceHolder.Callback so we get notified when the
			// underlying surface is created and destroyed.
			mHolder = getHolder();
			mHolder.addCallback(this);
			// deprecated setting, but required on Android versions prior to 3.0
			mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int w,
				int h) {
			// If your preview can change or rotate, take care of those events
			// here.
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

			} catch (Exception e) {
				finish();
				Log.d("TAG", "Error starting camera preview: " + e.getMessage());
			}
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			// The Surface has been created, now tell the camera where to draw
			// the preview.
			try {
				mCamera.setPreviewDisplay(holder);
				mCamera.startPreview();
			} catch (IOException | NullPointerException e) {
				Log.d("TAG", "Error setting camera preview: " + e.getMessage());
			}
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			// empty. Take care of releasing the Camera preview in your
			// activity.
		}
		
	}

	public static final int MEDIA_TYPE_IMAGE = 1;

	public static final int MEDIA_TYPE_VIDEO = 2;

	/** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open(); // attempt to get a Camera instance
		} catch (Exception e) {
			// Camera is not available (in use or does not exist)
		}
		return c; // returns null if camera is unavailable
	}

	/** Create a File for saving an image or video */
	private static File getOutputMediaFile(int type) {
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.
		// getBaseContext().getExternalFilesDir(Environment.DIRECTORY_DCIM)

		File mediaStorageDir = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
				"SnipTease");

		// This location works best if you want the created images to be shared
		// between applications and persist after your app has been uninstalled.

		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.d("SnipTease", "failed to create directory");
				return null;
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		File mediaFile;
		if (type == MEDIA_TYPE_IMAGE) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "IMG_" + timeStamp + ".jpg");
		} else if (type == MEDIA_TYPE_VIDEO) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "VID_" + timeStamp + ".mp4");

		} else {
			return null;
		}

		return mediaFile;
	}

	/** Create a file Uri for saving an image or video */
	private static Uri getOutputMediaFileUri(int type) {
		return Uri.fromFile(getOutputMediaFile(type));
	}

	private boolean isRecording = false;

	/** A basic Camera preview class */
	private Camera mCamera;

	MediaRecorder mMediaRecorder;

	private PictureCallback mPicture = new PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			File pictureFile = new File(
					Environment.getExternalStorageDirectory()
							+ "/tempSnipPic.jpg");
			snipIsAPic = true;
			if (pictureFile == null) {
				Log.d("Error creating media file, check storage permissions: ",
						"");
				return;
			}
			try {
				FileOutputStream fos = new FileOutputStream(pictureFile);
				fos.write(data);
				fos.close();
			} catch (FileNotFoundException e) {
				Log.d("File not found: " ,e.getMessage());
			} catch (IOException e) {
				Log.d("Error accessing file: " , e.getMessage());
			}
		}

	};

	private CameraPreview mPreview;

	public void buttonClearOnClick(View v) {
		Log.d("clear button", "");

		mCamera.startPreview();

		liveFeedMenu();

	}

	public void buttonSaveOnClick(View v) {
		
		File outFile = new File(Environment.getExternalStorageDirectory()
				+ "/tempSnipVideo.mp4");
		if (outFile.exists()) {
			outFile = getOutputMediaFile(MEDIA_TYPE_VIDEO);
			if (outFile == null) {
				Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(this, "Save Successful", Toast.LENGTH_SHORT)
						.show();
			}
		} else {
			File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
			if (pictureFile == null) {
				Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(this, "Save Successful", Toast.LENGTH_SHORT)
						.show();
			}
		}

	}

	public void buttonFlashOnClick(View v) {
		Log.d("flash button", "");
		if (flashOn == true) {

			p.setFlashMode(Parameters.FLASH_MODE_OFF);
			mCamera.setParameters(p);
			flashOn = false;
		} else if (flashOn == false) {

			p.setFlashMode(Parameters.FLASH_MODE_TORCH);
			mCamera.setParameters(p);
			flashOn = true;
		}

	}

	public void buttonFlipCamOnClick(View v) {
		Log.d("Flip Cam", "");
		if (mPreview != null) {
			mCamera.stopPreview();
		}
		// NB: if you don't release the current camera before switching, you app
		// will crash
		mCamera.release();
		// swap the id of the camera to be used
		if (camId == Camera.CameraInfo.CAMERA_FACING_BACK) {
			camId = Camera.CameraInfo.CAMERA_FACING_FRONT;
		} else {
			camId = Camera.CameraInfo.CAMERA_FACING_BACK;
		}
		mCamera = Camera.open(camId);
		// Code snippet for this method from somewhere on android developers, i
		// forget where
		// setCameraDisplayOrientation(CameraActivity.this, camId, mCamera);Not
		// needed
		mCamera.setDisplayOrientation(90);

		try {
			// this step is critical or preview on new camera will no know where
			// to render to
			mCamera.setPreviewDisplay(mHolder);
		} catch (IOException e) {
			e.printStackTrace();
		}
		mCamera.startPreview();

	}

	public void buttonSendPicOnClick(View v) {
		Intent intent = new Intent(this, SnipRecipientsActivity.class);
		intent.putExtra("snipIsAPic", snipIsAPic);
		intent.putExtra("snipQuestion", "");
		intent.putExtra("correctAnswer", "");
		intent.putExtra("firstFalseAnswer", "");
		intent.putExtra("secondFalseAnswer", "");
		intent.putExtra("thirdFalseAnswer", "");
		intent.putExtra("saveQuestion", "NO");
		startActivity(intent);
	}
	@Override
	public void onDestroy(){
	super.onDestroy();
	
	}

	public void buttonSendSnipOnClick(View v) {
		Log.d("send snip", "");
		Intent intent = new Intent(this, SnipAttachQuestionActivity.class);
		intent.putExtra("snipIsAPic", snipIsAPic);
		startActivity(intent);
	}

	public void buttonTakePicOnClick(View v) {

	
		postMediaMenuChange();
		mCamera.takePicture(null, null, mPicture);

	}

	public void postVideoViewer(Uri uri) {
		Log.d("The URI", uri.toString());
		VideoView videoViewPreview = (VideoView) findViewById(R.id.camera_video_view);
		mPreview.setVisibility(View.INVISIBLE);

		videoViewPreview.setVisibility(View.VISIBLE);
		videoViewPreview.setVideoURI(uri);

		mProgressbar.setVisibility(View.GONE);
		mCamera.stopPreview();

		postMediaMenuChange();
		videoViewPreview.requestFocus();
		videoViewPreview.start();
	}

	public void buttonTakeVideoOnClick(View v) {
		Button thisButton = (Button) findViewById(R.id.camera_snap_video);

		if (isRecording) {

			mProgressbar.setVisibility(View.GONE);
			// stop recording and release camera
			mMediaRecorder.stop(); // stop the recording
			isRecording = false;
			releaseMediaRecorder(); // release the MediaRecorder object
			mCamera.lock();
			// inform the user that recording has stopped
			thisButton.setText("Take Video");

			postVideoViewer(Uri.parse(Environment.getExternalStorageDirectory()
					+ "/tempSnipVideo.mp4"));
			snipIsAPic = false;
		} else {
			// initialize video camera
			if (prepareVideoRecorder()) {
				// Camera is available and unlocked, MediaRecorder is
				// prepared,
				// now you can start recording
				// mCamera.unlock();
				mProgressbar.setVisibility(View.VISIBLE);
				mMediaRecorder.start();
				isRecording = true;
				thisButton.setText("Stop");

				new Thread() {
					public void run() {

						int mProgressStatus = 0;
						while (mProgressStatus < 100 && isRecording) {

							mProgressbar.setProgress(mProgressStatus);
							mProgressStatus++;
							android.os.SystemClock.sleep(150);
						}

					}
				}.start();

				// inform the user that recording has started
			} else {
				// prepare didn't work, release the camera
				releaseMediaRecorder();
				// inform user
			}

		}

	}

	public void buttonUploadOnClick(View v) {
		Log.d("upload button", "");
	}

	public void liveFeedMenu() {
		buttonClear.setVisibility(View.GONE);
		buttonSave.setVisibility(View.GONE);
		buttonSendPic.setVisibility(View.GONE);
		buttonSendSnip.setVisibility(View.GONE);

		buttonFlash.setVisibility(View.VISIBLE);
		buttonFlipCam.setVisibility(View.VISIBLE);

		buttonUpload.setVisibility(View.VISIBLE);
		buttonTakePic.setVisibility(View.VISIBLE);
		buttonTakeVideo.setVisibility(View.VISIBLE);
	}

	@Override
	public void onPause() {
		super.onPause();
		releaseCamera();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		setContentView(R.layout.activity_camera);
		buttonClear = (Button) findViewById(R.id.camera_clear);
		mProgressbar = (ProgressBar) findViewById(R.id.camera_video_progress);
		buttonFlash = (Button) findViewById(R.id.camera_flash_tog);
		buttonFlipCam = (Button) findViewById(R.id.camera_flipcam);
		buttonSave = (Button) findViewById(R.id.camera_save);
		buttonSendPic = (Button) findViewById(R.id.camera_send_pic);
		buttonSendSnip = (Button) findViewById(R.id.camera_send_snip);
		buttonTakePic = (Button) findViewById(R.id.camera_snap_pic);
		buttonTakeVideo = (Button) findViewById(R.id.camera_snap_video);
		buttonUpload = (Button) findViewById(R.id.camera_upload);
		getActionBar().hide();

		// Create an instance of Camera
		mCamera = getCameraInstance();
		mCamera.setDisplayOrientation(90);
		p = mCamera.getParameters();
		// Create our Preview view and set it as the content of our activity.
		mPreview = new CameraPreview(this, mCamera);

		FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
		preview.addView(mPreview);
		GridLayout gridButtonHolder = (GridLayout) findViewById(R.id.camera_grid);
		gridButtonHolder.bringToFront();

		liveFeedMenu();
	}

	public void postMediaMenuChange() {
		buttonFlash.setVisibility(View.GONE);
		buttonFlipCam.setVisibility(View.GONE);

		buttonUpload.setVisibility(View.GONE);
		buttonTakePic.setVisibility(View.GONE);
		buttonTakeVideo.setVisibility(View.GONE);

		buttonClear.setVisibility(View.VISIBLE);
		buttonSave.setVisibility(View.VISIBLE);
		buttonSendPic.setVisibility(View.VISIBLE);
		buttonSendSnip.setVisibility(View.VISIBLE);

	}

	private boolean prepareVideoRecorder() {

		// mCamera = getCameraInstance();
		String mOutputFileName = Environment.getExternalStorageDirectory()
				+ "/tempSnipVideo.mp4";
		File outFile = new File(mOutputFileName);
		if (outFile.exists()) {
			outFile.delete();
		}
		try {
			mCamera.stopPreview();
			mCamera.unlock();
			mMediaRecorder = new MediaRecorder();
			mMediaRecorder.setCamera(mCamera);

			mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
			mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
			mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
			mMediaRecorder.setVideoSize(176, 144);
			mMediaRecorder.setVideoFrameRate(15);
			mMediaRecorder
					.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
			mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			mMediaRecorder.setMaxDuration(7000); // limit to 7 seconds
			mMediaRecorder.setPreviewDisplay(mHolder.getSurface());
			mMediaRecorder.setOutputFile(mOutputFileName);

			mMediaRecorder.prepare();
			// Log.v(TAG, "MediaRecorder initialized");
			// mInitBtn.setEnabled(false);
			// mStartBtn.setEnabled(true);
		} catch (Exception e) {
			// Log.v(TAG, "MediaRecorder failed to initialize");
			e.printStackTrace();

		}
		Log.d("prepare video", "return true");
		return true;
	}

	private void releaseCamera() {
		if (mCamera != null) {
			mCamera.release(); // release the camera for other applications
			mCamera = null;
		}
	}

	private void releaseMediaRecorder() {
		if (mMediaRecorder != null) {
			mMediaRecorder.reset(); // clear recorder configuration
			mMediaRecorder.release(); // release the recorder object
			mMediaRecorder = null;
			mCamera.lock(); // lock camera for later use
		}
	}
}