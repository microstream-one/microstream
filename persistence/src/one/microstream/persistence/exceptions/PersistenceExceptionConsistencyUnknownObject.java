package one.microstream.persistence.exceptions;

import one.microstream.chars.XChars;



public class PersistenceExceptionConsistencyUnknownObject extends PersistenceExceptionConsistency
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	final Object object;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceExceptionConsistencyUnknownObject(final Object object)
	{
		this(object, null, null);
	}

	public PersistenceExceptionConsistencyUnknownObject(
		final Object object,
		final String message
	)
	{
		this(object, message, null);
	}

	public PersistenceExceptionConsistencyUnknownObject(
		final Object object,
		final Throwable cause
	)
	{
		this(object, null, cause);
	}

	public PersistenceExceptionConsistencyUnknownObject(
		final Object object,
		final String message, final Throwable cause
	)
	{
		this(object, message, cause, true, true);
	}

	public PersistenceExceptionConsistencyUnknownObject(
		final Object object,
		final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
		this.object = object ;
	}



	///////////////////////////////////////////////////////////////////////////
	// getters //
	////////////

	public Object getObject()
	{
		return this.object;
	}


	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public String getMessage()
	{
		return "Unknown object: " + XChars.systemString(this.object)
			+ (super.getMessage() != null ? " Details: " + super.getMessage() : "")
		;
	}



}
