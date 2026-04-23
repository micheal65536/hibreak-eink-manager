package micheal65536.hibreak.einkmanager.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.google.android.material.appbar.MaterialToolbar;

import micheal65536.hibreak.einkmanager.R;
import micheal65536.hibreak.einkmanager.data.AdvancedSettings;
import micheal65536.hibreak.einkmanager.data.ProfilesDao;
import micheal65536.hibreak.einkmanager.data.ProfilesDatabase;

public final class DragSettingsActivity extends AppCompatActivity
{
	private ProfilesDao dao;
	private LiveData<AdvancedSettings> advancedSettings;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_drag_settings);

		((MaterialToolbar) this.findViewById(R.id.toolbar)).setNavigationIcon(R.drawable.ic_back);
		((MaterialToolbar) this.findViewById(R.id.toolbar)).setNavigationOnClickListener(view ->
		{
			this.finish();
		});

		this.dao = ProfilesDatabase.getInstance(this).dao();
		this.advancedSettings = Transformations.map(this.dao.getAdvancedSettings(), advancedSettings -> advancedSettings != null ? advancedSettings : AdvancedSettings.DEFAULT);

		this.advancedSettings.observe(this, advancedSettings ->
		{
			NumericInput.setValue(this.findViewById(R.id.drag_start_delay_static_value), advancedSettings.dragStartDelayStatic);
			NumericInput.setValue(this.findViewById(R.id.drag_start_delay_moving_value), advancedSettings.dragStartDelayMoving);
			NumericInput.setValue(this.findViewById(R.id.drag_start_distance_value), advancedSettings.dragStartDistance);
			NumericInput.setValue(this.findViewById(R.id.drag_end_delay_value), advancedSettings.dragEndDelay);
		});

		NumericInput.setUpCallbacks(this.findViewById(R.id.drag_start_delay_static_value), 0, 5000, 100, value ->
		{
			AdvancedSettings advancedSettings = this.getCurrentAdvancedSettings();
			if (advancedSettings.dragStartDelayStatic != value)
			{
				this.dao.saveAdvancedSettings(advancedSettings.withDragTriggerParams(value, advancedSettings.dragStartDelayMoving, advancedSettings.dragStartDistance, advancedSettings.dragEndDelay));
			}
		});
		NumericInput.setUpCallbacks(this.findViewById(R.id.drag_start_delay_moving_value), 0, 5000, 100, value ->
		{
			AdvancedSettings advancedSettings = this.getCurrentAdvancedSettings();
			if (advancedSettings.dragStartDelayMoving != value)
			{
				this.dao.saveAdvancedSettings(advancedSettings.withDragTriggerParams(advancedSettings.dragStartDelayStatic, value, advancedSettings.dragStartDistance, advancedSettings.dragEndDelay));
			}
		});
		NumericInput.setUpCallbacks(this.findViewById(R.id.drag_start_distance_value), 0, 200, 10, value ->
		{
			AdvancedSettings advancedSettings = this.getCurrentAdvancedSettings();
			if (advancedSettings.dragStartDistance != value)
			{
				this.dao.saveAdvancedSettings(advancedSettings.withDragTriggerParams(advancedSettings.dragStartDelayStatic, advancedSettings.dragStartDelayMoving, value, advancedSettings.dragEndDelay));
			}
		});
		NumericInput.setUpCallbacks(this.findViewById(R.id.drag_end_delay_value), 0, 5000, 100, value ->
		{
			AdvancedSettings advancedSettings = this.getCurrentAdvancedSettings();
			if (advancedSettings.dragEndDelay != value)
			{
				this.dao.saveAdvancedSettings(advancedSettings.withDragTriggerParams(advancedSettings.dragStartDelayStatic, advancedSettings.dragStartDelayMoving, advancedSettings.dragStartDistance, value));
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