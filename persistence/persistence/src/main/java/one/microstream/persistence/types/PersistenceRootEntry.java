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

import static one.microstream.X.mayNull;
import static one.microstream.X.notNull;

import java.util.function.Supplier;

import one.microstream.chars.XChars;

public interface PersistenceRootEntry
{
	public String identifier();
	
	public Object instance();
	
	public boolean isRemoved();
	
	
	
	public static PersistenceRootEntry New(final String identifier, final Supplier<?> instanceSupplier)
	{
		return new PersistenceRootEntry.Default(
			notNull(identifier)      ,
			mayNull(instanceSupplier) // null means deleted
		);
	}
	
	public final class Default implements PersistenceRootEntry
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final String      identifier      ;
		private final Supplier<?> instanceSupplier;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(final String identifier, final Supplier<?> instanceSupplier)
		{
			super();
			this.identifier       = identifier      ;
			this.instanceSupplier = instanceSupplier;
		}


		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final String identifier()
		{
			return this.identifier;
		}
		
		@Override
		public boolean isRemoved()
		{
			return this.instanceSupplier == null;
		}

		@Override
		public final Object instance()
		{
//			XDebug.println("Calling supplier.get() from " + XChars.systemString(this));
			
			return this.instanceSupplier != null
				? this.instanceSupplier.get()
				: null
			;
		}
		
		@Override
		public String toString()
		{
			return this.identifier + ": " + XChars.systemString(this.instance());
		}
		
	}
	
	
	@FunctionalInterface
	public interface Provider
	{
		public PersistenceRootEntry provideRootEntry(String identifier, Supplier<?> instanceSupplier);
	}
	
}
