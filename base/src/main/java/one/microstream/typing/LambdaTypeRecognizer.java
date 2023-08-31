package one.microstream.typing;

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

@FunctionalInterface
public interface LambdaTypeRecognizer
{
	public boolean isLambdaType(Class<?> type);
	
	
	
	/**
	 * Creates a {@link LambdaTypeRecognizer} instance with stateless default implementation for the most current
	 * approach (JDK version).<p>
	 * 
	 * Currently {@link #Java8Makeshift()}.
	 * 
	 * @return a new and stateless instance.
	 */
	public static LambdaTypeRecognizer New()
	{
		return Java8Makeshift();
	}
	
	/**
	 * Creates a {@link LambdaTypeRecognizer} instance with stateless makeshift implementation for Java 8 and
	 * compatible later versions.
	 * 
	 * @return a new and stateless instance for Java 8.
	 */
	public static LambdaTypeRecognizer Java8Makeshift()
	{
		return new LambdaTypeRecognizer.Java8Makeshift();
	}
	
	/**
	 * This is a makeshift implementation and by far not a completely safe approach.<p>
	 * The condition to recognize a lambda type is:<br>
	 * - {@link Class#isSynthetic()} must return {@code true}.<br>
	 * - The String returned by {@link Class#getName()} must contain the value {@code "$$Lambda$"}.
	 * <p>
	 * This approach works correctly to recognize lambda types in Java 8. However,
	 * it comes with the following two risks:<br>
	 * 1.) Should any synthetic but non-lambda class contain that String in its type name,
	 *     it will falsely be identified as a lambda.<br>
	 * 2.) Should the JDK-internal implementation detail lambda type String that the used String refers to ever
	 *     change in a future release, this approach will no longer recognize lambda types.
	 * <p>
	 * Unfortunately, there is no more reliable way known to the author of this class, despite intensive research.
	 * The underlying problem is that the Java type system (at least in version 8) does not provide a proper way
	 * of querying if a type is a lambda type. Brian Goetz wrote on Stackoverflow that if one needs to make such a
	 * distinction, one surely made a mistake. However, there is a very simple example that objectively proves him
	 * wrong:<br>
	 * In contrast to other generated classes like anonymous inner classes, the Java type system cannot resolve the
	 * type String for a lambda type that it itself created. This sets lambda types apart from other types in certain
	 * situations involving generic handling of types, thus creating an absolutely essential need to indeed recognize
	 * lambda types.<br>
	 * <p>
	 * To indicate the weakness of the current approach, its implementation is explicitly named
	 * {@link one.microstream.typing.LambdaTypeRecognizer.Java8Makeshift}.
	 * Hopefully, there will be a better, proper way in the future, causing a second implementation
	 * ("JavaXX") to be created.
	 *
	 */
	public final class Java8Makeshift implements LambdaTypeRecognizer
	{
		/*
		 * Singleton is an anti-pattern:
		 * Why permanently occupy memory (even a little) with an instance that, in many cases, is used never or
		 * only temporarily? Also, creating new instances has certain benefits (like stack allocation instead of
		 * heap allocation). If a permanent instance of this type is really needed, it can still be kept in a constant
		 * of a specific project's context. But it should never be forced into permanent memory if there is no need.
		 */
		
		
		public static String lambdaTypeNameSearchSubject()
		{
			/*
			 * Constants, even private ones, are an anti-pattern, too, because the value is already in the byte code
			 * and the method gets jitted away, anyway.
			 * So many anti-patterns and bad design decisions in the world of (Java) software development ...
			 */
			return "$$Lambda$";
		}
		
		/*
		 * Constructors are implementation details just like fields and thus should be hidden from the public API
		 * just the same to avoid problems with future changes.
		 */
		Java8Makeshift()
		{
			super();
		}
		
		// why do instance methods and static methods collide in the first place? The instance method has the implicit this argument...
		public static boolean staticIsLambdaType(final Class<?> type)
		{
			// searching from the end forward should be (marginally) faster, so why not.
			return type.isSynthetic() && type.getName().lastIndexOf(Java8Makeshift.lambdaTypeNameSearchSubject()) > 0;
		}

		@Override
		public final boolean isLambdaType(final Class<?> type)
		{
			return Java8Makeshift.staticIsLambdaType(type);
		}
		

	}
	
}
