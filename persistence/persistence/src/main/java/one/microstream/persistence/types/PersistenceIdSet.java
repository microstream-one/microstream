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

import one.microstream.collections.CapacityExceededException;
import one.microstream.collections.interfaces.Sized;
import one.microstream.collections.interfaces._longCollector;
import one.microstream.functional._longIterable;
import one.microstream.functional._longProcedure;

public interface PersistenceIdSet extends _longIterable, Sized
{
	@Override
	public long size();

	@Override
	public void iterate(_longProcedure iterator);



	final class Default implements PersistenceIdSet, _longCollector
	{
		///////////////////////////////////////////////////////////////////////////
		// constants //
		//////////////

		private static final int DEFAULT_CAPACITY = 64;



		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private long[] data = new long[DEFAULT_CAPACITY];
		private int    size;



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public void accept(final long id)
		{
			if(this.size >= this.data.length)
			{
				if(this.size >= Integer.MAX_VALUE)
				{
					throw new CapacityExceededException();
				}
				System.arraycopy(this.data, 0, this.data = new long[(int)(this.data.length * 2.0f)], 0, this.size);
			}
			this.data[this.size++] = id;
		}

		@Override
		public void iterate(final _longProcedure procedure)
		{
			final long[] data = this.data;
			final int    size = this.size;

			for(int i = 0; i < size; i++)
			{
				procedure.accept(data[i]);
			}
		}

		@Override
		public long size()
		{
			return this.size;
		}

		@Override
		public boolean isEmpty()
		{
			return this.size == 0;
		}

	}

}
