/**
 * The embedded camera screen
 */
package me.openphoto.android.app;

import java.io.ByteArrayOutputStream;

import me.openphoto.android.app.camera.PreviewHelper;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.Toast;

/**
 * The embedded camera screen
 * 
 * @author pas
 */
public class CameraActivity extends Activity implements OnClickListener,
		OnTouchListener {

	private SurfaceView preview = null;
	private Button btnTakePicture;
	private PreviewHelper ph;

	/**
	 * Creates the Activity's content
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set content
		setContentView(R.layout.camera);

		// Set camera preview stuff
		preview = (SurfaceView) findViewById(R.id.SurfaceView01);
		preview.setOnTouchListener(this);

		ph = new PreviewHelper(preview);

		// Set listeners
		btnTakePicture = (Button) findViewById(R.id.l_camera_btn_take_photo);
		btnTakePicture.setOnClickListener(this);
	}

	/**
	 * Handle the camera/search button being pressed
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_CAMERA
				|| keyCode == KeyEvent.KEYCODE_SEARCH) {
			takePicture();

			return (true);
		}

		return (super.onKeyDown(keyCode, event));
	}

	/**
	 * Button clicked
	 */
	public void onClick(View v) {
		if (v.equals(btnTakePicture)) {
			Log.i("PAS", "Button pressed to take a picture");
			takePicture();
		}
	}

	/**
	 * Camera surface touch event
	 * <p>
	 * TODO Use this for zooming or other cool stuff
	 */
	public boolean onTouch(View v, MotionEvent event) {
		float x = event.getX();
		float y = event.getY();
		Log.i("PAS",
				"onTouch(): Someone is touching me here: " + String.valueOf(x)
						+ ", " + String.valueOf(y));
		return false;
	}

	/**
	 * Take a picture with the camera
	 */
	private void takePicture() {
		Log.i("PAS takePicture()", "Take a picture");
		ph.takePicture(new SavePhotoTask());
	}

	class SavePhotoTask extends AsyncTask<byte[], Void, String> {
		/**
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {
			Toast message = Toast.makeText(CameraActivity.this,
					R.string.uploading, Toast.LENGTH_SHORT);
			message.show();
		}

		@Override
		protected String doInBackground(byte[]... jpeg) {
			byte[] image = jpeg[0];
			Log.i("doInBackground()", "image: " + image.length);

			int origBytes = image.length;
			Log.i("PAS", "Orig size: " + String.valueOf(origBytes));

			// Create Bitmap from image
			Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, origBytes);

			// Resize image
			int width = bitmap.getWidth();
			int height = bitmap.getHeight();
			Log.i("PAS",
					"Image size: " + String.valueOf(width) + "x"
							+ String.valueOf(height));
			float scale = getSmallScales(width, height);
			if (scale != 1) {
				Matrix matrix = new Matrix();
				matrix.setScale(scale, scale);
				Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width,
						height, matrix, true);

				// Convert back to byte array
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
				image = baos.toByteArray();
			}

			int resizeBytes = image.length;
			Log.i("PAS", "Resize size: " + String.valueOf(resizeBytes));

			// Send image to server
			String fullUploadUrl = "http://something";
			Log.i("PAS", "fullUploadUrl: " + fullUploadUrl);

			// TODO The API call to upload a photo
			String jsonString = "Response";

			return jsonString;
		}

		/**
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(String result) {
			// Something here
		}

	}

	/**
	 * Figures out what scale/ratio to use to get a smaller image
	 * 
	 * @param width The original width of the image
	 * @param height The original height of the image
	 * @return The "scale" value to use matrix.postScale()
	 */
	private float getSmallScales(int width, int height) {
		int maxWidth = 1024;
		int maxHeight = 1024;
		float scale = 1; // No scaling

		if (width <= maxWidth && height < maxHeight) {
			Log.i("PAS", "getSmallScales(): No scaling - 1");
			return scale;
		}

		if (width > height) {
			scale = (float) maxWidth / width;
		} else {
			scale = (float) maxHeight / height;
		}

		Log.i("PAS", "getSmallScales(): Scale is: " + String.valueOf(scale));
		return scale;
	}
}
