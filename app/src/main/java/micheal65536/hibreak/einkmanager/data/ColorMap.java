package micheal65536.hibreak.einkmanager.data;

public final class ColorMap
{
	public final int redMin;
	public final int redMax;
	public final int greenMin;
	public final int greenMax;
	public final int blueMin;
	public final int blueMax;

	public ColorMap(int redMin, int redMax, int greenMin, int greenMax, int blueMin, int blueMax)
	{
		this.redMin = redMin;
		this.redMax = redMax;
		this.greenMin = greenMin;
		this.greenMax = greenMax;
		this.blueMin = blueMin;
		this.blueMax = blueMax;
	}
}