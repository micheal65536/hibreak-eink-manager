package micheal65536.hibreak.einkmanager.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Checkable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

import micheal65536.hibreak.einkmanager.ApplySettingsService;
import micheal65536.hibreak.einkmanager.EInkAccessibilityService;
import micheal65536.hibreak.einkmanager.R;
import micheal65536.hibreak.einkmanager.data.ContrastMap;
import micheal65536.hibreak.einkmanager.data.FullRefreshType;
import micheal65536.hibreak.einkmanager.data.GlobalSettings;
import micheal65536.hibreak.einkmanager.data.GlobalSettingsWithForcedColorMap;
import micheal65536.hibreak.einkmanager.data.ProfilesDao;
import micheal65536.hibreak.einkmanager.data.ProfilesDatabase;
import micheal65536.hibreak.einkmanager.data.RefreshMode;

public final class GlobalSettingsFragment extends Fragment
{
	private ProfilesDao dao;
	private LiveData<GlobalSettingsWithForcedColorMap> globalSettings;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		this.dao = ProfilesDatabase.getInstance(this.requireContext()).dao();

		this.globalSettings = Transformations.map(this.dao.getGlobalSettingsWithForcedColorMap(), globalSettings -> globalSettings != null ? globalSettings : new GlobalSettingsWithForcedColorMap(GlobalSettings.DEFAULT, null));
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_global_settings, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
	{
		this.globalSettings.observe(this.getViewLifecycleOwner(), globalSettingsWithForcedColorMap ->
		{
			GlobalSettings globalSettings = globalSettingsWithForcedColorMap.globalSettings;

			((Checkable) view.findViewById(R.id.refresh_mode_normal)).setChecked(globalSettings.defaultRefreshMode == RefreshMode.NORMAL);
			((Checkable) view.findViewById(R.id.refresh_mode_fast)).setChecked(globalSettings.defaultRefreshMode == RefreshMode.FAST);
			((Checkable) view.findViewById(R.id.refresh_mode_high_quality)).setChecked(globalSettings.defaultRefreshMode == RefreshMode.HIGH_QUALITY);

			((Checkable) view.findViewById(R.id.enable_drag_mode_switch)).setChecked(globalSettings.enableDragModeSwitch);
			((Checkable) view.findViewById(R.id.enable_ime_mode_switch)).setChecked(globalSettings.enableImeModeSwitch);
			((Checkable) view.findViewById(R.id.allow_drag_mode_during_ime)).setChecked(globalSettings.allowDragModeDuringIme);
			view.findViewById(R.id.allow_drag_mode_during_ime).setEnabled(globalSettings.enableDragModeSwitch && !globalSettings.enableImeModeSwitch);

			((Checkable) view.findViewById(R.id.use_last_app_mode_in_system_ui)).setChecked(globalSettings.useLastAppModeInSystemUI);
			((Checkable) view.findViewById(R.id.manual_full_refresh_type_blank)).setChecked(globalSettings.manualFullRefreshType == FullRefreshType.BLANK);
			((Checkable) view.findViewById(R.id.manual_full_refresh_type_invert)).setChecked(globalSettings.manualFullRefreshType == FullRefreshType.INVERT);

			((Checkable) view.findViewById(R.id.use_global_contrast_map)).setChecked(globalSettings.useGlobalContrastMap);
			((Checkable) view.findViewById(R.id.use_global_contrast_map_in_high_quality_mode)).setChecked(globalSettings.useGlobalContrastMapInHighQualityMode);
			view.findViewById(R.id.use_global_contrast_map_in_high_quality_mode).setEnabled(globalSettings.useGlobalContrastMap);
			NumericInput.setValue(view.findViewById(R.id.global_contrast_map_min), globalSettings.globalContrastMap.min);
			NumericInput.setValue(view.findViewById(R.id.global_contrast_map_max), globalSettings.globalContrastMap.max);

			((Checkable) view.findViewById(R.id.use_fast_mode_contrast_map)).setChecked(globalSettings.useFastModeContrastMap);
			NumericInput.setValue(view.findViewById(R.id.fast_mode_contrast_map_min), globalSettings.fastModeContrastMap.min);
			NumericInput.setValue(view.findViewById(R.id.fast_mode_contrast_map_max), globalSettings.fastModeContrastMap.max);

			((Checkable) view.findViewById(R.id.forced_color_map_enable)).setChecked(globalSettings.forcedColorMapId != null);
			if (globalSettingsWithForcedColorMap.forcedColorMap != null)
			{
				((SwitchMaterial) view.findViewById(R.id.forced_color_map_enable)).setText(this.getString(R.string.forced_color_map_enable_with_name, globalSettingsWithForcedColorMap.forcedColorMap.name));
			}
			else
			{
				((SwitchMaterial) view.findViewById(R.id.forced_color_map_enable)).setText(R.string.forced_color_map_enable);
			}
		});

		((MaterialButton) view.findViewById(R.id.refresh_mode_normal)).addOnCheckedChangeListener((view1, checked) ->
		{
			if (checked)
			{
				((Checkable) view.findViewById(R.id.refresh_mode_fast)).setChecked(false);
				((Checkable) view.findViewById(R.id.refresh_mode_high_quality)).setChecked(false);

				GlobalSettings globalSettings = this.getCurrentGlobalSettings();
				if (globalSettings.defaultRefreshMode != RefreshMode.NORMAL)
				{
					this.dao.saveGlobalSettings(globalSettings.withDefaultRefreshMode(RefreshMode.NORMAL).withForcedMode(globalSettings.forcedColorMapId, false, globalSettings.forceDisableColorMaps));
				}
			}
		});
		((MaterialButton) view.findViewById(R.id.refresh_mode_fast)).addOnCheckedChangeListener((view1, checked) ->
		{
			if (checked)
			{
				((Checkable) view.findViewById(R.id.refresh_mode_normal)).setChecked(false);
				((Checkable) view.findViewById(R.id.refresh_mode_high_quality)).setChecked(false);

				GlobalSettings globalSettings = this.getCurrentGlobalSettings();
				if (globalSettings.defaultRefreshMode != RefreshMode.FAST)
				{
					this.dao.saveGlobalSettings(globalSettings.withDefaultRefreshMode(RefreshMode.FAST).withForcedMode(globalSettings.forcedColorMapId, false, globalSettings.forceDisableColorMaps));
				}
			}
		});
		((MaterialButton) view.findViewById(R.id.refresh_mode_high_quality)).addOnCheckedChangeListener((view1, checked) ->
		{
			if (checked)
			{
				((Checkable) view.findViewById(R.id.refresh_mode_normal)).setChecked(false);
				((Checkable) view.findViewById(R.id.refresh_mode_fast)).setChecked(false);

				GlobalSettings globalSettings = this.getCurrentGlobalSettings();
				if (globalSettings.defaultRefreshMode != RefreshMode.HIGH_QUALITY)
				{
					this.dao.saveGlobalSettings(globalSettings.withDefaultRefreshMode(RefreshMode.HIGH_QUALITY).withForcedMode(globalSettings.forcedColorMapId, false, globalSettings.forceDisableColorMaps));
				}
			}
		});

		((SwitchMaterial) view.findViewById(R.id.enable_drag_mode_switch)).setOnCheckedChangeListener((view1, checked) ->
		{
			GlobalSettings globalSettings = this.getCurrentGlobalSettings();
			if (globalSettings.enableDragModeSwitch != checked)
			{
				this.dao.saveGlobalSettings(globalSettings.withModeSwitching(checked, globalSettings.enableImeModeSwitch, globalSettings.allowDragModeDuringIme).withForcedMode(globalSettings.forcedColorMapId, false, globalSettings.forceDisableColorMaps));
			}
		});
		((SwitchMaterial) view.findViewById(R.id.enable_ime_mode_switch)).setOnCheckedChangeListener((view1, checked) ->
		{
			GlobalSettings globalSettings = this.getCurrentGlobalSettings();
			if (globalSettings.enableImeModeSwitch != checked)
			{
				this.dao.saveGlobalSettings(globalSettings.withModeSwitching(globalSettings.enableDragModeSwitch, checked, globalSettings.allowDragModeDuringIme).withForcedMode(globalSettings.forcedColorMapId, false, globalSettings.forceDisableColorMaps));
			}
		});
		((SwitchMaterial) view.findViewById(R.id.allow_drag_mode_during_ime)).setOnCheckedChangeListener((view1, checked) ->
		{
			GlobalSettings globalSettings = this.getCurrentGlobalSettings();
			if (globalSettings.allowDragModeDuringIme != checked)
			{
				this.dao.saveGlobalSettings(globalSettings.withModeSwitching(globalSettings.enableDragModeSwitch, globalSettings.enableImeModeSwitch, checked));
			}
		});
		((Button) view.findViewById(R.id.drag_settings)).setOnClickListener(view1 ->
		{
			Intent intent = new Intent(this.requireContext(), DragSettingsActivity.class);
			this.requireContext().startActivity(intent);
		});

		((SwitchMaterial) view.findViewById(R.id.use_last_app_mode_in_system_ui)).setOnCheckedChangeListener((view1, checked) ->
		{
			GlobalSettings globalSettings = this.getCurrentGlobalSettings();
			if (globalSettings.useLastAppModeInSystemUI != checked)
			{
				this.dao.saveGlobalSettings(globalSettings.withUseLastAppModeInSystemUI(checked));
			}
		});
		((MaterialButton) view.findViewById(R.id.manual_full_refresh_type_blank)).addOnCheckedChangeListener((view1, checked) ->
		{
			if (checked)
			{
				((Checkable) view.findViewById(R.id.manual_full_refresh_type_invert)).setChecked(false);

				GlobalSettings globalSettings = this.getCurrentGlobalSettings();
				if (globalSettings.manualFullRefreshType != FullRefreshType.BLANK)
				{
					this.dao.saveGlobalSettings(globalSettings.withFullRefreshSettings(FullRefreshType.BLANK));
				}
			}
		});
		((MaterialButton) view.findViewById(R.id.manual_full_refresh_type_invert)).addOnCheckedChangeListener((view1, checked) ->
		{
			if (checked)
			{
				((Checkable) view.findViewById(R.id.manual_full_refresh_type_blank)).setChecked(false);

				GlobalSettings globalSettings = this.getCurrentGlobalSettings();
				if (globalSettings.manualFullRefreshType != FullRefreshType.INVERT)
				{
					this.dao.saveGlobalSettings(globalSettings.withFullRefreshSettings(FullRefreshType.INVERT));
				}
			}
		});

		((SwitchMaterial) view.findViewById(R.id.use_global_contrast_map)).setOnCheckedChangeListener((view1, checked) ->
		{
			GlobalSettings globalSettings = this.getCurrentGlobalSettings();
			if (globalSettings.useGlobalContrastMap != checked)
			{
				this.dao.saveGlobalSettings(globalSettings.withGlobalContrastMap(checked, globalSettings.useGlobalContrastMapInHighQualityMode, globalSettings.globalContrastMap).withForcedMode(null, globalSettings.forceFastMode, false));
			}
		});
		((SwitchMaterial) view.findViewById(R.id.use_global_contrast_map_in_high_quality_mode)).setOnCheckedChangeListener((view1, checked) ->
		{
			GlobalSettings globalSettings = this.getCurrentGlobalSettings();
			if (globalSettings.useGlobalContrastMapInHighQualityMode != checked)
			{
				this.dao.saveGlobalSettings(globalSettings.withGlobalContrastMap(globalSettings.useGlobalContrastMap, checked, globalSettings.globalContrastMap).withForcedMode(null, globalSettings.forceFastMode, false));
			}
		});
		NumericInput.setUpCallbacks(view.findViewById(R.id.global_contrast_map_min), 0, 100, 10, value ->
		{
			GlobalSettings globalSettings = this.getCurrentGlobalSettings();
			if (globalSettings.globalContrastMap.min != value)
			{
				this.dao.saveGlobalSettings(globalSettings.withGlobalContrastMap(globalSettings.useGlobalContrastMap, globalSettings.useGlobalContrastMapInHighQualityMode, new ContrastMap(value, globalSettings.globalContrastMap.max)).withForcedMode(null, globalSettings.forceFastMode, false));
			}
		});
		NumericInput.setUpCallbacks(view.findViewById(R.id.global_contrast_map_max), 0, 100, 10, value ->
		{
			GlobalSettings globalSettings = this.getCurrentGlobalSettings();
			if (globalSettings.globalContrastMap.max != value)
			{
				this.dao.saveGlobalSettings(globalSettings.withGlobalContrastMap(globalSettings.useGlobalContrastMap, globalSettings.useGlobalContrastMapInHighQualityMode, new ContrastMap(globalSettings.globalContrastMap.min, value)).withForcedMode(null, globalSettings.forceFastMode, false));
			}
		});

		((SwitchMaterial) view.findViewById(R.id.use_fast_mode_contrast_map)).setOnCheckedChangeListener((view1, checked) ->
		{
			GlobalSettings globalSettings = this.getCurrentGlobalSettings();
			if (globalSettings.useFastModeContrastMap != checked)
			{
				this.dao.saveGlobalSettings(globalSettings.withFastModeContrastMap(checked, globalSettings.fastModeContrastMap).withForcedMode(null, globalSettings.forceFastMode, false));
			}
		});
		NumericInput.setUpCallbacks(view.findViewById(R.id.fast_mode_contrast_map_min), 0, 100, 10, value ->
		{
			GlobalSettings globalSettings = this.getCurrentGlobalSettings();
			if (globalSettings.fastModeContrastMap.min != value)
			{
				this.dao.saveGlobalSettings(globalSettings.withFastModeContrastMap(globalSettings.useFastModeContrastMap, new ContrastMap(value, globalSettings.fastModeContrastMap.max)).withForcedMode(null, globalSettings.forceFastMode, false));
			}
		});
		NumericInput.setUpCallbacks(view.findViewById(R.id.fast_mode_contrast_map_max), 0, 100, 10, value ->
		{
			GlobalSettings globalSettings = this.getCurrentGlobalSettings();
			if (globalSettings.fastModeContrastMap.max != value)
			{
				this.dao.saveGlobalSettings(globalSettings.withFastModeContrastMap(globalSettings.useFastModeContrastMap, new ContrastMap(globalSettings.fastModeContrastMap.min, value)).withForcedMode(null, globalSettings.forceFastMode, false));
			}
		});

		((SwitchMaterial) view.findViewById(R.id.forced_color_map_enable)).setOnCheckedChangeListener((view1, checked) ->
		{
			GlobalSettings globalSettings = this.getCurrentGlobalSettings();
			if (checked && globalSettings.forcedColorMapId == null)
			{
				Intent intent = new Intent(this.requireContext(), ColorMapChooserDialogActivity.class);
				this.startActivityForResult(intent, 0);
				((SwitchMaterial) view.findViewById(R.id.forced_color_map_enable)).setChecked(false);
			}
			else if (!checked && globalSettings.forcedColorMapId != null)
			{
				this.dao.saveGlobalSettings(globalSettings.withForcedMode(null, globalSettings.forceFastMode, globalSettings.forceDisableColorMaps));
			}
		});
		((Button) view.findViewById(R.id.saved_color_maps)).setOnClickListener(view1 ->
		{
			Intent intent = new Intent(this.requireContext(), SavedColorMapsActivity.class);
			this.requireContext().startActivity(intent);
		});

		this.globalSettings.observe(this.getViewLifecycleOwner(), globalSettings ->
		{
			if (!EInkAccessibilityService.isRunning())
			{
				ApplySettingsService.applySettings(this.requireContext(), false, false, false, false);
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
					this.dao.saveGlobalSettings(globalSettings.withForcedMode(id, globalSettings.forceFastMode, globalSettings.forceDisableColorMaps));
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
}