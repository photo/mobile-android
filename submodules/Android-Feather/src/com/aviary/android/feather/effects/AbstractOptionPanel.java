package com.aviary.android.feather.effects;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import com.aviary.android.feather.Constants;
import com.aviary.android.feather.effects.AbstractEffectPanel.OptionPanel;
import com.aviary.android.feather.library.services.EffectContext;
import com.aviary.android.feather.library.services.PreferenceService;
import com.aviary.android.feather.widget.VibrationWidget;

abstract class AbstractOptionPanel extends AbstractEffectPanel implements OptionPanel {

	/** The current option view. */
	protected ViewGroup mOptionView;

	/**
	 * Instantiates a new abstract option panel.
	 * 
	 * @param context
	 *           the context
	 */
	public AbstractOptionPanel( EffectContext context ) {
		super( context );
	}

	@Override
	public final ViewGroup getOptionView( LayoutInflater inflater, ViewGroup parent ) {
		mOptionView = generateOptionView( inflater, parent );
		return mOptionView;
	}

	/**
	 * Gets the panel option view.
	 * 
	 * @return the option view
	 */
	public final ViewGroup getOptionView() {
		return mOptionView;
	}

	@Override
	protected void onDispose() {
		mOptionView = null;
		super.onDispose();
	}

	@Override
	public void setEnabled( boolean value ) {
		getOptionView().setEnabled( value );
		super.setEnabled( value );
	}

	/**
	 * Generate option view.
	 * 
	 * @param inflater
	 *           the inflater
	 * @param parent
	 *           the parent
	 * @return the view group
	 */
	protected abstract ViewGroup generateOptionView( LayoutInflater inflater, ViewGroup parent );

	/**
	 * Disable vibration feedback for each view in the passed array if necessary
	 * 
	 * @param views
	 */
	protected void disableHapticIsNecessary( VibrationWidget... views ) {
		boolean vibration = true;
		if ( Constants.containsValue( Constants.EXTRA_TOOLS_DISABLE_VIBRATION ) ) {
			vibration = false;
		} else {
			PreferenceService pref_service = getContext().getService( PreferenceService.class );
			if ( null != pref_service ) {
				if ( pref_service.isStandalone() ) {
					vibration = pref_service.getStandaloneBoolean( "feather_app_vibration", true );
				}
			}
		}

		for ( VibrationWidget view : views ) {
			view.setVibrationEnabled( vibration );
		}
	}

}
