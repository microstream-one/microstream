package one.microstream.experimental.proxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Consumer;

import one.microstream.util.aspects.AspectWrapper;
import one.microstream.util.aspects.LockedAspectWrapper;

public class MainTestProxy
{
	public static void main(final String[] args)
	{
		// simple Human instance named Joe
		final Human joe = new Human.Implementation("Joe");


		System.out.println("Calling joe.doStuff()... ");
		joe.doStuff();
		System.out.println();


		// proxy instance counts as instanceof wrapped instance's type, of course
		System.out.println(joe.getName()+" equals mutexed new human called \"Joe\": "+joe.equals(mutex(new Human.Implementation("Joe"))));
		System.out.println("Mutexed new human called \"Joe\" equals "+joe.getName()+": "+mutex((Human)new Human.Implementation("Joe")).equals(joe));
		System.out.println();

		final Human joeWithDebugging = addDebugging(joe); // still joe instance with debugging aspect wrapped around it
		System.out.println("Calling joeWithDebugging.doStuff()... ");
		joeWithDebugging.doStuff();
		System.out.println();

		// only problem: proxy instance's concrete class if of course not the class of the wrapped instance's implementation
		System.out.println("joeWithDebugging's class: "+joeWithDebugging.getClass());
		System.out.println();

		// returning sekf is rerouted to return the proxy again 8-)
		joeWithDebugging.say("a").say("b").say("c");
		System.out.println();


		final Consumer<Throwable> someExceptionHandler = new Consumer<Throwable>(){
			@Override public void accept(final Throwable e){
				System.out.print("HANDLER: For the record: >>"+e.getMessage()+"<< Exiting... ");
				System.exit(-1);
			}
		};

		// joe instance wrapped in debugging instance, wrapped in exception handler instance
		final Human joeWithDebuggingAndExceptionHandler = addExceptionHandler(joeWithDebugging, someExceptionHandler);
		System.out.println("Calling joeWithDebuggingAndExceptionHandler.doStupidThings()... ");
		joeWithDebuggingAndExceptionHandler.doStupidThings();
		System.out.println();
	}

	
	/**
	 * Generic mutex aspect via proxy instance. With implicit (hidden) mutex object.
	 *
	 * @param <T>
	 * @param subject
	 * @return
	 */
	public static final <T> T mutex(final T subject)
	{
		return LockedAspectWrapper.wrapLocked(subject);
	}

	/**
	 * Generic mutex aspect via proxy instance. With explicit mutex object.
	 *
	 * @param <T>
	 * @param subject
	 * @param mutex
	 * @return
	 */
	public static final <T> T mutex(final T subject, final Object mutex)
	{
		return LockedAspectWrapper.wrapLocked(subject, mutex);
	}


	public static final <T> T addDebugging(final T subject)
	{
		return new AspectWrapper<T>(subject){
			@Override
			public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable
			{
				System.out.println("DEBUG: Calling "+method);
				try
				{
					return super.invoke(proxy, method, args);
				}
				catch(final InvocationTargetException e)
				{
					throw e.getCause();
				}
			}
		}.newProxyInstance();
	}

	public static final <T> T addExceptionHandler(final T subject, final Consumer<Throwable> exceptionHandler)
	{
		return new AspectWrapper<T>(subject){
			@Override
			public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable
			{
				try
				{
					return super.invoke(proxy, method, args);
				}
				catch(final InvocationTargetException e)
				{
					exceptionHandler.accept(e.getCause());
					throw e.getCause();
				}
			}
		}.newProxyInstance();
	}


//	@SuppressWarnings("unchecked")
//	public static final <T> T mutex(final T subject, final Object mutex)
//	{
//		final LocalMutexProxy<T> proxy;
//		return (T)java.lang.reflect.Proxy.newProxyInstance(
//			subject.getClass().getClassLoader(),
//			(proxy = new LocalMutexProxy<T>(subject, mutex)).getProxyInterfaces(),
//			proxy
//		);
//	}
//
//	@SuppressWarnings("unchecked")
//	public static final <T> T mutex(final T subject)
//	{
//		final LocalMutexProxy<T> proxy;
//		return (T)java.lang.reflect.Proxy.newProxyInstance(
//			subject.getClass().getClassLoader(),
//			(proxy = new LocalMutexProxy<T>(subject, new Object())).getProxyInterfaces(),
//			proxy
//		);
//	}

}


//
//
//
//final class LocalMutexProxy<T> extends AspectWrapper<T>
//{
//	private final Object mutex;
//
//	public LocalMutexProxy(final T wrappee, final Object mutex)
//	{
//		super(wrappee);
//		this.mutex = mutex;
//	}
//
//	@Override
//	public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable
//	{
//		synchronized(this.mutex) {
//			try
//			{
//				return super.invoke(proxy, method, args);
//			}
//			catch(final InvocationTargetException e)
//			{
//				throw e.getCause();
//			}
//		}
//	}
//
//}

