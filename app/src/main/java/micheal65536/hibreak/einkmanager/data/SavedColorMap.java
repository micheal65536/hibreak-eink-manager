package micheal65536.hibreak.einkmanager.data;

import androidx.annotation.NonNull;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "colorMaps")
public class SavedColorMap
{
	@PrimaryKey(autoGenerate = true)
	public final long id;

	@NonNull
	public final String name;
	@NonNull
	@Embedded(prefix = "colorMap_")
	public final ColorMap colorMap;

	public SavedColorMap(long id, @NonNull String name, @NonNull ColorMap colorMap)
	{
		this.id = id;
		this.name = name;
		this.colorMap = colorMap;
	}
}