package net.jadoth.storage.exceptions;

import net.jadoth.collections.types.XGettingSequence;

public class StorageExceptionDisruptingExceptions extends StorageException
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final XGettingSequence<Throwable> problems;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public StorageExceptionDisruptingExceptions(
		final XGettingSequence<Throwable> problems
	)
	{
		super();
		this.problems = problems;
	}

	public StorageExceptionDisruptingExceptions(
		final XGettingSequence<Throwable> problems,
		final String                      message
	)
	{
		super(message);
		this.problems = problems;
	}

	public StorageExceptionDisruptingExceptions(
		final XGettingSequence<Throwable> problems,
		final Throwable                   cause
	)
	{
		super(cause);
		this.problems = problems;
	}

	public StorageExceptionDisruptingExceptions(
		final XGettingSequence<Throwable> problems,
		final String                      message ,
		final Throwable                   cause
	)
	{
		super(message, cause);
		this.problems = problems;
	}

	public StorageExceptionDisruptingExceptions(
		final XGettingSequence<Throwable> problems          ,
		final String                      message           ,
		final Throwable                   cause             ,
		final boolean                     enableSuppression ,
		final boolean                     writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
		this.problems = problems;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	public final XGettingSequence<Throwable> problems()
	{
		return this.problems;
	}
	
}
