package one.microstream.persistence.exceptions;

public class PersistenceExceptionTypeConsistencyDefinitionValidation
extends PersistenceExceptionTypeConsistencyDictionary
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceExceptionTypeConsistencyDefinitionValidation()
	{
		this(null, null);
	}

	public PersistenceExceptionTypeConsistencyDefinitionValidation(final String message)
	{
		this(message, null);
	}

	public PersistenceExceptionTypeConsistencyDefinitionValidation(final Throwable cause)
	{
		this(null, cause);
	}

	public PersistenceExceptionTypeConsistencyDefinitionValidation(final String message, final Throwable cause)
	{
		this(message, cause, true, true);
	}

	public PersistenceExceptionTypeConsistencyDefinitionValidation(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}



}
