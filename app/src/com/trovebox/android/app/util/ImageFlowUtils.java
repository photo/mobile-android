
package com.trovebox.android.app.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.widget.LinearLayout;

import android.content.Context;
import android.util.FloatMath;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.trovebox.android.app.bitmapfun.util.ImageWorker;

/**
 * This is an util class which is used to build images flow layout based on
 * listview. Images get resized keeping aspect ratio to fit whole the line
 * 
 * @author Eugene Popovich
 * @param <T>
 */
public abstract class ImageFlowUtils<T>
{
    static final String TAG = ImageFlowUtils.class.getSimpleName();
    List<ItemGroupWrapper<T>> itemGroups;
    int totalWidth;
    int imageHeight;
    int maxImageHeight;
    int borderSize;
    Stack<View> unusedViews = new Stack<View>();
    int lastUsedWidth = 0;
    int lastIndex = -1;

    protected ImageFlowUtils.ImageHeightResult calculateImageHeightResult(
            List<T> values
            ) {
        int usedWidth = 0;
        int imageHeight = this.imageHeight;
        int nonRedistributedWidth = 0;
        int totalWidthWithoutBorders = totalWidth - 2 * borderSize * values.size();
        List<Float> ratios = new ArrayList<Float>();
        float totalRatio = 0;
        for (T value : values)
        {
            float ratio = getRatio(value);
            ratios.add(ratio);
            totalRatio += ratio;
            int width = (int) (ratio * imageHeight);
            usedWidth += width;
            CommonUtils.debug(TAG,
                    "Width: " + getWidth(value)
                            + "; Height: " + getHeight(value)
                            + "; Ratio: " + ratio
                            + "; RWidth: " + width
                            + "; Used width: " + usedWidth);
        }
        int rest = totalWidthWithoutBorders - usedWidth;

        CommonUtils.debug(TAG, "Used width:" + usedWidth
                + "; Item height" + imageHeight
                + "; Total width without borders:" + totalWidthWithoutBorders
                + "; Rest: " + rest
                + "; Border size:" + borderSize
                + "; Total size:" + totalWidth);
        {
            imageHeight = (int) ((float) totalWidthWithoutBorders / totalRatio);
            boolean limitReached = maxImageHeight > 0 && imageHeight >= maxImageHeight;
            if (limitReached)
            {
                imageHeight = maxImageHeight;
            } else
            {
                nonRedistributedWidth = totalWidthWithoutBorders;
                for (Float r : ratios)
                {
                    nonRedistributedWidth -= (int) (r * imageHeight);
                }
            }
        }
        return new ImageHeightResult(imageHeight, Math.max(0, nonRedistributedWidth));
    }

    /**
     * Force rebuild the images groups based on their sizes
     */
    public void rebuildGroups()
    {
        buildGroups(totalWidth, imageHeight, maxImageHeight, borderSize, true);
    }

    /**
     * Should be called in case item is deleted from model or replaced by
     * another item to clear optimization information used in buildGroups method
     */
    public void onGroupsStructureModified()
    {
        lastUsedWidth = 0;
        lastIndex = -1;
    }

    /**
     * Build images groups
     * 
     * @param totalWidth the parent container total width
     * @param imageHeight the desired height of the image
     * @param borderSize the border size of each image container
     */
    public void buildGroups(
            int totalWidth,
            int imageHeight,
            int borderSize)
    {
        buildGroups(totalWidth, imageHeight, -1, borderSize);
    }

    /**
     * Build images groups
     * 
     * @param totalWidth the parent container total width
     * @param imageHeight the desired height of the image
     * @param maxImageHeight the max possible height of the image. If it is <= 0
     *            then ignored
     * @param borderSize the border size of each image container
     */
    public void buildGroups(
            int totalWidth,
            int imageHeight,
            int maxImageHeight,
            int borderSize)
    {
        buildGroups(totalWidth, imageHeight, maxImageHeight, borderSize, false);
    }

