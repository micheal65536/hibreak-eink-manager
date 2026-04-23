package micheal65536.hibreak.einkmanager.core;

public abstract class RawTouchReader extends LinuxInputEventReader
{
	private boolean hasPointerDown = false;
	private int currentEventPointerIndex;
	private int primaryPointerX;
	private int primaryPointerY;
	private boolean secondPointerDown;

	public RawTouchReader()
	{
		super("/dev/input/event2");
	}

	@Override
	final void onEvent(short type, short code, int value)
	{
		if (!this.hasPointerDown)
		{
			if (type == 0x0001 && code == 0x014a && value == 1)
			{
				this.hasPointerDown = true;
				this.currentEventPointerIndex = 0;
				this.primaryPointerX = -1;
				this.primaryPointerY = -1;
				this.secondPointerDown = false;
			}
		}
		else
		{
			if (type == 0x0001 && code == 0x014a && value == 0)
			{
				this.hasPointerDown = false;
				this.onLastPointerUp();
			}
			else
			{
				if (type == 0x0003 && code == 0x002f)
				{
					this.currentEventPointerIndex = value;
				}

				if (this.currentEventPointerIndex == 0)
				{
					if (!this.secondPointerDown)
					{
						boolean alreadyHavePosition = this.primaryPointerX != -1 && this.primaryPointerY != -1;
						boolean moved = false;
						if (type == 0x0003 && code == 0x0035)
						{
							this.primaryPointerX = value;
							moved = true;
						}
						else if (type == 0x0003 && code == 0x0036)
						{
							this.primaryPointerY = value;
							moved = true;
						}
						if (moved)
						{
							if (this.primaryPointerX != -1 && this.primaryPointerY != -1)
							{
								if (alreadyHavePosition)
								{
									this.onFirstPointerMove(this.primaryPointerX, this.primaryPointerY);
								}
								else
								{
									this.onFirstPointerDown(this.primaryPointerX, this.primaryPointerY);
								}
							}
						}
					}
				}
				else
				{
					if (!this.secondPointerDown)
					{
						this.secondPointerDown = true;
						this.onSecondPointerDown();
					}
				}
			}
		}
	}

	public abstract void onFirstPointerDown(int x, int y);

	public abstract void onFirstPointerMove(int x, int y);

	public abstract void onSecondPointerDown();

	public abstract void onLastPointerUp();
}