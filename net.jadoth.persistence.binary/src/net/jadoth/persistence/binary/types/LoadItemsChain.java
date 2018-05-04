package net.jadoth.persistence.binary.types;

import static net.jadoth.collections.XArrays.copy;

import net.jadoth.functional._longProcedure;
import net.jadoth.swizzling.types.SwizzleIdSet;

public interface LoadItemsChain
{
	public boolean containsLoadItem(long oid);

	public void addLoadItem(long oid);

	public boolean isEmpty();

	public SwizzleIdSet[] getObjectIdSets();

	public void clear();

//	public void transferLoadEntries(BuildItemsHolder<I> builder);


	final class Entry
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		final long oid;
		Entry next, link;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		Entry(final long oid)
		{
			super();
			this.oid  = oid ;
			this.link = null;
			this.next = null;
		}
	}

	public abstract class AbstractImplementation implements LoadItemsChain
	{
		///////////////////////////////////////////////////////////////////////////
		// constants        //
		/////////////////////

		private static final int INITIAL_HASH_SLOTS_LENGTH = 256; // 1 << 8. MUST be a power of 2 value.



		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

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
					entry.link = newSlots[(int)(entry.oid & newModulo)];
					newSlots[(int)(entry.oid & newModulo)] = entry;
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

		protected abstract Entry enqueueEntry(final long oid, final Entry link);

		private void internalPutNewLoadItem(final long oid)
		{
//			JadothConsole.debugln("load " + oid);

			this.hashSlots[(int)(oid & this.hashRange)] = this.enqueueEntry(oid, this.hashSlots[(int)(oid & this.hashRange)]);
			if(++this.size >= this.hashRange)
			{
				this.rebuildLoadOidSet();
			}
		}


		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final void addLoadItem(final long oid)
		{
			for(Entry entry = this.hashSlots[(int)(oid & this.hashRange)]; entry != null; entry = entry.link)
			{
				if(entry.oid == oid)
				{
					return;
				}
			}
			this.internalPutNewLoadItem(oid);
		}

		@Override
		public final boolean containsLoadItem(final long oid)
		{
			// ids are assumed to be roughly sequential, hence (id ^ id >>> 32) should not be necessary for distribution
			for(Entry entry = this.hashSlots[(int)(oid & this.hashRange)]; entry != null; entry = entry.link)
			{
				if(entry.oid == oid)
				{
					return true;
				}
			}
			return false;
		}

//		@Override
//		public final void transferLoadEntries(final BuildItemsHolder<Entry> builder)
//		{
//			builder.addBuildItemChain(this.transferChainFirstEntry());
//
//			// reset loadItems structure for next loading step
//			this.clearLoadItems();
//		}

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

	public final class Simple extends LoadItemsChain.AbstractImplementation implements SwizzleIdSet
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

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
		final Entry enqueueEntry(final long oid, final Entry link)
		{
			return (this.chainTail = this.chainTail.next = new Entry(oid)).link = link;
		}


		@Override
		public final SwizzleIdSet[] getObjectIdSets()
		{
			return new SwizzleIdSet[]{this};
		}

		@Override
		public void iterate(final _longProcedure iterator)
		{
			for(Entry entry = this.chainHead.next; entry != null; entry = entry.next)
			{
				iterator.accept(entry.oid);
			}
		}

//		@Override
//		public long[] toArray()
//		{
//			final long[] oids;
//			this.toArray(oids = new long[this.size()], 0);
//			return oids;
//		}

//		@Override
//		public void toArray(final long[] target, final int offset)
//		{
//			int i = offset;
//			for(Entry entry = this.chainHead.next; entry != null; entry = entry.next)
//			{
//				target[i++] = entry.oid;
//			}
//		}

	}

	public final class ChannelHashing extends LoadItemsChain.AbstractImplementation
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final int     hashMod;
		private final Entry[] hashChainHeads, hashChainTails;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		public ChannelHashing(final int hashRange)
		{
			super();
			this.hashMod = hashRange - 1;
			final Entry[] hashChainHeads = this.hashChainHeads = new Entry[hashRange];
			for(int i = 0; i < hashChainHeads.length; i++)
			{
				hashChainHeads[i] = new Entry(0);
			}
			this.hashChainTails = copy(hashChainHeads);
		}

		@Override
		public SwizzleIdSet[] getObjectIdSets()
		{
			final Entry[] hashChainHeads = this.hashChainHeads;
			final SwizzleIdSet[] idSets = new SwizzleIdSet[hashChainHeads.length];
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
		protected Entry enqueueEntry(final long oid, final Entry link)
		{
			final Entry entry;
			(entry = new Entry(oid)).link = link;
			return this.hashChainTails[(int)(this.hashMod & oid)] =
				this.hashChainTails[(int)(this.hashMod & oid)].next = entry
			;
		}

		static final class ChainItemObjectIdSet implements SwizzleIdSet
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields  //
			/////////////////////

			private final Entry first;
//			private final int   size ;



			///////////////////////////////////////////////////////////////////////////
			// constructors     //
			/////////////////////

			public ChainItemObjectIdSet(final Entry first)
			{
				super();
				this.first = first;

//				/* (10.09.2015 TM)NOTE: size calculation is very inefficient.
//				 * As size is not needed at all, it is removed experimentally.
//				 * If size is required in the future, it should be passed with the first entry,
//				 * calculated by the calling context via some simple (currentSize - previousSize)
//				 * or something like that.
//				 */
//				int size = 0;
//				for(Entry e = first; e != null; e = e.next)
//				{
//					size++;
//				}
//				this.size = size;
			}

			@Override
			public boolean isEmpty()
			{
				return this.first == null;
//				return this.size == 0;
			}

			@Override
			public long size()
			{
				// (10.09.2015 TM)NOTE: VERY inefficient (currently never called). See constructor.
				int size = 0;
				for(Entry e = this.first; e != null; e = e.next)
				{
					size++;
				}
				return size;
//				return this.size;
			}

			@Override
			public void iterate(final _longProcedure iterator)
			{
				for(Entry e = this.first; e != null; e = e.next)
				{
					iterator.accept(e.oid);
				}
			}

		}

	}

}
