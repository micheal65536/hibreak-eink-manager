package micheal65536.hibreak.einkmanager.core;

import android.util.Log;

import androidx.annotation.Nullable;

final class ColorMatrixHelper
{
	private static final String TAG = "ColorMatrixHelper";

	public static void applyColorMatrix(@Nullable ColorMatrix colorMatrix)
	{
		Log.d(TAG, String.format("apply color matrix"));
		try
		{
			if (colorMatrix != null)
			{
				float[] matrix = new float[]{
						colorMatrix.redScale, 0.0f, 0.0f, 0.0f,
						0.0f, colorMatrix.greenScale, 0.0f, 0.0f,
						0.0f, 0.0f, colorMatrix.blueScale, 0.0f,
						colorMatrix.redOffset, colorMatrix.greenOffset, colorMatrix.blueOffset, 1.0f
				};
				StringBuilder matrixStringBuilder = new StringBuilder();
				for (float v : matrix)
				{
					matrixStringBuilder.append(" f ");
					matrixStringBuilder.append(v);
				}
				if (RootCommandExecutor.runRootCommand("service call SurfaceFlinger 1015 i32 1" + matrixStringBuilder.toString()).equals("Result: Parcel(NULL)\n"))
				{
					Log.d(TAG, "apply color matrix success");
					return;
				}
			}
			else
			{
				if (RootCommandExecutor.runRootCommand("service call SurfaceFlinger 1015 i32 0").equals("Result: Parcel(NULL)\n"))
				{
					Log.d(TAG, "clear color map success");
					return;
				}
			}
		}
		catch (RootCommandExecutor.RootExecutionException exception)
		{
			Log.e(TAG, "command execution failed");
		}
		Log.e(TAG, "apply color matrix fail");
	}
}