package one.microstream.storage.types;

/*-
 * #%L
 * microstream-storage
 * %%
 * Copyright (C) 2019 - 2021 MicroStream Software
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
public interface StorageThreadNameProvider
{
	public String provideThreadName(StorageThreadProviding threadProvider, String definedThreadName);
	
	
	
	public static StorageThreadNameProvider NoOp()
	{
		return new NoOp();
	}
	
	public final class NoOp implements StorageThreadNameProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		 NoOp()
		{
			super();
		}
		 
		 
		 
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public String provideThreadName(
			final StorageThreadProviding threadProvider   ,
			final String                 definedThreadName
		)
		{
			return definedThreadName;
		}
		
	}
	
	
	public static StorageThreadNameProvider Prefixer(final String prefix)
	{
		return new Prefixer(
			notNull(prefix)
		);
	}
	
	public final class Prefixer implements StorageThreadNameProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final String prefix;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Prefixer(final String prefix)
		{
			super();
			this.prefix = prefix;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final String provideThreadName(
			final StorageThreadProviding threadProvider   ,
			final String                 definedThreadName
		)
		{
			return this.prefix + definedThreadName;
		}
		
	}
	
}
