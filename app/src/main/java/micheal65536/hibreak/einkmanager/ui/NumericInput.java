package micheal65536.hibreak.einkmanager.ui;

import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;

import java.util.function.Consumer;

import micheal65536.hibreak.einkmanager.R;

public final class NumericInput
{
	public static void setValue(@NonNull ViewGroup viewGroup, int value)
	{
		((EditText) viewGroup.findViewById(R.id.value)).setText(valueToString(value));
	}

	public static int getValue(@NonNull ViewGroup viewGroup, int min, int max)
	{
		return stringToValue(((EditText) viewGroup.findViewById(R.id.value)).getText().toString(), min, max);
	}

	public static void setUpCallbacks(@NonNull ViewGroup viewGroup, int min, int max, int step, @NonNull Consumer<Integer> callback)
	{
		((Button) viewGroup.findViewById(R.id.decrement)).setOnClickListener(view ->
		{
			callback.accept(decrementValue(getValue(viewGroup, min, max), step, min));
		});
		((Button) viewGroup.findViewById(R.id.increment)).setOnClickListener(view ->
		{
			callback.accept(incrementValue(getValue(viewGroup, min, max), step, max));
		});
		((EditText) viewGroup.findViewById(R.id.value)).setOnEditorActionListener((view, actionId, event) ->
		{
			if (actionId == EditorInfo.IME_ACTION_DONE)
			{
				callback.accept(getValue(viewGroup, min, max));
				return true;
			}
			return false;
		});
		viewGroup.findViewById(R.id.value).setOnFocusChangeListener((view, hasFocus) ->
		{
			if (!hasFocus)
			{
				callback.accept(getValue(viewGroup, min, max));
			}
		});
	}

	public static void setEnabled(@NonNull ViewGroup viewGroup, boolean enabled)
	{
		viewGroup.findViewById(R.id.decrement).setEnabled(enabled);
		viewGroup.findViewById(R.id.increment).setEnabled(enabled);
		viewGroup.findViewById(R.id.value).setEnabled(enabled);
	}

	@NonNull
	private static String valueToString(int value)
	{
		return Integer.toString(value);
	}

	private static int stringToValue(@NonNull String string, int min, int max)
	{
		try
		{
			int value = Integer.parseInt(string);
			if (value < min)
			{
				value = min;
			}
			if (value > max)
			{
				value = max;
			}
			return value;
		}
		catch (NumberFormatException exception)
		{
			return 0;
		}
	}

	private static int decrementValue(int value, int step, int min)
	{
		if (value % step != 0)
		{
			value = value - (value % step);
		}
		else
		{
			value = value - step;
		}
		if (value < min)
		{
			value = min;
		}
		return value;
	}

	private static int incrementValue(int value, int step, int max)
	{
		if (value % step != 0)
		{
			value = value - (value % step) + step;
		}
		else
		{
			value = value + step;
		}
		if (value > max)
		{
			value = max;
		}
		return value;
	}
}