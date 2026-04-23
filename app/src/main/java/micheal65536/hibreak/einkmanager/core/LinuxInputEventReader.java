package micheal65536.hibreak.einkmanager.core;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

abstract class LinuxInputEventReader
{
	private static final String TAG = "InputEventReader";

	private Process process;
	private Thread thread;
	private boolean stopped = false;

	public LinuxInputEventReader(@NonNull String path)
	{
		Looper looper = Looper.myLooper();
		if (looper == null)
		{
			throw new IllegalStateException();
		}
		Handler handler = new Handler(looper);

		try
		{
			this.process = RootCommandExecutor.startRootCommand(String.format("cat %s", path));
		}
		catch (RootCommandExecutor.RootExecutionException exception)
		{
			Log.e(TAG, "failed running root command");
			this.stopped = true;
			return;
		}

		this.thread = new Thread(() ->
		{
			Log.d(TAG, String.format("starting reader on %s", path));

			InputStream inputStream = this.process.getInputStream();
			byte[] eventBytes = new byte[24];
			ByteBuffer byteBuffer = ByteBuffer.wrap(eventBytes);
			byteBuffer.order(ByteOrder.nativeOrder());
			while (!Thread.interrupted())
			{
				try
				{
					for (int i = 0; i < eventBytes.length; i++)
					{
						int v = inputStream.read();
						if (v == -1)
						{
							break;
						}
						eventBytes[i] = (byte) v;
					}
				}
				catch (IOException exception)
				{
					break;
				}
				if (Thread.interrupted())
				{
					break;
				}

				byteBuffer.position(16);
				short type = byteBuffer.getShort();
				short code = byteBuffer.getShort();
				int value = byteBuffer.getInt();
				handler.post(() ->
				{
					if (!this.stopped)
					{
						this.onEvent(type, code, value);
					}
				});
			}

			this.process.destroyForcibly();

			Log.d(TAG, String.format("stopped reader on %s", path));
		});
		this.thread.start();
	}

	public final void stop()
	{
		Log.d(TAG, "requesting stop");
		this.stopped = true;
		this.thread.interrupt();
		this.process.destroyForcibly();
	}

	abstract void onEvent(short type, short code, int value);
}