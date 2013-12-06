package com.trovebox.android.common.net;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Eugene Popovich
 */
public class UploadMetaDataUtils {
    /**
     * Get the album ids comma separated string from the upload meta data
     * 
     * @param uploadMetaData
     * @return
     */
    public static String getAlbumIds(UploadMetaData uploadMetaData)
    {
        Map<String, String> albums = uploadMetaData.getAlbums();
        String result = null;
        if(albums != null)
        {
            result = getCommaSeparatedString(albums.keySet());
        }
        return result;
    }

    /**
     * Get the album names comma separated string from the upload meta data
     * 
     * @param uploadMetaData
     * @return
     */
    public static String getAlbumNames(UploadMetaData uploadMetaData)
    {
        Map<String, String> albums = uploadMetaData.getAlbums();
        String result = null;
        if (albums != null)
        {
            result = getCommaSeparatedString(albums.values());
        }
        return result;
    }

    /**
     * Get comma separated string from collection
     * 
     * @param strings
     * @return
     */
    public static String getCommaSeparatedString(Collection<String> strings)
    {
        if (strings != null && !strings.isEmpty())
        {
            Iterator<String> it = strings.iterator();
            StringBuilder sb = new StringBuilder(it.next());
            while (it.hasNext())
            {
                sb.append("," + it.next());
            }
            return sb.toString();
        } else
        {
            return null;
        }
    }
}
