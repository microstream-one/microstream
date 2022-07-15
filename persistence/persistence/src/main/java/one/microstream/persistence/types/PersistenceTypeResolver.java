package one.microstream.persistence.types;

/*-
 * #%L
 * microstream-persistence
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

import one.microstream.reflect.ClassLoaderProvider;
import one.microstream.reflect.XReflect;

public interface PersistenceTypeResolver
{
	public default String substituteClassIdentifierSeparator()
	{
		return Persistence.substituteClassIdentifierSeparator();
	}
	
	public default String deriveTypeName(final Class<?> type)
	{
		return Persistence.derivePersistentTypeName(type, this.substituteClassIdentifierSeparator());
	}
	
	public default ClassLoader getTypeResolvingClassLoader(final String typeName)
	{
		return XReflect.defaultTypeResolvingClassLoader();
	}
	
	public default Class<?> resolveType(final String typeName)
	{
		return Persistence.resolveType(
			typeName,
			this.getTypeResolvingClassLoader(typeName),
			this.substituteClassIdentifierSeparator()
		);
	}
	
	public default Class<?> tryResolveType(final String typeName)
	{
		return Persistence.tryResolveType(typeName, this.getTypeResolvingClassLoader(typeName));
	}
	
	
	
//	public static PersistenceTypeResolver New()
//	{
//		return New(
//			ClassLoaderProvider.New()
//		);
//	}
	
	public static PersistenceTypeResolver New(final ClassLoaderProvider classLoaderProvider)
	{
		return new PersistenceTypeResolver.Default(
			notNull(classLoaderProvider)
		);
	}
	
	public final class Default implements PersistenceTypeResolver
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final ClassLoaderProvider classLoaderProvider;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(final ClassLoaderProvider classLoaderProvider)
		{
			super();
			this.classLoaderProvider = classLoaderProvider;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public ClassLoader getTypeResolvingClassLoader(final String typeName)
		{
			return this.classLoaderProvider.provideClassLoader(typeName);
		}
		
	}
			
}
