package one.microstream.memory;

import one.microstream.exceptions.BaseException;

public class MemoryException extends BaseException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public MemoryException()
	{
		super();
	}

	public MemoryException(final String message)
	{
		super(message);
	}

	public MemoryException(final Throwable cause)
	{
		super(cause);
	}

	public MemoryException(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	public MemoryException(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public String assembleDetailString()
	{
		return "Generic memory handling exception";
	}
	
}
