package micheal65536.hibreak.einkmanager.core;

public abstract class RawFunctionButtonReader extends LinuxInputEventReader
{
	private boolean state = false;

	public RawFunctionButtonReader()
	{
		super("/dev/input/event1");
	}

	@Override
	final void onEvent(short type, short code, int value)
	{
		if (type == 0x0001 && code == 0x0240)
		{
			if (value == 1 || value == 0)
			{
				boolean state = value == 1;
				if (state != this.state)
				{
					this.onStateChange(state);
				}
			}
		}
	}

	public abstract void onStateChange(boolean state);
}