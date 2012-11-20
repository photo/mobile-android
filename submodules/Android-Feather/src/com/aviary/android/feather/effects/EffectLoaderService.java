package com.aviary.android.feather.effects;

import java.io.IOException;
import com.aviary.android.feather.R;
import com.aviary.android.feather.library.content.EffectEntry;
import com.aviary.android.feather.library.filters.FilterLoaderFactory;
import com.aviary.android.feather.library.filters.FilterLoaderFactory.Filters;
import com.aviary.android.feather.library.log.LoggerFactory;
import com.aviary.android.feather.library.log.LoggerFactory.Logger;
import com.aviary.android.feather.library.log.LoggerFactory.LoggerType;
import com.aviary.android.feather.library.services.EffectContext;
import com.aviary.android.feather.library.services.EffectContextService;

/**
 * The Class EffectLoaderService.
 */
public class EffectLoaderService extends EffectContextService {

	public static final String NAME = "effect-loader";

	/**
	 * Instantiates a new effect loader service.
	 * 
	 * @param context
	 *           the context
	 */
	public EffectLoaderService( EffectContext context ) {
		super( context );
	}

	/**
	 * Passing a {@link EffectEntry} return an instance of {@link AbstractEffectPanel} used to create the requested tool.
	 * 
	 * @param entry
	 *           the entry
	 * @return the abstract effect panel
	 */
	public AbstractEffectPanel load( EffectEntry entry ) {

		AbstractEffectPanel panel = null;
		final EffectContext context = getContext();

		switch ( entry.name ) {
			case ADJUST:
				panel = new AdjustEffectPanel( context, Filters.ADJUST );
				break;

			case BRIGHTNESS:
				panel = new NativeEffectRangePanel( context, Filters.BRIGHTNESS, "brightness" );
				break;

			case SATURATION:
				panel = new NativeEffectRangePanel( context, Filters.SATURATION, "saturation" );
				break;

			case CONTRAST:
				panel = new NativeEffectRangePanel( context, Filters.CONTRAST, "contrast" );
				break;

			case SHARPNESS:
				panel = new NativeEffectRangePanel( context, Filters.SHARPNESS, "sharpen" );
				break;

			case COLORTEMP:
				panel = new NativeEffectRangePanel( context, Filters.COLORTEMP, "temperature" );
				break;

			case ENHANCE:
				panel = new EnhanceEffectPanel( context, Filters.ENHANCE );
				break;

			case EFFECTS:
				panel = new NativeEffectsPanel( context );
				//panel = new EffectsPanel( context, FeatherIntent.PluginType.TYPE_FILTER );
				break;

			//case BORDERS:
			//	panel = new EffectsPanel( context, FeatherIntent.PluginType.TYPE_BORDER );
			//	break;

			case CROP:
				panel = new CropPanel( context );
				break;

			case RED_EYE:
				panel = new DelayedSpotDrawPanel( context, Filters.RED_EYE, false );
				break;

			case WHITEN:
				panel = new DelayedSpotDrawPanel( context, Filters.WHITEN, false );
				break;

			case BLEMISH:
				panel = new DelayedSpotDrawPanel( context, Filters.BLEMISH, false );
				break;

			case DRAWING:
				panel = new DrawingPanel( context );
				break;

			case STICKERS:
				panel = new StickersPanel( context );
				break;

			case TEXT:
				panel = new TextPanel( context );
				break;

			case MEME:
				panel = new MemePanel( context );
				break;

			default:
				Logger logger = LoggerFactory.getLogger( "EffectLoaderService", LoggerType.ConsoleLoggerType );
				logger.error( "Effect with " + entry.name + " could not be found" );
				break;
		}
		return panel;
	}

	/** The Constant mAllEntries. */
	static final EffectEntry[] mAllEntries;

	static {
		mAllEntries = new EffectEntry[] {
			new EffectEntry( FilterLoaderFactory.Filters.ENHANCE, R.drawable.feather_tool_icon_enhance, R.string.enhance ),
			new EffectEntry( FilterLoaderFactory.Filters.EFFECTS, R.drawable.feather_tool_icon_effects, R.string.effects ),
			/*new EffectEntry( FilterLoaderFactory.Filters.BORDERS, R.drawable.feather_tool_icon_borders, R.string.feather_borders ),*/
			new EffectEntry( FilterLoaderFactory.Filters.STICKERS, R.drawable.feather_tool_icon_stickers, R.string.stickers ),
			new EffectEntry( FilterLoaderFactory.Filters.ADJUST, R.drawable.feather_tool_icon_adjust, R.string.adjust ),
			new EffectEntry( FilterLoaderFactory.Filters.CROP, R.drawable.feather_tool_icon_crop, R.string.crop ),
			new EffectEntry( FilterLoaderFactory.Filters.BRIGHTNESS, R.drawable.feather_tool_icon_brightness, R.string.brightness ),
			new EffectEntry( FilterLoaderFactory.Filters.COLORTEMP, R.drawable.feather_tool_icon_temperature,
					R.string.feather_tool_temperature ),
			new EffectEntry( FilterLoaderFactory.Filters.CONTRAST, R.drawable.feather_tool_icon_contrast, R.string.contrast ),
			new EffectEntry( FilterLoaderFactory.Filters.SATURATION, R.drawable.feather_tool_icon_saturation, R.string.saturation ),
			new EffectEntry( FilterLoaderFactory.Filters.SHARPNESS, R.drawable.feather_tool_icon_sharpen, R.string.sharpen ),
			new EffectEntry( FilterLoaderFactory.Filters.DRAWING, R.drawable.feather_tool_icon_draw, R.string.draw ),
			new EffectEntry( FilterLoaderFactory.Filters.TEXT, R.drawable.feather_tool_icon_text, R.string.text ),
			new EffectEntry( FilterLoaderFactory.Filters.MEME, R.drawable.feather_tool_icon_meme, R.string.meme ),
			new EffectEntry( FilterLoaderFactory.Filters.RED_EYE, R.drawable.feather_tool_icon_redeye, R.string.red_eye ),
			new EffectEntry( FilterLoaderFactory.Filters.WHITEN, R.drawable.feather_tool_icon_whiten, R.string.whiten ),
			new EffectEntry( FilterLoaderFactory.Filters.BLEMISH, R.drawable.feather_tool_icon_blemish, R.string.blemish ), };
	}

	/**
	 * Return a list of available effects.
	 * 
	 * @return the effects
	 */
	public EffectEntry[] getEffects() {
		return mAllEntries;
	}

	public static final EffectEntry[] getAllEntries() {
		return mAllEntries;
	}

	/**
	 * Check if the current application context has a valid folder "stickers" inside its assets folder.
	 * 
	 * @return true, if successful
	 */
	public boolean hasStickers() {
		try {
			String[] list = null;
			list = getContext().getBaseContext().getAssets().list( "stickers" );
			return list.length > 0;
		} catch ( IOException e ) {}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.library.services.EffectContextService#dispose()
	 */
	@Override
	public void dispose() {}
}
