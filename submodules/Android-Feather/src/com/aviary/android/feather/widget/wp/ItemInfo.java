package com.aviary.android.feather.widget.wp;

// TODO: Auto-generated Javadoc
/**
 * The Class ItemInfo.
 */
public abstract class ItemInfo {

	/** The Constant NO_ID. */
	static final int NO_ID = -1;

	/** The id. */
	long id = NO_ID;

	/** The item type. */
	int itemType;

	/** The container. */
	long container = NO_ID;

	/** The screen. */
	int screen = -1;

	/** The cell x. */
	int cellX = -1;

	/** The cell y. */
	int cellY = -1;

	/** The span x. */
	int spanX = 1;

	/** The span y. */
	int spanY = 1;

	/**
	 * Instantiates a new item info.
	 */
	ItemInfo() {}

	/**
	 * Instantiates a new item info.
	 * 
	 * @param info
	 *           the info
	 */
	ItemInfo( ItemInfo info ) {
		id = info.id;
		cellX = info.cellX;
		cellY = info.cellY;
		spanX = info.spanX;
		spanY = info.spanY;
		screen = info.screen;
		itemType = info.itemType;
		container = info.container;
	}

}
