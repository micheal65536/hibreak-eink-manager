package micheal65536.hibreak.einkmanager.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import micheal65536.hibreak.einkmanager.R;
import micheal65536.hibreak.einkmanager.data.ProfilesDao;
import micheal65536.hibreak.einkmanager.data.ProfilesDatabase;
import micheal65536.hibreak.einkmanager.data.SavedColorMap;

public final class ColorMapChooserDialogActivity extends AppCompatActivity
{
	private ProfilesDao dao;
	private LiveData<List<SavedColorMap>> savedColorMaps;

	private ListAdapter<SavedColorMap, RecyclerView.ViewHolder> adapter;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_color_map_chooser_dialog);

		this.adapter = new ListAdapter<>(new DiffUtil.ItemCallback<SavedColorMap>()
		{
			@Override
			public boolean areItemsTheSame(@NonNull SavedColorMap oldItem, @NonNull SavedColorMap newItem)
			{
				return newItem.id == oldItem.id;
			}

			@Override
			public boolean areContentsTheSame(@NonNull SavedColorMap oldItem, @NonNull SavedColorMap newItem)
			{
				return newItem.id == oldItem.id && newItem.name.equals(oldItem.name);
			}
		})
		{
			@NonNull
			@Override
			public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
			{
				return new RecyclerView.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_saved_color_maps_list_item, parent, false))
				{
					// empty
				};
			}

			@Override
			public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position)
			{
				View view = holder.itemView;
				SavedColorMap savedColorMap = this.getItem(position);
				((TextView) view.findViewById(R.id.name)).setText(savedColorMap.name);
				view.setOnClickListener(view1 ->
				{
					Intent intent = new Intent();
					intent.putExtra("id", savedColorMap.id);
					ColorMapChooserDialogActivity.this.setResult(Activity.RESULT_OK, intent);
					ColorMapChooserDialogActivity.this.finish();
				});
			}
		};
		RecyclerView recyclerView = this.findViewById(R.id.recycler_view);
		LinearLayoutManager layoutManager = new LinearLayoutManager(this);
		layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		recyclerView.setLayoutManager(layoutManager);
		recyclerView.setAdapter(this.adapter);

		this.dao = ProfilesDatabase.getInstance(this).dao();
		this.savedColorMaps = this.dao.getAllColorMaps();
		this.savedColorMaps.observe(this, savedColorMaps ->
		{
			this.adapter.submitList(savedColorMaps.stream().sorted(Comparator.comparing(a -> a.name.toLowerCase(Locale.ROOT))).collect(Collectors.toList()));
		});
	}
}