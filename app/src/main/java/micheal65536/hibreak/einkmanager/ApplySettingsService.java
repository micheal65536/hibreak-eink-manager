package micheal65536.hibreak.einkmanager;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;

import micheal65536.hibreak.einkmanager.core.ColorMatrix;
import micheal65536.hibreak.einkmanager.core.EInkManager;
import micheal65536.hibreak.einkmanager.data.AdvancedSettings;
import micheal65536.hibreak.einkmanager.data.ColorMap;
import micheal65536.hibreak.einkmanager.data.ContrastMap;
import micheal65536.hibreak.einkmanager.data.GlobalSettings;
import micheal65536.hibreak.einkmanager.data.GlobalSettingsWithForcedColorMap;
import micheal65536.hibreak.einkmanager.data.ProfilesDao;
import micheal65536.hibreak.einkmanager.data.ProfilesDatabase;
import micheal65536.hibreak.einkmanager.data.RefreshMode;

public final class ApplySettingsService extends Service
{
	public static void applySettings(@NonNull Context context, boolean forceIfAccessibilityServiceIsRunning, boolean forceIfNoChanges, boolean disableHWOverlays, boolean setVcom)
	{
		Intent intent = new Intent(context, ApplySettingsService.class);
		intent.putExtra("forceIfAccessibilityServiceIsRunning", forceIfAccessibilityServiceIsRunning);
		intent.putExtra("forceIfNoChanges", forceIfNoChanges);
		intent.putExtra("disableHWOverlays", disableHWOverlays);
		intent.putExtra("setVcom", setVcom);
		ContextCompat.startForegroundService(context, intent);
	}

	//

	@Nullable
	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	//

	private static final String TAG = "ApplySettingsService";

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		Log.i(TAG, "service started");

		boolean forceIfAccessibilityServiceIsRunning = intent.getBooleanExtra("forceIfAccessibilityServiceIsRunning", false);
		boolean forceIfNoChanges = intent.getBooleanExtra("forceIfNoChanges", false);
		boolean disableHWOverlays = intent.getBooleanExtra("disableHWOverlays", false);
		boolean setVcom = intent.getBooleanExtra("setVcom", false);

		NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
		notificationManager.createNotificationChannel(
				new NotificationChannelCompat.Builder("applySettings", NotificationManagerCompat.IMPORTANCE_LOW)
						.setName(this.getString(R.string.service_apply_settings_notification_channel_name))
						.build());
		Notification notification = new NotificationCompat.Builder(this, "applySettings")
				.setSmallIcon(R.drawable.ic_service_apply_settings)
				.setContentTitle(this.getString(R.string.service_apply_settings_notification_text))
				.setOngoing(true)
				.build();
		if (Build.VERSION.SDK_INT >= 34)
		{
			this.startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SHORT_SERVICE);
		}
		else
		{
			this.startForeground(1, notification);
		}

		ProfilesDao dao = ProfilesDatabase.getInstance(this).dao();
		LiveData<GlobalSettingsWithForcedColorMap> globalSettingsLiveData = Transformations.map(dao.getGlobalSettingsWithForcedColorMap(), globalSettings -> globalSettings != null ? globalSettings : new GlobalSettingsWithForcedColorMap(GlobalSettings.DEFAULT, null));
		globalSettingsLiveData.observeForever(new Observer<>()
		{
			@Override
			public void onChanged(GlobalSettingsWithForcedColorMap globalSettings)
			{
				globalSettingsLiveData.removeObserver(this);
				LiveData<AdvancedSettings> advancedSettingsLiveData = Transformations.map(dao.getAdvancedSettings(), advancedSettings -> advancedSettings != null ? advancedSettings : AdvancedSettings.DEFAULT);
				advancedSettingsLiveData.observeForever(new Observer<>()
				{
					@Override
					public void onChanged(AdvancedSettings advancedSettings)
					{
						advancedSettingsLiveData.removeObserver(this);

						if (setVcom)
						{
							EInkManager.setVcom(advancedSettings.vcom);
						}

						if (disableHWOverlays)
						{
							EInkManager.disableHWOverlays();
						}

						if (forceIfAccessibilityServiceIsRunning || !EInkAccessibilityService.isRunning())
						{
							RefreshMode refreshMode = globalSettings.globalSettings.forceFastMode ? RefreshMode.FAST : globalSettings.globalSettings.defaultRefreshMode;
							ContrastMap globalContrastMap = !globalSettings.globalSettings.forceDisableColorMaps && globalSettings.globalSettings.useGlobalContrastMap && (refreshMode != RefreshMode.HIGH_QUALITY || globalSettings.globalSettings.useGlobalContrastMapInHighQualityMode) ? globalSettings.globalSettings.globalContrastMap : null;
							ContrastMap fastModeContrastMap = refreshMode == RefreshMode.FAST && !globalSettings.globalSettings.forceDisableColorMaps && globalSettings.globalSettings.useFastModeContrastMap ? globalSettings.globalSettings.fastModeContrastMap : null;
							ColorMap forcedColorMap = globalSettings.forcedColorMap != null ? globalSettings.forcedColorMap.colorMap : null;
							EInkManager.setExplicitRefreshParamSwitchDelay(advancedSettings.explicitRefreshRegularPostDelay, advancedSettings.explicitRefreshBlankPostDelay);
							EInkManager.applyRefreshModeAndColorMatrix(refreshMode, ColorMatrix.createColorMatrix(globalContrastMap, fastModeContrastMap, null, forcedColorMap), forceIfNoChanges);
						}

						EInkManager.onCompletion(() ->
						{
							ApplySettingsService.this.stopSelf(startId);
						});
					}
				});
			}
		});

		return Service.START_NOT_STICKY;
	}
}