package com.morgan.design.android.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.morgan.design.Constants;
import com.morgan.design.android.UserPreferencesActivity;
import com.morgan.design.android.domain.types.OverviewMode;
import com.morgan.design.android.domain.types.Temperature;
import com.morgan.design.android.domain.types.WindSpeed;

public class PreferenceUtils {

	public static final String PREF_ENABLE_GOOGLE_ANALYTICS = "enableGoogleAnalytics";

	public static final String PREF_CHANGELOG = "changelog";
	public static final String PREF_POLLING_SCHEDULE = "pollingSchedule";
	public static final String PREF_START_ON_BOOT = "startOnBoot";
	public static final String PREF_OVERVIEW_MODE = "overviewMode";
	public static final String PREF_TEMPERATURE_MODE = "temperatureMode";
	public static final String PREF_WIND_MODE = "windSpeedMode";

	public static void openUserPreferenecesActivity(final Activity activity) {
		final Intent intent = new Intent(activity, UserPreferencesActivity.class);
		activity.startActivityForResult(intent, Constants.UPDATED_PREFERENCES);
	}

	private static SharedPreferences getPrefs(final Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context);
	}

	public static boolean getChangelogPref(final Context context) {
		return getPrefs(context).getBoolean(PREF_CHANGELOG, true);
	}

	public static boolean setChangelogPref(final Context context, final boolean value) {
		return getPrefs(context).edit().putBoolean(PREF_CHANGELOG, value).commit();
	}

	public static String getPollingSchedule(final Context context) {
		return getPrefs(context).getString(PREF_POLLING_SCHEDULE, "15");
	}

	public static boolean setPollingSchedule(final Context context, final String minutes) {
		return getPrefs(context).edit().putString(PREF_POLLING_SCHEDULE, minutes).commit();
	}

	public static OverviewMode getOverviewMode(final Context context) {
		return OverviewMode.to(getPrefs(context).getString(PREF_OVERVIEW_MODE, OverviewMode.WEB.name()));
	}

	public static boolean setOverviewMode(final Context context, final String overviewMode) {
		return getPrefs(context).edit().putString(PREF_OVERVIEW_MODE, overviewMode).commit();
	}

	public static Temperature getTemperatureMode(final Context context) {
		return Temperature.to(getPrefs(context).getString(PREF_TEMPERATURE_MODE, Temperature.CELSIUS.getAbrev()));
	}

	public static boolean setTemperatureMode(final Context context, final String temperature) {
		return getPrefs(context).edit().putString(PREF_TEMPERATURE_MODE, temperature).commit();
	}

	public static WindSpeed getWindSpeedMode(final Context context) {
		return WindSpeed.fromPref(getPrefs(context).getString(PREF_WIND_MODE, WindSpeed.MPH.toString().toLowerCase()));
	}

	public static boolean setWindSpeedMode(final Context context, final String wind) {
		return getPrefs(context).edit().putString(PREF_WIND_MODE, wind).commit();
	}

	public static boolean shouldStartOnBoot(final Context context) {
		final boolean shouldStartOnBoot = getPrefs(context).getBoolean(PREF_START_ON_BOOT, true);
		Logger.d("PreferenceUtils", "ShouldStartOnBoot [%s]", shouldStartOnBoot);
		return shouldStartOnBoot;
	}

	public static boolean setShouldStartOnBoot(final Context context, final boolean shouldStartOnBoot) {
		return getPrefs(context).edit().putBoolean(PREF_START_ON_BOOT, shouldStartOnBoot).commit();
	}
}
