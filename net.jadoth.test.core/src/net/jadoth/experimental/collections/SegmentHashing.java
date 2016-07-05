package net.jadoth.experimental.collections;


/* idea for hashing structure with less pointer resolution and instance header overhead.
 * drawback: collections using this concept could not be ordered (Enum, Table).
 */
public class SegmentHashing<E>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////


	@SuppressWarnings("unchecked")
	private static <E> Segment<E>[] newSlots(final int capacity)
	{
		return new Segment[capacity];
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private Segment<E>[] slots;

	private final int hashRange;
	private int size;






	public SegmentHashing(final int initialCapacity)
	{
		super();
		this.hashRange = (this.slots = newSlots(initialCapacity)).length - 1;
	}


	public boolean contains(final E element)
	{
		final Segment<E> e;
		return (e = this.slots[this.hashRange & System.identityHashCode(element)]) == null
			?false
			:e.contains(element)
		;
	}

	public boolean add(final E element)
	{
		final Segment<E> s;
		if((s = this.slots[this.hashRange & System.identityHashCode(element)]) == null)
		{
			this.putHashChainHead(element);
			return true;
		}
		if(s.contains(element))
		{
			return false;
		}
		s.add(element);
		this.size++;
		return true;
	}

	private void putHashChainHead(final E element)
	{
		(this.slots[this.hashRange & System.identityHashCode(element)] = new Segment<>()).e1 = element;
		this.size++;
	}



	// hardcoded tiny "array" with link to next segment at the end
	static final class Segment<E>
	{
		E e1, e2, e3, e4;
		Segment<E> link;

		final boolean contains(final E element)
		{
			if(this.e1 == element || this.e2 == element || this.e3 == element || this.e4 == element)
			{
				return true;
			}
			return this.link == null ?false :this.link.contains(element);
		}

		final void add(final E element)
		{
			if(this.e1 == null)
			{
				this.e1 = element;
			}
			else if(this.e2 == null){
				this.e2 = element;
			}
			else if(this.e3 == null){
				this.e3 = element;
			}
			else if(this.e4 == null){
				this.e4 = element;
			}
			else if(this.link != null){
				this.link.add(element);
			}
			else
			{
				(this.link = new Segment<>()).e1 = element;
			}
		}

	}
}
