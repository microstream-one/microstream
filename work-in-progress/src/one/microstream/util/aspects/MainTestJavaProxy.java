package one.microstream.util.aspects;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Comparator;

import one.microstream.X;

public class MainTestJavaProxy
{
	
	
	public static void main(final String[] args)
	{
		final Comparator<?> c = (Comparator<?>)Proxy.newProxyInstance(
			MainTestJavaProxy.class.getClassLoader(),
			X.array(Comparator.class),
			new Logic()
		);
		
		System.out.println(c.getClass());
	}
	
	
	static final class Logic implements InvocationHandler
	{

		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable
		{
			System.out.println("Called " + method.getName());
			return null;
		}
		
	}
	
}
