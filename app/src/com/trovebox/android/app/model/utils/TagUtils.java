
package com.trovebox.android.app.model.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import android.text.TextUtils;

import com.trovebox.android.app.net.UploadMetaDataUtils;

/**
 * @author Eugene Popovich
 */
public class TagUtils {

    /**
     * Split the tagsString to the tags
     * 
     * @param tagsString
     * @return
     */
    public static Set<String> getTags(String tagsString)
    {
        Set<String> result = new HashSet<String>();
        if (!TextUtils.isEmpty(tagsString))
        {
            String[] tagsArray = tagsString.split("[\\s\\t\\n]*,[\\s\\t\\n]*");
            result.addAll(Arrays.asList(tagsArray));
        }
        return result;
    }

    /**
     * Get the tags string from the tags collection
     * 
     * @param tags
     * @return
     */
    public static String getTagsString(Collection<String> tags)
    {
        return UploadMetaDataUtils.getCommaSeparatedString(tags);
    }

}
