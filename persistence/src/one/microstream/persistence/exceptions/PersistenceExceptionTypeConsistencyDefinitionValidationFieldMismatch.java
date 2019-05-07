package one.microstream.persistence.exceptions;

import java.lang.reflect.Field;

public class PersistenceExceptionTypeConsistencyDefinitionValidationFieldMismatch
extends PersistenceExceptionTypeConsistencyDefinitionValidation
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Field actualField ;
	private final Field definedField;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceExceptionTypeConsistencyDefinitionValidationFieldMismatch(
		final Field actualField ,
		final Field definedField
	)
	{
		this(actualField, definedField, null, null);
	}

	public PersistenceExceptionTypeConsistencyDefinitionValidationFieldMismatch(
		final Field actualField ,
		final Field definedField,
		final String message
	)
	{
		this(actualField, definedField, message, null);
	}

	public PersistenceExceptionTypeConsistencyDefinitionValidationFieldMismatch(
		final Field actualField ,
		final Field definedField,
		final Throwable cause
	)
	{
		this(actualField, definedField, null, cause);
	}

	public PersistenceExceptionTypeConsistencyDefinitionValidationFieldMismatch(
		final Field actualField ,
		final Field definedField,
		final String message, final Throwable cause
	)
	{
		this(actualField, definedField, message, cause, true, true);
	}

	public PersistenceExceptionTypeConsistencyDefinitionValidationFieldMismatch(
		final Field actualField ,
		final Field definedField,
		final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
		this.actualField  = actualField ;
		this.definedField = definedField;
	}



	///////////////////////////////////////////////////////////////////////////
	// getters //
	////////////

	public Object getActualType()
	{
		return this.actualField;
	}

	public Object getDefinedType()
	{
		return this.definedField;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public String getMessage()
	{
		return "Field mismatch: actual field = \"" + this.actualField
			+ "\", defined field = \"" + this.definedField + "\"."
			+ (super.getMessage() != null ? " Details: " + super.getMessage() : "")
		;
	}



}
