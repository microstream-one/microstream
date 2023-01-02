package one.microstream.persistence.binary.types;

/*-
 * #%L
 * microstream-persistence-binary
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

import static one.microstream.collections.XArrays.copy;

import one.microstream.functional._longProcedure;
import one.microstream.persistence.types.PersistenceIdSet;

public interface LoadItemsChain
{
	public boolean containsLoadItem(long objectId);

	public void addLoadItem(long objectId);

	public boolean isEmpty();

	public PersistenceIdSet[] getObjectIdSets();

	public void clear();

	final class Entry
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final long objectId;
		Entry next, link;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Entry(final long objectId)
		{
			super();
			this.objectId = objectId;
			this.link     = null    ;
			this.next     = null    ;
		}
	}

	public abstract class Abstract implements LoadItemsChain
	{
		///////////////////////////////////////////////////////////////////////////
		// constants //
		//////////////

		private static final int INITIAL_HASH_SLOTS_LENGTH = 256; // 1 << 8. MUST be a power of 2 value.



		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private Entry[] hashSlots = new Entry[INITIAL_HASH_SLOTS_LENGTH];
		private int     hashRange = this.hashSlots.length - 1;
		private int     size     ;



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		private void rebuildLoadOidSet()
		{
			final int newModulo; // potential int overflow ignored deliberately
			final Entry[] newSlots = new Entry[(newModulo = (this.hashSlots.length << 1) - 1) + 1];
			for(Entry entry : this.hashSlots)
			{
				for(Entry next; entry != null; entry = next)
				{
					next = entry.link;
					entry.link = newSlots[(int)(entry.objectId & newModulo)];
					newSlots[(int)(entry.objectId & newModulo)] = entry;
				}
			}
			this.hashSlots = newSlots;
			this.hashRange = newModulo;
		}

		protected abstract void clearChain();

		@Override
		public final void clear()
		{
			this.clearChain();
			final Entry[] slots = this.hashSlots;
			for(int i = 0; i < slots.length; i++)
			{
				slots[i] = null;
			}
			this.size = 0;
		}

		protected abstract Entry enqueueEntry(final long objectId, final Entry link);

		private void internalPutNewLoadItem(final long objectId)
		{

			this.hashSlots[(int)(objectId & this.hashRange)] =
				this.enqueueEntry(objectId, this.hashSlots[(int)(objectId & this.hashRange)])
			;
			if(++this.size >= this.hashRange)
			{
				this.rebuildLoadOidSet();
			}
		}


		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final void addLoadItem(final long objectId)
		{
			for(Entry entry = this.hashSlots[(int)(objectId & this.hashRange)]; entry != null; entry = entry.link)
			{
				if(entry.objectId == objectId)
				{
					return;
				}
			}
			this.internalPutNewLoadItem(objectId);
		}

		@Override
		public final boolean containsLoadItem(final long objectId)
		{
			// ids are assumed to be roughly sequential, hence (id ^ id >>> 32) should not be necessary for distribution
			for(Entry entry = this.hashSlots[(int)(objectId & this.hashRange)]; entry != null; entry = entry.link)
			{
				if(entry.objectId == objectId)
				{
					return true;
				}
			}
			return false;
		}

		@Override
		public final boolean isEmpty()
		{
			return this.size == 0;
		}

		public final long size()
		{
			return this.size;
		}

	}

	public final class Simple extends LoadItemsChain.Abstract implements PersistenceIdSet
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final Entry   chainHead = new Entry(0)  ;
		private       Entry   chainTail = this.chainHead;



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		protected
		final void clearChain()
		{
			(this.chainTail = this.chainHead).next = null;
		}

		@Override
		protected
		final Entry enqueueEntry(final long objectId, final Entry link)
		{
			return (this.chainTail = this.chainTail.next = new Entry(objectId)).link = link;
		}


		@Override
		public final PersistenceIdSet[] getObjectIdSets()
		{
			return new PersistenceIdSet[]{this};
		}

		@Override
		public void iterate(final _longProcedure iterator)
		{
			for(Entry entry = this.chainHead.next; entry != null; entry = entry.next)
			{
				iterator.accept(entry.objectId);
			}
		}


	}

	public final class ChannelHashing extends LoadItemsChain.Abstract
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final int     hashRange;
		private final Entry[] hashChainHeads, hashChainTails;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public ChannelHashing(final int channelCount)
		{
			super();
			this.hashRange = channelCount - 1;
			final Entry[] hashChainHeads = this.hashChainHeads = new Entry[channelCount];
			for(int i = 0; i < hashChainHeads.length; i++)
			{
				hashChainHeads[i] = new Entry(0);
			}
			this.hashChainTails = copy(hashChainHeads);
		}

		@Override
		public PersistenceIdSet[] getObjectIdSets()
		{
			final Entry[] hashChainHeads = this.hashChainHeads;
			final PersistenceIdSet[] idSets = new PersistenceIdSet[hashChainHeads.length];
			for(int i = 0; i < hashChainHeads.length; i++)
			{
				idSets[i] = new ChainItemObjectIdSet(hashChainHeads[i].next);
			}
			return idSets;
		}

		@Override
		protected void clearChain()
		{
			final Entry[] hashChainHeads = this.hashChainHeads;
			final Entry[] hashChainTails = this.hashChainTails;
			for(int i = 0; i < hashChainHeads.length; i++)
			{
				(hashChainTails[i] = hashChainHeads[i]).next = null;
			}
		}

		@Override
		protected Entry enqueueEntry(final long objectId, final Entry link)
		{
			final Entry entry;
			(entry = new Entry(objectId)).link = link;
			return this.hashChainTails[(int)(this.hashRange & objectId)] =
				this.hashChainTails[(int)(this.hashRange & objectId)].next = entry
			;
		}

		public static final class ChainItemObjectIdSet implements PersistenceIdSet
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////

			private final Entry first;



			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////

			public ChainItemObjectIdSet(final Entry first)
			{
				super();
				this.first = first;

			}

			@Override
			public boolean isEmpty()
			{
				return this.first == null;
			}

			@Override
			public long size()
			{
				// (10.09.2015 TM)NOTE: VERY inefficient (currently never called).
				int size = 0;
				for(Entry e = this.first; e != null; e = e.next)
				{
					size++;
				}
				return size;
			}

			@Override
			public void iterate(final _longProcedure iterator)
			{
				for(Entry e = this.first; e != null; e = e.next)
				{
					iterator.accept(e.objectId);
				}
			}

		}

	}

}
