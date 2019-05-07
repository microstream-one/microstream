package one.microstream.persistence.exceptions;


public class PersistenceExceptionTypeHandlerConsistencyUnhandledTypeId
extends PersistenceExceptionTypeHandlerConsistency
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	final long typeId ;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceExceptionTypeHandlerConsistencyUnhandledTypeId(
		final long typeId
	)
	{
		this(typeId, null, null);
	}

	public PersistenceExceptionTypeHandlerConsistencyUnhandledTypeId(
		final long typeId,
		final String message
	)
	{
		this(typeId, message, null);
	}

	public PersistenceExceptionTypeHandlerConsistencyUnhandledTypeId(
		final long typeId,
		final Throwable cause
	)
	{
		this(typeId, null, cause);
	}

	public PersistenceExceptionTypeHandlerConsistencyUnhandledTypeId(
		final long typeId,
		final String message, final Throwable cause
	)
	{
		this(typeId, message, cause, true, true);
	}

	public PersistenceExceptionTypeHandlerConsistencyUnhandledTypeId(
		final long typeId,
		final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
		this.typeId            = typeId           ;
	}



	///////////////////////////////////////////////////////////////////////////
	// getters //
	////////////

	public long getTypeId()
	{
		return this.typeId;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public String getMessage()
	{
		return "No type handler found for type id \"" + this.typeId + "\"."
			+ (super.getMessage() != null ? " Details: " + super.getMessage() : "")
		;
	}



}
