package one.microstream.persistence.types;

/*-
 * #%L
 * MicroStream Persistence
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

import one.microstream.collections.EqHashTable;
import one.microstream.collections.HashEnum;

public interface PersistenceLiveStorerRegistry extends PersistenceStorer.CreationObserver
{
	@Override
	public default void observeCreatedStorer(final PersistenceStorer storer)
	{
		this.registerStorer(storer);
	}

	public void registerStorer(PersistenceStorer storer);

	public boolean clearGroupAndAdvance(long oldGroupId, long newGroupId);



	public static PersistenceLiveStorerRegistry New()
	{
		return new PersistenceLiveStorerRegistry.Default();
	}

	public final class Default implements PersistenceLiveStorerRegistry
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final EqHashTable<Long, HashEnum<PersistenceStorer>> storerGroups = EqHashTable.New();
		private long currentGroupId;



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
		public synchronized void registerStorer(final PersistenceStorer storer)
		{
			HashEnum<PersistenceStorer> storerGroup = this.storerGroups.get(this.currentGroupId);
			if(storerGroup == null)
			{
				this.storerGroups.add(this.currentGroupId, storerGroup = HashEnum.New());
			}

//			XDebug.print("Registering storer " + XChars.systemString(storer) + " to id Group " + this.currentGroupId);
			storerGroup.add(storer);
		}

		@Override
		public synchronized boolean clearGroupAndAdvance(final long oldGroupId, final long newGroupId)
		{
			final long removeCount = this.storerGroups.removeBy(e -> e.key() <= oldGroupId);

//			XDebug.println(Thread.currentThread() + " removed " + removeCount + " idGroups with id <= " + oldGroupId + ".");

			this.currentGroupId = newGroupId;

			return removeCount > 0;
		}

	}

}
