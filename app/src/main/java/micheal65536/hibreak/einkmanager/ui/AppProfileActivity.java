package micheal65536.hibreak.einkmanager.ui;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Checkable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

import micheal65536.hibreak.einkmanager.R;
import micheal65536.hibreak.einkmanager.data.AppProfile;
import micheal65536.hibreak.einkmanager.data.ColorMap;
import micheal65536.hibreak.einkmanager.data.ProfilesDao;
import micheal65536.hibreak.einkmanager.data.ProfilesDatabase;
import micheal65536.hibreak.einkmanager.data.RefreshMode;

public final class AppProfileActivity extends AppCompatActivity
{
	private String appId;

	private ProfilesDao dao;
	private LiveData<AppProfile> appProfile;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_app_profile);

		this.appId = this.getIntent().getStringExtra("appId");
		if (this.appId == null)
		{
			this.finish();
			return;
		}

		this.dao = ProfilesDatabase.getInstance(this).dao();
		this.appProfile = Transformations.map(this.dao.getAppProfile(this.appId), appProfile -> appProfile != null ? appProfile : AppProfile.newDefault(this.appId));

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
		((MaterialToolbar) this.findViewById(R.id.toolbar)).setTitle(name);
		((MaterialToolbar) this.findViewById(R.id.toolbar)).setNavigationIcon(R.drawable.ic_back);
		((MaterialToolbar) this.findViewById(R.id.toolbar)).setNavigationOnClickListener(view ->
		{
			if (this.getIntent().getBooleanExtra("fromQuickSettings", false))
			{
				Intent intent = new Intent(this, MainActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				this.startActivity(intent);
			}
			this.finish();
		});
		((MaterialToolbar) this.findViewById(R.id.toolbar)).getMenu().findItem(R.id.delete).setOnMenuItemClickListener(item ->
		{
			this.dao.deleteAppProfile(this.appId);
			this.finish();
			return true;
		});

		this.appProfile.observe(this, appProfile ->
		{
			((Checkable) this.findViewById(R.id.enabled)).setChecked(appProfile.enabled);

			((Checkable) this.findViewById(R.id.refresh_mode_default)).setChecked(appProfile.refreshMode == null);
			((Checkable) this.findViewById(R.id.refresh_mode_normal)).setChecked(appProfile.refreshMode == RefreshMode.NORMAL);
			((Checkable) this.findViewById(R.id.refresh_mode_fast)).setChecked(appProfile.refreshMode == RefreshMode.FAST);
			((Checkable) this.findViewById(R.id.refresh_mode_high_quality)).setChecked(appProfile.refreshMode == RefreshMode.HIGH_QUALITY);

			((Checkable) this.findViewById(R.id.enable_drag_mode_switch_default)).setChecked(appProfile.enableDragModeSwitch == null);
			((Checkable) this.findViewById(R.id.enable_drag_mode_switch_off)).setChecked(appProfile.enableDragModeSwitch == Boolean.FALSE);
			((Checkable) this.findViewById(R.id.enable_drag_mode_switch_on)).setChecked(appProfile.enableDragModeSwitch == Boolean.TRUE);
			((Checkable) this.findViewById(R.id.enable_ime_mode_switch_default)).setChecked(appProfile.enableImeModeSwitch == null);
			((Checkable) this.findViewById(R.id.enable_ime_mode_switch_off)).setChecked(appProfile.enableImeModeSwitch == Boolean.FALSE);
			((Checkable) this.findViewById(R.id.enable_ime_mode_switch_on)).setChecked(appProfile.enableImeModeSwitch == Boolean.TRUE);

			((Checkable) this.findViewById(R.id.disable_global_contrast_map)).setChecked(appProfile.disableGlobalContrastMap);
			((Checkable) this.findViewById(R.id.use_color_map)).setChecked(appProfile.useColorMap);
			NumericInput.setValue(this.findViewById(R.id.color_map_red_min), appProfile.colorMap.redMin);
			NumericInput.setValue(this.findViewById(R.id.color_map_red_max), appProfile.colorMap.redMax);
			NumericInput.setValue(this.findViewById(R.id.color_map_green_min), appProfile.colorMap.greenMin);
			NumericInput.setValue(this.findViewById(R.id.color_map_green_max), appProfile.colorMap.greenMax);
			NumericInput.setValue(this.findViewById(R.id.color_map_blue_min), appProfile.colorMap.blueMin);
			NumericInput.setValue(this.findViewById(R.id.color_map_blue_max), appProfile.colorMap.blueMax);
		});

		((SwitchMaterial) this.findViewById(R.id.enabled)).setOnCheckedChangeListener((view1, checked) ->
		{
			AppProfile appProfile = this.getCurrentAppProfile();
			if (appProfile.enabled != checked)
			{
				this.dao.saveAppProfile(appProfile.withEnabled(checked));
			}
		});

		((MaterialButton) this.findViewById(R.id.refresh_mode_default)).addOnCheckedChangeListener((view1, checked) ->
		{
			if (checked)
			{
				((Checkable) this.findViewById(R.id.refresh_mode_normal)).setChecked(false);
				((Checkable) this.findViewById(R.id.refresh_mode_fast)).setChecked(false);
				((Checkable) this.findViewById(R.id.refresh_mode_high_quality)).setChecked(false);

				AppProfile appProfile = this.getCurrentAppProfile();
				if (appProfile.refreshMode != null)
				{
					this.dao.saveAppProfile(appProfile.withRefreshMode(null, appProfile.enableDragModeSwitch, appProfile.enableImeModeSwitch));
				}
			}
		});
		((MaterialButton) this.findViewById(R.id.refresh_mode_normal)).addOnCheckedChangeListener((view1, checked) ->
		{
			if (checked)
			{
				((Checkable) this.findViewById(R.id.refresh_mode_default)).setChecked(false);
				((Checkable) this.findViewById(R.id.refresh_mode_fast)).setChecked(false);
				((Checkable) this.findViewById(R.id.refresh_mode_high_quality)).setChecked(false);

				AppProfile appProfile = this.getCurrentAppProfile();
				if (appProfile.refreshMode != RefreshMode.NORMAL)
				{
					this.dao.saveAppProfile(appProfile.withRefreshMode(RefreshMode.NORMAL, appProfile.enableDragModeSwitch, appProfile.enableImeModeSwitch));
				}
			}
		});
		((MaterialButton) this.findViewById(R.id.refresh_mode_fast)).addOnCheckedChangeListener((view1, checked) ->
		{
			if (checked)
			{
				((Checkable) this.findViewById(R.id.refresh_mode_default)).setChecked(false);
				((Checkable) this.findViewById(R.id.refresh_mode_normal)).setChecked(false);
				((Checkable) this.findViewById(R.id.refresh_mode_high_quality)).setChecked(false);

				AppProfile appProfile = this.getCurrentAppProfile();
				if (appProfile.refreshMode != RefreshMode.FAST)
				{
					this.dao.saveAppProfile(appProfile.withRefreshMode(RefreshMode.FAST, appProfile.enableDragModeSwitch, appProfile.enableImeModeSwitch));
				}
			}
		});
		((MaterialButton) this.findViewById(R.id.refresh_mode_high_quality)).addOnCheckedChangeListener((view1, checked) ->
		{
			if (checked)
			{
				((Checkable) this.findViewById(R.id.refresh_mode_default)).setChecked(false);
				((Checkable) this.findViewById(R.id.refresh_mode_normal)).setChecked(false);
				((Checkable) this.findViewById(R.id.refresh_mode_fast)).setChecked(false);

				AppProfile appProfile = this.getCurrentAppProfile();
				if (appProfile.refreshMode != RefreshMode.HIGH_QUALITY)
				{
					this.dao.saveAppProfile(appProfile.withRefreshMode(RefreshMode.HIGH_QUALITY, appProfile.enableDragModeSwitch, appProfile.enableImeModeSwitch));
				}
			}
		});

		((MaterialButton) this.findViewById(R.id.enable_drag_mode_switch_default)).addOnCheckedChangeListener((view1, checked) ->
		{
			if (checked)
			{
				((Checkable) this.findViewById(R.id.enable_drag_mode_switch_off)).setChecked(false);
				((Checkable) this.findViewById(R.id.enable_drag_mode_switch_on)).setChecked(false);

				AppProfile appProfile = this.getCurrentAppProfile();
				if (appProfile.enableDragModeSwitch != null)
				{
					this.dao.saveAppProfile(appProfile.withRefreshMode(appProfile.refreshMode, null, appProfile.enableImeModeSwitch));
				}
			}
		});
		((MaterialButton) this.findViewById(R.id.enable_drag_mode_switch_off)).addOnCheckedChangeListener((view1, checked) ->
		{
			if (checked)
			{
				((Checkable) this.findViewById(R.id.enable_drag_mode_switch_default)).setChecked(false);
				((Checkable) this.findViewById(R.id.enable_drag_mode_switch_on)).setChecked(false);

				AppProfile appProfile = this.getCurrentAppProfile();
				if (appProfile.enableDragModeSwitch != Boolean.FALSE)
				{
					this.dao.saveAppProfile(appProfile.withRefreshMode(appProfile.refreshMode, Boolean.FALSE, appProfile.enableImeModeSwitch));
				}
			}
		});
		((MaterialButton) this.findViewById(R.id.enable_drag_mode_switch_on)).addOnCheckedChangeListener((view1, checked) ->
		{
			if (checked)
			{
				((Checkable) this.findViewById(R.id.enable_drag_mode_switch_default)).setChecked(false);
				((Checkable) this.findViewById(R.id.enable_drag_mode_switch_off)).setChecked(false);

				AppProfile appProfile = this.getCurrentAppProfile();
				if (appProfile.enableDragModeSwitch != Boolean.TRUE)
				{
					this.dao.saveAppProfile(appProfile.withRefreshMode(appProfile.refreshMode, Boolean.TRUE, appProfile.enableImeModeSwitch));
				}
			}
		});

		((MaterialButton) this.findViewById(R.id.enable_ime_mode_switch_default)).addOnCheckedChangeListener((view1, checked) ->
		{
			if (checked)
			{
				((Checkable) this.findViewById(R.id.enable_ime_mode_switch_off)).setChecked(false);
				((Checkable) this.findViewById(R.id.enable_ime_mode_switch_on)).setChecked(false);

				AppProfile appProfile = this.getCurrentAppProfile();
				if (appProfile.enableImeModeSwitch != null)
				{
					this.dao.saveAppProfile(appProfile.withRefreshMode(appProfile.refreshMode, appProfile.enableDragModeSwitch, null));
				}
			}
		});
		((MaterialButton) this.findViewById(R.id.enable_ime_mode_switch_off)).addOnCheckedChangeListener((view1, checked) ->
		{
			if (checked)
			{
				((Checkable) this.findViewById(R.id.enable_ime_mode_switch_default)).setChecked(false);
				((Checkable) this.findViewById(R.id.enable_ime_mode_switch_on)).setChecked(false);

				AppProfile appProfile = this.getCurrentAppProfile();
				if (appProfile.enableImeModeSwitch != Boolean.FALSE)
				{
					this.dao.saveAppProfile(appProfile.withRefreshMode(appProfile.refreshMode, appProfile.enableDragModeSwitch, Boolean.FALSE));
				}
			}
		});
		((MaterialButton) this.findViewById(R.id.enable_ime_mode_switch_on)).addOnCheckedChangeListener((view1, checked) ->
		{
			if (checked)
			{
				((Checkable) this.findViewById(R.id.enable_ime_mode_switch_default)).setChecked(false);
				((Checkable) this.findViewById(R.id.enable_ime_mode_switch_off)).setChecked(false);

				AppProfile appProfile = this.getCurrentAppProfile();
				if (appProfile.enableImeModeSwitch != Boolean.TRUE)
				{
					this.dao.saveAppProfile(appProfile.withRefreshMode(appProfile.refreshMode, appProfile.enableDragModeSwitch, Boolean.TRUE));
				}
			}
		});

		((SwitchMaterial) this.findViewById(R.id.disable_global_contrast_map)).setOnCheckedChangeListener((view1, checked) ->
		{
			AppProfile appProfile = this.getCurrentAppProfile();
			if (appProfile.disableGlobalContrastMap != checked)
			{
				this.dao.saveAppProfile(appProfile.withColorMap(checked, appProfile.useColorMap, appProfile.colorMap));
			}
		});
		((SwitchMaterial) this.findViewById(R.id.use_color_map)).setOnCheckedChangeListener((view1, checked) ->
		{
			AppProfile appProfile = this.getCurrentAppProfile();
			if (appProfile.useColorMap != checked)
			{
				this.dao.saveAppProfile(appProfile.withColorMap(appProfile.disableGlobalContrastMap, checked, appProfile.colorMap));
			}
		});
		NumericInput.setUpCallbacks(this.findViewById(R.id.color_map_red_min), 0, 100, 10, value ->
		{
			AppProfile appProfile = this.getCurrentAppProfile();
			if (appProfile.colorMap.redMin != value)
			{
				this.dao.saveAppProfile(appProfile.withColorMap(appProfile.disableGlobalContrastMap, appProfile.useColorMap, new ColorMap(value, appProfile.colorMap.redMax, appProfile.colorMap.greenMin, appProfile.colorMap.greenMax, appProfile.colorMap.blueMin, appProfile.colorMap.blueMax)));
			}
		});
		NumericInput.setUpCallbacks(this.findViewById(R.id.color_map_red_max), 0, 100, 10, value ->
		{
			AppProfile appProfile = this.getCurrentAppProfile();
			if (appProfile.colorMap.redMax != value)
			{
				this.dao.saveAppProfile(appProfile.withColorMap(appProfile.disableGlobalContrastMap, appProfile.useColorMap, new ColorMap(appProfile.colorMap.redMin, value, appProfile.colorMap.greenMin, appProfile.colorMap.greenMax, appProfile.colorMap.blueMin, appProfile.colorMap.blueMax)));
			}
		});
		NumericInput.setUpCallbacks(this.findViewById(R.id.color_map_green_min), 0, 100, 10, value ->
		{
			AppProfile appProfile = this.getCurrentAppProfile();
			if (appProfile.colorMap.greenMin != value)
			{
				this.dao.saveAppProfile(appProfile.withColorMap(appProfile.disableGlobalContrastMap, appProfile.useColorMap, new ColorMap(appProfile.colorMap.redMin, appProfile.colorMap.redMax, value, appProfile.colorMap.greenMax, appProfile.colorMap.blueMin, appProfile.colorMap.blueMax)));
			}
		});
		NumericInput.setUpCallbacks(this.findViewById(R.id.color_map_green_max), 0, 100, 10, value ->
		{
			AppProfile appProfile = this.getCurrentAppProfile();
			if (appProfile.colorMap.greenMax != value)
			{
				this.dao.saveAppProfile(appProfile.withColorMap(appProfile.disableGlobalContrastMap, appProfile.useColorMap, new ColorMap(appProfile.colorMap.redMin, appProfile.colorMap.redMax, appProfile.colorMap.greenMin, value, appProfile.colorMap.blueMin, appProfile.colorMap.blueMax)));
			}
		});
		NumericInput.setUpCallbacks(this.findViewById(R.id.color_map_blue_min), 0, 100, 10, value ->
		{
			AppProfile appProfile = this.getCurrentAppProfile();
			if (appProfile.colorMap.greenMin != value)
			{
				this.dao.saveAppProfile(appProfile.withColorMap(appProfile.disableGlobalContrastMap, appProfile.useColorMap, new ColorMap(appProfile.colorMap.redMin, appProfile.colorMap.redMax, appProfile.colorMap.greenMin, appProfile.colorMap.greenMax, value, appProfile.colorMap.blueMax)));
			}
		});
		NumericInput.setUpCallbacks(this.findViewById(R.id.color_map_blue_max), 0, 100, 10, value ->
		{
			AppProfile appProfile = this.getCurrentAppProfile();
			if (appProfile.colorMap.greenMax != value)
			{
				this.dao.saveAppProfile(appProfile.withColorMap(appProfile.disableGlobalContrastMap, appProfile.useColorMap, new ColorMap(appProfile.colorMap.redMin, appProfile.colorMap.redMax, appProfile.colorMap.greenMin, appProfile.colorMap.greenMax, appProfile.colorMap.blueMin, value)));
			}
		});
	}

	@NonNull
	private AppProfile getCurrentAppProfile()
	{
		AppProfile appProfile = this.appProfile.getValue();
		return appProfile != null ? appProfile : AppProfile.newDefault(this.appId);
	}
}