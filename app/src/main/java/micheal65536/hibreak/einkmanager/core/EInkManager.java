package micheal65536.hibreak.einkmanager.core;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import micheal65536.hibreak.einkmanager.data.FullRefreshType;
import micheal65536.hibreak.einkmanager.data.RefreshMode;

public final class EInkManager
{
	private static RefreshMode lastRefreshMode = null;
	private static ColorMatrix lastColorMatrix = null;

	private static int explicitRefreshRegularPostDelay = 30;
	private static int explicitRefreshBlankPostDelay = 500;

	private static final ExecutorService executor = Executors.newSingleThreadExecutor();

	public static void applyRefreshModeAndColorMatrix(@NonNull RefreshMode refreshMode, @Nullable ColorMatrix colorMatrix, boolean force)
	{
		executor.submit(() ->
		{
			boolean changed = false;

			boolean colorMatrixChanged = true;
			if (!force)
			{
				if (colorMatrix == null && lastColorMatrix == null)
				{
					colorMatrixChanged = false;
				}
				if (colorMatrix != null && lastColorMatrix != null && colorMatrix.redScale == lastColorMatrix.redScale && colorMatrix.redOffset == lastColorMatrix.redOffset && colorMatrix.greenScale == lastColorMatrix.greenScale && colorMatrix.greenOffset == lastColorMatrix.greenOffset && colorMatrix.blueScale == lastColorMatrix.blueScale && colorMatrix.blueOffset == lastColorMatrix.blueOffset)
				{
					colorMatrixChanged = false;
				}
			}
			if (colorMatrixChanged)
			{
				ColorMatrixHelper.applyColorMatrix(colorMatrix);
				lastColorMatrix = colorMatrix;
				changed = true;
			}

			if (force || refreshMode != lastRefreshMode)
			{
				int code;
				switch (refreshMode)
				{
					case NORMAL:
						code = 132;
						break;
					case FAST:
						code = 16;
						break;
					case HIGH_QUALITY:
						code = 180;
						break;
					default:
						code = 132;
						break;
				}

				EInkHelper.setRefreshMode(code);
				lastRefreshMode = refreshMode;
				changed = true;
			}

			if (changed)
			{
				MiscHelper.setprop("vendor.xrz.patch.fcmt", "1");
				MiscHelper.setprop("vendor.xrz.patch.frfsh", "1");
				MiscHelper.surfaceFlingerFullRefresh();
				MiscHelper.sleep(explicitRefreshRegularPostDelay);
				MiscHelper.setprop("vendor.xrz.patch.frfsh", "0");
				MiscHelper.setprop("vendor.xrz.patch.fcmt", "0");
			}
		});
	}

	public static void fullRefresh(@NonNull FullRefreshType fullRefreshType)
	{
		executor.submit(() ->
		{
			int refreshCode;
			switch (fullRefreshType)
			{
				case BLANK:
					refreshCode = 1;
					break;
				case INVERT:
					refreshCode = 4;
					break;
				default:
					refreshCode = 1;
					break;
			}

			int regularCode;
			switch (lastRefreshMode)
			{
				case NORMAL:
					regularCode = 132;
					break;
				case FAST:
					regularCode = 16;
					break;
				case HIGH_QUALITY:
					regularCode = 180;
					break;
				default:
					regularCode = 132;
					break;
			}

			MiscHelper.setprop("vendor.xrz.patch.fcmt", "1");
			MiscHelper.setprop("vendor.xrz.patch.frfsh", "1");
			EInkHelper.setRefreshMode(refreshCode);
			MiscHelper.surfaceFlingerFullRefresh();
			MiscHelper.sleep(fullRefreshType == FullRefreshType.BLANK ? explicitRefreshBlankPostDelay : explicitRefreshRegularPostDelay);
			EInkHelper.setRefreshMode(regularCode);
			MiscHelper.surfaceFlingerFullRefresh();
			MiscHelper.sleep(explicitRefreshRegularPostDelay);
			MiscHelper.setprop("vendor.xrz.patch.frfsh", "0");
			MiscHelper.setprop("vendor.xrz.patch.fcmt", "0");
		});
	}

	public static void powerOff()
	{
		executor.submit(() ->
		{
			EInkHelper.setDisplayPowerState(false);
		});
	}

	public static void setVcom(int vcom)
	{
		executor.submit(() ->
		{
			EInkHelper.setVcom(vcom);
		});
	}

	public static void disableHWOverlays()
	{
		executor.submit(() ->
		{
			MiscHelper.disableHWOverlays();
		});
	}

	public static void onCompletion(@NonNull Runnable runnable)
	{
		executor.submit(runnable);
	}

	public static void setExplicitRefreshParamSwitchDelay(int explicitRefreshRegularPostDelay, int explicitRefreshBlankPostDelay)
	{
		executor.submit(() ->
		{
			EInkManager.explicitRefreshRegularPostDelay = explicitRefreshRegularPostDelay;
			EInkManager.explicitRefreshBlankPostDelay = explicitRefreshBlankPostDelay;
		});
	}
}