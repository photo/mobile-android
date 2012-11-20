package com.aviary.android.feather.async_tasks;

import android.content.Context;
import android.os.Bundle;
import com.aviary.android.feather.library.media.ExifInterfaceWrapper;
import com.aviary.android.feather.library.services.ThreadPoolService.BGCallable;

public class ExifTask extends BGCallable<String, Bundle> {

	@Override
	public Bundle call( Context context, String path ) {

		if ( path == null ) {
			return null;
		}

		Bundle result = new Bundle();

		try {
			ExifInterfaceWrapper exif = new ExifInterfaceWrapper( path );
			exif.copyTo( result );

		} catch ( Throwable t ) {
			t.printStackTrace();
		}
		return result;
	}

}
