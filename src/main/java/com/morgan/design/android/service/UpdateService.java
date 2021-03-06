package com.morgan.design.android.service;

import static com.morgan.design.Constants.APPLICATION_UPDATE_AVAILABLE;
import static com.morgan.design.Constants.UPDATE_SITE;
import static com.morgan.design.android.util.ObjectUtils.isNot;
import static com.morgan.design.android.util.ObjectUtils.isNull;
import static com.morgan.design.android.util.ObjectUtils.isZero;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.util.ByteArrayBuffer;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;

import com.morgan.design.Logger;
import com.morgan.design.android.util.BuildUtils;
import com.morgan.design.android.util.PreferenceUtils;

/**
 * @author James Edward Morgan <br />
 *         Service capable of checking for updates and prompting the user under certain conditions.
 */
public class UpdateService extends IntentService {

	public static final String LOG_TAG = "UpdateService";

	public UpdateService() {
		super("com.morgan.design.android.service.UpdateService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		AlarmManager mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

		if (isNot(PreferenceUtils.enableDailyUpdateCheck(this))) {
			Logger.d(LOG_TAG, "Daily update check is disabled");
			mAlarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, System.currentTimeMillis(), AlarmManager.INTERVAL_DAY,
					PendingIntent.getService(this, 0, new Intent(this, UpdateService.class), PendingIntent.FLAG_CANCEL_CURRENT));
			return;
		}

		long lastUpdateTime = PreferenceUtils.getLastTimCheckedForUpdate(this);

		/* Has it been over 24hrs since the last check? */
		if (BuildUtils.isRunningEmmulator() || (lastUpdateTime + (24 * 60 * 60 * 1000)) < System.currentTimeMillis()) {

			Logger.d(LOG_TAG, "Triggering daily update check");

			PreferenceUtils.setLastTimCheckedForUpdate(this, System.currentTimeMillis());

			checkUpdate.start();
		}
	}

	public void launchUpdateStrategy(Update result) {
		try {
			int currentVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;

			// If applicable for update
			if (result.updateType.isAvailable() && (result.versionCode > currentVersion)) {
				switch (result.updateType) {
					case FORCED:
						Logger.d(LOG_TAG, "Attempt to show application update available dialog, new version=[%S], old version=[%s]", result.versionCode,
								currentVersion);
						attemptToForceShowDialog(result, currentVersion);
						break;
					case ON_NEXT_OPEN:
						Logger.d(LOG_TAG, "Setting application update available dialog for next application open, new version=[%S], old version=[%s]",
								result.versionCode, currentVersion);
						enableDialogForNextOpen(result, currentVersion);
						break;
				}
			}
		}
		catch (Exception exception) {
			Logger.w(LOG_TAG, "Unable to determine update strategy", exception);
		}
		finally {
			AlarmManager mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
			mAlarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, System.currentTimeMillis(), AlarmManager.INTERVAL_DAY,
					PendingIntent.getService(this, 0, new Intent(this, UpdateService.class), PendingIntent.FLAG_CANCEL_CURRENT));
		}
	}

	private void attemptToForceShowDialog(Update result, int currentVersion) {
		// Always set preference in-case no activity is available and for a alert dialog
		enableDialogForNextOpen(result, currentVersion);
		// Send show update dialog broadcast
		sendBroadcast(new Intent(APPLICATION_UPDATE_AVAILABLE));
	}

	private void enableDialogForNextOpen(Update result, int currentVersion) {
		// Set Preference for next boot
		// If found on home screen, send broadcast to show application & clear preference flag
		PreferenceUtils.setShowUpdateDialogOnNextOpen(this, true);
	}

	private final Thread checkUpdate = new Thread() {
		@Override
		public void run() {
			try {
				String updateString = getUpdateCode();
				if (isNull(updateString)) {
					launchUpdateStrategy(Update.fail());
					return;
				}
				Logger.d(LOG_TAG, "Found update string=[%s]", updateString);

				int versionCode = parseVersion(updateString);
				if (isZero(versionCode)) {
					launchUpdateStrategy(Update.fail());
					return;
				}
				Logger.d(LOG_TAG, "Found update code=[%s]", versionCode);

				launchUpdateStrategy(new Update(versionCode, parseUpdateType(updateString)));
			}
			catch (Exception e) {
			}
		}
	};

	private UpdateType parseUpdateType(String updateCode) {
		try {
			String code = updateCode.substring(updateCode.length() - 1, updateCode.length());
			return UpdateType.fromCode(code);
		}
		catch (Throwable throwable) {
			Logger.w(LOG_TAG, "Unable to parse update type", throwable);
		}
		return UpdateType.NONE;
	}

	private int parseVersion(String updateCode) {
		try {
			return Integer.parseInt(updateCode.substring(0, updateCode.length() - 1));
		}
		catch (Throwable throwable) {
			Logger.w(LOG_TAG, "Unable to parse update code", throwable);
			return 0;
		}
	}

	private String getUpdateCode() {
		try {
			URL updateURL = new URL(UPDATE_SITE);
			URLConnection conn = updateURL.openConnection();
			// Set to 8K to prevent:
			// "Default buffer size used in BufferedInputStream constructor. It would be better to be explicit if an 8k buffer is required."
			BufferedReader bis = new BufferedReader(new InputStreamReader(conn.getInputStream()), 8192);
			ByteArrayBuffer baf = new ByteArrayBuffer(50);

			int current = 0;
			while ((current = bis.read()) != -1) {
				baf.append((byte) current);
			}
			// Clean any potential line endings
			return new String(baf.toByteArray()).replace("\n", "");
		}
		catch (Throwable throwable) {
			Logger.w(LOG_TAG, "Unable to reach update site", throwable);
			return null;
		}
	}

	private static class Update {
		int versionCode;
		UpdateType updateType;

		public Update(int versionCode, UpdateType type) {
			this.versionCode = versionCode;
			this.updateType = type;
		}

		public static Update fail() {
			return new Update(0, UpdateType.NONE);
		}
	}

	private enum UpdateType {
		/* No update available */
		NONE(""),
		/* Attempt to inform User immediately */
		FORCED("f"),
		/* Inform User on Next launch */
		ON_NEXT_OPEN("n"),
		/* Update available but not critical */
		SILENT("s");

		private String code;

		private UpdateType(String code) {
			this.code = code;
		}

		public boolean isAvailable() {
			return !equals(NONE) || !equals(SILENT);
		}

		public static UpdateType fromCode(String type) {
			for (UpdateType up : values()) {
				if (up.code.equalsIgnoreCase(type)) {
					return up;
				}
			}
			return UpdateType.NONE;
		}
	}
}
