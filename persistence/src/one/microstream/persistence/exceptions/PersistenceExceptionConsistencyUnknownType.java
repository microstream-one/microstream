package one.microstream.persistence.exceptions;


public class PersistenceExceptionConsistencyUnknownType extends PersistenceExceptionConsistency
{
	///////////////////////////////////////////////////////////////////////////
	// instance types //
	////////////////////

	final Class<?> type;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceExceptionConsistencyUnknownType(final Class<?> type)
	{
		this(type, null, null);
	}

	public PersistenceExceptionConsistencyUnknownType(final Class<?> type, final String message)
	{
		this(type, message, null);
	}

	public PersistenceExceptionConsistencyUnknownType(final Class<?> type, final Throwable cause)
	{
		this(type, null, cause);
	}

	public PersistenceExceptionConsistencyUnknownType(final Class<?> type, final String message, final Throwable cause)
	{
		this(type, message, cause, true, true);
	}

	public PersistenceExceptionConsistencyUnknownType(
		final Class<?>  type              ,
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
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
		return "Unknown type: \"" + this.type + "\"."
			+ (super.getMessage() != null ? " Details: " + super.getMessage() : "")
		;
	}

}
