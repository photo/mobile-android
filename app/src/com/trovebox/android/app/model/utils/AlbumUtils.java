
package com.trovebox.android.app.model.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.trovebox.android.app.net.UploadMetaDataUtils;
import com.trovebox.android.app.util.compare.ToStringComparator;

/**
 * @author Eugene Popovich
 */
public class AlbumUtils {

    /**
     * Get the album names string from the albums map
     * 
     * @param albums
     * @return
     */
    public static String getAlbumsString(Map<String, String> albums)
    {
        Collection<String> values = albums == null ? null : albums.values();
        if (values != null)
        {
            values = new ArrayList<String>(values);
            Collections.sort((List<String>) values, new ToStringComparator());
        }
        return UploadMetaDataUtils.getCommaSeparatedString(values);
    }

}
