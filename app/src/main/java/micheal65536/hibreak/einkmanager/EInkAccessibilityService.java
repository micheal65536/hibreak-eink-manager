package micheal65536.hibreak.einkmanager;

import android.accessibilityservice.AccessibilityService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityWindowInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import micheal65536.hibreak.einkmanager.core.ColorMatrix;
import micheal65536.hibreak.einkmanager.core.EInkManager;
import micheal65536.hibreak.einkmanager.core.RawFunctionButtonReader;
import micheal65536.hibreak.einkmanager.core.RawTouchReader;
import micheal65536.hibreak.einkmanager.data.AdvancedSettings;
import micheal65536.hibreak.einkmanager.data.AppProfile;
import micheal65536.hibreak.einkmanager.data.ColorMap;
import micheal65536.hibreak.einkmanager.data.ContrastMap;
import micheal65536.hibreak.einkmanager.data.GlobalSettings;
import micheal65536.hibreak.einkmanager.data.GlobalSettingsWithForcedColorMap;
import micheal65536.hibreak.einkmanager.data.ProfilesDao;
import micheal65536.hibreak.einkmanager.data.ProfilesDatabase;
import micheal65536.hibreak.einkmanager.data.RefreshMode;

public final class EInkAccessibilityService extends AccessibilityService
{
	private static boolean accessibilityServiceStarted = false;
	private static String lastAppIdForQuickSettings = null;

	public static boolean isRunning()
	{
		return accessibilityServiceStarted;
	}

	@Nullable
	public static String getLastAppIdForQuickSettings()
	{
		return lastAppIdForQuickSettings;
	}

	//

	private static final String TAG = "AccessibilityService";

	//

	private final Observer<GlobalSettingsWithForcedColorMap> globalSettingsObserver = globalSettings ->
	{
		EInkAccessibilityService.this.onSettingsChanged();
	};
	private final Observer<List<AppProfile>> appProfilesObserver = profiles ->
	{
		EInkAccessibilityService.this.onSettingsChanged();
	};
	private final Observer<AdvancedSettings> advancedSettingsObserver = advancedSettings ->
	{
		EInkAccessibilityService.this.onAdvancedSettingsChanged();
	};
	private LiveData<GlobalSettingsWithForcedColorMap> globalSettingsLiveData;
	private LiveData<List<AppProfile>> appProfilesLiveData;
	private LiveData<AdvancedSettings> advancedSettingsLiveData;

