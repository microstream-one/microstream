package one.microstream.exceptions;

public class MultiCauseException extends BaseException
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final Throwable[] causes;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public MultiCauseException(final Throwable... causes)
	{
		super();
		this.causes = causes;
	}
	
	public MultiCauseException(final Throwable[] causes, final Throwable cause)
	{
		super(cause);
		this.causes = causes;
	}
	
	public MultiCauseException(final Throwable[] causes, final String message)
	{
		super(message);
		this.causes = causes;
	}
	
	public MultiCauseException(final Throwable[] causes, final String message, final Throwable cause)
	{
		super(message, cause);
		this.causes = causes;
	}
	
	public MultiCauseException(
		final Throwable[] causes            ,
		final String      message           ,
		final Throwable   cause             ,
		final boolean     enableSuppression ,
		final boolean     writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
		this.causes = causes;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	public Throwable[] causes()
	{
		return this.causes;
	}
	
}
