package com.aviary.android.feather.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.aviary.android.feather.R;
import com.aviary.android.feather.graphics.AnimatedRotateDrawable;
import com.aviary.android.feather.library.graphics.drawable.FastBitmapDrawable;
import com.aviary.android.feather.widget.IToast;
import com.aviary.android.feather.widget.wp.CellLayout;

/**
 * Variuos UI utilities.
 * 
 * @author alessandro
 */
public class UIUtils {

	private static Context mContext;
	private static LayoutInflater mLayoutInflater;

	/**
	 * Inits the.
	 * 
	 * @param context
	 *           the context
	 */
	public static void init( Context context ) {
		mContext = context;
	}

	public static LayoutInflater getLayoutInflater() {
		if ( mLayoutInflater == null ) {
			mLayoutInflater = (LayoutInflater) mContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
		}
		return mLayoutInflater;
	}

	/**
	 * Show custom toast.
	 * 
	 * @param viewResId
	 *           the view res id
	 */
	public static void showCustomToast( int viewResId ) {
		showCustomToast( viewResId, Toast.LENGTH_SHORT );
	}

	/**
	 * Show custom toast.
	 * 
	 * @param viewResId
	 *           the view res id
	 * @param duration
	 *           the duration
	 */
	public static void showCustomToast( int viewResId, int duration ) {
		showCustomToast( viewResId, duration, Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM );
	}

	public static IToast createModalLoaderToast() {
		IToast mToastLoader = IToast.make( mContext, -1 );
		View view = getLayoutInflater().inflate( R.layout.feather_progress_view, null );
		AnimatedRotateDrawable d = new AnimatedRotateDrawable( mContext.getResources(), R.drawable.feather_spinner_white_76, 12, 100 );
		ProgressBar progress = (ProgressBar) view.findViewById( R.id.progress );
		progress.setIndeterminateDrawable( d );
		mToastLoader.setView( view );
		return mToastLoader;
	}

	/**
	 * Display a system Toast using a custom ui view.
	 * 
	 * @param viewResId
	 *           the view res id
	 * @param duration
	 *           the duration
	 * @param gravity
	 *           the gravity
	 */
	public static void showCustomToast( int viewResId, int duration, int gravity ) {
		View layout = getLayoutInflater().inflate( viewResId, null );

		Toast toast = new Toast( mContext.getApplicationContext() );

		toast.setGravity( gravity, 0, 0 );
		toast.setDuration( duration );
		toast.setView( layout );
		toast.show();
	}

	/**
	 * Draw folder icon.
	 * 
	 * @param folder
	 *           the folder
	 * @param icon
	 *           the icon
	 * @param icon_new
	 *           the icon_new
	 * @return the drawable
	 */
	public static Drawable drawFolderIcon( Drawable folder, Drawable icon, Drawable icon_new ) {

		final int w = folder.getIntrinsicWidth();
		final int h = folder.getIntrinsicHeight();
		folder.setBounds( 0, 0, w, h );

		Bitmap bitmap = Bitmap.createBitmap( w, h, Config.ARGB_8888 );
		Canvas canvas = new Canvas( bitmap );
		folder.draw( canvas );

		float icon_w = (float) w / 1.5f;
		float icon_h = (float) h / 1.5f;
		float icon_left = ( w - icon_w ) / 2;
		float icon_top = ( h - icon_h ) / 2;

		icon.setBounds( (int) icon_left, (int) icon_top, (int) ( icon_left + icon_w ), (int) ( icon_top + icon_h ) );
		icon.setColorFilter( new PorterDuffColorFilter( 0xFFFFFFFF, Mode.MULTIPLY ) );
		icon.setFilterBitmap( true );
		icon.draw( canvas );

		if ( icon_new != null ) {
			icon_new.setBounds( 0, 0, (int) ( w / 2.5 ), (int) ( h / 2.5 ) );
			icon_new.draw( canvas );
		}

		return new FastBitmapDrawable( bitmap );
	}

	/**
	 * Try to calculate the optimal number of columns for the current screen.
	 * 
	 * @return the screen optimal columns
	 * @see CellLayout#setNumCols(int)
	 */
	public static int getScreenOptimalColumns() {
		return getScreenOptimalColumns( 0 );
	}

	/**
	 * Gets the screen optimal columns.
	 * 
	 * @param drawable_width
	 *           the drawable_width
	 * @return the screen optimal columns
	 */
	public static int getScreenOptimalColumns( int drawable_width ) {
		DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
		double a = (double) metrics.widthPixels / (double) metrics.densityDpi; // 2.25
		int b = (int) Math.ceil( a * 2.0 ); // 5

		if ( ( b * drawable_width ) > metrics.widthPixels ) {
			return metrics.widthPixels / drawable_width;
		}

		return Math.min( Math.max( b, 3 ), 10 );
	}

	public static int getScreenOptimalColumnsPixels( int cell_pixels ) {
		DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
		double a = (double) metrics.widthPixels;
		int columns = (int) ( a / cell_pixels );
		return columns;
	}

}
