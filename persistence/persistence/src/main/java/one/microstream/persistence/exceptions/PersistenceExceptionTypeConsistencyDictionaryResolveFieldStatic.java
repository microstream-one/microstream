package one.microstream.persistence.exceptions;

import java.lang.reflect.Field;

public class PersistenceExceptionTypeConsistencyDictionaryResolveFieldStatic
extends PersistenceExceptionTypeConsistencyDictionaryResolveField
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceExceptionTypeConsistencyDictionaryResolveFieldStatic(final Field field)
	{
		this(field, null, null);
	}

	public PersistenceExceptionTypeConsistencyDictionaryResolveFieldStatic(final Field field, final String message)
	{
		this(field, message, null);
	}

	public PersistenceExceptionTypeConsistencyDictionaryResolveFieldStatic(final Field field, final Throwable cause)
	{
		this(field, null, cause);
	}

	public PersistenceExceptionTypeConsistencyDictionaryResolveFieldStatic(
		final Field     field  ,
		final String    message,
		final Throwable cause
	)
	{
		this(field, message, cause, true, true);
	}

	public PersistenceExceptionTypeConsistencyDictionaryResolveFieldStatic(final Field field,
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
		return "Not a static (non-final) field: " + this.getField() + "."
			+ (super.getMessage() != null ? " Details: " + super.getMessage() : "")
		;
	}



}
