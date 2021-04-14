package one.microstream.persistence.exceptions;

import java.lang.reflect.Field;

public class PersistenceExceptionTypeConsistencyDictionaryResolveFieldType
extends PersistenceExceptionTypeConsistencyDictionaryResolveField
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Class<?> dictionaryFieldType;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceExceptionTypeConsistencyDictionaryResolveFieldType(
		final Field    field,
		final Class<?> dictionaryFieldType
	)
	{
		this(field, dictionaryFieldType, null, null);
	}

	public PersistenceExceptionTypeConsistencyDictionaryResolveFieldType(
		final Field    field,
		final Class<?> dictionaryFieldType,
		final String message
	)
	{
		this(field, dictionaryFieldType, message, null);
	}

	public PersistenceExceptionTypeConsistencyDictionaryResolveFieldType(
		final Field    field,
		final Class<?> dictionaryFieldType,
		final Throwable cause
	)
	{
		this(field, dictionaryFieldType, null, cause);
	}

	public PersistenceExceptionTypeConsistencyDictionaryResolveFieldType(
		final Field    field,
		final Class<?> dictionaryFieldType,
		final String message, final Throwable cause
	)
	{
		this(field, dictionaryFieldType, message, cause, true, true);
	}

	public PersistenceExceptionTypeConsistencyDictionaryResolveFieldType(
		final Field     field,
		final Class<?>  dictionaryFieldType,
		final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace
	)
	{
		super(field, message, cause, enableSuppression, writableStackTrace);
		this.dictionaryFieldType = dictionaryFieldType;
	}



	///////////////////////////////////////////////////////////////////////////
	// getters //
	////////////

	public Class<?> getDictionaryFieldType()
	{
		return this.dictionaryFieldType;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public String getMessage()
	{
		return "Error on validation of field " + this.getField()
			+ ": when matching dictionary field type (" + this.dictionaryFieldType
			+ ") with type of actual field (" + this.getField().getType() + ")."
			+ (super.getMessage() != null ? " Details: " + super.getMessage() : "")
		;
	}



}
