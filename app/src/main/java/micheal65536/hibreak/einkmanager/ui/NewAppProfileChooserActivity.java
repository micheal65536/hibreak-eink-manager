package micheal65536.hibreak.einkmanager.ui;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.Comparator;
import java.util.Locale;
import java.util.stream.Collectors;

import micheal65536.hibreak.einkmanager.R;

public final class NewAppProfileChooserActivity extends AppCompatActivity
{
	private ListAdapter<AppInfo, RecyclerView.ViewHolder> adapter;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_new_app_profile_chooser);

		((MaterialToolbar) this.findViewById(R.id.toolbar)).setNavigationIcon(R.drawable.ic_back);
		((MaterialToolbar) this.findViewById(R.id.toolbar)).setNavigationOnClickListener(view ->
		{
			this.finish();
		});

		this.adapter = new ListAdapter<>(new DiffUtil.ItemCallback<AppInfo>()
		{
			@Override
			public boolean areItemsTheSame(@NonNull AppInfo oldItem, @NonNull AppInfo newItem)
			{
				return newItem.appId.equals(oldItem.appId);
			}

			@Override
			public boolean areContentsTheSame(@NonNull AppInfo oldItem, @NonNull AppInfo newItem)
			{
				return newItem.appId.equals(oldItem.appId) && ((newItem.name == null && oldItem.name == null) || (newItem.name != null && oldItem.name != null && newItem.name.equals(oldItem.name)));
			}
		})
		{
			@NonNull
			@Override
			public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
			{
				return new RecyclerView.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_new_app_profile_chooser_list_item, parent, false))
				{
					// empty
				};
			}

			@Override
			public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position)
			{
				View view = holder.itemView;
				AppInfo appInfo = this.getItem(position);
				((TextView) view.findViewById(R.id.app_name)).setText(appInfo.name != null ? appInfo.name : appInfo.appId);
				if (appInfo.icon != null)
				{
					((ImageView) view.findViewById(R.id.app_icon)).setImageDrawable(appInfo.icon);
					view.findViewById(R.id.app_icon).setVisibility(View.VISIBLE);
				}
				else
				{
					view.findViewById(R.id.app_icon).setVisibility(View.INVISIBLE);
				}
				view.setOnClickListener(view1 ->
				{
					Intent intent = new Intent(NewAppProfileChooserActivity.this, AppProfileActivity.class);
					intent.putExtra("appId", appInfo.appId);
					NewAppProfileChooserActivity.this.startActivity(intent);
					NewAppProfileChooserActivity.this.finish();
				});
			}
		};
		RecyclerView recyclerView = this.findViewById(R.id.recycler_view);
		LinearLayoutManager layoutManager = new LinearLayoutManager(this);
		layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		recyclerView.setLayoutManager(layoutManager);
		recyclerView.setAdapter(this.adapter);

		PackageManager packageManager = this.getPackageManager();
		this.adapter.submitList(packageManager.getInstalledApplications(0).stream()
											  .filter(applicationInfo -> packageManager.getLaunchIntentForPackage(applicationInfo.packageName) != null)
											  .map(applicationInfo -> new AppInfo(applicationInfo.packageName, packageManager.getApplicationLabel(applicationInfo).toString(), packageManager.getApplicationIcon(applicationInfo)))
											  .sorted(Comparator.comparing(a -> (a.name != null ? a.name : a.appId).toLowerCase(Locale.ROOT)))
											  .collect(Collectors.toList())
		);
	}

	private static final class AppInfo
	{
		@NonNull
		public final String appId;
		@Nullable
		public final String name;
		@Nullable
		public final Drawable icon;

		public AppInfo(@NonNull String appId, @Nullable String name, @Nullable Drawable icon)
		{
			this.appId = appId;
			this.name = name;
			this.icon = icon;
		}
	}
}