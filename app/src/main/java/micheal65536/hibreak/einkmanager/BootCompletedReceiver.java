package micheal65536.hibreak.einkmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public final class BootCompletedReceiver extends BroadcastReceiver
{
	private static final String TAG = "BootCompletedReceiver";

	@Override
	public void onReceive(Context context, Intent intent)
	{
		Log.i(TAG, "applying settings on boot");
		ApplySettingsService.applySettings(context, false, true, true, true);
	}
}