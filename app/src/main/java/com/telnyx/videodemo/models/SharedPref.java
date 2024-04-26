package com.telnyx.videodemo.models;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;

public class SharedPref {
    private static final String PREFS_NAME = "com.telnyx.videodemo";
    private static final String OFFLINE_ROOM_KEY = "offline_room";
    private SharedPreferences preferences;

    public SharedPref(Context context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveOfflineRoom(OfflineRoom room) {
        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(room);
        editor.putString(OFFLINE_ROOM_KEY, json);
        editor.apply();
    }

    public OfflineRoom getOfflineRoom() {
        Gson gson = new Gson();
        String json = preferences.getString(OFFLINE_ROOM_KEY, "");
        if (json.isEmpty()) return null;
        return gson.fromJson(json, OfflineRoom.class);
    }
}