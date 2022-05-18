package one.microstream.reflect;

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

import static one.microstream.X.notNull;

@FunctionalInterface
public interface ClassLoaderProvider
{
	/**
	 * Provides the {@link ClassLoader} instance to be used with {@link XReflect#resolveType(String, ClassLoader)}
	 * to resolve the passed typeName.<br>
	 * The {@literal typeName} should usually not be required to determine the returned {@link ClassLoader}.
	 * It is just an optionally available information in case the responsible {@link ClassLoader} has to be determined
	 * based on the specific type (e.g. the package path or a sub path of it).
	 * 
	 * @param typeName the full qualified name of the type to be resolved.
	 * 
	 * @return the {@link ClassLoader} instance to be used to resolve the passed type name.
	 */
	public ClassLoader provideClassLoader(String typeName);
	
	
	
	/**
	 * The {@link ClassLoader} used by the default {@link ClassLoaderProvider} implementation.
	 * 
	 * @return {@code ClassLoader.getSystemClassLoader()}
	 */
	public static ClassLoader systemClassLoader()
	{
		return ClassLoader.getSystemClassLoader();
	}
	
	
	
	public static ClassLoaderProvider New(final ClassLoader classLoader)
	{
		return new ClassLoaderProvider.Default(
			notNull(classLoader)
		);
	}
	
	public final class Default implements ClassLoaderProvider
	{
		private final ClassLoader subject;
		
		Default(final ClassLoader subject)
		{
			super();
			this.subject = subject;
		}
		
		@Override
		public ClassLoader provideClassLoader(final String typeName)
		{
			return this.subject;
		}
		
	}
	
	public static ClassLoaderProvider System()
	{
		return new ClassLoaderProvider.System();
	}
	
	public final class System implements ClassLoaderProvider
	{
		System()
		{
			super();
		}
		
		@Override
		public ClassLoader provideClassLoader(final String typeName)
		{
			return ClassLoaderProvider.systemClassLoader();
		}
		
	}
	
}
