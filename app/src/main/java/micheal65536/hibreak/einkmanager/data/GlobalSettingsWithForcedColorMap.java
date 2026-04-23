package micheal65536.hibreak.einkmanager.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Embedded;
import androidx.room.Relation;

public class GlobalSettingsWithForcedColorMap
{
	@NonNull
	@Embedded
	public final GlobalSettings globalSettings;

	@Relation(parentColumn = "forcedColorMapId", entityColumn = "id")
	@Nullable
	public final SavedColorMap forcedColorMap;

	public GlobalSettingsWithForcedColorMap(@NonNull GlobalSettings globalSettings, @Nullable SavedColorMap forcedColorMap)
	{
		this.globalSettings = globalSettings;
		this.forcedColorMap = forcedColorMap;
	}
}