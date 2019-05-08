package one.microstream.persistence.test;

import one.microstream.functional.InstanceDispatcherLogic;
import one.microstream.util.logging.InvocationLogger;
import one.microstream.util.logging.LoggingAspect;



public class InvocationLogging
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	protected static final boolean LOGGING_ENABLED   = false;
	protected static final InvocationLogging LOGGING = new InvocationLogging();

	protected static final InstanceDispatcherLogic dispatcher = new InstanceDispatcherLogic(){
		@Override public <T> T apply(final T subject) {
			return dispatch(subject);
		}
	};



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	protected static final <T> T dispatch(final T instance)
	{
		return LOGGING_ENABLED ?LOGGING.addLoggingAspect(instance) :instance; // revolutionary! :D
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final InvocationLogger invocationLogger;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public InvocationLogging()
	{
		this(new InvocationLogger.Default());
	}

	public InvocationLogging(final InvocationLogger invocationLogger)
	{
		super();
		this.invocationLogger = invocationLogger;
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	public final <T> T addLoggingAspect(final T subject)
	{
		return new LoggingAspect<>(this.invocationLogger, subject).newProxyInstance();
	}

}
