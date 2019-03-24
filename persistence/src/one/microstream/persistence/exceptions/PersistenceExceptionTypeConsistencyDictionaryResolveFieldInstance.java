package one.microstream.persistence.exceptions;

import java.lang.reflect.Field;

public class PersistenceExceptionTypeConsistencyDictionaryResolveFieldInstance
extends PersistenceExceptionTypeConsistencyDictionaryResolveField
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceExceptionTypeConsistencyDictionaryResolveFieldInstance(final Field field)
	{
		this(field, null, null);
	}

	public PersistenceExceptionTypeConsistencyDictionaryResolveFieldInstance(final Field field, final String message)
	{
		this(field, message, null);
	}

	public PersistenceExceptionTypeConsistencyDictionaryResolveFieldInstance(final Field field, final Throwable cause)
	{
		this(field, null, cause);
	}

	public PersistenceExceptionTypeConsistencyDictionaryResolveFieldInstance(
		final Field     field  ,
		final String    message,
		final Throwable cause
	)
	{
		this(field, message, cause, true, true);
	}

	public PersistenceExceptionTypeConsistencyDictionaryResolveFieldInstance(final Field field,
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(field, message, cause, enableSuppression, writableStackTrace);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public String getMessage()
	{
		return "Not a instance (non-static) field: " + this.getField() + "."
			+ (super.getMessage() != null ? " Details: " + super.getMessage() : "")
		;
	}



}
