package net.jadoth.experimental.collections;

@Deprecated
public class SegmentList<E> extends AbstractSegment<E>
{
	AbstractSegment<E> first;


	/* (20.07.2015 TM)NOTE: Problem:
	 * every trivial appending add() would have to increment several elementCount accross all hierarchies
	 * scattered around the RAM (every parent-jump is a potential cache miss).
	 * This might make trivial collecting relatively slow, which is contraproductive for the case of a
	 * general purpose collection.
	 *
	 * General purpose means:
	 * - RAM- and performance-efficient trivial collecting
	 * - good inserting and deleting performance
	 * - acceptable random access performance
	 *
	 * all three can be achieved with a simple one-dimensional array-backed SegmentList with properly chosen segment size
	 */


	@Override
	public final E at(final long index)
	{
		return this.first.navigateTo(index);
	}

}

abstract class AbstractSegment<E>
{
	AbstractSegment<E> parent, next, prev;
	long elementCount;

	abstract E at(long index);

	final E navigateTo(final long index)
	{
		if(index > this.elementCount)
		{
			return this.next.at(index - this.elementCount);
		}
		return this.at(index);
	}

}


final class Node<E> extends AbstractSegment<E>
{
	AbstractSegment<E> child;
	int childCount;

	@Override
	final E at(final long index)
	{
		return this.child.navigateTo(index);
	}

}

final class Leaf<E> extends AbstractSegment<E>
{
	E[] elements;

	@Override
	final E at(final long index)
	{
		return this.elements[(int)index]; // cast is safe via logic
	}
}
