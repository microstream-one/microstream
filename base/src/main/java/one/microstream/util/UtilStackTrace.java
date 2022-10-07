package one.microstream.util;

/*-
 * #%L
 * microstream-base
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import static java.lang.System.arraycopy;

public class UtilStackTrace
{
	public static final <T extends Throwable> T cutStacktraceTo(final T t, final Class<?> c, final String methodName)
	{
		final StackTraceElement[] stackTrace = t.getStackTrace();
		final String cName = c.getName();

		int stackTracesToSkip = 0;
		for(final StackTraceElement ste : stackTrace)
		{
			if(ste.getClassName().equals(cName) && ste.getMethodName().equals(methodName))
			{
				break;
			}
			stackTracesToSkip++;
		}
		return UtilStackTrace.cutStacktraceByN(t, stackTracesToSkip);
	}

	public static final <T extends Throwable> T cutStacktraceByOne(final T throwable)
	{
		final StackTraceElement[] st1, st2;
		arraycopy(st1 = throwable.getStackTrace(), 1, st2 = new StackTraceElement[st1.length - 1], 0, st1.length - 1);
		throwable.setStackTrace(st2);
		return throwable;
	}

	public static <T extends Throwable> T cutStacktraceByN(final T throwable, final int n)
	{
		throwable.setStackTrace(cutStacktraceByN(throwable.getStackTrace(), n));
		return throwable;
	}

	public static StackTraceElement[] cutStacktraceByN(final StackTraceElement[] stacktrace, final int n)
	{
		// note: n < 0 is inherently checked by arraycopy. n == 0 is nonsense, but harmless
		final StackTraceElement[] st2;
		arraycopy(stacktrace, n, st2 = new StackTraceElement[stacktrace.length - n], 0, stacktrace.length - n);
		return st2;
	}
	
	/**
	 * Get the method name of the first method of the stack trace of the supplied throwable.
	 * 
	 * @param <T> type parameter.
	 * @param throwable that supplies the stack trace.
	 * @return The Method name as String.
	 */
	public static <T extends Throwable> String getThrowingMethodName(final T throwable)
	{
		final StackTraceElement[] stackTrace = throwable.getStackTrace();
		if(stackTrace.length > 0)
		{
			return stackTrace[0].getMethodName();
		}
		return "unkown method name";
	}

}
