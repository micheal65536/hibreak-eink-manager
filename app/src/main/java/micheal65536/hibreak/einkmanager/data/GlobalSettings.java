package micheal65536.hibreak.einkmanager.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "globalSettings", foreignKeys = @ForeignKey(entity = SavedColorMap.class, parentColumns = "id", childColumns = "forcedColorMapId", onDelete = ForeignKey.SET_NULL, onUpdate = ForeignKey.CASCADE))
public class GlobalSettings
{
	@PrimaryKey
	protected long id = 1;

	@NonNull
	public final RefreshMode defaultRefreshMode;
	public final boolean enableDragModeSwitch;
	public final boolean enableImeModeSwitch;
	public final boolean allowDragModeDuringIme;
	public final boolean useLastAppModeInSystemUI;

	public final FullRefreshType manualFullRefreshType;

	public final boolean useGlobalContrastMap;
	public final boolean useGlobalContrastMapInHighQualityMode;
	@NonNull
	@Embedded(prefix = "globalContrastMap_")
	public final ContrastMap globalContrastMap;

	public final boolean useFastModeContrastMap;
	@NonNull
	@Embedded(prefix = "fastModeContrastMap_")
	public final ContrastMap fastModeContrastMap;

	@Nullable
	public final Long forcedColorMapId;
	public final boolean forceFastMode;

	public GlobalSettings(@NonNull RefreshMode defaultRefreshMode, boolean enableDragModeSwitch, boolean enableImeModeSwitch, boolean allowDragModeDuringIme, boolean useLastAppModeInSystemUI, FullRefreshType manualFullRefreshType, boolean useGlobalContrastMap, boolean useGlobalContrastMapInHighQualityMode, @NonNull ContrastMap globalContrastMap, boolean useFastModeContrastMap, @NonNull ContrastMap fastModeContrastMap, @Nullable Long forcedColorMapId, boolean forceFastMode)
	{
		this.defaultRefreshMode = defaultRefreshMode;
		this.enableDragModeSwitch = enableDragModeSwitch;
		this.enableImeModeSwitch = enableImeModeSwitch;
		this.allowDragModeDuringIme = allowDragModeDuringIme;
		this.useLastAppModeInSystemUI = useLastAppModeInSystemUI;
		this.manualFullRefreshType = manualFullRefreshType;
		this.useGlobalContrastMap = useGlobalContrastMap;
		this.useGlobalContrastMapInHighQualityMode = useGlobalContrastMapInHighQualityMode;
		this.globalContrastMap = globalContrastMap;
		this.useFastModeContrastMap = useFastModeContrastMap;
		this.fastModeContrastMap = fastModeContrastMap;
		this.forcedColorMapId = forcedColorMapId;
		this.forceFastMode = forceFastMode;
	}

	@NonNull
	public GlobalSettings withDefaultRefreshMode(@NonNull RefreshMode defaultRefreshMode)
	{
		return new GlobalSettings(
				defaultRefreshMode, this.enableDragModeSwitch, this.enableImeModeSwitch, this.allowDragModeDuringIme, this.useLastAppModeInSystemUI,
				this.manualFullRefreshType,
				this.useGlobalContrastMap, this.useGlobalContrastMapInHighQualityMode, this.globalContrastMap,
				this.useFastModeContrastMap, this.fastModeContrastMap,
				this.forcedColorMapId, this.forceFastMode
		);
	}

	@NonNull
	public GlobalSettings withModeSwitching(boolean enableDragModeSwitch, boolean enableImeModeSwitch, boolean allowDragModeDuringIme)
	{
		return new GlobalSettings(
				this.defaultRefreshMode, enableDragModeSwitch, enableImeModeSwitch, allowDragModeDuringIme, this.useLastAppModeInSystemUI,
				this.manualFullRefreshType,
				this.useGlobalContrastMap, this.useGlobalContrastMapInHighQualityMode, this.globalContrastMap,
				this.useFastModeContrastMap, this.fastModeContrastMap,
				this.forcedColorMapId, this.forceFastMode
		);
	}

