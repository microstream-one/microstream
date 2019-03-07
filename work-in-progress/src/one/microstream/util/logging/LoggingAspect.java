package one.microstream.util.logging;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import one.microstream.util.aspects.AspectWrapper;

public class LoggingAspect<T> extends AspectWrapper<T>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	private final InvocationLogger logger;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public LoggingAspect(final InvocationLogger logger, final T subject)
	{
		super(subject);
		this.logger = logger;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable
	{
		this.logger.logInvocation(this.getSubject(), method, args);
		try
		{
			final Object returnValue = super.invoke(proxy, method, args);
			this.logger.logReturn(this.getSubject(), returnValue, method, args);
			return returnValue;
		}
		catch(final InvocationTargetException e)
		{
			throw e.getCause();
		}
	}
}
