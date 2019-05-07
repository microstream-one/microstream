package one.microstream.persistence.exceptions;

import one.microstream.chars.XChars;
import one.microstream.persistence.types.PersistenceTypeHandler;


public class PersistenceExceptionTypeHandlerConsistencyConflictedTypeId
extends PersistenceExceptionTypeHandlerConsistency
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	final long                        typeId           ;
	final PersistenceTypeHandler<?, ?> actualTypeHandler;
	final PersistenceTypeHandler<?, ?> passedTypeHandler;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceExceptionTypeHandlerConsistencyConflictedTypeId(
		final long                        typeId           ,
		final PersistenceTypeHandler<?, ?> actualTypeHandler,
		final PersistenceTypeHandler<?, ?> passedTypeHandler
	)
	{
		this(typeId, actualTypeHandler, passedTypeHandler, null, null);
	}

	public PersistenceExceptionTypeHandlerConsistencyConflictedTypeId(
		final long                        typeId           ,
		final PersistenceTypeHandler<?, ?> actualTypeHandler,
		final PersistenceTypeHandler<?, ?> passedTypeHandler,
		final String message
	)
	{
		this(typeId, actualTypeHandler, passedTypeHandler, message, null);
	}

	public PersistenceExceptionTypeHandlerConsistencyConflictedTypeId(
		final long                        typeId           ,
		final PersistenceTypeHandler<?, ?> actualTypeHandler,
		final PersistenceTypeHandler<?, ?> passedTypeHandler,
		final Throwable cause
	)
	{
		this(typeId, actualTypeHandler, passedTypeHandler, null, cause);
	}

	public PersistenceExceptionTypeHandlerConsistencyConflictedTypeId(
		final long                        typeId           ,
		final PersistenceTypeHandler<?, ?> actualTypeHandler,
		final PersistenceTypeHandler<?, ?> passedTypeHandler,
		final String message, final Throwable cause
	)
	{
		this(typeId, actualTypeHandler, passedTypeHandler, message, cause, true, true);
	}

	public PersistenceExceptionTypeHandlerConsistencyConflictedTypeId(
		final long                        typeId           ,
		final PersistenceTypeHandler<?, ?> actualTypeHandler,
		final PersistenceTypeHandler<?, ?> passedTypeHandler,
		final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
		this.typeId            = typeId           ;
		this.actualTypeHandler = actualTypeHandler;
		this.passedTypeHandler = passedTypeHandler;
	}



	///////////////////////////////////////////////////////////////////////////
	// getters //
	////////////

	public long getTypeId()
	{
		return this.typeId;
	}

	public PersistenceTypeHandler<?, ?> getActualTypeHandler()
	{
		return this.actualTypeHandler;
	}

	public PersistenceTypeHandler<?, ?> getPassedTypeHandler()
	{
		return this.passedTypeHandler;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public String getMessage()
	{
		return "Type id \"" + this.typeId + "\" is already associated to type handler "
			+ XChars.systemString(this.actualTypeHandler)
			+ ", cannot be associated to type handler \"" + XChars.systemString(this.passedTypeHandler) + "\" as well."
			+ (super.getMessage() != null ? " Details: " + super.getMessage() : "")
		;
	}



}
