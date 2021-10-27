package com.ramlaxmaninnovation.mds.utils;

import static android.content.Context.MODE_PRIVATE;
import static com.ramlaxmaninnovation.mds.utils.Constant.USER_DETAILS_PREF;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

public class UserPrefManager {

    private SharedPreferences userPref;
    private SharedPreferences.Editor userPrefEditor;
    private Context mContext;

    // shared pref mode
    private static final int PRIVATE_MODE = 0;

    // Shared preferences file name
    public static final String PREF_NAME = "com-ramlaxmaninnovation-mds-shared-pref";

    private static final String SET_THRESHOLD = "threshold_set";


    private static final String SET_CAMERA_VIEW = "set_camera_view";

    private static final String APP_LANGUAGE = "app_language";


    private static final String APP_NETWORK = "app_network";


    private static final String APP_TERMINAL_ID = "app_terminal_id";
    private static final String LOCATION = "location";

    private static final String CURRENT_REGISTERED_NURSE_ID = "current_registered_nurse_id";
    private static final String CURRENT_REGISTERED_NURSE_NAME = "current_registered_nurse_name";
    private static final String CURRENT_REGISTERED_NURSE_PHOTO = "current_registered_nurse_photo";


    public UserPrefManager(Context context) {
        this.mContext = context;
        userPref = mContext.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        userPrefEditor = userPref.edit();
    }


    public void setLanguage(String language) {
        userPrefEditor.putString(APP_LANGUAGE, language);
        userPrefEditor.commit();
    }


    public String getLanguage() {
        return userPref.getString(APP_LANGUAGE, "ja");
    }


    public void setNetworkStatus(Boolean networkStatus) {
        userPrefEditor.putBoolean(APP_NETWORK, networkStatus);
        userPrefEditor.commit();
    }

    public Boolean getNetworkStatus() {
        return userPref.getBoolean(APP_NETWORK, true);
    }


    public void setThreshold(String threshold) {
        userPrefEditor.putString(SET_THRESHOLD, threshold);
        userPrefEditor.commit();
    }


    public String getThreshold() {
        return userPref.getString(SET_THRESHOLD, "0.9");
    }

    public void setLocation(String Location) {
        userPrefEditor.putString(LOCATION, Location);
        userPrefEditor.commit();
    }


    public String getLocation() {
        return userPref.getString(LOCATION, "not_found");
    }



    public void setId(Integer Location) {
        userPrefEditor.putInt("id", Location);
        userPrefEditor.commit();
    }


    public int getId() {
        return userPref.getInt("id", 0);
    }

    public void setTerminalName(String threshold) {
        userPrefEditor.putString(APP_TERMINAL_ID, threshold);
        userPrefEditor.commit();
    }


    public String getTerminalName() {
        return userPref.getString(APP_TERMINAL_ID, "NOT FOUND");
    }


    public void setNurseDetails(String id, String name, String photo) {
        userPrefEditor.putString(CURRENT_REGISTERED_NURSE_ID, id);
        userPrefEditor.putString(CURRENT_REGISTERED_NURSE_NAME, name);
        userPrefEditor.putString(CURRENT_REGISTERED_NURSE_PHOTO,photo);
        userPrefEditor.commit();
    }


    public List<String> getNurseDetails() {
        List<String> nurse_details = new ArrayList<>();
        nurse_details.add(userPref.getString(CURRENT_REGISTERED_NURSE_ID, "NOT FOUND"));
        nurse_details.add(userPref.getString(CURRENT_REGISTERED_NURSE_NAME, "NOT FOUND"));
        nurse_details.add(userPref.getString(CURRENT_REGISTERED_NURSE_PHOTO, "NOT FOUND"));
        return nurse_details;
    }


    public void setCameraView(String cameraView) {
        userPrefEditor.putString(SET_CAMERA_VIEW, cameraView);
        userPrefEditor.commit();
    }


    public String getCameraView() {
        return userPref.getString(SET_CAMERA_VIEW, "front");
    }


    public void clearData() {
        userPrefEditor = userPref.edit();
        // Clearing all data from Shared Preferences
        userPrefEditor.clear();
        userPrefEditor.apply();

        SharedPreferences pref = mContext.getSharedPreferences(USER_DETAILS_PREF, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.apply();


    }

}
