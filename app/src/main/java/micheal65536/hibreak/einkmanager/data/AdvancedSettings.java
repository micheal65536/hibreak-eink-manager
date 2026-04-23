package micheal65536.hibreak.einkmanager.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "advancedSettings")
public class AdvancedSettings
{
	@PrimaryKey
	protected long id = 1;

	public final int dragStartDelayStatic;
	public final int dragStartDelayMoving;
	public final int dragStartDistance;
	public final int dragEndDelay;

	public final int navbarManualFullRefreshDelay;

	public final int explicitRefreshRegularPostDelay;
	public final int explicitRefreshBlankPostDelay;

	public final int screenPowerOffDelay;
	public final int vcom;

	public AdvancedSettings(int dragStartDelayStatic, int dragStartDelayMoving, int dragStartDistance, int dragEndDelay, int navbarManualFullRefreshDelay, int explicitRefreshRegularPostDelay, int explicitRefreshBlankPostDelay, int screenPowerOffDelay, int vcom)
	{
		this.dragStartDelayStatic = dragStartDelayStatic;
		this.dragStartDelayMoving = dragStartDelayMoving;
		this.dragStartDistance = dragStartDistance;
		this.dragEndDelay = dragEndDelay;
		this.navbarManualFullRefreshDelay = navbarManualFullRefreshDelay;
		this.explicitRefreshRegularPostDelay = explicitRefreshRegularPostDelay;
		this.explicitRefreshBlankPostDelay = explicitRefreshBlankPostDelay;
		this.screenPowerOffDelay = screenPowerOffDelay;
		this.vcom = vcom;
	}

	@NonNull
	public AdvancedSettings withDragTriggerParams(int dragStartDelayStatic, int dragStartDelayMoving, int dragStartDistance, int dragEndDelay)
	{
		return new AdvancedSettings(
				dragStartDelayStatic, dragStartDelayMoving, dragStartDistance, dragEndDelay,
				this.navbarManualFullRefreshDelay,
				this.explicitRefreshRegularPostDelay, this.explicitRefreshBlankPostDelay,
				this.screenPowerOffDelay, this.vcom
		);
	}

	@NonNull
	public AdvancedSettings withFullRefreshDelay(int navbarManualFullRefreshDelay)
	{
		return new AdvancedSettings(
				this.dragStartDelayStatic, this.dragStartDelayMoving, this.dragStartDistance, this.dragEndDelay,
				navbarManualFullRefreshDelay,
				this.explicitRefreshRegularPostDelay, this.explicitRefreshBlankPostDelay,
				this.screenPowerOffDelay, this.vcom
		);
	}

	@NonNull
	public AdvancedSettings withExplicitRefreshPostDelay(int explicitRefreshRegularPostDelay, int explicitRefreshBlankPostDelay)
	{
		return new AdvancedSettings(
				this.dragStartDelayStatic, this.dragStartDelayMoving, this.dragStartDistance, this.dragEndDelay,
				this.navbarManualFullRefreshDelay,
				explicitRefreshRegularPostDelay, explicitRefreshBlankPostDelay,
				this.screenPowerOffDelay, this.vcom
		);
	}

	@NonNull
	public AdvancedSettings withScreenPowerOffDelay(int screenPowerOffDelay)
	{
		return new AdvancedSettings(
				this.dragStartDelayStatic, this.dragStartDelayMoving, this.dragStartDistance, this.dragEndDelay,
				this.navbarManualFullRefreshDelay,
				this.explicitRefreshRegularPostDelay, this.explicitRefreshBlankPostDelay,
				screenPowerOffDelay, this.vcom
		);
	}

	@NonNull
	public AdvancedSettings withVcom(int vcom)
	{
		return new AdvancedSettings(
				this.dragStartDelayStatic, this.dragStartDelayMoving, this.dragStartDistance, this.dragEndDelay,
				this.navbarManualFullRefreshDelay,
				this.explicitRefreshRegularPostDelay, this.explicitRefreshBlankPostDelay,
				this.screenPowerOffDelay, vcom
		);
	}

	public static final AdvancedSettings DEFAULT = new AdvancedSettings(
			1000, 100, 20, 1000,
			500,
			50, 500,
			2000, -1300
	);
}