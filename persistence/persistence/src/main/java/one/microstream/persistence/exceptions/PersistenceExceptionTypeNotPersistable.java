package one.microstream.persistence.exceptions;

public class PersistenceExceptionTypeNotPersistable extends PersistenceException
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Class<?> type;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceExceptionTypeNotPersistable(
		final Class<?> type
	)
	{
		this(type, null, null);
	}

	public PersistenceExceptionTypeNotPersistable(
		final Class<?> type,
		final String message
	)
	{
		this(type, message, null);
	}

	public PersistenceExceptionTypeNotPersistable(
		final Class<?> type,
		final Throwable cause
	)
	{
		this(type, null, cause);
	}

	public PersistenceExceptionTypeNotPersistable(
		final Class<?> type,
		final String message, final Throwable cause
	)
	{
		this(type, message, cause, true, true);
	}

	public PersistenceExceptionTypeNotPersistable(
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
		return "Type not persistable: \"" + this.type + "\"."
			+ (super.getMessage() != null ? " Details: " + super.getMessage() : "")
		;
	}

}
