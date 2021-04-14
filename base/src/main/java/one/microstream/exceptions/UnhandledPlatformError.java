package one.microstream.exceptions;

public class UnhandledPlatformError extends Error
{
	public UnhandledPlatformError()
	{
		super();
	}

	public UnhandledPlatformError(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public UnhandledPlatformError(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	public UnhandledPlatformError(final String message)
	{
		super(message);
	}

	public UnhandledPlatformError(final Throwable cause)
	{
		super(cause);
	}
	
}
