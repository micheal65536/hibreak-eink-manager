package micheal65536.hibreak.einkmanager.core;

import android.util.Log;

import java.util.Locale;
import java.util.regex.Pattern;

final class EInkHelper
{
	private static final String TAG = "EInkHelper";

	public static int getCurrentRefreshMode()
	{
		try
		{
			String string = RootCommandExecutor.runRootCommand("cat /sys/kernel/debug/eink_debug/global_mode");
			try
			{
				int mode = Integer.parseInt(Pattern.compile("^mode=0x([0-9a-f]{1,})$").matcher(string).group(1), 16);
				Log.d(TAG, String.format("get refresh mode %d", mode));
				return mode;
			}
			catch (Exception exception)
			{
				Log.e(TAG, String.format("unable to parse refresh mode string \"%s\"", string));
			}
		}
		catch (RootCommandExecutor.RootExecutionException exception)
		{
			Log.e(TAG, "command execution failed");
		}
		Log.e(TAG, "get refresh mode fail");
		return -1;
	}

	public static void setRefreshMode(int mode)
	{
		Log.d(TAG, String.format("set refresh mode %d", mode));
		if (mode < 0)
		{
			throw new IllegalArgumentException();
		}

		try
		{
			RootCommandExecutor.runRootCommand(String.format(Locale.ROOT, "echo %d > /sys/kernel/debug/eink_debug/global_mode", mode));
			Log.d(TAG, "set refresh mode success");
			return;
		}
		catch (RootCommandExecutor.RootExecutionException exception)
		{
			Log.e(TAG, "command execution failed");
		}
		Log.e(TAG, "set refresh mode fail");
	}

	public static boolean getCurrentDisplayPowerState()
	{
		try
		{
			String string = RootCommandExecutor.runRootCommand("cat /sys/bus/i2c/devices/2-0062/SYV776A_reg");
			try
			{
				int power = Integer.parseInt(Pattern.compile("^\\[7\\] = ([0-9a-f]{1,})$").matcher(string).group(1), 16);
				Log.d(TAG, String.format("get power %d", power));
				return power != 0;
			}
			catch (Exception exception)
			{
				Log.e(TAG, String.format("unable to parse reg string \"%s\"", string));
			}
		}
		catch (RootCommandExecutor.RootExecutionException exception)
		{
			Log.e(TAG, "command execution failed");
		}
		Log.e(TAG, "get power fail");
		return false;
	}

	public static void setDisplayPowerState(boolean power)
	{
		Log.d(TAG, String.format("set power %d", power ? 1 : 0));
		try
		{
			RootCommandExecutor.runRootCommand(String.format(Locale.ROOT, "echo %d > /sys/bus/i2c/devices/2-0062/SYV776A_turnOnOff_power", power ? 1 : 0));
			Log.d(TAG, "set power success");
			return;
		}
		catch (RootCommandExecutor.RootExecutionException exception)
		{
			Log.e(TAG, "command execution failed");
		}
		Log.e(TAG, "set power fail");
	}

	public static int getCurrentVcomSetting()
	{
		try
		{
			String string = RootCommandExecutor.runRootCommand("cat /sys/kernel/debug/eink_debug/vcom");
			try
			{
				int vcom = Integer.parseInt(string, 10);
				Log.d(TAG, String.format("get vcom %d", vcom));
				return vcom;
			}
			catch (Exception exception)
			{
				Log.e(TAG, String.format("unable to parse vcom string \"%s\"", string));
			}
		}
		catch (RootCommandExecutor.RootExecutionException exception)
		{
			Log.e(TAG, "command execution failed");
		}
		Log.e(TAG, "get vcom fail");
		return 0;
	}

	public static void setVcom(int vcom)
	{
		if (vcom > 0 || vcom < -1500)
		{
			Log.w(TAG, String.format("not setting vcom to %d", vcom));
			return;
		}

		Log.d(TAG, String.format("set vcom %d", vcom));
		try
		{
			RootCommandExecutor.runRootCommand(String.format(Locale.ROOT, "echo %d > /sys/bus/i2c/devices/2-0062/SYV776A_store_vcom", vcom));
			Log.d(TAG, "set vcom success");
			return;
		}
		catch (RootCommandExecutor.RootExecutionException exception)
		{
			Log.e(TAG, "command execution failed");
		}
		Log.e(TAG, "set vcom fail");
	}
}