package com.aviary.android.feather;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import com.aviary.android.feather.library.log.LoggerFactory;
import com.aviary.android.feather.library.utils.SystemUtils;

// TODO: Auto-generated Javadoc
/**
 * The Class Constants.
 */
public class Constants {

	private static Boolean mEnableFastPreview;

	/** The original bundle. */
	private static Bundle mOriginalBundle = new Bundle();

	/** Arbitrary number indicating a fast cpu. */
	public static final int BOGO_CPU_FAST = 1400;

	public static final int MHZ_CPU_FAST = 1000;

	/** Arbitrary number indicating a medium fast cpu. */
	public static final int BOGO_CPU_MEDIUM = 950;

	public static final int ANDROID_SDK = android.os.Build.VERSION.SDK_INT;
	
	private static final String LOG_TAG = "constants";

	/** The original Intent */
	private static Intent mOriginalIntent = new Intent();

	/**
	 * Initialize the constant fields used in feather like the screen resolution, memory available, etc and copy all the extras field
	 * from the original activity's intent.
	 * 
	 * @param activity
	 *           the activity
	 */
	public static void init( Activity activity ) {
		LoggerFactory.log( "ANDROID_SDK: " + ANDROID_SDK );
		initContext( activity );
		initIntent( activity.getIntent() );
	}

	/**
	 * Get if the fast preview mode is enabled. If the 'effect-enable-fast-preview' intent-extra has been passed within the original
	 * intent the intent value will be used, otherwise the device cpu speed will be used to determine the return value
	 * 
	 * @return
	 */
	public static boolean getFastPreviewEnabled() {
		if ( mEnableFastPreview == null ) {
			boolean value = false;
			if ( containsValue( EXTRA_EFFECTS_ENABLE_FAST_PREVIEW ) ) {
				value = getValueFromIntent( EXTRA_EFFECTS_ENABLE_FAST_PREVIEW, false );
			} else {

				int mhz = SystemUtils.getCpuMhz();
				LoggerFactory.log( "CPU MHZ: " + mhz );

				if ( mhz > 0 ) {
					value = mhz >= MHZ_CPU_FAST;
				} else {
					float speed = SystemUtils.getCpuSpeed();
					value = speed >= BOGO_CPU_FAST;
				}
			}
			mEnableFastPreview = value;
		}
		return mEnableFastPreview.booleanValue();
	}

	/**
	 * Return is external packs are enabled
	 * 
	 * @return
	 */
	public static boolean getExternalPacksEnabled() {
		return getValueFromIntent( EXTRA_EFFECTS_ENABLE_EXTERNAL_PACKS, true );
	}

	/**
	 * Inits the context.
	 * 
	 * @param context
	 *           the context
	 */
	private static void initContext( Context context ) {
		final DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		SCREEN_WIDTH = metrics.widthPixels;
		SCREEN_HEIGHT = metrics.heightPixels;

		double[] mem = new double[3];
		getMemoryInfo( mem );
		MAX_MEMORY = mem[2];
	}
	
	/**
	 * Get information about device memory
	 * @param outValues
	 */
	public static void getMemoryInfo( double[] outValues ) {
		double used = Double.valueOf( Runtime.getRuntime().totalMemory() ) / 1048576.0;
		double total = Double.valueOf( Runtime.getRuntime().maxMemory() ) / 1048576.0;
		double free = total - used;
		
		Log.d( LOG_TAG, "memory: " + free + " of " + total );
		
		outValues[0] = free;
		outValues[1] = used;
		outValues[2] = total;
	}	

	public static void update( Context context ) {
		final DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		SCREEN_WIDTH = metrics.widthPixels;
		SCREEN_HEIGHT = metrics.heightPixels;
	}

	/**
	 * Register the original host {@link #android.content.Intent}.
	 * 
	 * @param intent
	 *           the intent
	 */
	private static void initIntent( Intent intent ) {
		if ( intent != null ) {
			Bundle extras = intent.getExtras();
			if ( extras != null ) {
				mOriginalBundle = (Bundle) extras.clone();
			}
			mOriginalIntent = new Intent( intent );
		}
	}

	public static Intent getOriginalIntent() {
		return mOriginalIntent;
	}

	public static Bundle getOriginalBundle() {
		return mOriginalBundle;
	}

	/**
	 * Gets a value from the original intent.
	 * 
	 * @param <T>
	 *           the generic type
	 * @param key
	 *           the key
	 * @param defaultValue
	 *           the default value
	 * @return the value from intent
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getValueFromIntent( String key, T defaultValue ) {
		if ( mOriginalBundle != null ) {
			if ( mOriginalBundle.containsKey( key ) ) {
				T value;
				try {
					value = (T) mOriginalBundle.get( key );
				} catch ( ClassCastException e ) {
					return defaultValue;
				}

				if ( value != null ) return value;
				return defaultValue;
			}
		}
		return defaultValue;
	}

	/**
	 * Check if the key string exists in the original bundle.
	 * 
	 * @param key
	 *           the key
	 * @return true, if successful
	 */
	public static boolean containsValue( String key ) {
		if ( mOriginalBundle != null ) {
			return mOriginalBundle.containsKey( key );
		}
		return false;
	}

