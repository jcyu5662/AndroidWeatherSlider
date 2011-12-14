package com.morgan.design;

public class Constants {

	// Google Analytics
	public static final String GOOGLE_ANALYTICS_KEY = "UA-27624701-1";

	// Application Crash Report Google Docs Keys
	public static final String ANDROID_DOCS_CRASH_REPORT_KEY = "dHJsdXA5Nk5VVXRZdzFlcWY3bWhpWkE6MQ";

	// Email's
	public static final String[] FEEDBACK_EMAIL = new String[] { "appfeedback@morgan-design.com" };

	// Web URL's
	public static final String YAHOO_WEATHER_FORECAST_LINK = "http://weather.yahoo.com/forecast/%s.html";

	// Broadcast receiver actions
	public static final String LATEST_WEATHER_QUERY_COMPLETE = "com.morgan.design.intent.COMPLETED_LATEST_WEATHER_LOAD";
	public static final String PREFERENCES_UPDATED = "com.morgan.design.intent.PREFERENCES_UPDATED";
	public static final String GET_WEATHER_FORCAST = "com.morgan.design.android.broadcast.GET_WEATHER_FORCAST";

	// Activity Result codes
	public static final int ENTER_LOCATION = 1;
	public static final int SELECT_LOCATION = 2;
	public static final int UPDATED_PREFERENCES = 3;

	// Intent Extras
	public static final String FROM_LAUNCHER = "FROM_LAUNCHER";
	public static final String CURRENT_WEATHER_WOEID = "CURRENT_WEATHER_WOEID";
	public static final String CURRENT_WEATHER = "CURRENT_WEATHER";
	public static final String WOIED_LOCAITONS = "WOIED_LOCAITONS";
	public static final String SUCCESSFUL = "SUCCESSFUL";
	public static final String FORCAST_ENTRY = "FORCAST_ENTRY";

	public static final String TEMPERATURE_UNIT = "TEMPERATURE_UNIT";

}
