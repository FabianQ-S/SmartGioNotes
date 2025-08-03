package com.sgionotes.utils;
import android.content.Context;
import android.content.SharedPreferences;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sgionotes.R;
import com.sgionotes.models.UserProfile;
public class UserProfileManager {
    private static final String PREFS_PREFIX = "user_profile_";
    private static final String KEY_NOMBRES = "nombres";
    private static final String KEY_APELLIDOS = "apellidos";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PROFILE_ICON = "profile_icon";
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Context context;

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
        this.context = context;
        initializePrefs();
    }

    private void initializePrefs() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = currentUser != null ? currentUser.getUid() : "default";
        String prefsName = PREFS_PREFIX + userId;

        prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void saveUserProfile(UserProfile profile) {
        initializePrefs();
        editor.putString(KEY_NOMBRES, profile.getNombres());
        editor.putString(KEY_APELLIDOS, profile.getApellidos());
        editor.putString(KEY_EMAIL, profile.getEmail());
        editor.putInt(KEY_PROFILE_ICON, profile.getProfileIcon());
        editor.apply();
    }

    public UserProfile getUserProfile() {
        initializePrefs();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String defaultEmail = currentUser != null ? (currentUser.getEmail() != null ? currentUser.getEmail() : "") : "";
        String nombres = prefs.getString(KEY_NOMBRES, "Usuario");
        String apellidos = prefs.getString(KEY_APELLIDOS, "");
        String email = prefs.getString(KEY_EMAIL, defaultEmail);
        int profileIcon = prefs.getInt(KEY_PROFILE_ICON, R.drawable.outline_account_circle_24);
        if (email.isEmpty() && currentUser != null && currentUser.getEmail() != null) {
            email = currentUser.getEmail();
            UserProfile profile = new UserProfile(nombres, apellidos, email, profileIcon);
            saveUserProfile(profile);
        }
        return new UserProfile(nombres, apellidos, email, profileIcon);
    }

    public void clearUserProfile() {
        initializePrefs();
        editor.clear();
        editor.apply();
    }
}
