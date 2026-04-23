package micheal65536.hibreak.einkmanager.core;

import androidx.annotation.Nullable;

import micheal65536.hibreak.einkmanager.data.ColorMap;
import micheal65536.hibreak.einkmanager.data.ContrastMap;

public final class ColorMatrix
{
	public final float redScale;
	public final float redOffset;
	public final float greenScale;
	public final float greenOffset;
	public final float blueScale;
	public final float blueOffset;

	public ColorMatrix(float redScale, float redOffset, float greenScale, float greenOffset, float blueScale, float blueOffset)
	{
		this.redScale = redScale;
		this.redOffset = redOffset;
		this.greenScale = greenScale;
		this.greenOffset = greenOffset;
		this.blueScale = blueScale;
		this.blueOffset = blueOffset;
	}

	@Nullable
	public static ColorMatrix createColorMatrix(@Nullable ContrastMap globalContrastMap, @Nullable ContrastMap fastModeContrastMap, @Nullable ColorMap appColorMap, @Nullable ColorMap forcedColorMap)
	{
		if (globalContrastMap != null && globalContrastMap.min == 0 && globalContrastMap.max == 100)
		{
			globalContrastMap = null;
		}
		if (fastModeContrastMap != null && fastModeContrastMap.min == 0 && fastModeContrastMap.max == 100)
		{
			fastModeContrastMap = null;
		}
		if (appColorMap != null && appColorMap.redMin == 0 && appColorMap.redMax == 100 && appColorMap.greenMin == 0 && appColorMap.greenMax == 100 && appColorMap.blueMin == 0 && appColorMap.blueMax == 100)
		{
			appColorMap = null;
		}
		if (forcedColorMap != null && forcedColorMap.redMin == 0 && forcedColorMap.redMax == 100 && forcedColorMap.greenMin == 0 && forcedColorMap.greenMax == 100 && forcedColorMap.blueMin == 0 && forcedColorMap.blueMax == 100)
		{
			forcedColorMap = null;
		}

		if (globalContrastMap == null && fastModeContrastMap == null && appColorMap == null && forcedColorMap == null)
		{
			return null;
		}

		float redMin = 0.0f;
		float redMax = 1.0f;
		float greenMin = 0.0f;
		float greenMax = 1.0f;
		float blueMin = 0.0f;
		float blueMax = 1.0f;
		float t;
		if (appColorMap != null)
		{
			t = redMin + (redMax - redMin) * (appColorMap.redMax / 100.0f);
			redMin = redMin + (redMax - redMin) * (appColorMap.redMin / 100.0f);
			redMax = t;
			t = greenMin + (greenMax - greenMin) * (appColorMap.greenMax / 100.0f);
			greenMin = greenMin + (greenMax - greenMin) * (appColorMap.greenMin / 100.0f);
			greenMax = t;
			t = blueMin + (blueMax - blueMin) * (appColorMap.blueMax / 100.0f);
			blueMin = blueMin + (blueMax - blueMin) * (appColorMap.blueMin / 100.0f);
			blueMax = t;
		}
		if (forcedColorMap != null)
		{
			t = redMin + (redMax - redMin) * (forcedColorMap.redMax / 100.0f);
			redMin = redMin + (redMax - redMin) * (forcedColorMap.redMin / 100.0f);
			redMax = t;
			t = greenMin + (greenMax - greenMin) * (forcedColorMap.greenMax / 100.0f);
			greenMin = greenMin + (greenMax - greenMin) * (forcedColorMap.greenMin / 100.0f);
			greenMax = t;
			t = blueMin + (blueMax - blueMin) * (forcedColorMap.blueMax / 100.0f);
			blueMin = blueMin + (blueMax - blueMin) * (forcedColorMap.blueMin / 100.0f);
			blueMax = t;
		}
		if (globalContrastMap != null)
		{
			t = redMin + (redMax - redMin) * (globalContrastMap.max / 100.0f);
			redMin = redMin + (redMax - redMin) * (globalContrastMap.min / 100.0f);
			redMax = t;
			t = greenMin + (greenMax - greenMin) * (globalContrastMap.max / 100.0f);
			greenMin = greenMin + (greenMax - greenMin) * (globalContrastMap.min / 100.0f);
			greenMax = t;
			t = blueMin + (blueMax - blueMin) * (globalContrastMap.max / 100.0f);
			blueMin = blueMin + (blueMax - blueMin) * (globalContrastMap.min / 100.0f);
			blueMax = t;
		}
		if (fastModeContrastMap != null)
		{
			t = redMin + (redMax - redMin) * (fastModeContrastMap.max / 100.0f);
			redMin = redMin + (redMax - redMin) * (fastModeContrastMap.min / 100.0f);
			redMax = t;
			t = greenMin + (greenMax - greenMin) * (fastModeContrastMap.max / 100.0f);
			greenMin = greenMin + (greenMax - greenMin) * (fastModeContrastMap.min / 100.0f);
			greenMax = t;
			t = blueMin + (blueMax - blueMin) * (fastModeContrastMap.max / 100.0f);
			blueMin = blueMin + (blueMax - blueMin) * (fastModeContrastMap.min / 100.0f);
			blueMax = t;
		}

		float redScale = redMax == redMin ? 0.0f : 1.0f / (redMax - redMin);
		float redOffset = redMax == redMin ? 1.0f : -(redMin * redScale);
		float greenScale = greenMax == greenMin ? 0.0f : 1.0f / (greenMax - greenMin);
		float greenOffset = greenMax == greenMin ? 1.0f : -(greenMin * greenScale);
		float blueScale = blueMax == blueMin ? 0.0f : 1.0f / (blueMax - blueMin);
		float blueOffset = blueMax == blueMin ? 1.0f : -(blueMin * blueScale);
		return new ColorMatrix(redScale, redOffset, greenScale, greenOffset, blueScale, blueOffset);
	}
}