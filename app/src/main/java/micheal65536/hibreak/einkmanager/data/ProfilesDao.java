package micheal65536.hibreak.einkmanager.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

@Dao
public abstract class ProfilesDao
{
	@Query("SELECT * FROM globalSettings WHERE id = 1")
	public abstract LiveData<GlobalSettings> getGlobalSettings();

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	public abstract ListenableFuture<Long> saveGlobalSettings(GlobalSettings globalSettings);

	@Transaction
	@Query("SELECT * FROM globalSettings WHERE id = 1")
	public abstract LiveData<GlobalSettingsWithForcedColorMap> getGlobalSettingsWithForcedColorMap();

	//

	@Query("SELECT * FROM advancedSettings WHERE id = 1")
	public abstract LiveData<AdvancedSettings> getAdvancedSettings();

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	public abstract ListenableFuture<Long> saveAdvancedSettings(AdvancedSettings advancedSettings);

	//

	@Query("SELECT * FROM appProfiles WHERE appId = :appId")
	public abstract LiveData<AppProfile> getAppProfile(String appId);

	@Query("SELECT * FROM appProfiles")
	public abstract LiveData<List<AppProfile>> getAllAppProfiles();

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	public abstract ListenableFuture<Long> saveAppProfile(AppProfile profile);

	@Query("DELETE FROM appProfiles WHERE appId = :appId")
	public abstract ListenableFuture<Integer> deleteAppProfile(String appId);

	//

	@Query("SELECT * FROM colorMaps WHERE id = :id")
	public abstract LiveData<SavedColorMap> getColorMap(long id);

	@Query("SELECT * FROM colorMaps")
	public abstract LiveData<List<SavedColorMap>> getAllColorMaps();

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	public abstract ListenableFuture<Long> saveColorMap(SavedColorMap savedColorMap);

	@Query("DELETE FROM colorMaps WHERE id = :id")
	public abstract ListenableFuture<Integer> deleteColorMap(long id);
}