    /**
     * Build images groups
     * 
     * @param totalWidth the parent container total width
     * @param imageHeight the desired height of the image
     * @param maxImageHeight the max possible height of the image. If it is <= 0
     *            then ignored
     * @param borderSize the border size of each image container
     * @param force if true the groups will rebuild even if totalWidth is the
     *            same as was in previous buildGroups call
     */
    public void buildGroups(
            int totalWidth,
            int imageHeight,
            int maxImageHeight,
            int borderSize,
            boolean force)
    {
        this.maxImageHeight = maxImageHeight;
        if (!force && totalWidth == this.totalWidth && imageHeight == this.imageHeight)
        {
            return;
        }
        this.totalWidth = totalWidth;
        this.imageHeight = imageHeight;
        this.borderSize = borderSize;
        if (totalWidth == 0)
        {
            return;
        }
        if (lastIndex < 0)
        {
            itemGroups = new ArrayList<ItemGroupWrapper<T>>();
        }
        if (lastIndex + 1 == getSuperCount())
        {
            return;
        }

        List<T> itemGroup = itemGroups.isEmpty() ? new ArrayList<T>() :
                itemGroups.remove(itemGroups.size() - 1).itemGroup;
        int usedWidth = lastUsedWidth;
        for (int i = lastIndex + 1, size = getSuperCount(); i < size; i++)
        {
            T photo = getSuperItem(i);
            double ratio = getRatio(photo);
            int requiredWidth = (int) (ratio * imageHeight) + 2 * borderSize;
            if (usedWidth > 0 &&
                    requiredWidth + usedWidth > totalWidth)
            {
                itemGroups.add(new ItemGroupWrapper<T>(itemGroup, i - 1));
                itemGroup = new ArrayList<T>();
                usedWidth = requiredWidth;
            } else
            {
                usedWidth += requiredWidth;
            }
            itemGroup.add(photo);
            lastUsedWidth = usedWidth;
            lastIndex = i;
        }
        if (!itemGroup.isEmpty())
        {
            itemGroups.add(new ItemGroupWrapper<T>(itemGroup, lastIndex));
        }
    }

    /**
     * Get the ratio for photo dimensions
     * 
     * @param photo
     * @return
     */
    public float getRatio(T photo) {
        return getHeight(photo) == 0 ? 1 : (float) getWidth(photo)
                / (float) getHeight(photo);
    }

    /**
     * Get the object height
     * 
     * @param object
     * @return
     */
    public abstract int getHeight(T object);

    /**
     * Get the object width
     * 
     * @param object
     * @return
     */
    public abstract int getWidth(T object);

    /**
     * Get super class count method implementation result
     * 
     * @return
     */
    public abstract int getSuperCount();

    /**
     * Get super class item implementation result
     * 
     * @param position
     * @return
     */
    public abstract T getSuperItem(int position);

    /**
     * Get the built groups count
     * 
     * @return
     */
    public int getGroupsCount()
    {
        return itemGroups == null ? 0 : itemGroups.size();
    }

    /**
     * Get the group item which is used to build one line
     * 
     * @param position
     * @return
     */
    public List<T> getGroupItem(int position) {
        return itemGroups.get(position).itemGroup;
    }

    /**
     * Get the last element in group super position for the group position
     * 
     * @param position group position
     * @return position in super items container for the last element in the
     *         group
     */
    public int getSuperItemPositionForGroupPosition(int position) {
        ItemGroupWrapper<T> groupWrapper = itemGroups.get(position);
        int result = groupWrapper.firstElementSuperIndex + groupWrapper.itemGroup.size() - 1;
        CommonUtils.verbose(TAG,
                "getSuperItemPositionForGroupPosition: position %1$d; super position %2$d",
                position, result);
        return result;
    }

    protected static class ImageHeightResult
    {
        public int imageHeight;
        public int nonRedistributedWidth;

        public ImageHeightResult(int itemHeight, int nonRedistributedWidth) {
            super();
            this.imageHeight = itemHeight;
            this.nonRedistributedWidth = nonRedistributedWidth;
        }
    }

    /**
     * General get view method which builds the one single line of images
     * 
     * @param position
     * @param convertView
     * @param parent
     * @param lineLayoutId
     * @param childLayoutId
     * @param imageViewId
     * @param context
     * @return
     */
    public View getView(
            int position,
            View convertView,
            ViewGroup parent,
            int lineLayoutId,
            int childLayoutId,
            int imageViewId,
            Context context) {
        ViewGroup view;
        if (convertView == null)
        { // if it's not recycled, instantiate and initialize
            final LayoutInflater layoutInflater = (LayoutInflater)
                    context
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = (ViewGroup) layoutInflater.inflate(
                    lineLayoutId, null);
        } else
        { // Otherwise re-use the converted view
            view = (ViewGroup) convertView;
        }

        int childCount = view.getChildCount();
        List<T> values = (List<T>) getGroupItem(position);
        addOrReuseChilds(view, childCount, values,
                childLayoutId,
                imageViewId,
                context);
        removeUnusedViews(imageViewId, view, values, childCount);
        return view;
    }

