package net.jadoth.storage.types;

import net.jadoth.math.JadothMath;

public interface StorageOidMarkQueue
{
	public void enqueue(long oid);

	public int getNext(long[] buffer);

	public boolean hasElements();

	public void advanceTail(int amount);

	// (19.07.2016 TM)TODO: only for debugging, demove afterwards
//	public long size();



	public interface Creator
	{
		public StorageOidMarkQueue createOidMarkQueue(int segmentLength);



		public final class Implementation implements StorageOidMarkQueue.Creator
		{
			@Override
			public StorageOidMarkQueue createOidMarkQueue(final int segmentLength)
			{
				return new StorageOidMarkQueue.Implementation(segmentLength);
			}

		}

	}


	final class Implementation implements StorageOidMarkQueue
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final Segment root;

		private Segment head, tail;

		// (19.07.2016 TM)TODO: only for debugging, demove afterwards
//		long size;
//
//		@Override
//		public final long size()
//		{
//			return this.size;
//		}



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Implementation(final int segmentLength)
		{
			super();
			this.root = new Segment(JadothMath.positive(segmentLength), null);
			this.reset();
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		synchronized final void reset()
		{
			(this.head = this.tail = this.root.next = this.root).clear();
//			this.size = 0;
		}

		@Override
		public synchronized int getNext(final long[] buffer)
		{
			return this.tail.getNext(buffer);
		}

		@Override
		public synchronized void advanceTail(final int amount)
		{
			if(this.tail.advanceLowIndex(amount))
			{
//				debugln("advance segment");

				this.tail = this.tail.advanceTail();
			}
//			this.size -= amount;
		}

		@Override
		public final synchronized void enqueue(final long oid)
		{
//			debugln("enqueue "+oid + " ("+this.head.lowIndex+" / " +this.head.highIndex+")");
			if(this.head.enqueue(oid))
			{
//				debugln("segment full");

				/* (14.07.2016 TM)TODO: prevent Implementation size excess
				 * if the queue exceeds a certain size, a consolidation action should be taken
				 * to remove redundant OIDs.
				 * Otherwise, very frequent stores and the entity updates they cause could fill
				 * up the whole memory with a massively redundant mark queue.
				 * A consolidated mark queue can never fill the memory as it can in the very worst academic case
				 * only contain all OIDs of all entities, meaning a range of megabytes for millions of entities or gigabytes
				 * for billions. In other words: it will always pale in comparison to the memory already consumed for the
				 * meta data.
				 *
				 * An open addressing long set implementation should be used for that.
				 * See OpenAdressingMiniSet.
				 *
				 * Should also keep track of the last rebuild's consolidation amount to prevent continuous
				 * ineffective rebuilds.
				 */

				// either the next segment itself or a new segment created and enqueued by it.
				this.head = this.head.advanceHead();
			}

//			this.size++;

			/*
			 * notify potentially waiting channel that new work is waiting.
			 * Only owner channels ever wait on a mark queue instance.
			 * Only marking channels ever notify on a mark queue instance
			 * (including the owner channel itself, but then it is not waiting in the first place).
			 */
			this.notify();
		}

		@Override
		public synchronized boolean hasElements()
		{
			return this.head != this.tail || this.head.hasElements();
		}



		static final class Segment
		{
			private final long[]  oids     ;
			private final int     length   ;

			private       int     lowIndex ;
			private       int     highIndex;
			              Segment next     ;

			Segment(final int length, final Segment next)
			{
				super();
				this.oids = new long[this.length = JadothMath.positive(length)];
				this.next = next;
//				debugln("new segment");
			}

			final Segment advanceTail()
			{
				this.clear();
				return this.next;
			}

			final Segment advanceHead()
			{
				// either the next segment if it is empty or a new segment hooked in between this segment and the next
				return this.next.highIndex == 0
					? this.next
					: (this.next = new Segment(this.length, this.next))
				;
			}

			final boolean hasElements()
			{
				return this.lowIndex < this.highIndex;
			}

			final void clear()
			{
				this.lowIndex = this.highIndex = 0;
			}

			final int getNext(final long[] buffer)
			{
				if(this.lowIndex >= this.highIndex)
				{
					return 0;
				}

				final int copyLength = Math.min(this.highIndex - this.lowIndex, buffer.length);
				System.arraycopy(this.oids, this.lowIndex, buffer, 0, copyLength);

//				debugln("get next "+copyLength);

				return copyLength;
			}

			final boolean advanceLowIndex(final int amount)
			{
				// should never happen, but just in case. Better check here than causing data to get deleted erroneously by the GC.
				if(this.lowIndex + amount > this.highIndex)
				{
					throw new RuntimeException(); // (07.07.2016 TM)EXCP: proper exception
				}

				// report whether this segment is fully processed.
				return (this.lowIndex += amount) == this.length;
			}

			final boolean enqueue(final long oid)
			{
				// store oid in the current bucket.
				this.oids[this.highIndex] = oid;

				// report whether this segment is filled.
				return ++this.highIndex >= this.length;
			}

		}


//		public static void main(final String[] args)
//		{
//			final Implementation q = new Implementation(10);
	//
//			final long[] buffer = new long[20];
//			final int amount = 999;
//			int i = 0;
//			while(true)
//			{
//				if(i >= amount && !q.hasElements())
//				{
//					break;
//				}
	//
//				if(i < amount && Math.random() < 0.9)
//				{
//					q.enqueue(i++);
//				}
//				else
//				{
//					final int bufferSize = q.getNext(buffer);
//					q.advanceTail(bufferSize);
//				}
//			}
//			debugln(q.toString());
//		}

	}

}