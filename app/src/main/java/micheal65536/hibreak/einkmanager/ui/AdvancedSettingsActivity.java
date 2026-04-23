package micheal65536.hibreak.einkmanager.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.google.android.material.appbar.MaterialToolbar;

import micheal65536.hibreak.einkmanager.ApplySettingsService;
import micheal65536.hibreak.einkmanager.EInkAccessibilityService;
import micheal65536.hibreak.einkmanager.R;
import micheal65536.hibreak.einkmanager.data.AdvancedSettings;
import micheal65536.hibreak.einkmanager.data.ProfilesDao;
import micheal65536.hibreak.einkmanager.data.ProfilesDatabase;

public final class AdvancedSettingsActivity extends AppCompatActivity
{
	private ProfilesDao dao;
	private LiveData<AdvancedSettings> advancedSettings;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_advanced_settings);

		((MaterialToolbar) this.findViewById(R.id.toolbar)).setNavigationIcon(R.drawable.ic_back);
		((MaterialToolbar) this.findViewById(R.id.toolbar)).setNavigationOnClickListener(view ->
		{
			this.finish();
		});

		this.dao = ProfilesDatabase.getInstance(this).dao();
		this.advancedSettings = Transformations.map(this.dao.getAdvancedSettings(), advancedSettings -> advancedSettings != null ? advancedSettings : AdvancedSettings.DEFAULT);

		this.advancedSettings.observe(this, advancedSettings ->
		{
			NumericInput.setValue(this.findViewById(R.id.navbar_manual_full_refresh_delay_value), advancedSettings.navbarManualFullRefreshDelay);

			NumericInput.setValue(this.findViewById(R.id.explicit_refresh_regular_post_delay_value), advancedSettings.explicitRefreshRegularPostDelay);
			NumericInput.setValue(this.findViewById(R.id.explicit_refresh_blank_post_delay_value), advancedSettings.explicitRefreshBlankPostDelay);

			NumericInput.setValue(this.findViewById(R.id.screen_power_off_delay_value), advancedSettings.screenPowerOffDelay);

			NumericInput.setValue(this.findViewById(R.id.vcom_value), -advancedSettings.vcom);
		});

		NumericInput.setUpCallbacks(this.findViewById(R.id.navbar_manual_full_refresh_delay_value), 0, 5000, 100, value ->
		{
			AdvancedSettings advancedSettings = this.getCurrentAdvancedSettings();
			if (advancedSettings.navbarManualFullRefreshDelay != value)
			{
				this.dao.saveAdvancedSettings(advancedSettings.withFullRefreshDelay(value));
			}
		});

		NumericInput.setUpCallbacks(this.findViewById(R.id.explicit_refresh_regular_post_delay_value), 0, 500, 10, value ->
		{
			AdvancedSettings advancedSettings = this.getCurrentAdvancedSettings();
			if (advancedSettings.explicitRefreshRegularPostDelay != value)
			{
				this.dao.saveAdvancedSettings(advancedSettings.withExplicitRefreshPostDelay(value, advancedSettings.explicitRefreshBlankPostDelay));
			}
		});
		NumericInput.setUpCallbacks(this.findViewById(R.id.explicit_refresh_blank_post_delay_value), 0, 500, 10, value ->
		{
			AdvancedSettings advancedSettings = this.getCurrentAdvancedSettings();
			if (advancedSettings.explicitRefreshBlankPostDelay != value)
			{
				this.dao.saveAdvancedSettings(advancedSettings.withExplicitRefreshPostDelay(advancedSettings.explicitRefreshRegularPostDelay, value));
			}
		});

		NumericInput.setUpCallbacks(this.findViewById(R.id.screen_power_off_delay_value), 0, 5000, 100, value ->
		{
			AdvancedSettings advancedSettings = this.getCurrentAdvancedSettings();
			if (advancedSettings.screenPowerOffDelay != value)
			{
				this.dao.saveAdvancedSettings(advancedSettings.withScreenPowerOffDelay(value));
			}
		});

		NumericInput.setUpCallbacks(this.findViewById(R.id.vcom_value), 0, 1500, 50, value ->
		{
			AdvancedSettings advancedSettings = this.getCurrentAdvancedSettings();
			if (advancedSettings.vcom != -value)
			{
				this.dao.saveAdvancedSettings(advancedSettings.withVcom(-value));
			}
		});

		this.advancedSettings.observe(this, advancedSettings ->
		{
			if (!EInkAccessibilityService.isRunning())
			{
				ApplySettingsService.applySettings(this, false, false, false, true);
			}
		});
	}

	@NonNull
	private AdvancedSettings getCurrentAdvancedSettings()
	{
		AdvancedSettings advancedSettings = this.advancedSettings.getValue();
		return advancedSettings != null ? advancedSettings : AdvancedSettings.DEFAULT;
	}
}