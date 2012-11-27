
package me.openphoto.android.app.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import me.openphoto.android.app.bitmapfun.util.ImageWorker;
import android.content.Context;
import android.util.FloatMath;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.WazaBe.HoloEverywhere.LayoutInflater;

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
    List<List<T>> itemGroups;
    int totalWidth;
    int imageHeight;
    int maxImageHeight;
    int borderSize;
    Stack<View> unusedViews = new Stack<View>();

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
            float ratio = getHeight(value) == 0 ? 1 : (float) getWidth(value)
                    / (float) getHeight(value);
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
        if (rest > 0)
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
        if (!force && totalWidth == this.totalWidth)
        {
            return;
        }
        this.totalWidth = totalWidth;
        this.imageHeight = imageHeight;
        this.borderSize = borderSize;
        itemGroups = new ArrayList<List<T>>();
        if (totalWidth == 0)
        {
            return;
        }

        List<T> itemGroup = new ArrayList<T>();
        int usedWidth = 0;
        for (int i = 0, size = getSuperCount(); i < size; i++)
        {
            T photo = getSuperItem(i);
            double ratio = getHeight(photo) == 0 ? 1 : (float) getWidth(photo)
                    / (float) getHeight(photo);
            int requiredWidth = (int) (ratio * imageHeight) + 2 * borderSize;
            if (usedWidth > 0 &&
                    requiredWidth + usedWidth > totalWidth)
            {
                itemGroups.add(itemGroup);
                itemGroup = new ArrayList<T>();
                usedWidth = requiredWidth;
            } else
            {
                usedWidth += requiredWidth;
            }
            itemGroup.add(photo);
        }
        if (!itemGroup.isEmpty())
        {
            itemGroups.add(itemGroup);
        }
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
    public List<T> getGroupItem(int position)
    {
        return itemGroups.get(position);
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
     * @param imageWorker
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
            ImageWorker imageWorker,
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
                imageWorker,
                context);
        removeUnusedViews(imageViewId, view, values, childCount);
        return view;
    }

    protected void addOrReuseChilds(ViewGroup view,
            int childCount, List<T> values,
            int layoutId,
            int imageViewId,
            ImageWorker imageWorker,
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
                    imageWorker,
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
            ImageWorker imageWorker,
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

        float ratio = getHeight(value) == 0 ? 1 : (float) getWidth(value)
                / (float) getHeight(value);
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
        imageWorker.loadImage(value, imageView);
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
}
