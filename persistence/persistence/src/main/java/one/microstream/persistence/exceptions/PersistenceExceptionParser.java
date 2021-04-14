package one.microstream.persistence.exceptions;

public class PersistenceExceptionParser extends PersistenceException
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final int index;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceExceptionParser(
		final int index
	)
	{
		this(index, null, null);
	}

	public PersistenceExceptionParser(
		final int index,
		final String message
	)
	{
		this(index, message, null);
	}

	public PersistenceExceptionParser(
		final int index,
		final Throwable cause
	)
	{
		this(index, null, cause);
	}

	public PersistenceExceptionParser(
		final int index,
		final String message, final Throwable cause
	)
	{
		this(index, message, cause, true, true);
	}

	public PersistenceExceptionParser(
		final int index,
		final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
		this.index = index;
	}



	///////////////////////////////////////////////////////////////////////////
	// getters //
	////////////

	public int getIndex()
	{
		return this.index;
	}



}
