package com.signquest.app.data;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * ProfileManager — Per-user profile persistence using SharedPreferences.
 *
 * Each user gets their own SharedPreferences file: "signquest_profile_{userId}".
 * A global prefs file ("signquest_global") tracks all registered user IDs
 * and the currently active user.
 */
public class ProfileManager {

    // ── Global prefs (shared across all users) ──
    private static final String GLOBAL_PREFS        = "signquest_global";
    private static final String KEY_USER_IDS        = "user_ids";       // JSON array of IDs
    private static final String KEY_ACTIVE_USER_ID  = "active_user_id";

    // ── Per-user prefs keys ──
    private static final String PREFS_PREFIX         = "signquest_profile_";
    private static final String KEY_CHILD_NAME       = "child_name";
    private static final String KEY_AVATAR_ID        = "avatar_id";
    private static final String KEY_LANGUAGE         = "sign_language";
    private static final String KEY_BADGES_EARNED    = "badges_earned";
    private static final String KEY_TOTAL_TIME_MIN   = "total_time_minutes";
    private static final String KEY_SOUND_MUTED      = "sound_muted";
    private static final String KEY_PROFILE_EXISTS   = "profile_exists";
    private static final String KEY_COMPLETED_PREFIX = "completed_";

    /** Avatar constants matching the UI card IDs */
    public static final int AVATAR_NONE       = -1;
    public static final int AVATAR_PIKACHU    = 0;
    public static final int AVATAR_CHARMANDER = 1;
    public static final int AVATAR_BULBASAUR  = 2;
    public static final int AVATAR_SQUIRTLE   = 3;

    private final Context context;
    private final SharedPreferences prefs;      // per-user
    private final SharedPreferences globalPrefs;
    private final String userId;

    // ──────────────────────────────────────────────
    //  Constructors
    // ──────────────────────────────────────────────

    /** Create a ProfileManager for a specific user. */
    public ProfileManager(Context context, String userId) {
        this.context     = context.getApplicationContext();
        this.userId      = userId;
        this.globalPrefs = context.getSharedPreferences(GLOBAL_PREFS, Context.MODE_PRIVATE);
        this.prefs       = context.getSharedPreferences(PREFS_PREFIX + userId, Context.MODE_PRIVATE);
    }

    /** Convenience: uses the currently active user (falls back to "default"). */
    public ProfileManager(Context context) {
        this.context     = context.getApplicationContext();
        this.globalPrefs = context.getSharedPreferences(GLOBAL_PREFS, Context.MODE_PRIVATE);
        this.userId      = globalPrefs.getString(KEY_ACTIVE_USER_ID, "default");
        this.prefs       = context.getSharedPreferences(PREFS_PREFIX + userId, Context.MODE_PRIVATE);
    }

    // ──────────────────────────────────────────────
    //  Global user registry
    // ──────────────────────────────────────────────