	private final BroadcastReceiver screenStateBroadcastReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			if (Intent.ACTION_SCREEN_ON.equals(intent.getAction()))
			{
				EInkAccessibilityService.this.onScreenOn();
			}
			else if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction()))
			{
				EInkAccessibilityService.this.onScreenOff();
			}
		}
	};

	private final BroadcastReceiver manualRefreshBroadcastReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			String action = intent.getAction();
			if (action != null && action.equals("micheal65536.hibreak.einkmanager.REFRESH"))
			{
				EInkAccessibilityService.this.onManualRefreshBroadcast();
			}
		}
	};

	private final class TouchReader extends RawTouchReader
	{
		@Override
		public void onFirstPointerDown(int x, int y)
		{
			EInkAccessibilityService.this.onFirstPointerDown(x, y);
		}

		@Override
		public void onFirstPointerMove(int x, int y)
		{
			EInkAccessibilityService.this.onFirstPointerMove(x, y);
		}

		@Override
		public void onSecondPointerDown()
		{
			EInkAccessibilityService.this.onSecondPointerDown();
		}

		@Override
		public void onLastPointerUp()
		{
			EInkAccessibilityService.this.onLastPointerUp();
		}
	}

	private final class FunctionButtonReader extends RawFunctionButtonReader
	{
		@Override
		public void onStateChange(boolean state)
		{
			if (state)
			{
				EInkAccessibilityService.this.onFunctionButtonPressed();
			}
			else
			{
				EInkAccessibilityService.this.onFunctionButtonReleased();
			}
		}
	}

	private RawTouchReader rawTouchReader = null;
	private RawFunctionButtonReader rawFunctionButtonReader = null;

	private Handler handler;

	//

	private final Runnable delayedManualRefreshRunnable = () -> this.doManualRefresh();
	private final Runnable delayedDragStartRunnable = () -> this.dragStart();
	private final Runnable delayedDragStopRunnable = () -> this.dragStop();
	private final Runnable delayedPowerOffRunnable = () ->
	{
		Log.i(TAG, "powering screen off");
		EInkManager.powerOff();
	};

	//

	private GlobalSettingsWithForcedColorMap globalSettings = new GlobalSettingsWithForcedColorMap(GlobalSettings.DEFAULT, null);
	private AdvancedSettings advancedSettings = AdvancedSettings.DEFAULT;
	private AppProfile currentAppProfile = null;

	@Nullable
	private String lastAppId;
	private boolean inSystemUI;
	private boolean isDragging;
	private boolean isIMEOpen;

	//

	@Override
	protected void onServiceConnected()
	{
		this.lastAppId = null;
		this.inSystemUI = false;
		this.isDragging = false;
		this.isIMEOpen = false;

		lastAppIdForQuickSettings = null;
		accessibilityServiceStarted = true;
		Log.i(TAG, "accessibility service started");

		ProfilesDao dao = ProfilesDatabase.getInstance(this).dao();
		this.globalSettingsLiveData = dao.getGlobalSettingsWithForcedColorMap();
		this.globalSettingsLiveData.observeForever(this.globalSettingsObserver);
		this.appProfilesLiveData = dao.getAllAppProfiles();
		this.appProfilesLiveData.observeForever(this.appProfilesObserver);
		this.advancedSettingsLiveData = dao.getAdvancedSettings();
		this.advancedSettingsLiveData.observeForever(this.advancedSettingsObserver);

		IntentFilter screenStateIntentFilter = new IntentFilter();
		screenStateIntentFilter.addAction(Intent.ACTION_SCREEN_ON);
		screenStateIntentFilter.addAction(Intent.ACTION_SCREEN_OFF);
		this.registerReceiver(this.screenStateBroadcastReceiver, screenStateIntentFilter);
		IntentFilter manualRefreshIntentFilter = new IntentFilter();
		manualRefreshIntentFilter.addAction("micheal65536.hibreak.einkmanager.REFRESH");
		this.registerReceiver(this.manualRefreshBroadcastReceiver, manualRefreshIntentFilter);

		this.handler = new Handler(this.getMainLooper());

		if (this.getSystemService(PowerManager.class).isInteractive())
		{
			this.onScreenOn();
		}
	}

	@Override
	public boolean onUnbind(Intent intent)
	{
		accessibilityServiceStarted = false;
		lastAppIdForQuickSettings = null;
		Log.i(TAG, "accessibility service stopped");

		if (this.rawTouchReader != null)
		{
			this.rawTouchReader.stop();
			this.rawTouchReader = null;
		}
		if (this.rawFunctionButtonReader != null)
		{
			this.rawFunctionButtonReader.stop();
			this.rawFunctionButtonReader = null;
		}

		this.globalSettingsLiveData.removeObserver(this.globalSettingsObserver);
		this.appProfilesLiveData.removeObserver(this.appProfilesObserver);
		this.advancedSettingsLiveData.removeObserver(this.advancedSettingsObserver);

		this.unregisterReceiver(this.screenStateBroadcastReceiver);
		this.unregisterReceiver(this.manualRefreshBroadcastReceiver);

		this.handler.removeCallbacks(this.delayedManualRefreshRunnable);
		this.handler.removeCallbacks(this.delayedDragStartRunnable);
		this.handler.removeCallbacks(this.delayedDragStopRunnable);
		this.handler.removeCallbacks(this.delayedPowerOffRunnable);

		ApplySettingsService.applySettings(this, false, true, false, false);

		return false;
	}

	@Override
	public void onAccessibilityEvent(AccessibilityEvent event)
	{
		Log.d(TAG, String.format("event %d", event.getEventType()));

		if ((event.getEventType() & AccessibilityEvent.TYPE_WINDOWS_CHANGED) != 0)
		{
			this.onWindowsListChanged(this.getWindows());
		}
	}

	@Override
	public void onInterrupt()
	{
		// empty
	}

	//

	private void reapplySettings(boolean forceIfNoChanges)
	{
		RefreshMode refreshMode;
		if (this.currentAppProfile != null && this.currentAppProfile.enabled && this.currentAppProfile.refreshMode != null)
		{
			refreshMode = this.currentAppProfile.refreshMode;
		}
		else
		{
			refreshMode = this.globalSettings.globalSettings.defaultRefreshMode;
		}
		if (this.isDragging)
		{
			boolean dragModeEnabled = this.globalSettings.globalSettings.enableDragModeSwitch || (this.currentAppProfile != null && this.currentAppProfile.enabled && this.currentAppProfile.enableDragModeSwitch == Boolean.TRUE);
			boolean dragModeDisabledByCurrentAppProfile = this.currentAppProfile != null && this.currentAppProfile.enabled && this.currentAppProfile.enableDragModeSwitch == Boolean.FALSE;
			boolean dragModeDisabledByIMEOpen = this.isIMEOpen && !this.globalSettings.globalSettings.allowDragModeDuringIme;
			if (dragModeEnabled && !dragModeDisabledByCurrentAppProfile && !dragModeDisabledByIMEOpen)
			{
				refreshMode = RefreshMode.FAST;
			}
		}
		if (this.isIMEOpen)
		{
			boolean imeModeEnabled = this.globalSettings.globalSettings.enableImeModeSwitch || (this.currentAppProfile != null && this.currentAppProfile.enabled && this.currentAppProfile.enableImeModeSwitch == Boolean.TRUE);
			boolean imeModeDisabledByCurrentAppProfile = this.currentAppProfile != null && this.currentAppProfile.enabled && this.currentAppProfile.enableImeModeSwitch == Boolean.FALSE;
			if (imeModeEnabled && !imeModeDisabledByCurrentAppProfile)
			{
				refreshMode = RefreshMode.FAST;
			}
		}
		if (this.globalSettings.globalSettings.forceFastMode)
		{
			refreshMode = RefreshMode.FAST;
		}

		ContrastMap globalContrastMap = null;
		boolean globalContrastMapEnabled = this.globalSettings.globalSettings.useGlobalContrastMap;
		boolean globalContrastMapDisabledByHighQualityMode = !this.globalSettings.globalSettings.useGlobalContrastMapInHighQualityMode && refreshMode == RefreshMode.HIGH_QUALITY;
		boolean globalContrastMapDisabledByCurrentAppProfile = this.currentAppProfile != null && this.currentAppProfile.enabled && this.currentAppProfile.disableGlobalContrastMap;
		if (globalContrastMapEnabled && !globalContrastMapDisabledByHighQualityMode && !globalContrastMapDisabledByCurrentAppProfile)
		{
			globalContrastMap = this.globalSettings.globalSettings.globalContrastMap;
		}

		ContrastMap fastModeContrastMap = null;
		if (refreshMode == RefreshMode.FAST)
		{
			boolean fastModeContrastMapEnabled = this.globalSettings.globalSettings.useFastModeContrastMap;
			boolean fastModeContrastMapDisabledByCurrentAppProfile = this.currentAppProfile != null && this.currentAppProfile.enabled && this.currentAppProfile.refreshMode == RefreshMode.FAST && this.currentAppProfile.disableGlobalContrastMap;
			if (fastModeContrastMapEnabled && !fastModeContrastMapDisabledByCurrentAppProfile)
			{
				fastModeContrastMap = this.globalSettings.globalSettings.fastModeContrastMap;
			}
		}

		ColorMap appProfileColorMap = this.currentAppProfile != null && this.currentAppProfile.enabled && this.currentAppProfile.useColorMap ? this.currentAppProfile.colorMap : null;

		ColorMap forcedColorMap = this.globalSettings.globalSettings.forcedColorMapId != null && this.globalSettings.forcedColorMap != null ? this.globalSettings.forcedColorMap.colorMap : null;
		if (forcedColorMap != null)
		{
			appProfileColorMap = null;
		}

		EInkManager.setExplicitRefreshParamSwitchDelay(this.advancedSettings.explicitRefreshRegularPostDelay, this.advancedSettings.explicitRefreshBlankPostDelay);
		EInkManager.applyRefreshModeAndColorMatrix(refreshMode, ColorMatrix.createColorMatrix(globalContrastMap, fastModeContrastMap, appProfileColorMap, forcedColorMap), forceIfNoChanges);
	}

	private void doManualRefresh()
	{
		EInkManager.setExplicitRefreshParamSwitchDelay(this.advancedSettings.explicitRefreshRegularPostDelay, this.advancedSettings.explicitRefreshBlankPostDelay);
		EInkManager.fullRefresh(this.globalSettings.globalSettings.manualFullRefreshType);
	}

	private void dragStart()
	{
		Log.d(TAG, "drag starting");
		if (!this.isDragging)
		{
			this.isDragging = true;
			this.reapplySettings(false);
		}
	}

	private void dragStop()
	{
		Log.d(TAG, "drag stopped");
		if (this.isDragging)
		{
			this.isDragging = false;
			this.reapplySettings(false);
		}
	}

	@Nullable
	private AppProfile getApplicableAppProfile()
	{
		if (this.lastAppId == null)
		{
			return null;
		}
		if (!this.globalSettings.globalSettings.useLastAppModeInSystemUI && this.inSystemUI)
		{
			return null;
		}
		List<AppProfile> appProfiles = this.appProfilesLiveData.getValue();
		if (appProfiles == null)
		{
			return null;
		}
		return appProfiles.stream().filter(appProfile -> appProfile.appId.equals(this.lastAppId)).findAny().orElse(null);
	}

	//

	private void onSettingsChanged()
	{
		Log.d(TAG, "settings changed");

		this.globalSettings = this.globalSettingsLiveData.getValue();
		if (this.globalSettings == null)
		{
			this.globalSettings = new GlobalSettingsWithForcedColorMap(GlobalSettings.DEFAULT, null);
		}
		this.currentAppProfile = this.getApplicableAppProfile();

		this.reapplySettings(false);
	}

	private void onAdvancedSettingsChanged()
	{
		Log.d(TAG, "advanced settings changed");

		this.advancedSettings = this.advancedSettingsLiveData.getValue();
		if (this.advancedSettings == null)
		{
			this.advancedSettings = AdvancedSettings.DEFAULT;
		}

		EInkManager.setVcom(this.advancedSettings.vcom);

		this.reapplySettings(false);
	}

	private void onWindowsListChanged(@NonNull List<AccessibilityWindowInfo> windows)
	{
		Log.d(TAG, "window list changed");

		this.isIMEOpen = windows.stream().anyMatch(window -> window.getType() == AccessibilityWindowInfo.TYPE_INPUT_METHOD);
		Log.d(TAG, String.format("got IME state: %d", this.isIMEOpen ? 1 : 0));

		String[] openWindowAppIds = windows.stream()
										   .filter(window -> window.getType() == AccessibilityWindowInfo.TYPE_APPLICATION)
										   .map(window -> window.getRoot())
										   .filter(accessibilityNodeInfo -> accessibilityNodeInfo != null)
										   .map(accessibilityNodeInfo -> accessibilityNodeInfo.getPackageName().toString())
										   .toArray(String[]::new);
		Collections.reverse(Arrays.asList(openWindowAppIds));
		Log.d(TAG, String.format("got open windows: %s", String.join(" ", openWindowAppIds)));

		String appId = openWindowAppIds.length > 0 ? openWindowAppIds[0] : null;
		boolean appHasChanged = false;
		if (appId != null && !appId.equals(this.lastAppId))
		{
			this.lastAppId = appId;
			appHasChanged = true;
		}
		this.inSystemUI = appId == null;

		this.currentAppProfile = this.getApplicableAppProfile();
		this.reapplySettings(appHasChanged);

		if (appId != null && !appId.equals(this.getApplicationContext().getPackageName()))
		{
			lastAppIdForQuickSettings = appId;
		}
	}

	private void onScreenOn()
	{
		Log.i(TAG, "screen on");

		this.handler.removeCallbacks(this.delayedPowerOffRunnable);

		this.isDragging = false;

		EInkManager.setVcom(this.advancedSettings.vcom);
		EInkManager.disableHWOverlays();
		this.reapplySettings(true);

		this.rawTouchReader = new TouchReader();
		this.rawFunctionButtonReader = new FunctionButtonReader();
	}

	private void onScreenOff()
	{
		Log.i(TAG, "screen off");

		if (this.rawTouchReader != null)
		{
			this.rawTouchReader.stop();
			this.rawTouchReader = null;
		}
		if (this.rawFunctionButtonReader != null)
		{
			this.rawFunctionButtonReader.stop();
			this.rawFunctionButtonReader = null;
		}

		this.handler.removeCallbacks(this.delayedManualRefreshRunnable);
		this.handler.removeCallbacks(this.delayedDragStartRunnable);
		this.handler.removeCallbacks(this.delayedDragStopRunnable);

		this.handler.postDelayed(this.delayedPowerOffRunnable, this.advancedSettings.screenPowerOffDelay);
	}

	private void onManualRefreshBroadcast()
	{
		this.handler.postDelayed(this.delayedManualRefreshRunnable, this.advancedSettings.navbarManualFullRefreshDelay);
	}

	//

	private int dragStartX;
	private int dragStartY;
	private boolean dragMoving;

	private void onFirstPointerDown(int x, int y)
	{
		this.handler.removeCallbacks(this.delayedDragStopRunnable);
		if (!this.isDragging)
		{
			this.dragStartX = x;
			this.dragStartY = y;
			this.dragMoving = false;
			if (this.advancedSettings.dragStartDelayStatic != 0)
			{
				this.handler.postDelayed(this.delayedDragStartRunnable, this.advancedSettings.dragStartDelayStatic);
			}
		}
	}

	private void onFirstPointerMove(int x, int y)
	{
		if (!this.isDragging)
		{
			if (!this.dragMoving)
			{
				int dx = x - this.dragStartX;
				int dy = y - this.dragStartY;
				int d = this.advancedSettings.dragStartDistance;
				if (dx * dx + dy * dy >= d * d)
				{
					this.dragMoving = true;
					this.handler.postDelayed(this.delayedDragStartRunnable, this.advancedSettings.dragStartDelayMoving);
				}
			}
		}
	}

	private void onSecondPointerDown()
	{
		if (!this.isDragging)
		{
			this.dragStart();
		}
	}

	private void onLastPointerUp()
	{
		this.handler.removeCallbacks(this.delayedDragStartRunnable);
		if (this.isDragging)
		{
			this.handler.postDelayed(this.delayedDragStopRunnable, this.advancedSettings.dragEndDelay);
		}
	}

	//

	private long functionButtonLastPressTime = System.nanoTime() / 1000000;
	private static final long FUNCTION_BUTTON_DEBOUNCE_COOLDOWN_TIME = 500;

	private void onFunctionButtonPressed()
	{
		long time = System.nanoTime() / 1000000;
		if (time >= this.functionButtonLastPressTime + FUNCTION_BUTTON_DEBOUNCE_COOLDOWN_TIME)
		{
			this.functionButtonLastPressTime = time;
			this.doManualRefresh();
		}
	}

	private void onFunctionButtonReleased()
	{
		// empty
	}
}