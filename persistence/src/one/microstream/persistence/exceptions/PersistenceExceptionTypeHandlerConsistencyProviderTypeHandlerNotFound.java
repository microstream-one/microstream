package one.microstream.persistence.exceptions;

public class PersistenceExceptionTypeHandlerConsistencyProviderTypeHandlerNotFound
extends PersistenceExceptionTypeHandlerConsistencyProvider
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Class<?> type;
	private final Long     typeId;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceExceptionTypeHandlerConsistencyProviderTypeHandlerNotFound(
		final Class<?> type,
		final Long     typeId
	)
	{
		this(type, typeId, null, null);
	}

	public PersistenceExceptionTypeHandlerConsistencyProviderTypeHandlerNotFound(
		final Class<?> type,
		final Long     typeId,
		final String message
	)
	{
		this(type, typeId, message, null);
	}

	public PersistenceExceptionTypeHandlerConsistencyProviderTypeHandlerNotFound(
		final Class<?> type,
		final Long     typeId,
		final Throwable cause
	)
	{
		this(type, typeId, null, cause);
	}

	public PersistenceExceptionTypeHandlerConsistencyProviderTypeHandlerNotFound(
		final Class<?> type,
		final Long     typeId,
		final String message, final Throwable cause
	)
	{
		this(type, typeId, message, cause, true, true);
	}

	public PersistenceExceptionTypeHandlerConsistencyProviderTypeHandlerNotFound(
		final Class<?> type,
		final Long     typeId,
		final String message,
		final Throwable cause,
		final boolean enableSuppression,
		final boolean writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
		this.type   = type  ;
		this.typeId = typeId;
	}



	///////////////////////////////////////////////////////////////////////////
	// getters //
	////////////

	public Class<?> getType()
	{
		return this.type;
	}

	public Long getTypeId()
	{
		return this.typeId;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public String getMessage()
	{
		return "Type handler not found for type or type id \"" + (this.type == null  ? this.typeId  : this.type) + "\"."
			+ (super.getMessage() != null ? " Details: " + super.getMessage() : "")
		;
	}

}
