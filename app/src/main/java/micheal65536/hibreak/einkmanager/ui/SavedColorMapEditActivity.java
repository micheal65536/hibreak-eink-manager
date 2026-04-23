package micheal65536.hibreak.einkmanager.ui;

import android.os.Bundle;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;

import micheal65536.hibreak.einkmanager.R;
import micheal65536.hibreak.einkmanager.data.ColorMap;
import micheal65536.hibreak.einkmanager.data.ProfilesDao;
import micheal65536.hibreak.einkmanager.data.ProfilesDatabase;
import micheal65536.hibreak.einkmanager.data.SavedColorMap;

public final class SavedColorMapEditActivity extends AppCompatActivity
{
	private long id;

	private ProfilesDao dao;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_saved_color_map_edit);

		((MaterialToolbar) this.findViewById(R.id.toolbar)).setNavigationIcon(R.drawable.ic_back);
		((MaterialToolbar) this.findViewById(R.id.toolbar)).setNavigationOnClickListener(view ->
		{
			this.finish();
		});

		this.dao = ProfilesDatabase.getInstance(this).dao();

		if (savedInstanceState == null)
		{
			this.id = this.getIntent().getLongExtra("id", 0);
			if (this.id != 0)
			{
				LiveData<SavedColorMap> savedColorMapLiveData = this.dao.getColorMap(this.id);
				savedColorMapLiveData.observe(this, new Observer<>()
				{
					@Override
					public void onChanged(SavedColorMap savedColorMap)
					{
						savedColorMapLiveData.removeObserver(this);

						((EditText) SavedColorMapEditActivity.this.findViewById(R.id.name)).setText(savedColorMap.name);
						NumericInput.setValue(SavedColorMapEditActivity.this.findViewById(R.id.color_map_red_min), savedColorMap.colorMap.redMin);
						NumericInput.setValue(SavedColorMapEditActivity.this.findViewById(R.id.color_map_red_max), savedColorMap.colorMap.redMax);
						NumericInput.setValue(SavedColorMapEditActivity.this.findViewById(R.id.color_map_green_min), savedColorMap.colorMap.greenMin);
						NumericInput.setValue(SavedColorMapEditActivity.this.findViewById(R.id.color_map_green_max), savedColorMap.colorMap.greenMax);
						NumericInput.setValue(SavedColorMapEditActivity.this.findViewById(R.id.color_map_blue_min), savedColorMap.colorMap.blueMin);
						NumericInput.setValue(SavedColorMapEditActivity.this.findViewById(R.id.color_map_blue_max), savedColorMap.colorMap.blueMax);
					}
				});
			}
			else
			{
				NumericInput.setValue(SavedColorMapEditActivity.this.findViewById(R.id.color_map_red_min), 0);
				NumericInput.setValue(SavedColorMapEditActivity.this.findViewById(R.id.color_map_red_max), 100);
				NumericInput.setValue(SavedColorMapEditActivity.this.findViewById(R.id.color_map_green_min), 0);
				NumericInput.setValue(SavedColorMapEditActivity.this.findViewById(R.id.color_map_green_max), 100);
				NumericInput.setValue(SavedColorMapEditActivity.this.findViewById(R.id.color_map_blue_min), 0);
				NumericInput.setValue(SavedColorMapEditActivity.this.findViewById(R.id.color_map_blue_max), 100);
			}
		}
		else
		{
			this.id = savedInstanceState.getLong("id", 0);
		}

		((MaterialToolbar) this.findViewById(R.id.toolbar)).getMenu().findItem(R.id.save).setOnMenuItemClickListener(item ->
		{
			String name = ((EditText) this.findViewById(R.id.name)).getText().toString().trim();
			if (!name.isEmpty())
			{
				SavedColorMap savedColorMap = new SavedColorMap(this.id, name, new ColorMap(
						NumericInput.getValue(this.findViewById(R.id.color_map_red_min), 0, 100), NumericInput.getValue(this.findViewById(R.id.color_map_red_max), 0, 100),
						NumericInput.getValue(this.findViewById(R.id.color_map_green_min), 0, 100), NumericInput.getValue(this.findViewById(R.id.color_map_green_max), 0, 100),
						NumericInput.getValue(this.findViewById(R.id.color_map_blue_min), 0, 100), NumericInput.getValue(this.findViewById(R.id.color_map_blue_max), 0, 100)
				));
				Futures.addCallback(this.dao.saveColorMap(savedColorMap), new FutureCallback<>()
				{
					@Override
					public void onSuccess(Long id)
					{
						SavedColorMapEditActivity.this.id = id;
					}

					@Override
					public void onFailure(Throwable throwable)
					{
						// empty
					}
				}, this.getMainExecutor());
			}
			else
			{
				new MaterialAlertDialogBuilder(this).setMessage(R.string.saved_color_map_name_empty).setPositiveButton(R.string.close, (dialog, which) -> dialog.dismiss()).create().show();
			}

			return true;
		});
		((MaterialToolbar) this.findViewById(R.id.toolbar)).getMenu().findItem(R.id.delete).setOnMenuItemClickListener(item ->
		{
			if (this.id != -1)
			{
				this.dao.deleteColorMap(this.id);
			}

			this.finish();

			return true;
		});

		NumericInput.setUpCallbacks(this.findViewById(R.id.color_map_red_min), 0, 100, 10, value ->
		{
			NumericInput.setValue(this.findViewById(R.id.color_map_red_min), value);
		});
		NumericInput.setUpCallbacks(this.findViewById(R.id.color_map_red_max), 0, 100, 10, value ->
		{
			NumericInput.setValue(this.findViewById(R.id.color_map_red_max), value);
		});
		NumericInput.setUpCallbacks(this.findViewById(R.id.color_map_green_min), 0, 100, 10, value ->
		{
			NumericInput.setValue(this.findViewById(R.id.color_map_green_min), value);
		});
		NumericInput.setUpCallbacks(this.findViewById(R.id.color_map_green_max), 0, 100, 10, value ->
		{
			NumericInput.setValue(this.findViewById(R.id.color_map_green_max), value);
		});
		NumericInput.setUpCallbacks(this.findViewById(R.id.color_map_blue_min), 0, 100, 10, value ->
		{
			NumericInput.setValue(this.findViewById(R.id.color_map_blue_min), value);
		});
		NumericInput.setUpCallbacks(this.findViewById(R.id.color_map_blue_max), 0, 100, 10, value ->
		{
			NumericInput.setValue(this.findViewById(R.id.color_map_blue_max), value);
		});
	}
}