	/**
	 * Return the maximum image size allowed for this device. Be careful if you want to modify the return value because it's easy to
	 * throw an {@link OutOfMemoryError} in android expecially when dealing with {@link Bitmap}.<br />
	 * Part of the application available memory has been already taken by the host application.
	 * 
	 * @return the managed max image size
	 */
	public static final int getManagedMaxImageSize() {

		if ( containsValue( EXTRA_MAX_IMAGE_SIZE ) ) {
			int size = getValueFromIntent( EXTRA_MAX_IMAGE_SIZE, 0 );
			if ( size > 0 ) {
				return size;
			}
		}
		final int screen_size = Math.max( SCREEN_HEIGHT, SCREEN_WIDTH );

		if ( MAX_MEMORY >= 48 ) {
			return Math.min( screen_size, 1280 );
		} else if ( MAX_MEMORY >= 32 ) {
			return Math.min( screen_size, 900 );
		} else {
			return Math.min( screen_size, 700 );
		}
	}

	/**
	 * Return the max allowed heap size for application.
	 * 
	 * @return the application max memory
	 */
	public static double getApplicationMaxMemory() {
		return MAX_MEMORY;
	}

	/** The MAX image size */
	static int MAX_IMAGE_SIZE_LOCAL = -1;

	/** The max memory. */
	static double MAX_MEMORY = -1;

	/** The SCREEN width. */
	public static int SCREEN_WIDTH = -1;

	/** The SCREEN height. */
	public static int SCREEN_HEIGHT = -1;

	/** The Constant API_KEY. */
	public static final String API_KEY = "API_KEY";

	/** Result bitmap will be returned inline within the result Intent. */
	public static final String EXTRA_RETURN_DATA = "return-data";

	/** Define an output uri used by Feather to save the result bitmap in the specified location. */
	public static final String EXTRA_OUTPUT = "output";

	/**
	 * if an the EXTRA_OUTPUT is passed, this is used to determine the bitmap output format For valid values see
	 * Bitmap.CompressFormat
	 * 
	 * @see Bitmap.CompressFormat
	 */
	public static final String EXTRA_OUTPUT_FORMAT = "output-format";

	/**
	 * if EXTRA_OUTPUT is passed then this is used to determine the output quality ( if compress format is jpeg ) valid value: 0..100
	 */
	public static final String EXTRA_OUTPUT_QUALITY = "output-quality";

	/**
	 * If tools-list is passed among the intent to Feather then only the selected list of tools will be shown Actually the list of
	 * tools: SHARPEN, BRIGHTNESS, CONTRAST, SATURATION, ROTATE, FLIP, BLUR, EFFECTS, COLORS, RED_EYE, CROP, WHITEN, DRAWING,
	 * STICKERS.
	 */
	public static final String EXTRA_TOOLS_LIST = "tools-list";

	/**
	 * When the user click on the back-button and the image contains unsaved data a confirmation dialog appears by default. Setting
	 * this flag to true will hide that confirmation and the application will terminate.
	 */
	public static final String EXTRA_HIDE_EXIT_UNSAVE_CONFIRMATION = "hide-exit-unsave-confirmation";

	/**
	 * Depending on the curremt image size and the current user device, some effects can take longer than expected to render the
	 * image. Passing in the caller intent this flag as boolean "true" will affect the behavior of some of the feather's panels, such
	 * as the effect panel. All the panels will use a small progress loader in the toolbar. Passing this value as "false" a modal
	 * progress loader will be used. If you omit this extra in the calling intent, Feather will determine this value reading the
	 * device cpu speed. Moreover the effect panel, when this value is "true", will create also an intermediate fast preview of the
	 * current selected effect while the full size preview is being loaded.
	 */
	public static final String EXTRA_EFFECTS_ENABLE_FAST_PREVIEW = "effect-enable-fast-preview";

	/**
	 * By default feather offers to the final user the possibility to install external filters from the android market. If you want
	 * to disable this feature you can pass this extra boolean to the launching intent as "false". The default behavior is to enable
	 * the external filters.
	 */
	public static final String EXTRA_EFFECTS_ENABLE_EXTERNAL_PACKS = "effect-enable-external-pack";

	/**
	 * By default feather offers to the final user the possibility to install external stickers from the android market. If you want
	 * to disable this feature you can pass this extra boolean to the launching intent as "false". The default behavior is to enable
	 * the external stickers.
	 */
	public static final String EXTRA_STICKERS_ENABLE_EXTERNAL_PACKS = "stickers-enable-external-pack";

	/**
	 * By default Feather will resize the image loaded using the {@link Constants#getManagedMaxImageSize()} method based on the
	 * device memory. If you want to set at runtime the max image size allowed pass an integer value like this:<br />
	 * 
	 * <pre>
	 * intent.putExtra( &quot;max-image-size&quot;, 1024 );
	 * </pre>
	 * 
	 * Remember that the available application memory is shared between the host application and the Aviary editor, so you should
	 * keep that in mind when setting the max image size.
	 */
	public static final String EXTRA_MAX_IMAGE_SIZE = "max-image-size";

	/**
	 * If you want to enable the hi-res image post processing you need to pass a unique session id to the starting Intent. The
	 * session id string must be unique and must be 64 chars length
	 */
	public static final String EXTRA_OUTPUT_HIRES_SESSION_ID = "output-hires-session-id";

	/**
	 * By default some our effects come with extra borders. If you want to disable those borders pass this extra as a boolan 'false'
	 */
	public static final String EXTRA_EFFECTS_BORDERS_ENABLED = "effect-enable-borders";

	public static final String EXTRA_APP_ID = "app-id";

	/**
	 * Passing this key in the calling intent, with any value, will disable the haptic vibration used in certain tools
	 * 
	 * @since 2.1.5
	 */
	public static final String EXTRA_TOOLS_DISABLE_VIBRATION = "tools-vibration-disabled";

}
