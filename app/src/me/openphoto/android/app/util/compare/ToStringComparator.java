package me.openphoto.android.app.util.compare;

import java.util.Comparator;

/**
 * @author Eugene Popovich
 */
public class ToStringComparator implements Comparator<Object>
{
    @Override
    public int compare(Object o1, Object o2) {
        String desc1 = o1.toString();
        String desc2 = o2.toString();
        if (desc1 == null)
        {
            return -1;
        } else if (desc2 == null)
        {
            return 1;
        } else
        {
            return desc1.toLowerCase().compareTo(desc2.toLowerCase());
        }
    }

}