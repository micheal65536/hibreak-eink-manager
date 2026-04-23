package micheal65536.hibreak.einkmanager.ui;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
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
import androidx.fragment.app.Fragment;
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
import micheal65536.hibreak.einkmanager.data.AppProfile;
import micheal65536.hibreak.einkmanager.data.ProfilesDao;
import micheal65536.hibreak.einkmanager.data.ProfilesDatabase;

public final class AppProfilesListFragment extends Fragment
{
	private ProfilesDao dao;
	private LiveData<List<AppProfile>> appProfiles;

	private ListAdapter<AppInfo, RecyclerView.ViewHolder> adapter;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		this.dao = ProfilesDatabase.getInstance(this.requireContext()).dao();

		this.appProfiles = this.dao.getAllAppProfiles();
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_app_profiles_list, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
	{
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
				return new RecyclerView.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_app_profiles_list_item, parent, false))
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
					Intent intent = new Intent(AppProfilesListFragment.this.requireContext(), AppProfileActivity.class);
					intent.putExtra("appId", appInfo.appId);
					AppProfilesListFragment.this.requireContext().startActivity(intent);
				});
			}
		};
		RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
		LinearLayoutManager layoutManager = new LinearLayoutManager(this.requireContext());
		layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		recyclerView.setLayoutManager(layoutManager);
		recyclerView.setAdapter(this.adapter);

		PackageManager packageManager = this.requireContext().getPackageManager();
		this.appProfiles.observe(this.getViewLifecycleOwner(), appProfiles ->
		{
			this.adapter.submitList(appProfiles.stream().map(appProfile ->
			{
				String appId = appProfile.appId;
				try
				{
					ApplicationInfo applicationInfo = packageManager.getApplicationInfo(appId, 0);
					return new AppInfo(appProfile.appId, packageManager.getApplicationLabel(applicationInfo).toString(), packageManager.getApplicationIcon(applicationInfo));
				}
				catch (PackageManager.NameNotFoundException exception)
				{
					return new AppInfo(appId, null, null);
				}
			}).sorted(Comparator.comparing(a -> (a.name != null ? a.name : a.appId).toLowerCase(Locale.ROOT))).collect(Collectors.toList()));
		});
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