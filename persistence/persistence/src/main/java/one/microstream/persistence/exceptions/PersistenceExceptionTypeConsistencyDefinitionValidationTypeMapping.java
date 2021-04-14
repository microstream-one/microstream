package one.microstream.persistence.exceptions;

public class PersistenceExceptionTypeConsistencyDefinitionValidationTypeMapping
extends PersistenceExceptionTypeConsistencyDefinitionValidation
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final long     typeId;
	private final Class<?> type  ;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceExceptionTypeConsistencyDefinitionValidationTypeMapping(
		final long     typeId,
		final Class<?> type
	)
	{
		this(typeId, type, null, null);
	}

	public PersistenceExceptionTypeConsistencyDefinitionValidationTypeMapping(
		final long     typeId,
		final Class<?> type  ,
		final String message
	)
	{
		this(typeId, type, message, null);
	}

	public PersistenceExceptionTypeConsistencyDefinitionValidationTypeMapping(
		final long     typeId,
		final Class<?> type  ,
		final Throwable cause
	)
	{
		this(typeId, type, null, cause);
	}

	public PersistenceExceptionTypeConsistencyDefinitionValidationTypeMapping(
		final long     typeId,
		final Class<?> type  ,
		final String message, final Throwable cause
	)
	{
		this(typeId, type, message, cause, true, true);
	}

	public PersistenceExceptionTypeConsistencyDefinitionValidationTypeMapping(
		final long     typeId,
		final Class<?> type  ,
		final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
		this.typeId = typeId;
		this.type   = type  ;
	}



	///////////////////////////////////////////////////////////////////////////
	// getters //
	////////////

	public Class<?> getType()
	{
		return this.type;
	}

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
		return "Invalid type mapping: " + this.typeId + " " + this.type + "."
			+ (super.getMessage() != null ? " Details: " + super.getMessage() : "")
		;
	}



}
