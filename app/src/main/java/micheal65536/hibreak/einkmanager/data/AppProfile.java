package micheal65536.hibreak.einkmanager.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "appProfiles")
public class AppProfile
{
	@PrimaryKey
	@NonNull
	public final String appId;

	public final boolean enabled;

	@Nullable
	public final RefreshMode refreshMode;
	@Nullable
	public final Boolean enableDragModeSwitch;
	@Nullable
	public final Boolean enableImeModeSwitch;

	public final boolean disableGlobalContrastMap;
	public final boolean useColorMap;
	@NonNull
	@Embedded(prefix = "colorMap_")
	public final ColorMap colorMap;

	public AppProfile(@NonNull String appId, boolean enabled, @Nullable RefreshMode refreshMode, @Nullable Boolean enableDragModeSwitch, @Nullable Boolean enableImeModeSwitch, boolean disableGlobalContrastMap, boolean useColorMap, @NonNull ColorMap colorMap)
	{
		this.appId = appId;
		this.enabled = enabled;
		this.refreshMode = refreshMode;
		this.enableDragModeSwitch = enableDragModeSwitch;
		this.enableImeModeSwitch = enableImeModeSwitch;
		this.disableGlobalContrastMap = disableGlobalContrastMap;
		this.useColorMap = useColorMap;
		this.colorMap = colorMap;
	}

	@NonNull
	public AppProfile withEnabled(boolean enabled)
	{
		return new AppProfile(
				this.appId, enabled,
				this.refreshMode, this.enableDragModeSwitch, this.enableImeModeSwitch,
				this.disableGlobalContrastMap, this.useColorMap, this.colorMap
		);
	}

	@NonNull
	public AppProfile withRefreshMode(@Nullable RefreshMode refreshMode, @Nullable Boolean enableDragModeSwitch, @Nullable Boolean enableImeModeSwitch)
	{
		return new AppProfile(
				this.appId, this.enabled,
				refreshMode, enableDragModeSwitch, enableImeModeSwitch,
				this.disableGlobalContrastMap, this.useColorMap, this.colorMap
		);
	}

	@NonNull
	public AppProfile withColorMap(boolean disableGlobalContrastMap, boolean useColorMap, @NonNull ColorMap colorMap)
	{
		return new AppProfile(
				this.appId, this.enabled,
				this.refreshMode, this.enableDragModeSwitch, this.enableImeModeSwitch,
				disableGlobalContrastMap, useColorMap, colorMap
		);
	}

	@NonNull
	public static AppProfile newDefault(@NonNull String appId)
	{
		return new AppProfile(
				appId, true,
				null, null, null,
				false, false, new ColorMap(0, 100, 0, 100, 0, 100)
		);
	}
}