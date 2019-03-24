package one.microstream.util.aspects;

import java.lang.reflect.Method;


/**
 * {@link AspectWrapper} implementation adding mutex synchronization to any object of any type.
 *
 * @author Thomas Muenz
 *
 */
public class LockedAspectWrapper<T> extends AspectWrapper<T>
{
	///////////////////////////////////////////////////////////////////////////
	//  static methods   //
	/////////////////////

	public static <T> T wrapLocked(final T subject)
	{
		return new LockedAspectWrapper<>(subject).newProxyInstance();
	}

	public static final <T> T wrapLocked(final T subject, final Object mutex)
	{
		return new LockedAspectWrapper<>(subject, mutex).newProxyInstance();
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	final Object lock;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	protected LockedAspectWrapper(final T subject)
	{
		super(subject);
		this.lock = new Object();
	}

	protected LockedAspectWrapper(final T subject, final Object lock)
	{
		super(subject);
		this.lock = lock;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable
	{
		synchronized(this.lock)
		{
			try
			{
				// keep up pretending to be the subject, no matter the called method's actual logic
				final Object returnValue;
				return (returnValue = method.invoke(this.subject, args)) == this.subject ? proxy : returnValue;
			}
			catch(final java.lang.reflect.InvocationTargetException e)
			{
				throw e.getCause();
			}
		}
	}

}
