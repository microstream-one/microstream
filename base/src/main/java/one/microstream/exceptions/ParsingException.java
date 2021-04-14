package one.microstream.exceptions;

public class ParsingException extends BaseException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public ParsingException()
	{
		super();
	}

	public ParsingException(final Throwable cause)
	{
		super(cause);
	}

	public ParsingException(final String message)
	{
		super(message);
	}

	public ParsingException(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	public ParsingException(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
}
