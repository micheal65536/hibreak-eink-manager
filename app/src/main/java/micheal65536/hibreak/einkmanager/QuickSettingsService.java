package micheal65536.hibreak.einkmanager;

import android.app.PendingIntent;
import android.content.Intent;
import android.service.quicksettings.TileService;

import androidx.core.service.quicksettings.PendingIntentActivityWrapper;
import androidx.core.service.quicksettings.TileServiceCompat;

import micheal65536.hibreak.einkmanager.ui.QuickSettingsDialogActivity;

public final class QuickSettingsService extends TileService
{
	@Override
	public void onClick()
	{
		Intent intent = new Intent(this, QuickSettingsDialogActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
		TileServiceCompat.startActivityAndCollapse(this, new PendingIntentActivityWrapper(this, 0, intent, PendingIntent.FLAG_ONE_SHOT, true));
	}
}