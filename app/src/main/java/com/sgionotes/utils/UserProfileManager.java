package com.sgionotes.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.sgionotes.R;
import com.sgionotes.models.UserProfile;

public class UserProfileManager {

    private static final String PREFS_NAME = "user_profile";
    private static final String KEY_NOMBRES = "nombres";
    private static final String KEY_APELLIDOS = "apellidos";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PROFILE_ICON = "profile_icon";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    //MaterialIcons
    public static final int[] PROFILE_ICONS = {
        R.drawable.ic_person_24,
        R.drawable.ic_school_24,
        R.drawable.ic_group_24,
        R.drawable.ic_pets_24,
        R.drawable.ic_event_note_24,
        R.drawable.ic_artist_24,
        R.drawable.ic_nature_24,
        R.drawable.ic_my_location_24,
        R.drawable.ic_motorcycle_24
    };

    public UserProfileManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void saveUserProfile(UserProfile profile) {
        editor.putString(KEY_NOMBRES, profile.getNombres());
        editor.putString(KEY_APELLIDOS, profile.getApellidos());
        editor.putString(KEY_EMAIL, profile.getEmail());
        editor.putInt(KEY_PROFILE_ICON, profile.getProfileIcon());
        editor.apply();
    }

    public UserProfile getUserProfile() {
        String nombres = prefs.getString(KEY_NOMBRES, "Usuario");
        String apellidos = prefs.getString(KEY_APELLIDOS, "");
        String email = prefs.getString(KEY_EMAIL, "");
        int profileIcon = prefs.getInt(KEY_PROFILE_ICON, R.drawable.outline_account_circle_24);

        return new UserProfile(nombres, apellidos, email, profileIcon);
    }

    public void clearUserProfile() {
        editor.clear();
        editor.apply();
    }
}
