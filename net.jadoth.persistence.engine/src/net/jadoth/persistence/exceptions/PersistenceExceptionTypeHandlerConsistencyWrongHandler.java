package net.jadoth.persistence.exceptions;

import net.jadoth.chars.JadothStrings;
import net.jadoth.persistence.types.PersistenceTypeHandler;



public class PersistenceExceptionTypeHandlerConsistencyWrongHandler extends PersistenceExceptionTypeHandlerConsistency
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	final Class<?>                    type       ;
	final PersistenceTypeHandler<?, ?> typeHandler;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public PersistenceExceptionTypeHandlerConsistencyWrongHandler(
		final Class<?>                    type       ,
		final PersistenceTypeHandler<?, ?> typeHandler
	)
	{
		this(type, typeHandler, null, null);
	}

	public PersistenceExceptionTypeHandlerConsistencyWrongHandler(
		final Class<?>                    type       ,
		final PersistenceTypeHandler<?, ?> typeHandler,
		final String message
	)
	{
		this(type, typeHandler, message, null);
	}

	public PersistenceExceptionTypeHandlerConsistencyWrongHandler(
		final Class<?>                    type       ,
		final PersistenceTypeHandler<?, ?> typeHandler,
		final Throwable cause)
	{
		this(type, typeHandler, null, cause);
	}

	public PersistenceExceptionTypeHandlerConsistencyWrongHandler(
		final Class<?>                    type       ,
		final PersistenceTypeHandler<?, ?> typeHandler,
		final String message, final Throwable cause
	)
	{
		this(type, typeHandler, message, cause, true, true);
	}

	public PersistenceExceptionTypeHandlerConsistencyWrongHandler(
		final Class<?> type,
		final PersistenceTypeHandler<?, ?> typeHandler,
		final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
		this.type        = type       ;
		this.typeHandler = typeHandler;
	}



	///////////////////////////////////////////////////////////////////////////
	// getters          //
	/////////////////////

	public Class<?> getType()
	{
		return this.type;
	}

	public PersistenceTypeHandler<?, ?> getTypeHandler()
	{
		return this.typeHandler;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public String getMessage()
	{
		return "Wrong handler for type: \"" + this.type + "\": " + JadothStrings.systemString(this.typeHandler)
			+ " with type \"" + this.typeHandler.type() + "\"."
			+ (super.getMessage() != null ? " Details: " + super.getMessage() : "")
		;
	}



}
