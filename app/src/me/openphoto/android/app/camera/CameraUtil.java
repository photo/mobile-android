/**
 * A collection of utilities to help use the Camera and take a photo
 */
package me.openphoto.android.app.camera;

import java.util.List;

import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.Log;

/**
 * A collection of utilities to help use the Camera and take a photo
 * 
 * @author pas
 */
public class CameraUtil {
	/**
	 * Given a set of camera parameters, this method will choose the best
	 * possible camera preview size to fit inside the box defined by maxWidth
	 * and maxHeight
	 * 
	 * @param parameters Set of camera parameters
	 * @param maxWidth The maximum width possible for a camera preview
	 * @param maxHeight The maximum height possible for a camera preview
	 * @return The best possible camera preview size
	 */
	public static Camera.Size pickBestPreviewSize(Camera camera,
			Camera.Parameters parameters, int maxWidth, int maxHeight) {
		// Our return value
		Camera.Size bestSize = null;

		// First Step: See if we can get the list of available preview sizes for
		// the camera hardware
		List<Size> cameraSizes = parameters.getSupportedPreviewSizes();

		// If sizes are available, we need to pick the best one that fits inside
		// the box of maxWidth & maxHeight
		if (cameraSizes != null && cameraSizes.size() > 0) {
			// Find the best size
			int bestW = 0, bestH = 0;

			for (Size size : cameraSizes) {
				if (size.width <= maxWidth && size.height <= maxHeight) {
					// This size fits in the box
					if (size.width >= bestW && size.height >= bestH) {
						// Yay! This is the best Size so far
						bestW = size.width;
						bestH = size.height;
						bestSize = size;
					}
				}
			}
			// bestSize = camera.new Size(bestW, bestH);

		} else {
			// All sizes are valid, so just return the max sizes
			bestSize = camera.new Size(maxWidth, maxHeight);
		}

		Log.i("PAS",
				"CameraUtil.pickBestPreviewSize(): Best Size is: "
						+ String.valueOf(bestSize.width) + "x"
						+ String.valueOf(bestSize.height));
		return bestSize;
	}
}
