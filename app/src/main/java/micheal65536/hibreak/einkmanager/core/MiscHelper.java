package micheal65536.hibreak.einkmanager.core;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

final class MiscHelper
{
	private static final String TAG = "MiscHelper";

	public static void disableHWOverlays()
	{
		Log.d(TAG, "disable hw overlays");
		try
		{
			if (RootCommandExecutor.runRootCommand("service call SurfaceFlinger 1008 i32 1").equals("Result: Parcel(NULL)\n"))
			{
				Log.d(TAG, "disable hw overlays success");
			}
		}
		catch (RootCommandExecutor.RootExecutionException exception)
		{
			Log.e(TAG, "command execution failed");
		}
	}

	public static void surfaceFlingerFullRefresh()
	{
		Log.d(TAG, "trigger full refresh");
		try
		{
			if (RootCommandExecutor.runRootCommand("service call SurfaceFlinger 1006").equals("Result: Parcel(NULL)\n"))
			{
				Log.d(TAG, "trigger full refresh success");
			}
		}
		catch (RootCommandExecutor.RootExecutionException exception)
		{
			Log.e(TAG, "command execution failed");
		}
	}

	@Nullable
	public static String getprop(@NonNull String name)
	{
		Log.d(TAG, String.format("getprop %s", name));
		try
		{
			String string = RootCommandExecutor.runRootCommand(String.format("getprop %s", name));
			if (string.length() > 1 && string.substring(string.length() - 1, string.length()).equals("\n"))
			{
				Log.d(TAG, "getprop success");
				return string.substring(0, string.length() - 1);
			}
		}
		catch (RootCommandExecutor.RootExecutionException exception)
		{
			Log.e(TAG, "command execution failed");
		}
		Log.e(TAG, "getprop fail");
		return null;
	}

	public static void setprop(@NonNull String name, @NonNull String value)
	{
		Log.d(TAG, String.format("setprop %s %s", name, value));
		try
		{
			if (RootCommandExecutor.runRootCommand(String.format("setprop %s %s", name, value)).equals("\n"))
			{
				Log.d(TAG, "setprop success");
			}
		}
		catch (RootCommandExecutor.RootExecutionException exception)
		{
			Log.e(TAG, "command execution failed");
		}
	}

	public static void sleep(long millis)
	{
		try
		{
			Thread.sleep(millis);
		}
		catch (InterruptedException exception)
		{
			Log.e(TAG, "interrupted during sleep");
		}
	}
}