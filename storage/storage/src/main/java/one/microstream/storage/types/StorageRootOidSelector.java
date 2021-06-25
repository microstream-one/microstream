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

import one.microstream.functional._longProcedure;

public interface StorageRootOidSelector extends _longProcedure
{
	public void reset();

	public long yield();

	public default void resetGlobal()
	{
		this.reset();
	}

	public default void acceptGlobal(final long rootOid)
	{
		this.accept(rootOid);
	}

	public default long yieldGlobal()
	{
		return this.yield();
	}



	public final class Default implements StorageRootOidSelector
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private transient long currentMax;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default()
		{
			super();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final void accept(final long rootOid)
		{
			if(rootOid < this.currentMax)
			{
				return;
			}
			this.currentMax = rootOid;
		}

		@Override
		public final void reset()
		{
			this.currentMax = 0;

		}

		@Override
		public final long yield()
		{
			return this.currentMax;
		}

	}



	public interface Provider
	{
		public StorageRootOidSelector provideRootOidSelector(int channelIndex);



		public final class Default implements StorageRootOidSelector.Provider
		{
			@Override
			public final StorageRootOidSelector provideRootOidSelector(final int channelIndex)
			{
				return new StorageRootOidSelector.Default();
			}

		}

	}

}
