package one.microstream.experimental.aspectorientation.test;

import one.microstream.experimental.aspectorientation.MethodCall;
import one.microstream.experimental.aspectorientation.MethodExecutionContext;

/**
 *
 */

/**
 * @author Thomas Muenz
 *
 */
@SuppressWarnings("unused")
public abstract class MyAspects
{

	private static MethodExecutionContext noAspect = new MethodExecutionContext(){
		@Override
		public <R> R executeMethod(final MethodCall<R> methodhandle) {
			return methodhandle.execute();
		}
	};
	private static MethodExecutionContext exceptionHandlingAspect = new MethodExecutionContext(){
		@Override
		public <R> R executeMethod(final MethodCall<R> methodCall) {
			try
			{
				return methodCall.execute();
			}
			catch(RuntimeException e)
			{
				System.out.println(">>>Exception Handler Aspect: Caught >"+e+"< from "+methodCall+"()");
				throw e;
			}
		}
	};
	private static MethodExecutionContext consoleLoggingAspect = new MethodExecutionContext(){
		@Override
		public <R> R executeMethod(final MethodCall<R> methodhandle) {
			System.out.println(">>>Logging Aspect: Entering "+methodhandle+"()");
			final R returnValue = methodhandle.execute();
			System.out.println(">>>Logging Aspect: Leaving  "+methodhandle+"()");
			return returnValue;
		}
	};
	private static MethodExecutionContext exceptionHandlingConsoleLoggingAspect = new MethodExecutionContext(){
		@Override
		public <R> R executeMethod(final MethodCall<R> methodCall) {
			try
			{
				System.out.println(">>>Logging Aspect: Entering "+methodCall+"()");
				final R returnValue = methodCall.execute();
				System.out.println(">>>Logging Aspect: Leaving  "+methodCall+"()");
				return returnValue;
			}
			catch(RuntimeException e)
			{
				System.out.println(">>>Exception Handler Aspect: Caught >"+e+"< from "+methodCall+"()");
				throw e;
			}
		}
	};

	public static MethodExecutionContext getMethodExecutionContext(final Object o){
		return exceptionHandlingConsoleLoggingAspect;
	}

}
