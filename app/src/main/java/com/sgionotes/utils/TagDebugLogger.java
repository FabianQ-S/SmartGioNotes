package com.sgionotes.utils;

import android.util.Log;
import java.util.List;

public class TagDebugLogger {
    private static final String TAG = "TAG_DEBUG";
    private static final boolean DEBUG_ENABLED = true;

    public static void logTagOperation(String operation, String noteId, String tagInfo) {
        if (DEBUG_ENABLED) {
            Log.d(TAG, String.format("[%s] NoteID:%s | %s", operation, noteId, tagInfo));
        }
    }

    public static void logTagList(String context, String noteId, List<String> tags) {
        if (DEBUG_ENABLED) {
            String tagList = tags != null ? String.join(",", tags) : "NULL";
            Log.d(TAG, String.format("[%s] NoteID:%s | Tags:%s | Count:%d",
                context, noteId, tagList, tags != null ? tags.size() : 0));
        }
    }

    public static void logError(String operation, String noteId, String error) {
        if (DEBUG_ENABLED) {
            Log.e(TAG, String.format("[ERROR-%s] NoteID:%s | %s", operation, noteId, error));
        }
    }

    public static void logCritical(String message) {
        if (DEBUG_ENABLED) {
            Log.w(TAG, "[CRITICAL] " + message);
        }
    }

    public static void logFirebaseOperation(String operation, String noteId, String details) {
        if (DEBUG_ENABLED) {
            Log.w(TAG, String.format("[FIREBASE-%s] NoteID:%s | %s", operation, noteId, details));
        }
    }

    public static void logDataInconsistency(String noteId, String expected, String actual) {
        if (DEBUG_ENABLED) {
            Log.e(TAG, String.format("[DATA_INCONSISTENCY] NoteID:%s | Expected:%s | Actual:%s",
                noteId, expected, actual));
        }
    }

    public static void logListState(String context, String noteId, List<String> tagNames, List<String> tagIds) {
        if (DEBUG_ENABLED) {
            String nameList = tagNames != null ? String.join(",", tagNames) : "NULL";
            String idList = tagIds != null ? String.join(",", tagIds) : "NULL";
            Log.w(TAG, String.format("[LIST_STATE-%s] NoteID:%s | Names:%s (Count:%d) | IDs:%s (Count:%d)",
                context, noteId, nameList,
                tagNames != null ? tagNames.size() : 0,
                idList,
                tagIds != null ? tagIds.size() : 0));
        }
    }

    public static void logMethodCall(String methodName, String noteId, String params) {
        if (DEBUG_ENABLED) {
            Log.i(TAG, String.format("[METHOD_CALL] %s | NoteID:%s | Params:%s",
                methodName, noteId, params));
        }
    }
}
