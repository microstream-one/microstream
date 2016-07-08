package net.jadoth.storage.types;

import net.jadoth.math.JadothMath;

final class OidMarkQueue
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	static OidMarkQueue New(final int segmentLength)
	{
		return new OidMarkQueue(segmentLength);
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Segment root;

	private Segment head, tail;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	private OidMarkQueue(final int segmentLength)
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
	}

	synchronized int getNext(final long[] buffer)
	{
		return this.tail.getNext(buffer);
	}

	synchronized void advanceTail(final int amount)
	{
		if(this.tail.advanceLowIndex(amount))
		{
//			debugln("advance segment");

			this.tail = this.tail.advanceTail();
		}
	}

	synchronized void enqueue(final long oid)
	{
//		debugln("enqueue "+oid + " ("+this.head.lowIndex+" / " +this.head.highIndex+")");
		if(this.head.enqueue(oid))
		{
//			debugln("segment full");

			// either the next segment itself or a new segment created and enqueued by it.
			this.head = this.head.advanceHead();
		}
	}

	synchronized boolean hasElements()
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
//			debugln("new segment");
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

//			debugln("get next "+copyLength);

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


//	public static void main(final String[] args)
//	{
//		final OidMarkQueue q = new OidMarkQueue(10);
//
//		final long[] buffer = new long[20];
//		final int amount = 999;
//		int i = 0;
//		while(true)
//		{
//			if(i >= amount && !q.hasElements())
//			{
//				break;
//			}
//
//			if(i < amount && Math.random() < 0.9)
//			{
//				q.enqueue(i++);
//			}
//			else
//			{
//				final int bufferSize = q.getNext(buffer);
//				q.advanceTail(bufferSize);
//			}
//		}
//		debugln(q.toString());
//	}

}
