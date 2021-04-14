package one.microstream.exceptions;

/**
 *
 * 
 */
public class NotAnArrayException extends ClassCastException
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Class<?> wrongClass;
	private final Throwable cause;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public NotAnArrayException()
	{
		super();
		this.wrongClass = null;
		this.cause = null;
	}

	public NotAnArrayException(final String message, final Throwable cause)
	{
		super(message);
		this.wrongClass = null;
		this.cause = cause;
	}

	public NotAnArrayException(final String message)
	{
		super(message);
		this.wrongClass = null;
		this.cause = null;
	}

	public NotAnArrayException(final Throwable cause)
	{
		super();
		this.wrongClass = null;
		this.cause = cause;
	}

	public NotAnArrayException(final Class<?> wrongClass)
	{
		super();
		this.wrongClass = wrongClass;
		this.cause = null;
	}

	public NotAnArrayException(final Class<?> wrongClass, final Throwable cause)
	{
		super();
		this.wrongClass = wrongClass;
		this.cause = cause;
	}

	public NotAnArrayException(final Class<?> wrongClass, final String message)
	{
		super(message);
		this.wrongClass = wrongClass;
		this.cause = null;
	}

	public NotAnArrayException(final Class<?> wrongClass, final String message, final Throwable cause)
	{
		super(message);
		this.wrongClass = wrongClass;
		this.cause = cause;
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	public Class<?> getWrongClass()
	{
		return this.wrongClass;
	}

	@Override
	public synchronized Throwable getCause()
	{
		return this.cause;
	}

	@Override
	public String getMessage()
	{
		return "Wrong Class: " + this.wrongClass.getName();
	}

}
