package micheal65536.hibreak.einkmanager.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {GlobalSettings.class, AppProfile.class, SavedColorMap.class, AdvancedSettings.class}, version = 1)
public abstract class ProfilesDatabase extends RoomDatabase
{
	public abstract ProfilesDao dao();

	private static ProfilesDatabase instance;

	public synchronized static ProfilesDatabase getInstance(Context context)
	{
		if (instance == null)
		{
			instance = Room.databaseBuilder(context.createDeviceProtectedStorageContext(), ProfilesDatabase.class, "profiles").build();
		}
		return instance;
	}
}