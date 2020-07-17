package com.example.test;
import android.content.Context;
import android.content.SharedPreferences;
public class t2 {
    public static final String PREFERENCES_NAME = "rebuild_preference";
    private static final String a = "";
    private static final boolean b = false;

    /* renamed from: c  reason: collision with root package name */
    private static final int f20209c = -1;

    /* renamed from: d  reason: collision with root package name */
    private static final long f20210d = -1;

    /* renamed from: e  reason: collision with root package name */
    private static final float f20211e = -1.0f;

    private static SharedPreferences a(Context context) {
        return context.getSharedPreferences(PREFERENCES_NAME, 0);
    }

    public static void clear(Context context) {
        SharedPreferences.Editor edit = a(context).edit();
        edit.clear();
        edit.apply();
    }

    public static boolean getBoolean(Context context, String str) {
        return a(context).getBoolean(str, false);
    }

    public static float getFloat(Context context, String str) {
        return a(context).getFloat(str, -1.0f);
    }

    public static int getInt(Context context, String str) {
        return a(context).getInt(str, -1);
    }

    public static long getLong(Context context, String str) {
        return a(context).getLong(str, -1);
    }

    public static String getString(Context context, String str) {
        return a(context).getString(str, "");
    }

    public static void removeKey(Context context, String str) {
        SharedPreferences.Editor edit = a(context).edit();
        edit.remove(str);
        edit.apply();
    }

    public static void setBoolean(Context context, String str, boolean z) {
        SharedPreferences.Editor edit = a(context).edit();
        edit.putBoolean(str, z);
        edit.apply();
    }

    public static void setFloat(Context context, String str, float f2) {
        SharedPreferences.Editor edit = a(context).edit();
        edit.putFloat(str, f2);
        edit.apply();
    }

    public static void setInt(Context context, String str, int i2) {
        SharedPreferences.Editor edit = a(context).edit();
        edit.putInt(str, i2);
        edit.apply();
    }

    public static void setLong(Context context, String str, long j2) {
        SharedPreferences.Editor edit = a(context).edit();
        edit.putLong(str, j2);
        edit.apply();
    }

    public static void setString(Context context, String str, String str2) {
        SharedPreferences.Editor edit = a(context).edit();
        edit.putString(str, str2);
        edit.apply();
    }
}