    /** Generate a new unique user ID. */
    public static String generateUserId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    /** Get all registered user IDs. */
    public List<String> getAllUserIds() {
        String json = globalPrefs.getString(KEY_USER_IDS, "[]");
        List<String> ids = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                ids.add(arr.getString(i));
            }
        } catch (JSONException e) {
            // corrupted → return empty
        }
        return ids;
    }

    /** Register a user ID in the global list if not already present. */
    public void registerUser(String id) {
        List<String> ids = getAllUserIds();
        if (!ids.contains(id)) {
            ids.add(id);
            globalPrefs.edit().putString(KEY_USER_IDS, new JSONArray(ids).toString()).apply();
        }
    }

    /** Remove a user from the global list and delete their prefs. */
    public void deleteUser(String id) {
        List<String> ids = getAllUserIds();
        ids.remove(id);
        globalPrefs.edit().putString(KEY_USER_IDS, new JSONArray(ids).toString()).apply();

        // Clear that user's preferences
        context.getSharedPreferences(PREFS_PREFIX + id, Context.MODE_PRIVATE)
                .edit().clear().apply();

        // If the deleted user was active, clear active
        if (id.equals(getActiveUserId())) {
            globalPrefs.edit().remove(KEY_ACTIVE_USER_ID).apply();
        }
    }

    /** Set the currently active user ID. */
    public void setActiveUserId(String id) {
        globalPrefs.edit().putString(KEY_ACTIVE_USER_ID, id).apply();
    }

    /** Get the currently active user ID. */
    public String getActiveUserId() {
        return globalPrefs.getString(KEY_ACTIVE_USER_ID, null);
    }

    /** Get the current user's ID. */
    public String getUserId() {
        return userId;
    }

    /**
     * Read another user's profile name (for display in user-select list).
     * Returns "Explorer" if the user has no profile.
     */
    public static String getUserName(Context context, String userId) {
        SharedPreferences sp = context.getSharedPreferences(PREFS_PREFIX + userId, Context.MODE_PRIVATE);
        return sp.getString(KEY_CHILD_NAME, "Explorer");
    }

    /** Read another user's avatar ID (for display in user-select list). */
    public static int getUserAvatar(Context context, String userId) {
        SharedPreferences sp = context.getSharedPreferences(PREFS_PREFIX + userId, Context.MODE_PRIVATE);
        return sp.getInt(KEY_AVATAR_ID, AVATAR_NONE);
    }

    /** Read another user's language (for display in user-select list). */
    public static String getUserLanguage(Context context, String userId) {
        SharedPreferences sp = context.getSharedPreferences(PREFS_PREFIX + userId, Context.MODE_PRIVATE);
        return sp.getString(KEY_LANGUAGE, "ASL");
    }

    // ──────────────────────────────────────────────
    //  Profile creation
    // ──────────────────────────────────────────────

    public void saveProfile(String name, int avatarId) {
        saveProfile(name, avatarId, "ASL");
    }

    public void saveProfile(String name, int avatarId, String language) {
        prefs.edit()
                .putString(KEY_CHILD_NAME, name.trim())
                .putInt(KEY_AVATAR_ID, avatarId)
                .putString(KEY_LANGUAGE, language)
                .putBoolean(KEY_PROFILE_EXISTS, true)
                .apply();
        // Also register this user and set as active
        registerUser(userId);
        setActiveUserId(userId);
    }

    // ──────────────────────────────────────────────
    //  Getters
    // ──────────────────────────────────────────────

    public boolean hasProfile() {
        return prefs.getBoolean(KEY_PROFILE_EXISTS, false);
    }

    public String getChildName() {
        return prefs.getString(KEY_CHILD_NAME, "Explorer");
    }

    public int getAvatarId() {
        return prefs.getInt(KEY_AVATAR_ID, AVATAR_NONE);
    }

    public String getSignLanguage() {
        return prefs.getString(KEY_LANGUAGE, "ASL");
    }

    public int getBadgesEarned() {
        return prefs.getInt(KEY_BADGES_EARNED, 0);
    }

    public int getTotalTimeMinutes() {
        return prefs.getInt(KEY_TOTAL_TIME_MIN, 0);
    }

    public int getSignsLearnedCount() {
        int count = 0;
        java.util.Map<String, ?> allEntries = prefs.getAll();
        for (java.util.Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if (entry.getKey().startsWith(KEY_COMPLETED_PREFIX) && entry.getValue() instanceof Boolean && ((Boolean) entry.getValue())) {
                count++;
            }
        }
        return count;
    }

    public boolean isSoundMuted() {
        return prefs.getBoolean(KEY_SOUND_MUTED, false);
    }

    public void setSoundMuted(boolean muted) {
        prefs.edit().putBoolean(KEY_SOUND_MUTED, muted).apply();
    }

    // ──────────────────────────────────────────────
    //  Per-Sign Completion Tracking
    // ──────────────────────────────────────────────

    private String completionKey(String language, int levelId, String signKey) {
        return KEY_COMPLETED_PREFIX + language + "_" + levelId + "_" + signKey;
    }

    public void markSignCompleted(String language, int levelId, String signKey) {
        prefs.edit()
                .putBoolean(completionKey(language, levelId, signKey), true)
                .apply();
    }

    public boolean isSignCompleted(String language, int levelId, String signKey) {
        return prefs.getBoolean(completionKey(language, levelId, signKey), false);
    }

    public int getCompletedCount(String language, int levelId) {
        int count = 0;
        java.util.List<SignDataProvider.SignItem> signs =
                SignDataProvider.getSigns(language, levelId);
        for (SignDataProvider.SignItem sign : signs) {
            if (isSignCompleted(language, levelId, sign.getKey())) {
                count++;
            }
        }
        return count;
    }

    // ──────────────────────────────────────────────
    //  Level Unlock Logic
    // ──────────────────────────────────────────────

    public boolean isLevelUnlocked(String language, int levelId) {
        if (levelId <= SignDataProvider.LEVEL_ALPHABETS) return true;

        int previousLevel = levelId - 1;
        int totalInPrevious = SignDataProvider.getTotalSignCount(previousLevel);
        int completedInPrevious = getCompletedCount(language, previousLevel);
        return completedInPrevious >= totalInPrevious;
    }

    // ──────────────────────────────────────────────
    //  Progression helpers
    // ──────────────────────────────────────────────

    public void awardBadge() {
        int current = getBadgesEarned();
        prefs.edit().putInt(KEY_BADGES_EARNED, current + 1).apply();
    }

    public void addPracticeTime(int minutes) {
        int total = getTotalTimeMinutes();
        prefs.edit().putInt(KEY_TOTAL_TIME_MIN, total + minutes).apply();
    }

    public void setSignLanguage(String language) {
        prefs.edit().putString(KEY_LANGUAGE, language).apply();
    }

    public void clearProfile() {
        prefs.edit().clear().apply();
    }
}
