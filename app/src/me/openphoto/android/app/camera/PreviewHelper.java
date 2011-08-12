/**
 * Making a camera preview and taking a photo is complex - this class is here
 * to help
 */
package me.openphoto.android.app.camera;

import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.AsyncTask;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup.LayoutParams;

/**
 * Making a camera preview and taking a photo is complex - this class is here
 * to help
 * 
 * @author pas
 */
public class PreviewHelper implements SurfaceHolder.Callback {
	private SurfaceView preview = null;
	private SurfaceHolder previewHolder = null;
	private Camera camera = null;
	private AsyncTask<byte[], ?, ?> storePictureTask;

	/**
	 * Create an instance of camera preview screen, and assign the surface to
	 * use for the display
	 * <p>
	 * This constructor also adds itself as the SurfaceHolder callback
	 * 
	 * @param previewView The SurfaceView to contain the preview box
	 */
	public PreviewHelper(SurfaceView previewView) {
		this.preview = previewView;

		previewHolder = preview.getHolder();
		previewHolder.addCallback(this);
		previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	/**
	 * Enable camera and set the surface holder to 'previewHolder'
	 */
	public void surfaceCreated(SurfaceHolder holder) {
		camera = Camera.open();
		try {
			camera.setPreviewDisplay(holder);
		} catch (Throwable t) {
			Log.e("PreviewHelper",
					"Exception in surfaceCreated(): " + t.getMessage(), t);
		}
	}

	/**
	 * Called after surface has been created
	 * <p>
	 * This method will figure out how big the view is, and create a camera
	 * preview box that is a large as possible while maintaining the aspect
	 * ratio.
	 */
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Log.i("PAS", "PreviewHelper.surfaceChanged(): Size of surface: "
				+ String.valueOf(width) + "x" + String.valueOf(height));

		// Find best possible camera preview size
		Camera.Parameters parameters = camera.getParameters();
		Size s = CameraUtil.pickBestPreviewSize(camera, parameters, width,
				height);

		Log.i("PAS", "PreviewHelper.surfaceChanged(): Size of camera preview: "
				+ String.valueOf(s.width) + "x" + String.valueOf(s.height));

		// Resize surface to the size
		LayoutParams params = preview.getLayoutParams();
		params.width = s.width;
		params.height = s.height;
		preview.setLayoutParams(params);

		// Set parameters
		parameters.setPreviewSize(s.width, s.height);
		parameters.setPictureFormat(PixelFormat.JPEG);
		// TODO Android 2.1+: Flash to auto
		// parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
		camera.setParameters(parameters);

		// Start camera displaying the preview
		camera.startPreview();
	}

	/**
	 * Disable camera
	 */
	public void surfaceDestroyed(SurfaceHolder holder) {
		camera.stopPreview();
		camera.release();
		camera = null;
	}

	/**
	 * Takes a picture using the camera by first focusing the camera, then
	 * taking the picture, starting the picture storage task, and finally
	 * restarting the preview
	 * 
	 * @param storePictureTask The task to invoke once the picture data is
	 *            available
	 */
	public void takePicture(AsyncTask<byte[], ?, ?> storePictureTask) {
		this.storePictureTask = storePictureTask;

		// Step 1) Ask for focus
		camera.autoFocus(autoFocusCallback);
	}

	// Step 2) Ask to capture picture data
	Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
		public void onAutoFocus(boolean success, Camera camera) {
			camera.takePicture(null, null, photoCallback);
		}
	};

	// Step 3) Save picture data, and restart camera preview
	final Camera.PictureCallback photoCallback = new Camera.PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			Log.i("PAS", "Photo taken - data size: " + data.length);
			storePictureTask.execute(data);
			camera.startPreview();
		}
	};
}
