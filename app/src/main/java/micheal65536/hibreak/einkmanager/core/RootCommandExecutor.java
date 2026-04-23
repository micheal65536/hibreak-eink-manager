package micheal65536.hibreak.einkmanager.core;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

final class RootCommandExecutor
{
	@NonNull
	public static String runRootCommand(@NonNull String command) throws RootExecutionException
	{
		try
		{
			Process process = new ProcessBuilder("su", "-c", command).redirectErrorStream(true).start();
			ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
			InputStream inputStream = process.getInputStream();
			int b;
			while ((b = inputStream.read()) != -1)
			{
				outputBuffer.write(b);
			}
			while (process.isAlive())
			{
				try
				{
					process.waitFor();
				}
				catch (InterruptedException exception)
				{
					continue;
				}
			}
			if (process.exitValue() != 0)
			{
				throw new RootExecutionException();
			}
			return new String(outputBuffer.toByteArray(), StandardCharsets.UTF_8);
		}
		catch (RootExecutionException exception)
		{
			throw exception;
		}
		catch (Exception exception)
		{
			throw new RootExecutionException(exception);
		}
	}

	@NonNull
	public static Process startRootCommand(@NonNull String command) throws RootExecutionException
	{
		try
		{
			return new ProcessBuilder("su", "-c", command).start();
		}
		catch (IOException exception)
		{
			throw new RootExecutionException(exception);
		}
	}

	public static final class RootExecutionException extends Exception
	{
		public RootExecutionException()
		{
			super();
		}

		public RootExecutionException(Throwable cause)
		{
			super(cause);
		}
	}
}