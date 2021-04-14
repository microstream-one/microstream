package one.microstream.persistence.binary.exceptions;

public class BinaryPersistenceExceptionStateInstance extends BinaryPersistenceExceptionState
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Object instance;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryPersistenceExceptionStateInstance(final Object instance)
	{
		this(instance, null, null);
	}

	public BinaryPersistenceExceptionStateInstance(final Object instance,
		final String message
	)
	{
		this(instance, message, null);
	}

	public BinaryPersistenceExceptionStateInstance(final Object instance,
		final Throwable cause
	)
	{
		this(instance, null, cause);
	}

	public BinaryPersistenceExceptionStateInstance(final Object instance,
		final String message, final Throwable cause
	)
	{
		this(instance, message, cause, true, true);
	}

	public BinaryPersistenceExceptionStateInstance(final Object instance,
		final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
		this.instance = instance;
	}



	///////////////////////////////////////////////////////////////////////////
	// getters //
	////////////

	public Object getInstance()
	{
		return this.instance;
	}



}
