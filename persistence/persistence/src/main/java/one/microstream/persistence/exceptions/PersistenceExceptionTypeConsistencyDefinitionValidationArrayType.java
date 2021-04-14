package one.microstream.persistence.exceptions;

public class PersistenceExceptionTypeConsistencyDefinitionValidationArrayType
extends PersistenceExceptionTypeConsistencyDefinitionValidation
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Class<?> arrayType ;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceExceptionTypeConsistencyDefinitionValidationArrayType(
		final Class<?> actualType
	)
	{
		this(actualType, null, null);
	}

	public PersistenceExceptionTypeConsistencyDefinitionValidationArrayType(
		final Class<?> actualType,
		final String message
	)
	{
		this(actualType, message, null);
	}

	public PersistenceExceptionTypeConsistencyDefinitionValidationArrayType(
		final Class<?> actualType,
		final Throwable cause
	)
	{
		this(actualType, null, cause);
	}

	public PersistenceExceptionTypeConsistencyDefinitionValidationArrayType(
		final Class<?> actualType,
		final String message, final Throwable cause
	)
	{
		this(actualType, message, cause, true, true);
	}

	public PersistenceExceptionTypeConsistencyDefinitionValidationArrayType(
		final Class<?> actualType,
		final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
		this.arrayType  = actualType ;
	}



	///////////////////////////////////////////////////////////////////////////
	// getters //
	////////////

	public Class<?> getType()
	{
		return this.arrayType;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public String getMessage()
	{
		return "Arrays do not have fields describing the persistent state. Type = \"" + this.arrayType + "\"."
			+ (super.getMessage() != null ? " Details: " + super.getMessage() : "")
		;
	}



}
