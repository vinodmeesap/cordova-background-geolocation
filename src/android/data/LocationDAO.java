package com.transistorsoft.cordova.bggeo.data;

import org.json.JSONObject;
import org.json.JSONException;

public interface LocationDAO {
    public JSONObject[] getAllLocations();
    public boolean persistLocation(JSONObject l);
    public void deleteLocation(JSONObject l);
}