    protected void addOrReuseChilds(ViewGroup view,
            int childCount, List<T> values,
            int layoutId,
            int imageViewId,
            Context context) {
        View convertView;
        ImageFlowUtils.ImageHeightResult imageHeightResult = calculateImageHeightResult(values);
        int perStepExtraWidth = (int) FloatMath
                .ceil((float) imageHeightResult.nonRedistributedWidth
                        / (float) values.size());
        int usedExtraWidth = 0;
        CommonUtils.debug(TAG, "Total width: "
                + totalWidth);
        CommonUtils.debug(TAG, "Non redistributed width: "
                + imageHeightResult.nonRedistributedWidth);
        CommonUtils.debug(TAG, "Per step width: " + perStepExtraWidth);
        for (int i = 0, size = values.size(); i < size; i++)
        {
            int extraWidth = 0;
            if (usedExtraWidth < imageHeightResult.nonRedistributedWidth)
            {
                extraWidth = Math.min(imageHeightResult.nonRedistributedWidth - usedExtraWidth,
                        perStepExtraWidth);
                usedExtraWidth += extraWidth;
            }
            T value = values.get(i);
            boolean add = false;
            if (i < childCount)
            {
                convertView = view.getChildAt(i);
            } else
            {
                if (!unusedViews.isEmpty())
                {
                    CommonUtils.debug(TAG, "Reusing view from the stack");
                    convertView = unusedViews.pop();
                } else
                {
                    convertView = null;
                }
                add = true;
            }

            View singleImageView = getSingleImageView(
                    convertView, value,
                    imageHeightResult.imageHeight,
                    extraWidth,
                    layoutId,
                    imageViewId,
                    context);
            if (add)
            {
                view.addView(singleImageView);
            }
        }
    }

    protected void removeUnusedViews(int imageViewId,
            ViewGroup view,
            List<T> values,
            int childCount) {
        for (int i = childCount - 1, size = values.size(); i >= size; i--)
        {
            View subView = view.getChildAt(i);
            ImageView imageView = (ImageView)
                    subView.findViewById(imageViewId);
            ImageWorker.cancelPotentialWork(null, imageView);
            unusedViews.add(subView);
            view.removeViewAt(i);

        }
    }

    protected View getSingleImageView(
            View convertView,
            T value,
            int imageHeight,
            int extraWidth,
            int layoutId,
            int imageViewId,
            Context context) {
        View view;
        if (convertView == null)
        { // if it's not recycled, instantiate and initialize
            CommonUtils.debug(TAG, "Creating new view for child");
            final LayoutInflater layoutInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(
                    layoutId, null);
        } else
        { // Otherwise re-use the converted view
            view = convertView;
        }

        float ratio = getRatio(value);
        int height = imageHeight;
        int width = (int) (ratio * height) + extraWidth;

        CommonUtils.debug(TAG, "Processing image: " + value
                + "; Width: " + getWidth(value)
                + "; Height: " + getHeight(value)
                + "; Extra width: " + extraWidth
                + "; Req Width: " + width
                + "; Req Height: " + height);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                width + 2 * borderSize,
                height + 2 * borderSize);
        view.setLayoutParams(layoutParams);

        additionalSingleImageViewInit(view, value);
        ImageView imageView = (ImageView) view.findViewById(imageViewId);
        // Finally load the image asynchronously into the ImageView, this
        // also takes care of
        // setting a placeholder image while the background thread runs
        loadImage(value, imageView);
        return view;
    }

    /**
     * Perform additional child view initialization procedures. This should be
     * overriden if additional initialization is required
     * 
     * @param view
     * @param value
     */
    public void additionalSingleImageViewInit(View view, T value) {

    }

    /**
     * Get the stored totalWidth argument passed to lates buildGroups call
     * 
     * @return
     */
    public int getTotalWidth() {
        return totalWidth;
    }

    public abstract void loadImage(T value, ImageView imageView);

    public static class FlowObjectToStringWrapper<T>
    {
        T object;
        String toStringValue;

        public FlowObjectToStringWrapper(T object, String toStringValue) {
            super();
            this.object = object;
            this.toStringValue = toStringValue;
        }

        public T getObject()
        {
            return object;
        }

        @Override
        public String toString() {
            return toStringValue;
        }
    }

    /**
     * Wrapper for item group which also stores starting index in the super
     * items container
     * 
     * @param <T>
     */
    public static class ItemGroupWrapper<T> {
        List<T> itemGroup;
        /**
         * index of the first element of the itemGroup in the super items
         * container
         */
        int firstElementSuperIndex;

        ItemGroupWrapper(List<T> itemGroup, int lastElementSuperIndex) {
            this.itemGroup = itemGroup;
            firstElementSuperIndex = lastElementSuperIndex - itemGroup.size() + 1;
        }
    }
}