	@NonNull
	public GlobalSettings withUseLastAppModeInSystemUI(boolean useLastAppModeInSystemUI)
	{
		return new GlobalSettings(
				this.defaultRefreshMode, this.enableDragModeSwitch, this.enableImeModeSwitch, this.allowDragModeDuringIme, useLastAppModeInSystemUI,
				this.manualFullRefreshType,
				this.useGlobalContrastMap, this.useGlobalContrastMapInHighQualityMode, this.globalContrastMap,
				this.useFastModeContrastMap, this.fastModeContrastMap,
				this.forcedColorMapId, this.forceFastMode
		);
	}

	@NonNull
	public GlobalSettings withFullRefreshSettings(@NonNull FullRefreshType manualFullRefreshType)
	{
		return new GlobalSettings(
				this.defaultRefreshMode, this.enableDragModeSwitch, this.enableImeModeSwitch, this.allowDragModeDuringIme, this.useLastAppModeInSystemUI,
				manualFullRefreshType,
				this.useGlobalContrastMap, this.useGlobalContrastMapInHighQualityMode, this.globalContrastMap,
				this.useFastModeContrastMap, this.fastModeContrastMap,
				this.forcedColorMapId, this.forceFastMode
		);
	}

	@NonNull
	public GlobalSettings withDragTriggerParams(int dragStartDelayStatic, int dragStartDelayMoving, int dragStartDistance, int dragEndDelay)
	{
		return new GlobalSettings(
				this.defaultRefreshMode, this.enableDragModeSwitch, this.enableImeModeSwitch, this.allowDragModeDuringIme, this.useLastAppModeInSystemUI,
				this.manualFullRefreshType,
				this.useGlobalContrastMap, this.useGlobalContrastMapInHighQualityMode, this.globalContrastMap,
				this.useFastModeContrastMap, this.fastModeContrastMap,
				this.forcedColorMapId, this.forceFastMode
		);
	}

	@NonNull
	public GlobalSettings withGlobalContrastMap(boolean useGlobalContrastMap, boolean useGlobalContrastMapInHighQualityMode, @NonNull ContrastMap globalContrastMap)
	{
		return new GlobalSettings(
				this.defaultRefreshMode, this.enableDragModeSwitch, this.enableImeModeSwitch, this.allowDragModeDuringIme, this.useLastAppModeInSystemUI,
				this.manualFullRefreshType,
				useGlobalContrastMap, useGlobalContrastMapInHighQualityMode, globalContrastMap,
				this.useFastModeContrastMap, this.fastModeContrastMap,
				this.forcedColorMapId, this.forceFastMode
		);
	}

	@NonNull
	public GlobalSettings withFastModeContrastMap(boolean useFastModeContrastMap, @NonNull ContrastMap fastModeContrastMap)
	{
		return new GlobalSettings(
				this.defaultRefreshMode, this.enableDragModeSwitch, this.enableImeModeSwitch, this.allowDragModeDuringIme, this.useLastAppModeInSystemUI,
				this.manualFullRefreshType,
				this.useGlobalContrastMap, this.useGlobalContrastMapInHighQualityMode, this.globalContrastMap,
				useFastModeContrastMap, fastModeContrastMap,
				this.forcedColorMapId, this.forceFastMode
		);
	}

	@NonNull
	public GlobalSettings withForcedMode(@Nullable Long forcedColorMapId, boolean forceFastMode)
	{
		return new GlobalSettings(
				this.defaultRefreshMode, this.enableDragModeSwitch, this.enableImeModeSwitch, this.allowDragModeDuringIme, this.useLastAppModeInSystemUI,
				this.manualFullRefreshType,
				this.useGlobalContrastMap, this.useGlobalContrastMapInHighQualityMode, this.globalContrastMap,
				this.useFastModeContrastMap, this.fastModeContrastMap,
				forcedColorMapId, forceFastMode
		);
	}

	public static final GlobalSettings DEFAULT = new GlobalSettings(
			RefreshMode.NORMAL, true, true, true, true,
			FullRefreshType.INVERT,
			false, false, new ContrastMap(0, 100),
			false, new ContrastMap(0, 100),
			null, false
	);
}