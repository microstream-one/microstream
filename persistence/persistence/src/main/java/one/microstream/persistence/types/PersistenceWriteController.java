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

import one.microstream.afs.types.WriteController;
import one.microstream.persistence.exceptions.PersistenceExceptionStoringDisabled;


@FunctionalInterface
public interface PersistenceWriteController extends WriteController
{
	public default void validateIsStoringEnabled()
	{
		if(this.isStoringEnabled())
		{
			return;
		}

		throw new PersistenceExceptionStoringDisabled();
	}

	public default boolean isStoringEnabled()
	{
		return this.isWritable();
	}
	
	
	public static PersistenceWriteController Wrap(final WriteController writeController)
	{
		return new PersistenceWriteController.Wrapper(
			notNull(writeController)
		);
	}
	
	public final class Wrapper implements PersistenceWriteController
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final WriteController writeController;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Wrapper(final WriteController writeController)
		{
			super();
			this.writeController = writeController;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final void validateIsWritable()
		{
			this.writeController.validateIsWritable();
		}
		
		@Override
		public final boolean isWritable()
		{
			return this.writeController.isWritable();
		}
		
		@Override
		public final boolean isStoringEnabled()
		{
			return this.isWritable();
		}
				
	}
	
	public static PersistenceWriteController Enabled()
	{
		// Singleton is (usually) an anti pattern.
		return new PersistenceWriteController.Enabled();
	}
	
	public final class Enabled implements PersistenceWriteController
	{
		Enabled()
		{
			super();
		}
		
		@Override
		public final void validateIsWritable()
		{
			// no-op
		}
		
		@Override
		public final void validateIsStoringEnabled()
		{
			// no-op
		}

		@Override
		public final boolean isWritable()
		{
			return true;
		}
		
		@Override
		public final boolean isStoringEnabled()
		{
			return true;
		}
		
	}
	
	public static PersistenceWriteController Disabled()
	{
		// Singleton is (usually) an anti pattern.
		return new PersistenceWriteController.Disabled();
	}
	
	public final class Disabled implements PersistenceWriteController
	{
		Disabled()
		{
			super();
		}

		@Override
		public final boolean isWritable()
		{
			return false;
		}
		
		@Override
		public final boolean isStoringEnabled()
		{
			return false;
		}
		
	}
	
}
