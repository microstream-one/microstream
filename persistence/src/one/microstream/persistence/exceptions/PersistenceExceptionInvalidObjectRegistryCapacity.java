package one.microstream.persistence.exceptions;

public class PersistenceExceptionInvalidObjectRegistryCapacity extends PersistenceException
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static final String messageBody()
	{
		return "Invalid capacity";
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final long invalidCapacity;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceExceptionInvalidObjectRegistryCapacity(
		final long invalidCapacity
	)
	{
		this(invalidCapacity, null, null);
	}

	public PersistenceExceptionInvalidObjectRegistryCapacity(
		final long      invalidCapacity,
		final Throwable cause
	)
	{
		this(invalidCapacity, null, cause);
	}

	public PersistenceExceptionInvalidObjectRegistryCapacity(
		final long      invalidCapacity,
		final String    message
	)
	{
		this(invalidCapacity, message, null);
	}

	public PersistenceExceptionInvalidObjectRegistryCapacity(
		final long      invalidCapacity,
		final String    message        ,
		final Throwable cause
	)
	{
		this(invalidCapacity, message, cause, true, true);
	}

	public PersistenceExceptionInvalidObjectRegistryCapacity(
		final long      invalidCapacity   ,
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
		this.invalidCapacity = invalidCapacity;
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	public final long invalidCapacity()
	{
		return this.invalidCapacity;
	}
	
	@Override
	public String assembleDetailString()
	{
		return messageBody() + ": " + this.invalidCapacity + ".";
	}
	
	@Override
	public String toString()
	{
		return this.assembleDetailString();
	}

}
