package one.microstream.persistence.exceptions;

public class PersistenceExceptionTypeNotTypeIdMappable extends PersistenceException
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Class<?> type;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceExceptionTypeNotTypeIdMappable(
		final Class<?> type
	)
	{
		this(type, null, null);
	}

	public PersistenceExceptionTypeNotTypeIdMappable(
		final Class<?> type,
		final String message
	)
	{
		this(type, message, null);
	}

	public PersistenceExceptionTypeNotTypeIdMappable(
		final Class<?> type,
		final Throwable cause
	)
	{
		this(type, null, cause);
	}

	public PersistenceExceptionTypeNotTypeIdMappable(
		final Class<?> type,
		final String message, final Throwable cause
	)
	{
		this(type, message, cause, true, true);
	}

	public PersistenceExceptionTypeNotTypeIdMappable(
		final Class<?> type,
		final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
		this.type = type;
	}



	///////////////////////////////////////////////////////////////////////////
	// getters //
	////////////

	public Class<?> getType()
	{
		return this.type;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public String getMessage()
	{
		return "Type not type id mappable: \"" + this.type + "\"."
			+ (super.getMessage() != null ? " Details: " + super.getMessage() : "")
		;
	}

}
