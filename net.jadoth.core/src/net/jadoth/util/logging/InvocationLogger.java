package net.jadoth.util.logging;

import static net.jadoth.Jadoth.systemString;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import net.jadoth.util.JadothTypes;
import net.jadoth.util.chars.VarString;

public interface InvocationLogger
{
	public void logInvocation(Object delegate, Type returnType, Object... arguments);

	public void logReturn(Object delegate, Object returnValue, Type returnType, Object... arguments);

	public void logInvocation(Object delegate, Method method, Object... arguments);

	public void logReturn(Object delegate, Object returnValue, Method method, Object... arguments);



	public class Implementation implements InvocationLogger
	{
		private static String toLogString(final Object value)
		{
			return JadothTypes.isPrimitiveWrapperType(value)
				? value.toString()
				: systemString(value)
			;
		}

		protected void log(final String logString)
		{
			System.out.println(logString);
		}

		protected void internalLogInvocation(
			final Object delegate,
			final Type returnType,
			final String methodname,
			final Object... arguments
		)
		{
			final VarString vc = VarString.New()
				.add("->").tab()
				.add(systemString(delegate)).tab()
				.add(returnType).tab()
				.add(methodname).add("()")
			;
			if(arguments != null)
			{
				for(final Object arg : arguments)
				{
					vc.tab().add(toLogString(arg));
				}
			}
			this.log(vc.toString());
		}

		protected void internalLogReturn(
			final Object delegate,
			final Object returnValue,
			final Type returnType,
			final String methodname,
			final Object... arguments
		)
		{
			final VarString vc = VarString.New()
				.add(toLogString(returnValue)).tab()
				.add(systemString(delegate)).tab()
				.add(returnType).tab()
				.add(methodname).add("()")
			;
			if(arguments != null)
			{
				for(final Object arg : arguments)
				{
					vc.tab().add(toLogString(arg));
				}
			}
			this.log(vc.toString());
		}

		@Override
		public void logInvocation(final Object delegate, final Type returnType, final Object... arguments)
		{
			this.internalLogInvocation(delegate, returnType, new Throwable().getStackTrace()[1].getMethodName(), arguments);
		}

		@Override
		public void logReturn(final Object delegate, final Object returnValue, final Type returnType, final Object... arguments)
		{
			this.internalLogReturn(delegate, returnValue, returnType, new Throwable().getStackTrace()[1].getMethodName(), arguments);
		}

		@Override
		public void logInvocation(final Object delegate, final Method method, final Object... arguments)
		{
			this.internalLogInvocation(delegate, method.getReturnType(), method.getName(), arguments);
		}

		@Override
		public void logReturn(final Object delegate, final Object returnValue, final Method method, final Object... arguments)
		{
			this.internalLogReturn(delegate, returnValue, method.getReturnType(), method.getName(), arguments);
		}

	}

}
