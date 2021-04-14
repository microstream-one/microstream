package one.microstream.com;

public class ComExceptionTimeout extends ComException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public ComExceptionTimeout()
	{
		super();
	}

	public ComExceptionTimeout(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ComExceptionTimeout(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	public ComExceptionTimeout(final String message)
	{
		super(message);
	}

	public ComExceptionTimeout(final Throwable cause)
	{
		super(cause);
	}

}
