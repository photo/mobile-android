package com.aviary.android.feather.receivers;

import java.util.Iterator;
import java.util.Set;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import com.aviary.android.feather.library.content.FeatherIntent;
import com.aviary.android.feather.library.log.LoggerFactory;
import com.aviary.android.feather.library.log.LoggerFactory.Logger;
import com.aviary.android.feather.library.log.LoggerFactory.LoggerType;

/**
 * Main receiver used to listen for new packages installations. When a package, which is recognized as a feather plugin, has been
 * installed/removed or updated this receiver will broadcast a notification.
 * 
 * @author alessandro
 * 
 */
public class FeatherSystemReceiver extends BroadcastReceiver {

	/** The logger. */
	static Logger logger = LoggerFactory.getLogger( "FeatherSystemReceiver", LoggerType.ConsoleLoggerType );

	/**
	 * A package in the system has been installed/replaced or removed. Check if the package is a valid feather plugin and send a
	 * broadcast notification of the newly package. A valid feather plugin must have a resource with the follwing integer
	 * identifiers: is_sticker indicates it's a sticker pack plugin is_filter indicates it's a filter pack plugin is_tool indicates
	 * it's a new tool plugin ( not used )
	 * 
	 * @param context
	 *           the context
	 * @param intent
	 *           the intent
	 */
	@Override
	public void onReceive( Context context, Intent intent ) {

		final String action = intent.getAction();

		if ( null != action ) {
			logger.info( "onReceive", action );

			if ( Intent.ACTION_PACKAGE_ADDED.equals( action ) ) {
				handlePackageAdded( context, intent );
			} else if ( Intent.ACTION_PACKAGE_REMOVED.equals( action ) ) {
				handlePackageRemoved( context, intent );
			} else if ( Intent.ACTION_PACKAGE_REPLACED.equals( action ) ) {
				handlePackageReplaced( context, intent );
			}
		}
	}

	/**
	 * Handle package.
	 * 
	 * @param context
	 *           the context
	 * @param packageName
	 *           the package name
	 * @param intent
	 *           the intent
	 */
	private void handlePackage( Context context, String packageName, Intent intent ) {
		Resources res = null;
		int is_sticker = 0;
		int is_filter = 0;
		int is_tool = 0;
		int is_border = 0;

		try {
			res = context.getPackageManager().getResourcesForApplication( packageName );
		} catch ( NameNotFoundException e ) {
			e.printStackTrace();
		}

		if ( null != res ) {

			int resid = 0;
			resid = res.getIdentifier( "is_sticker", "integer", packageName );
			if ( resid != 0 ) is_sticker = res.getInteger( resid );

			resid = res.getIdentifier( "is_filter", "integer", packageName );
			if ( resid != 0 ) is_filter = res.getInteger( resid );

			resid = res.getIdentifier( "is_tool", "integer", packageName );
			if ( resid != 0 ) is_tool = res.getInteger( resid );

			resid = res.getIdentifier( "is_border", "integer", packageName );
			if ( resid != 0 ) is_border = res.getInteger( resid );
		}

		intent.putExtra( FeatherIntent.PACKAGE_NAME, packageName );
		intent.putExtra( FeatherIntent.IS_STICKER, is_sticker );
		intent.putExtra( FeatherIntent.IS_FILTER, is_filter );
		intent.putExtra( FeatherIntent.IS_TOOL, is_tool );
		intent.putExtra( FeatherIntent.IS_BORDER, is_border );
		intent.putExtra( FeatherIntent.APPLICATION_CONTEXT, context.getApplicationContext().getPackageName() );
	}

	/**
	 * Handle package replaced.
	 * 
	 * @param context
	 *           the context
	 * @param intent
	 *           the intent
	 */
	private void handlePackageReplaced( Context context, Intent intent ) {

		logger.info( "handlePackageReplaced: " + intent );

		Uri data = intent.getData();
		String path = data.getSchemeSpecificPart();

		if ( null != path ) {
			if ( path.startsWith( FeatherIntent.PLUGIN_BASE_PACKAGE ) ) {
				Intent newIntent = new Intent( FeatherIntent.ACTION_PLUGIN_REPLACED );
				newIntent.setData( data );
				handlePackage( context, path, newIntent );
				newIntent.putExtra( FeatherIntent.ACTION, FeatherIntent.ACTION_PLUGIN_REPLACED );
				context.sendBroadcast( newIntent );
			}
		}
	}

	/**
	 * Handle package removed.
	 * 
	 * @param context
	 *           the context
	 * @param intent
	 *           the intent
	 */
	private void handlePackageRemoved( Context context, Intent intent ) {

		logger.info( "handlePackageRemoved: " + intent );

		Uri data = intent.getData();
		String path = data.getSchemeSpecificPart();

		Bundle extras = intent.getExtras();
		boolean is_replacing = isReplacing( extras );

		if ( null != path && !is_replacing ) {
			if ( path.startsWith( FeatherIntent.PLUGIN_BASE_PACKAGE ) ) {
				Intent newIntent = new Intent( FeatherIntent.ACTION_PLUGIN_REMOVED );
				newIntent.setData( data );
				handlePackage( context, path, newIntent );
				newIntent.putExtra( FeatherIntent.ACTION, FeatherIntent.ACTION_PLUGIN_REMOVED );
				context.sendBroadcast( newIntent );
			}
		}
	}

	/**
	 * Handle package added.
	 * 
	 * @param context
	 *           the context
	 * @param intent
	 *           the intent
	 */
	private void handlePackageAdded( Context context, Intent intent ) {

		logger.info( "handlePackageAdded: " + intent );

		Uri data = intent.getData();
		String path = data.getSchemeSpecificPart();

		Bundle extras = intent.getExtras();
		boolean is_replacing = isReplacing( extras );

		if ( null != path && !is_replacing ) {
			if ( path.startsWith( FeatherIntent.PLUGIN_BASE_PACKAGE ) ) {
				Intent newIntent = new Intent( FeatherIntent.ACTION_PLUGIN_ADDED );
				newIntent.setData( data );
				handlePackage( context, path, newIntent );
				newIntent.putExtra( FeatherIntent.ACTION, FeatherIntent.ACTION_PLUGIN_ADDED );
				context.sendBroadcast( newIntent );
			}
		}
	}

	/**
	 * Prints the bundle.
	 * 
	 * @param bundle
	 *           the bundle
	 */
	@SuppressWarnings("unused")
	private void printBundle( Bundle bundle ) {
		if ( null != bundle ) {
			Set<String> set = bundle.keySet();
			Iterator<String> iterator = set.iterator();
			while ( iterator.hasNext() ) {
				String key = iterator.next();
				Object value = bundle.get( key );
				logger.log( "		", key, value );
			}
		}
	}

	/**
	 * The operation.
	 * 
	 * @param bundle
	 *           the bundle
	 * @return true, if is replacing
	 */
	private boolean isReplacing( Bundle bundle ) {
		if ( bundle != null && bundle.containsKey( Intent.EXTRA_REPLACING ) ) return bundle.getBoolean( Intent.EXTRA_REPLACING );
		return false;
	}
}
