package micheal65536.hibreak.einkmanager.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Checkable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

import micheal65536.hibreak.einkmanager.EInkAccessibilityService;
import micheal65536.hibreak.einkmanager.R;
import micheal65536.hibreak.einkmanager.data.AppProfile;
import micheal65536.hibreak.einkmanager.data.FullRefreshType;
import micheal65536.hibreak.einkmanager.data.GlobalSettings;
import micheal65536.hibreak.einkmanager.data.GlobalSettingsWithForcedColorMap;
import micheal65536.hibreak.einkmanager.data.ProfilesDao;
import micheal65536.hibreak.einkmanager.data.ProfilesDatabase;
import micheal65536.hibreak.einkmanager.data.RefreshMode;

public final class QuickSettingsDialogActivity extends AppCompatActivity
{
	@Nullable
	private String appId;

	private ProfilesDao dao;
	private LiveData<GlobalSettingsWithForcedColorMap> globalSettings;
	private LiveData<AppProfile> appProfile;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_quick_settings_dialog);

		this.appId = EInkAccessibilityService.isRunning() ? EInkAccessibilityService.getLastAppIdForQuickSettings() : null;

		this.dao = ProfilesDatabase.getInstance(this).dao();
		this.globalSettings = Transformations.map(this.dao.getGlobalSettingsWithForcedColorMap(), globalSettings -> globalSettings != null ? globalSettings : new GlobalSettingsWithForcedColorMap(GlobalSettings.DEFAULT, null));
		this.appProfile = this.appId != null ? Transformations.map(this.dao.getAppProfile(this.appId), appProfile -> appProfile != null ? appProfile : AppProfile.newDefault(this.appId).withEnabled(false)) : new MutableLiveData<>(null);

		if (this.appId != null)
		{
			PackageManager packageManager = this.getPackageManager();
			String name;
			try
			{
				name = packageManager.getApplicationLabel(packageManager.getApplicationInfo(this.appId, 0)).toString();
			}
			catch (PackageManager.NameNotFoundException exception)
			{
				name = this.appId;
			}
			((SwitchMaterial) this.findViewById(R.id.enable_app_profile)).setText(this.getString(R.string.quick_settings_enable_app_profile_with_name, name));
		}

		this.globalSettings.observe(this, globalSettings ->
		{
			((Checkable) this.findViewById(R.id.force_fast_mode)).setChecked(globalSettings.globalSettings.forceFastMode);
			((Checkable) this.findViewById(R.id.forced_color_map_enable)).setChecked(globalSettings.globalSettings.forcedColorMapId != null);
			if (globalSettings.forcedColorMap != null)
			{
				((SwitchMaterial) this.findViewById(R.id.forced_color_map_enable)).setText(this.getString(R.string.forced_color_map_enable_with_name, globalSettings.forcedColorMap.name));
			}
			else
			{
				((SwitchMaterial) this.findViewById(R.id.forced_color_map_enable)).setText(R.string.forced_color_map_enable);
			}

			((Checkable) this.findViewById(R.id.global_refresh_mode_normal)).setChecked(globalSettings.globalSettings.defaultRefreshMode == RefreshMode.NORMAL);
			((Checkable) this.findViewById(R.id.global_refresh_mode_fast)).setChecked(globalSettings.globalSettings.defaultRefreshMode == RefreshMode.FAST);
			((Checkable) this.findViewById(R.id.global_refresh_mode_high_quality)).setChecked(globalSettings.globalSettings.defaultRefreshMode == RefreshMode.HIGH_QUALITY);
			((Checkable) this.findViewById(R.id.enable_drag_mode_switch)).setChecked(globalSettings.globalSettings.enableDragModeSwitch);
			((Checkable) this.findViewById(R.id.enable_ime_mode_switch)).setChecked(globalSettings.globalSettings.enableImeModeSwitch);
			((Checkable) this.findViewById(R.id.manual_full_refresh_type_blank)).setChecked(globalSettings.globalSettings.manualFullRefreshType == FullRefreshType.BLANK);
			((Checkable) this.findViewById(R.id.manual_full_refresh_type_invert)).setChecked(globalSettings.globalSettings.manualFullRefreshType == FullRefreshType.INVERT);
		});
		this.appProfile.observe(this, appProfile ->
		{
			((Checkable) this.findViewById(R.id.enable_app_profile)).setChecked(appProfile != null && appProfile.enabled);
			this.findViewById(R.id.enable_app_profile).setEnabled(appProfile != null);

			((Checkable) this.findViewById(R.id.app_profile_refresh_mode_default)).setChecked(appProfile != null && appProfile.refreshMode == null);
			((Checkable) this.findViewById(R.id.app_profile_refresh_mode_normal)).setChecked(appProfile != null && appProfile.refreshMode == RefreshMode.NORMAL);
			((Checkable) this.findViewById(R.id.app_profile_refresh_mode_fast)).setChecked(appProfile != null && appProfile.refreshMode == RefreshMode.FAST);
			((Checkable) this.findViewById(R.id.app_profile_refresh_mode_high_quality)).setChecked(appProfile != null && appProfile.refreshMode == RefreshMode.HIGH_QUALITY);
			this.findViewById(R.id.app_profile_refresh_mode_default).setEnabled(appProfile != null);
			this.findViewById(R.id.app_profile_refresh_mode_normal).setEnabled(appProfile != null);
			this.findViewById(R.id.app_profile_refresh_mode_fast).setEnabled(appProfile != null);
			this.findViewById(R.id.app_profile_refresh_mode_high_quality).setEnabled(appProfile != null);

			this.findViewById(R.id.global_refresh_mode_normal).setEnabled(appProfile == null || !appProfile.enabled || appProfile.refreshMode == null);
			this.findViewById(R.id.global_refresh_mode_fast).setEnabled(appProfile == null || !appProfile.enabled || appProfile.refreshMode == null);
			this.findViewById(R.id.global_refresh_mode_high_quality).setEnabled(appProfile == null || !appProfile.enabled || appProfile.refreshMode == null);
			this.findViewById(R.id.enable_drag_mode_switch).setEnabled(appProfile == null || !appProfile.enabled || appProfile.enableDragModeSwitch == null);
			this.findViewById(R.id.enable_ime_mode_switch).setEnabled(appProfile == null || !appProfile.enabled || appProfile.enableImeModeSwitch == null);
		});

		((SwitchMaterial) this.findViewById(R.id.force_fast_mode)).setOnCheckedChangeListener((view1, checked) ->
		{
			GlobalSettings globalSettings = this.getCurrentGlobalSettings();
			if (globalSettings.forceFastMode != checked)
			{
				this.dao.saveGlobalSettings(globalSettings.withForcedMode(globalSettings.forcedColorMapId, checked));
			}
		});
		((SwitchMaterial) this.findViewById(R.id.forced_color_map_enable)).setOnCheckedChangeListener((view1, checked) ->
		{
			GlobalSettings globalSettings = this.getCurrentGlobalSettings();
			if (checked && globalSettings.forcedColorMapId == null)
			{
				Intent intent = new Intent(this, ColorMapChooserDialogActivity.class);
				this.startActivityForResult(intent, 0);
				((SwitchMaterial) this.findViewById(R.id.forced_color_map_enable)).setChecked(false);
			}
			else if (!checked && globalSettings.forcedColorMapId != null)
			{
				this.dao.saveGlobalSettings(globalSettings.withForcedMode(null, globalSettings.forceFastMode));
			}
		});

		((MaterialButton) this.findViewById(R.id.global_refresh_mode_normal)).addOnCheckedChangeListener((view1, checked) ->
		{
			if (checked)
			{
				((Checkable) this.findViewById(R.id.global_refresh_mode_fast)).setChecked(false);
				((Checkable) this.findViewById(R.id.global_refresh_mode_high_quality)).setChecked(false);

				GlobalSettings globalSettings = this.getCurrentGlobalSettings();
				if (globalSettings.defaultRefreshMode != RefreshMode.NORMAL)
				{
					this.dao.saveGlobalSettings(globalSettings.withDefaultRefreshMode(RefreshMode.NORMAL));
				}
			}
		});
		((MaterialButton) this.findViewById(R.id.global_refresh_mode_fast)).addOnCheckedChangeListener((view1, checked) ->
		{
			if (checked)
			{
				((Checkable) this.findViewById(R.id.global_refresh_mode_normal)).setChecked(false);
				((Checkable) this.findViewById(R.id.global_refresh_mode_high_quality)).setChecked(false);

				GlobalSettings globalSettings = this.getCurrentGlobalSettings();
				if (globalSettings.defaultRefreshMode != RefreshMode.FAST)
				{
					this.dao.saveGlobalSettings(globalSettings.withDefaultRefreshMode(RefreshMode.FAST));
				}
			}
		});
		((MaterialButton) this.findViewById(R.id.global_refresh_mode_high_quality)).addOnCheckedChangeListener((view1, checked) ->
		{
			if (checked)
			{
				((Checkable) this.findViewById(R.id.global_refresh_mode_normal)).setChecked(false);
				((Checkable) this.findViewById(R.id.global_refresh_mode_fast)).setChecked(false);

				GlobalSettings globalSettings = this.getCurrentGlobalSettings();
				if (globalSettings.defaultRefreshMode != RefreshMode.HIGH_QUALITY)
				{
					this.dao.saveGlobalSettings(globalSettings.withDefaultRefreshMode(RefreshMode.HIGH_QUALITY));
				}
			}
		});
		((SwitchMaterial) this.findViewById(R.id.enable_drag_mode_switch)).setOnCheckedChangeListener((view1, checked) ->
		{
			GlobalSettings globalSettings = this.getCurrentGlobalSettings();
			if (globalSettings.enableDragModeSwitch != checked)
			{
				this.dao.saveGlobalSettings(globalSettings.withModeSwitching(checked, globalSettings.enableImeModeSwitch, globalSettings.allowDragModeDuringIme));
			}
		});
		((SwitchMaterial) this.findViewById(R.id.enable_ime_mode_switch)).setOnCheckedChangeListener((view1, checked) ->
		{
			GlobalSettings globalSettings = this.getCurrentGlobalSettings();
			if (globalSettings.enableImeModeSwitch != checked)
			{
				this.dao.saveGlobalSettings(globalSettings.withModeSwitching(globalSettings.enableDragModeSwitch, checked, globalSettings.allowDragModeDuringIme));
			}
		});
		((MaterialButton) this.findViewById(R.id.manual_full_refresh_type_blank)).addOnCheckedChangeListener((view1, checked) ->
		{
			if (checked)
			{
				((Checkable) this.findViewById(R.id.manual_full_refresh_type_invert)).setChecked(false);

				GlobalSettings globalSettings = this.getCurrentGlobalSettings();
				if (globalSettings.manualFullRefreshType != FullRefreshType.BLANK)
				{
					this.dao.saveGlobalSettings(globalSettings.withFullRefreshSettings(FullRefreshType.BLANK));
				}
			}
		});
		((MaterialButton) this.findViewById(R.id.manual_full_refresh_type_invert)).addOnCheckedChangeListener((view1, checked) ->
		{
			if (checked)
			{
				((Checkable) this.findViewById(R.id.manual_full_refresh_type_blank)).setChecked(false);

				GlobalSettings globalSettings = this.getCurrentGlobalSettings();
				if (globalSettings.manualFullRefreshType != FullRefreshType.INVERT)
				{
					this.dao.saveGlobalSettings(globalSettings.withFullRefreshSettings(FullRefreshType.INVERT));
				}
			}
		});

		((SwitchMaterial) this.findViewById(R.id.enable_app_profile)).setOnCheckedChangeListener((view1, checked) ->
		{
			AppProfile appProfile = this.getCurrentAppProfile();
			if (appProfile != null && appProfile.enabled != checked)
			{
				this.dao.saveAppProfile(appProfile.withEnabled(checked));
			}
		});
		((MaterialButton) this.findViewById(R.id.app_profile_refresh_mode_default)).addOnCheckedChangeListener((view1, checked) ->
		{
			if (checked)
			{
				((Checkable) this.findViewById(R.id.app_profile_refresh_mode_normal)).setChecked(false);
				((Checkable) this.findViewById(R.id.app_profile_refresh_mode_fast)).setChecked(false);
				((Checkable) this.findViewById(R.id.app_profile_refresh_mode_high_quality)).setChecked(false);

				AppProfile appProfile = this.getCurrentAppProfile();
				if (appProfile != null && appProfile.refreshMode != null)
				{
					this.dao.saveAppProfile(appProfile.withRefreshMode(null, appProfile.enableDragModeSwitch, appProfile.enableImeModeSwitch));
				}
			}
		});
		((MaterialButton) this.findViewById(R.id.app_profile_refresh_mode_normal)).addOnCheckedChangeListener((view1, checked) ->
		{
			if (checked)
			{
				((Checkable) this.findViewById(R.id.app_profile_refresh_mode_default)).setChecked(false);
				((Checkable) this.findViewById(R.id.app_profile_refresh_mode_fast)).setChecked(false);
				((Checkable) this.findViewById(R.id.app_profile_refresh_mode_high_quality)).setChecked(false);

				AppProfile appProfile = this.getCurrentAppProfile();
				if (appProfile != null && appProfile.refreshMode != RefreshMode.NORMAL)
				{
					this.dao.saveAppProfile(appProfile.withRefreshMode(RefreshMode.NORMAL, appProfile.enableDragModeSwitch, appProfile.enableImeModeSwitch));
				}
			}
		});
		((MaterialButton) this.findViewById(R.id.app_profile_refresh_mode_fast)).addOnCheckedChangeListener((view1, checked) ->
		{
			if (checked)
			{
				((Checkable) this.findViewById(R.id.app_profile_refresh_mode_default)).setChecked(false);
				((Checkable) this.findViewById(R.id.app_profile_refresh_mode_normal)).setChecked(false);
				((Checkable) this.findViewById(R.id.app_profile_refresh_mode_high_quality)).setChecked(false);

				AppProfile appProfile = this.getCurrentAppProfile();
				if (appProfile != null && appProfile.refreshMode != RefreshMode.FAST)
				{
					this.dao.saveAppProfile(appProfile.withRefreshMode(RefreshMode.FAST, appProfile.enableDragModeSwitch, appProfile.enableImeModeSwitch));
				}
			}
		});
		((MaterialButton) this.findViewById(R.id.app_profile_refresh_mode_high_quality)).addOnCheckedChangeListener((view1, checked) ->
		{
			if (checked)
			{
				((Checkable) this.findViewById(R.id.app_profile_refresh_mode_default)).setChecked(false);
				((Checkable) this.findViewById(R.id.app_profile_refresh_mode_normal)).setChecked(false);
				((Checkable) this.findViewById(R.id.app_profile_refresh_mode_fast)).setChecked(false);

				AppProfile appProfile = this.getCurrentAppProfile();
				if (appProfile != null && appProfile.refreshMode != RefreshMode.HIGH_QUALITY)
				{
					this.dao.saveAppProfile(appProfile.withRefreshMode(RefreshMode.HIGH_QUALITY, appProfile.enableDragModeSwitch, appProfile.enableImeModeSwitch));
				}
			}
		});

		((Button) this.findViewById(R.id.more_settings)).setOnClickListener(view1 ->
		{
			AppProfile appProfile = this.getCurrentAppProfile();
			if (appProfile != null && appProfile.enabled)
			{
				Intent intent = new Intent(this, AppProfileActivity.class);
				intent.putExtra("appId", appProfile.appId);
				intent.putExtra("fromQuickSettings", true);
				intent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
				this.startActivity(intent);
				this.finish();
			}
			else
			{
				Intent intent = new Intent(this, MainActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
				this.startActivity(intent);
				this.finish();
			}
		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == 0 && resultCode == Activity.RESULT_OK)
		{
			long id = data.getLongExtra("id", 0);
			if (id != 0)
			{
				GlobalSettings globalSettings = this.getCurrentGlobalSettings();
				if (globalSettings.forcedColorMapId == null || globalSettings.forcedColorMapId != id)
				{
					this.dao.saveGlobalSettings(globalSettings.withForcedMode(id, globalSettings.forceFastMode));
				}
			}
		}
	}

	@NonNull
	private GlobalSettings getCurrentGlobalSettings()
	{
		GlobalSettingsWithForcedColorMap globalSettings = this.globalSettings.getValue();
		return globalSettings != null ? globalSettings.globalSettings : GlobalSettings.DEFAULT;
	}

	@Nullable
	private AppProfile getCurrentAppProfile()
	{
		if (this.appId != null)
		{
			AppProfile appProfile = this.appProfile.getValue();
			return appProfile != null ? appProfile : AppProfile.newDefault(this.appId).withEnabled(false);
		}
		else
		{
			return null;
		}
	}
}