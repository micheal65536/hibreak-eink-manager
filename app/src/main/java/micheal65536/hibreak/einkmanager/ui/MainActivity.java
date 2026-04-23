package micheal65536.hibreak.einkmanager.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import micheal65536.hibreak.einkmanager.ApplySettingsService;
import micheal65536.hibreak.einkmanager.R;

public final class MainActivity extends AppCompatActivity
{
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_main);

		TabLayout tabLayout = this.findViewById(R.id.tab_layout);
		ViewPager2 viewPager = this.findViewById(R.id.view_pager);
		viewPager.setAdapter(new FragmentStateAdapter(this)
		{
			@NonNull
			@Override
			public Fragment createFragment(int position)
			{
				switch (position)
				{
					case 0:
						return new GlobalSettingsFragment();
					case 1:
						return new AppProfilesListFragment();
					default:
						throw new IndexOutOfBoundsException();
				}
			}

			@Override
			public int getItemCount()
			{
				return 2;
			}
		});
		new TabLayoutMediator(tabLayout, viewPager, (tab, position) ->
		{
			switch (position)
			{
				case 0:
					tab.setText(R.string.tab_global_settings);
					break;
				case 1:
					tab.setText(R.string.tab_app_profiles);
					break;
			}
		}).attach();

		((MaterialToolbar) MainActivity.this.findViewById(R.id.toolbar)).getMenu().findItem(R.id.advanced_settings).setOnMenuItemClickListener(item ->
		{
			Intent intent = new Intent(this, AdvancedSettingsActivity.class);
			this.startActivity(intent);
			return true;
		});
		((MaterialToolbar) MainActivity.this.findViewById(R.id.toolbar)).getMenu().findItem(R.id.add_app_profile).setOnMenuItemClickListener(item ->
		{
			Intent intent = new Intent(this, NewAppProfileChooserActivity.class);
			this.startActivity(intent);
			return true;
		});
		viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback()
		{
			@Override
			public void onPageSelected(int position)
			{
				Menu menu = ((MaterialToolbar) MainActivity.this.findViewById(R.id.toolbar)).getMenu();
				menu.findItem(R.id.advanced_settings).setVisible(position == 0);
				menu.findItem(R.id.add_app_profile).setVisible(position == 1);
			}
		});
	}

	@Override
	protected void onStart()
	{
		super.onStart();

		ApplySettingsService.applySettings(this, false, true, true, true);
	}
}