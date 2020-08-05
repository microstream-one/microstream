package one.microstream.storage.exceptions;

import one.microstream.chars.VarString;
import one.microstream.collections.types.XGettingSequence;

public class StorageExceptionDisruptingExceptions extends StorageException
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final XGettingSequence<Throwable> disruptions;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public StorageExceptionDisruptingExceptions(
		final XGettingSequence<Throwable> disruptions
	)
	{
		super();
		this.disruptions = disruptions;
	}

	public StorageExceptionDisruptingExceptions(
		final XGettingSequence<Throwable> disruptions,
		final String                      message
	)
	{
		super(message);
		this.disruptions = disruptions;
	}

	public StorageExceptionDisruptingExceptions(
		final XGettingSequence<Throwable> disruptions,
		final Throwable                   cause
	)
	{
		super(cause);
		this.disruptions = disruptions;
	}

	public StorageExceptionDisruptingExceptions(
		final XGettingSequence<Throwable> disruptions,
		final String                      message    ,
		final Throwable                   cause
	)
	{
		super(message, cause);
		this.disruptions = disruptions;
	}

	public StorageExceptionDisruptingExceptions(
		final XGettingSequence<Throwable> disruptions       ,
		final String                      message           ,
		final Throwable                   cause             ,
		final boolean                     enableSuppression ,
		final boolean                     writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
		this.disruptions = disruptions;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	public final XGettingSequence<Throwable> disruptions()
	{
		return this.disruptions;
	}
	
	@Override
	public String assembleOutputString()
	{
		final VarString vs = VarString.New("Disruptions: {");
		for(final Throwable d : this.disruptions)
		{
			vs.add(d.getClass().getName()).add(':').add(d.getMessage()).add(',').blank();
		}
		vs.deleteLast().add('}');
		
		return vs.toString();
	}
	
}
