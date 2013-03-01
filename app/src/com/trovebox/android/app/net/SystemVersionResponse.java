
package com.trovebox.android.app.net;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * The class to represent API system version json response
 * 
 * @author Eugene Popovich
 */
public class SystemVersionResponse extends TroveboxResponse {
    private String api;
    private String database;
    private String filesystem;
    private String system;
    private boolean isHosted;
    private String[] databaseType;
    private String[] filesystemType;

    public SystemVersionResponse(JSONObject json) throws JSONException {
        super(RequestType.SYSTEM_VERSION, json);
        json = json.optJSONObject("result");
        if (json != null)
        {
            api = json.optString("api");
            database = json.optString("database");
            filesystem = json.optString("filesystem");
            system = json.optString("system");
            isHosted = json.optBoolean("isHosted");
            databaseType = getOptStringArray(json.optJSONArray("databaseType"));
            filesystemType = getOptStringArray(json.optJSONArray("filesystemType"));
        }
    }

    String[] getOptStringArray(JSONArray array)
    {
        String[] result = null;
        if (array != null)
        {
            result = new String[array.length()];
            for (int i = 0, size = array.length(); i < size; i++)
            {
                result[i] = array.optString(i);
            }
        }
        return result;
    }

    public String getApi() {
        return api;
    }

    public String getDatabase() {
        return database;
    }

    public String getFilesystem() {
        return filesystem;
    }

    public String getSystem() {
        return system;
    }

    /**
     * @return true if server is hosted, false if server is self-hosted
     */
    public boolean isHosted() {
        return isHosted;
    }

    public String[] getDatabaseType() {
        return databaseType;
    }

    public String[] getFilesystemType() {
        return filesystemType;
    }
}
