package one.microstream.persistence.exceptions;

import java.lang.reflect.Field;

public class PersistenceExceptionTypeConsistencyDictionaryResolveField
extends PersistenceExceptionTypeConsistencyDictionaryResolve
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Field field;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceExceptionTypeConsistencyDictionaryResolveField(final Field field)
	{
		this(field, null, null);
	}

	public PersistenceExceptionTypeConsistencyDictionaryResolveField(final Field field, final String message)
	{
		this(field, message, null);
	}

	public PersistenceExceptionTypeConsistencyDictionaryResolveField(final Field field, final Throwable cause)
	{
		this(field, null, cause);
	}

	public PersistenceExceptionTypeConsistencyDictionaryResolveField(
		final Field     field  ,
		final String    message,
		final Throwable cause
	)
	{
		this(field, message, cause, true, true);
	}

	public PersistenceExceptionTypeConsistencyDictionaryResolveField(final Field field,
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
		this.field = field;
	}



	///////////////////////////////////////////////////////////////////////////
	// getters //
	////////////

	public Field getField()
	{
		return this.field;
	}



}
