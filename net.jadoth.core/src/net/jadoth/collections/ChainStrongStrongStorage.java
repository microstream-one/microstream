package net.jadoth.collections;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static net.jadoth.Jadoth.BREAK;
import static net.jadoth.Jadoth.checkArrayRange;
import static net.jadoth.collections.AbstractChainEntry.HOP_NEXT;
import static net.jadoth.collections.AbstractChainEntry.HOP_PREV;

import java.util.Comparator;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import net.jadoth.Jadoth;
import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.functional.Aggregator;
import net.jadoth.functional.BiProcedure;
import net.jadoth.functional.IndexProcedure;
import net.jadoth.reference.ReferenceType;
import net.jadoth.util.Equalator;
import net.jadoth.util.KeyValue;
import net.jadoth.util.branching.ThrowBreak;
import net.jadoth.util.chars.VarString;


public final class ChainStrongStrongStorage<K, V, EN extends AbstractChainEntry<KeyValue<K, V>, K, V, EN>>
extends AbstractChainKeyValueStorage<K, V, EN>
{
	// CHECKSTYLE.OFF: FinalParameter: A LOT of methods use that pattern in this class

	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public ChainStrongStrongStorage(final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent, final EN head)
	{
		super(parent, head);
	}



	static <K, V, EN extends AbstractChainEntry<KeyValue<K, V>, K, V, EN>>
	void entriesMergesortHead(final EN head, final Comparator<? super K> comparator)
	{
		EN last, entry;
		try
		{
			entry = entriesMergesort0(head.next, comparator); // sort
		}
		catch(final Throwable e)
		{
			// rollback 8-)
			for(entry = head.prev; (entry = (last = entry).prev) != head;)
			{
				entry.next = last;
			}
			throw e;
		}

		// reattach sorted chain to head and rebuild prev direction
		(head.next = entry).prev = head;      // entry is new start entry
		while((entry = (last = entry).next) != null)
		{
			entry.prev = last;                        // rebuild prev references
		}
		head.prev = last;                            // last entry now is new end entry (obviously)
	}

	static <K, V, EN extends AbstractChainEntry<KeyValue<K, V>, K, V, EN>> void entriesMergesortRange(
		final EN preFirst,
		final EN postLast,
		final EN head,
		final Comparator<? super K> comparator
	)
	{
		EN last, entry;
		try
		{
			if(postLast == null)
			{
				entry = entriesMergesort0(preFirst.next, comparator); // sort normally
			}
			else
			{
				entry = entriesRngMergesort0(preFirst.next, postLast, comparator); // sort
			}
		}
		catch(final Throwable e)
		{
			// rollback 8-)
			for(entry = (postLast == null ? head : postLast).prev; (entry = (last = entry).prev) != preFirst;)
			{
				entry.next = last;
			}
			throw e;
		}

		// reattach sorted chain to head and rebuild prev direction
		(preFirst.next = entry).prev = preFirst;      // entry is new start entry
		while((entry = (last = entry).next) != null)
		{
			entry.prev = last;                        // rebuild prev references
		}
		if(postLast != null)
		{
			postLast.prev = last;                     // last entry now is new end entry
		}
		else
		{
			head.prev = last;
		}
	}

	private static <K, V, EN extends AbstractChainEntry<KeyValue<K, V>, K, V, EN>> EN entriesMergesort0(final EN chain, final Comparator<? super K> comparator)
	{
		// special case handling for empty or trivial chain
		if(chain == null || chain.next == null)
		{
			return chain;
		}

		// inlined iterative splitting
		EN chain2, t1, t2 = chain2 = (t1 = chain).next;
		while(t2 != null && (t1 = t1.next = t2.next) != null)
		{
			t2 = t2.next = t1.next;
		}

		// merging
		return entriesMerge1(entriesMergesort0(chain, comparator), entriesMergesort0(chain2, comparator), comparator);
	}

	private static <K, V, EN extends AbstractChainEntry<KeyValue<K, V>, K, V, EN>> EN entriesRngMergesort0(
		final EN chain,
		final EN end,
		final Comparator<? super K> cmp
	)
	{
		// special case handling for empty or trivial chain
		if(chain == null || chain.next == null)
		{
			return chain;
		}

		// inlined iterative splitting
		EN chain2, t1, t2 = chain2 = (t1 = chain).next;
		while(t2 != end && (t1 = t1.next = t2.next) != end)
		{
			t2 = t2.next = t1.next;
		}

		// merging
		return entriesRngMerge1(entriesRngMergesort0(chain, end, cmp), entriesRngMergesort0(chain2, end, cmp), end, cmp);
	}

	// merge iterative
	private static <K, V, EN extends AbstractChainEntry<KeyValue<K, V>, K, V, EN>>
	EN entriesMerge1(EN c1, EN c2, final Comparator<? super K> cmp)
	{
		if(c1 == null)
		{
			return c2;
		}
		if(c2 == null)
		{
			return c1;
		}

		final EN c;
		if(cmp.compare(c1.key(), c2.key()) < 0)
		{
			c1 = (c = c1).next;
		}
		else
		{
			c2 = (c = c2).next;
		}

		for(EN t = c;;)
		{
			if(c1 == null)
			{
				t.next = c2;
				break;
			}
			else if(c2 == null)
			{
				t.next = c1;
				break;
			}
			else if(cmp.compare(c1.key(), c2.key()) < 0)
			{
				c1 = (t = t.next = c1).next;
			}
			else
			{
				c2 = (t = t.next = c2).next;
			}
		}
		return c;
	}

	// merge iterative
	private static <K, V, EN extends AbstractChainEntry<KeyValue<K, V>, K, V, EN>> EN entriesRngMerge1(
		      EN c1,
		      EN c2,
		final EN end,
		final Comparator<? super K> cmp
	)
	{
		if(c1 == end)
		{
			return c2;
		}
		if(c2 == end)
		{
			return c1;
		}

		final EN c;
		if(cmp.compare(c1.key(), c2.key()) < 0)
		{
			c1 = (c = c1).next;
		}
		else
		{
			c2 = (c = c2).next;
		}

		for(EN t = c;;)
		{
			if(c1 == end)
			{
				t.next = c2;
				break;
			}
			else if(c2 == end)
			{
				t.next = c1;
				break;
			}
			else if(cmp.compare(c1.key(), c2.key()) < 0)
			{
				c1 = (t = t.next = c1).next;
			}
			else
			{
				c2 = (t = t.next = c2).next;
			}
		}
		return c;
	}

	private static <K, V, EN extends AbstractChainEntry<KeyValue<K, V>, K, V, EN>> void keyMergesortHead(final EN head, final Comparator<? super K> comparator)
	{
		EN last, entry;
		try
		{
			entry = mergesort0(head.next, comparator); // sort
		}
		catch(final Throwable e)
		{
			// rollback 8-)
			for(entry = head.prev; (entry = (last = entry).prev) != head;)
			{
				entry.next = last;
			}
			throw e;
		}

		// reattach sorted chain to head and rebuild prev direction
		(head.next = entry).prev = head;              // entry is new start entry
		while((entry = (last = entry).next) != null)
		{
			entry.prev = last;                        // rebuild prev references
		}
		head.prev = last;                             // last entry now is new end entry (obviously)
	}

	private static <K, V, EN extends AbstractChainEntry<KeyValue<K, V>, K, V, EN>> void keyMergesortRange(
		final EN preFirst,
		final EN postLast,
		final EN head,
		final Comparator<? super K> comparator
	)
	{
		EN last, entry;
		try
		{
			if(postLast == null)
			{
				entry = mergesort0(preFirst.next, comparator); // sort normally
			}
			else
			{
				entry = rngMergesort0(preFirst.next, postLast, comparator); // sort
			}
		}
		catch(final Throwable e)
		{
			// rollback 8-)
			for(entry = (postLast == null ? head : postLast).prev; (entry = (last = entry).prev) != preFirst;)
			{
				entry.next = last;
			}
			throw e;
		}

		// reattach sorted chain to head and rebuild prev direction
		(preFirst.next = entry).prev = preFirst;      // entry is new start entry
		while((entry = (last = entry).next) != null)
		{
			entry.prev = last;                        // rebuild prev references
		}
		if(postLast != null)
		{
			postLast.prev = last;                     // entry now is new end entry
		}
		else
		{
			head.prev = last;
		}
	}

	private static <K, V, EN extends AbstractChainEntry<KeyValue<K, V>, K, V, EN>> EN mergesort0(final EN chain, final Comparator<? super K> comparator)
	{
		// special case handling for empty or trivial chain
		if(chain == null || chain.next == null)
		{
			return chain;
		}

		// inlined iterative splitting
		EN chain2, t1, t2 = chain2 = (t1 = chain).next;
		while(t2 != null && (t1 = t1.next = t2.next) != null)
		{
			t2 = t2.next = t1.next;
		}

		// merging
		return merge1(mergesort0(chain, comparator), mergesort0(chain2, comparator), comparator);
	}

	private static <K, V, EN extends AbstractChainEntry<KeyValue<K, V>, K, V, EN>> EN rngMergesort0(
		final EN chain,
		final EN end,
		final Comparator<? super K> cmp
	)
	{
		// special case handling for empty or trivial chain
		if(chain == null || chain.next == null)
		{
			return chain;
		}

		// inlined iterative splitting
		EN chain2, t1, t2 = chain2 = (t1 = chain).next;
		while(t2 != end && (t1 = t1.next = t2.next) != end)
		{
			t2 = t2.next = t1.next;
		}

		// merging
		return rngMerge1(rngMergesort0(chain, end, cmp), rngMergesort0(chain2, end, cmp), end, cmp);
	}

	// merge iterative
	private static <K, V, EN extends AbstractChainEntry<KeyValue<K, V>, K, V, EN>>
	EN merge1(EN c1, EN c2, final Comparator<? super K> cmp)
	{
		if(c1 == null)
		{
			return c2;
		}
		if(c2 == null)
		{
			return c1;
		}

		final EN c;
		if(cmp.compare(c1.key(), c2.key()) < 0)
		{
			c1 = (c = c1).next;
		}
		else
		{
			c2 = (c = c2).next;
		}

		for(EN t = c;;)
		{
			if(c1 == null)
			{
				t.next = c2;
				break;
			}
			else if(c2 == null)
			{
				t.next = c1;
				break;
			}
			else if(cmp.compare(c1.key(), c2.key()) < 0)
			{
				c1 = (t = t.next = c1).next;
			}
			else
			{
				c2 = (t = t.next = c2).next;
			}
		}
		return c;
	}

	// merge iterative
	private static <K, V, EN extends AbstractChainEntry<KeyValue<K, V>, K, V, EN>> EN rngMerge1(
		      EN c1,
		      EN c2,
		final EN end,
		final Comparator<? super K> cmp
	)
	{
		if(c1 == end)
		{
			return c2;
		}
		if(c2 == end)
		{
			return c1;
		}

		final EN c;
		if(cmp.compare(c1.key(), c2.key()) < 0)
		{
			c1 = (c = c1).next;
		}
		else
		{
			c2 = (c = c2).next;
		}

		for(EN t = c;;)
		{
			if(c1 == end)
			{
				t.next = c2;
				break;
			}
			else if(c2 == end)
			{
				t.next = c1;
				break;
			}
			else if(cmp.compare(c1.key(), c2.key()) < 0)
			{
				c1 = (t = t.next = c1).next;
			}
			else
			{
				c2 = (t = t.next = c2).next;
			}
		}
		return c;
	}


	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final Iterator<K> keyIterator()
	{
		return new KeyItr();
	}

	@Override
	public final boolean keyEqualsContent(final XGettingCollection<? extends K> other, final Equalator<? super K> equalator)
	{
		if(Jadoth.to_int(this.parent.size()) != Jadoth.to_int(other.size()))
		{
			return false;
		}

		if(other instanceof AbstractSimpleArrayCollection<?>)
		{
			final int otherSize = Jadoth.to_int(other.size());
			final K[] otherData = AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)other);
			EN entry = this.head.next;
			for(int i = 0; i < otherSize; i++, entry = entry.next)
			{
				if(!equalator.equal(entry.key(), otherData[i]))
				{
					return false;
				}
			}
			return true;
		}

		final Aggregator<K, Boolean> agg = new Aggregator<K, Boolean>()
		{
			private EN entry = ChainStrongStrongStorage.this.head;
			private boolean notEqual; // false by default

			@Override
			public final void accept(final K element)
			{
				if((this.entry = this.entry.next) == null)
				{
					this.notEqual = true;
					throw BREAK; // chain is too short
				}
				if(!equalator.equal(element, this.entry.key()))
				{
					this.notEqual = true;
					throw BREAK; // unequal element found
				}
			}

			@Override
			public final Boolean yield()
			{
				/*
				 * no explicitely unequal pair may have been found (obviously)
				 * current entry may not be null (otherwise chain was too short)
				 * but next entry in chain must be null (otherwise chain is too long)
				 */
				return this.notEqual || this.entry == null || (this.entry = this.entry.next) != null
					? FALSE
					: TRUE
				;
			}
		};

		other.iterate(agg);
		return agg.yield();
	}



	///////////////////////////////////////////////////////////////////////////
	// special map logic //
	//////////////////////

	@Override
	public final V searchValue(final K key, final Equalator<? super K> equalator)
	{
		for(EN e = this.head.next; e != null; e = e.next)
		{
			if(equalator.equal(e.key(), key))
			{
				return e.value();
			}
		}
		return null;
	}


	///////////////////////////////////////////////////////////////////////////
	//   containing     //
	/////////////////////

	// containing - null //

	@Override
	public final boolean keyContainsNull()
	{
		for(EN e = this.head.next; e != null; e = e.next)
		{
			if(e.hasNullKey())
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public final boolean keyRngContainsNull(final int offset, int length)
	{
		EN e = this.getRangeChainEntry(offset, length); // validate range and scroll to offset
		if(length < 0)
		{
			for(; length++ < 0; e = e.prev)
			{
				if(e.hasNullKey())
				{
					return true;
				}
			}
		}
		else
		{
			for(; length-- > 0; e = e.next)
			{
				if(e.hasNullKey())
				{
					return true;
				}
			}
		}
		return false;
	}

	// containing - identity //

	@Override
	public final boolean keyContainsId(final K element)
	{
		for(EN e = this.head.next; e != null; e = e.next)
		{
			if(e.key() == element)
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public final boolean keyRngContainsId(final int offset, int length, final K element)
	{
		EN e = this.getRangeChainEntry(offset, length); // validate range and scroll to offset
		if(length < 0)
		{
			for(; length++ < 0; e = e.prev)
			{
				if(e.key() == element)
				{
					return true;
				}
			}
		}
		else
		{
			for(; length-- > 0; e = e.next)
			{
				if(e.key() == element)
				{
					return true;
				}
			}
		}
		return false;
	}

	// containing - logical //

	@Override
	public final boolean keyContains(final K element)
	{
		for(EN e = this.head.next; e != null; e = e.next)
		{
			if(e.key() == element)
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public final boolean keyContains(final K sample, final Equalator<? super K> equalator)
	{
		for(EN e = this.head.next; e != null; e = e.next)
		{
			if(equalator.equal(e.key(), sample))
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public final boolean keyRngContains(final int offset, int length, final K element)
	{
		EN e = this.getRangeChainEntry(offset, length); // validate range and scroll to offset
		if(length < 0)
		{
			for(; length++ < 0; e = e.prev)
			{
				if(e.key() == element)
				{
					return true;
				}
			}
		}
		else
		{
			for(; length-- > 0; e = e.next)
			{
				if(e.key() == element)
				{
					return true;
				}
			}
		}
		return false;
	}

	// containing - all array //

	@Override
	public final boolean keyContainsAll(final K[] elements, final int elementsOffset, final int elementsLength)
	{
		final EN first;
		if((first = this.head.next) == null)
		{
			return false; // size 0
		}
		final int d;
		if((d = ChainStorageStrong.validateArrayIteration(elements, elementsOffset, elementsLength)) == 0)
		{
			return true;
		}
		final int elementsBound = elementsOffset + elementsLength;

		main:
		for(int ei = elementsOffset; ei != elementsBound; ei += d)
		{
			final K element = elements[ei];
			for(EN e = first; e != null; e = e.next)
			{
				if(e.key() == element)
				{
					continue main;
				}
			}
			return false;  // one element was not found in this list, return false
		}
		return true; // all elements have been found, return true
	}

	@Override
	public final boolean keyRngContainsAll(
		final int offset,
		      int length,
		final K[] elements,
		final int elementsOffset,
		final int elementsLength
	)
	{
		final EN first;
		if((first = this.getRangeChainEntry(offset, length)) == null)
		{
			return false; // size 0
		}
		final int d;
		if((d = ChainStorageStrong.validateArrayIteration(elements, elementsOffset, elementsLength)) == 0)
		{
			return true;
		}
		final int elementsBound = elementsOffset + elementsLength;

		// hopping direction
		final AbstractChainEntry.Hopper hop;
		if(length < 0)
		{
			hop = HOP_PREV;
			length = -length;
		}
		else
		{
			hop = HOP_NEXT;
		}

		main:
		for(int ei = elementsOffset; ei != elementsBound; ei += d)
		{
			final K element = elements[ei];
			for(EN e = first; e != null; e = hop.hop(e))
			{
				if(e.key() == element)
				{
					continue main;
				}
			}
			return false;  // one element was not found in this list, return false
		}
		return true;  // all elements have been found, return true
	}

	// containing - all collection //

	@Override
	public final boolean keyContainsAll(final XGettingCollection<? extends K> elements)
	{
		if(elements instanceof AbstractSimpleArrayCollection<?>)
		{
			// directly check array against array without predicate function or method calls
			return this.keyContainsAll(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)elements),
				0,
				Jadoth.to_int(elements.size())
			);
		}

		// iterate by predicate function
		return elements.applies(e ->
		{
			return this.keyContains(e);
		});
	}

	@Override
	public final boolean keyRngContainsAll(final int offset, final int length, final XGettingCollection<? extends K> elements)
	{
		if(elements instanceof AbstractSimpleArrayCollection<?>)
		{
			return this.keyRngContainsAll(
				offset, length,
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)elements), 0, Jadoth.to_int(elements.size())
			);
		}

		// iterate by predicate function
		final EN first; // validate range and scroll to offset
		if((first = this.getRangeChainEntry(offset, length)) == null)
		{
			return false;
		}

		// iterate by predicate function
		if(length < 0)
		{
			return elements.applies(e->
			{
				int len = length;
				for(EN entry = first; len++ < 0; entry = entry.prev)
				{
					if(entry.key() == e)
					{
						return true;
					}
				}
				return false;
			});
		}
		return elements.applies(e ->
		{
			int len = length;
			for(EN entry = first; len-- > 0; entry = entry.next)
			{
				if(entry.key() == e)
				{
					return true;
				}
			}
			return false;
		});
	}


	///////////////////////////////////////////////////////////////////////////
	//    applying      //
	/////////////////////

	// applying - single //

	@Override
	public final boolean keyApplies(final Predicate<? super K> predicate)
	{
		try
		{
			for(EN e = this.head.next; e != null; e = e.next)
			{
				if(predicate.test(e.key()))
				{
					return true;
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return false;
	}

	@Override
	public final boolean keyRngApplies(final int offset, int length, final Predicate<? super K> predicate)
	{
		EN e = this.getRangeChainEntry(offset, length); // validate range and scroll to offset
		try
		{
			if(length < 0)
			{
				for(; length++ < 0; e = e.prev)
				{
					if(predicate.test(e.key()))
					{
						return true;
					}
				}
			}
			else
			{
				for(; length-- > 0; e = e.next)
				{
					if(predicate.test(e.key()))
					{
						return true;
					}
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return false;
	}

	// applying - all //

	@Override
	public final boolean keyAppliesAll(final Predicate<? super K> predicate)
	{
		try
		{
			for(EN e = this.head.next; e != null; e = e.next)
			{
				if(!predicate.test(e.key()))
				{
					return false;
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return true;
	}

	@Override
	public final boolean keyRngAppliesAll(final int offset, int length, final Predicate<? super K> predicate)
	{
		EN e = this.getRangeChainEntry(offset, length); // validate range and scroll to offset
		try
		{
			if(length < 0)
			{
				for(; length++ < 0; e = e.prev)
				{
					if(!predicate.test(e.key()))
					{
						return false;
					}
				}
			}
			else
			{
				for(; length-- > 0; e = e.next)
				{
					if(!predicate.test(e.key()))
					{
						return false;
					}
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return true;
	}



	///////////////////////////////////////////////////////////////////////////
	//    counting      //
	/////////////////////

	// counting - element //

	@Override
	public final int keyCount(final K element)
	{
		int count = 0;
		for(EN e = this.head.next; e != null; e = e.next)
		{
			if(e.key() == element)
			{
				count++;
			}
		}
		return count;
	}

	@Override
	public final int keyCount(final K sample, final Equalator<? super K> equalator)
	{
		int count = 0;
		for(EN e = this.head.next; e != null; e = e.next)
		{
			if(equalator.equal(e.key(), sample))
			{
				count++;
			}
		}
		return count;
	}

	@Override
	public final int keyRngCount(final int offset, int length, final K element)
	{
		EN e = this.getRangeChainEntry(offset, length); // validate range and scroll to offset
		int count = 0;
		if(length < 0)
		{
			for(; length++ < 0; e = e.prev)
			{
				if(e.key() == element)
				{
					count++;
				}
			}
		}
		else
		{
			for(; length-- > 0; e = e.next)
			{
				if(e.key() == element)
				{
					count++;
				}
			}
		}
		return count;
	}

	// counting - predicate //

	@Override
	public final int keyCount(final Predicate<? super K> predicate)
	{
		int count = 0;
		try
		{
			for(EN e = this.head.next; e != null; e = e.next)
			{
				if(predicate.test(e.key()))
				{
					count++;
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return count;
	}

	@Override
	public final int keyRngCount(final int offset, int length, final Predicate<? super K> predicate)
	{
		EN e = this.getRangeChainEntry(offset, length); // validate range and scroll to offset
		int count = 0;
		try
		{
			if(length < 0)
			{
				for(; length++ < 0; e = e.prev)
				{
					if(predicate.test(e.key()))
					{
						count++;
					}
				}
			}
			else
			{
				for(; length-- > 0; e = e.next)
				{
					if(predicate.test(e.key()))
					{
						count++;
					}
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return count;
	}



	///////////////////////////////////////////////////////////////////////////
	// data arithmetic  //
	/////////////////////

	// data - data sets //

	@Override
	public final <C extends Consumer<? super K>> C keyIntersect(
		final XGettingCollection<? extends K> samples,
		final Equalator<? super K> equalator,
		final C target
	)
	{
		if(samples instanceof AbstractSimpleArrayCollection<?>)
		{
			final K[] array = AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)samples);
			final int size = Jadoth.to_int(samples.size());
			ch:
			for(EN entry = this.head.next; entry != null; entry = entry.next)
			{
				final K element = entry.key();
				for(int i = 0; i < size; i++)
				{
					if(equalator.equal(element, array[i]))
					{
						target.accept(element);
						continue ch;
					}
				}
			}
			return target;
		}

		/* has to be the long way around because:
		 * - can't directly pass an instance of type E to a collection of type ? extends E.
		 * - chain's equal element instances must be added, not samples'.
		 */
		final CachedSampleEquality<K> equalCurrentElement = new CachedSampleEquality<>(equalator);
		for(EN entry = this.head.next; entry != null; entry = entry.next)
		{
			equalCurrentElement.sample = entry.key();
			if(samples.containsSearched(equalCurrentElement))
			{
				target.accept(equalCurrentElement.sample);
			}
		}
		return target;
	}

	@Override
	public final <C extends Consumer<? super K>> C keyExcept(
		final XGettingCollection<? extends K> samples,
		final Equalator<? super K> equalator,
		final C target
	)
	{
		if(samples instanceof AbstractSimpleArrayCollection<?>)
		{
			final K[] array = AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)samples);
			final int size = Jadoth.to_int(samples.size());
			ch:
			for(EN entry = this.head.next; entry != null; entry = entry.next)
			{
				final K element = entry.key();
				for(int i = 0; i < size; i++)
				{
					if(equalator.equal(element, array[i]))
					{
						continue ch;
					}
				}
				target.accept(element);
			}
			return target;
		}

		/* has to be the long way around because:
		 * - can't directly pass an instance of type E to a collection of type ? extends E.
		 * - chain's equal element instances must be added, not samples'.
		 */
		final CachedSampleEquality<K> equalCurrentElement = new CachedSampleEquality<>(equalator);
		ch:
		for(EN entry = this.head.next; entry != null; entry = entry.next)
		{
			equalCurrentElement.sample = entry.key();
			if(samples.containsSearched(equalCurrentElement))
			{
				continue ch;
			}
			target.accept(equalCurrentElement.sample);
		}
		return target;
	}

	@Override
	public final <C extends Consumer<? super K>> C keyUnion(
		final XGettingCollection<? extends K> samples,
		final Equalator<? super K> equalator,
		final C target
	)
	{
		this.keyCopyTo(target);
		if(samples instanceof AbstractSimpleArrayCollection<?>)
		{
			final K[] array = AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)samples);
			final int size = Jadoth.to_int(samples.size());
			ch:
			for(int i = 0; i < size; i++)
			{
				final K sample = array[i];
				for(EN entry = this.head.next; entry != null; entry = entry.next)
				{
					if(equalator.equal(entry.key(), sample))
					{
						continue ch;
					}
				}
				target.accept(sample);
			}
			return target;
		}

		/* has to be the long way around because:
		 * - can't directly pass an instance of type E to a collection of type ? extends E.
		 * - chain's equal element instances must be added, not samples'.
		 */
		samples.iterate(e ->
		{
			// local reference to AIC field
			final Equalator<? super K> equalator2 = equalator;

			for(EN entry = ChainStrongStrongStorage.this.head.next; entry != null; entry = entry.next)
			{
				if(equalator2.equal(e, entry.key()))
				{
					return;
				}
			}
			target.accept(e);
		});
		return target;
	}

	@Override
	public final <C extends Consumer<? super K>> C keyRngIntersect(
		final int offset,
		      int length,
		final XGettingCollection<? extends K> samples,
		final Equalator<? super K> equalator,
		final C target
	)
	{
		final EN first = this.getRangeChainEntry(offset, length);
		if(length < 0)
		{
			length = -length;
		}
		final AbstractChainEntry.Hopper ch = length < 0 ? AbstractChainEntry.HOP_PREV : AbstractChainEntry.HOP_NEXT;

		if(samples instanceof AbstractSimpleArrayCollection<?>)
		{
			final K[] array = AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)samples);
			final int size = Jadoth.to_int(samples.size());
			ch:
			for(EN entry = first; length-- > 0; entry = ch.hop(entry))
			{
				final K element = entry.key();
				for(int i = 0; i < size; i++)
				{
					if(equalator.equal(element, array[i]))
					{
						target.accept(element);
						continue ch;
					}
				}
			}
			return target;
		}

		/* has to be the long way around because:
		 * - can't directly pass an instance of type E to a collection of type ? extends E.
		 * - chain's equal element instances must be added, not samples'.
		 */
		final CachedSampleEquality<K> equalCurrentElement = new CachedSampleEquality<>(equalator);
		for(EN entry = first; length-- > 0; entry = ch.hop(entry))
		{
			equalCurrentElement.sample = entry.key();
			if(samples.containsSearched(equalCurrentElement))
			{
				target.accept(equalCurrentElement.sample);
			}
		}
		return target;
	}

	@Override
	public final <C extends Consumer<? super K>> C keyRngExcept(
		final int offset,
		      int length,
		final XGettingCollection<? extends K> samples,
		final Equalator<? super K> equalator,
		final C target
	)
	{

		final EN first = this.getRangeChainEntry(offset, length);
		if(length < 0)
		{
			length = -length;
		}
		final AbstractChainEntry.Hopper ch = length < 0 ? AbstractChainEntry.HOP_PREV : AbstractChainEntry.HOP_NEXT;

		if(samples instanceof AbstractSimpleArrayCollection<?>)
		{
			final K[] array = AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)samples);
			final int size = Jadoth.to_int(samples.size());
			ch:
			for(EN entry = first; length-- > 0; entry = ch.hop(entry))
			{
				final K element = entry.key();
				for(int i = 0; i < size; i++)
				{
					if(equalator.equal(element, array[i]))
					{
						continue ch;
					}
				}
				target.accept(element);
			}
			return target;
		}

		/* has to be the long way around because:
		 * - can't directly pass an instance of type E to a collection of type ? extends E.
		 * - chain's equal element instances must be added, not samples'.
		 */
		final CachedSampleEquality<K> equalCurrentElement = new CachedSampleEquality<>(equalator);
		ch:
		for(EN entry = first; length-- > 0; entry = ch.hop(entry))
		{
			equalCurrentElement.sample = entry.key();
			if(samples.containsSearched(equalCurrentElement))
			{
				continue ch;
			}
			target.accept(equalCurrentElement.sample);
		}
		return target;
	}

	@Override
	public final <C extends Consumer<? super K>> C keyRngUnion(
		final int offset,
		final int length,
		final XGettingCollection<? extends K> samples,
		final Equalator<? super K> equalator,
		final C target
	)
	{
		final EN first = this.getRangeChainEntry(offset, length);
		final AbstractChainEntry.Hopper ch = length < 0 ? AbstractChainEntry.HOP_PREV : AbstractChainEntry.HOP_NEXT;

		this.keyRngCopyTo(offset, length, target);
		if(samples instanceof AbstractSimpleArrayCollection<?>)
		{
			final K[] array = AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)samples);
			final int size = Jadoth.to_int(samples.size());
			ar:
			for(int i = 0; i < size; i++)
			{
				final K sample = array[i];
				int len = length;
				for(EN entry = first; len-- > 0; entry = ch.hop(entry))
				{
					if(equalator.equal(entry.key(), sample))
					{
						continue ar;
					}
				}
				target.accept(sample);
			}
			return target;
		}

		final int normalizedLength = length >= 0 ? length : -length;
		samples.iterate(e ->
		{
			// local reference to AIC field
			final Equalator<? super K> equalator2 = equalator;

			int len = normalizedLength;
			for(EN entry = first; len-- > 0; entry = ch.hop(entry))
			{
				if(equalator2.equal(e, entry.key()))
				{
					return;
				}
			}
			target.accept(e);
		});
		return target;
	}

	// data - copying //

	@Override
	public final <C extends Consumer<? super K>> C keyCopyTo(final C target)
	{
		for(EN e = this.head.next; e != null; e = e.next)
		{
			target.accept(e.key());
		}
		return target;
	}

	@Override
	public final <C extends Consumer<? super K>> C keyRngCopyTo(final int offset, int length, final C target)
	{
		final EN first;
		if((first = this.getRangeChainEntry(offset, length)) == null)
		{
			return target;
		}

		if(length > 0)
		{
			for(EN e = first; length-- > 0; e = e.next)
			{
				target.accept(e.key());
			}
		}
		else
		{
			for(EN e = first; length++ < 0; e = e.prev)
			{
				target.accept(e.key());
			}
		}
		return target;
	}

	@Override
	public final <C extends Consumer<? super K>> C keyCopySelection(final C target, final long... indices)
	{
		final int length = indices.length, size = Jadoth.to_int(this.parent.size());

		// validate all indices before copying the first element
		for(int i = 0; i < length; i++)
		{
			if(indices[i] < 0 || indices[i] >= size)
			{
				throw new IndexOutOfBoundsException(exceptionIndexOutOfBounds(size, indices[i]));
			}
		}

		// actual copying. Note: can't sort indices as copying order might be relevant
		for(int i = 0; i < length; i++)
		{
			target.accept(this.getChainEntry(indices[i]).key()); // scrolling is pretty inefficient here :(
		}

		return target;
	}

	// data - conditional copying //

	@Override
	public final <C extends Consumer<? super K>> C keyCopyTo(final C target, final Predicate<? super K> predicate)
	{
		try
		{
			for(EN entry = this.head.next; entry != null; entry = entry.next)
			{
				if(predicate.test(entry.key()))
				{
					target.accept(entry.key());
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return target;
	}

	@Override
	public final <C extends Consumer<? super K>> C keyRngCopyTo(
		final int offset,
		      int length,
		final C target,
		final Predicate<? super K> predicate
	)
	{
		final EN first;
		if((first = this.getRangeChainEntry(offset, length)) == null)
		{
			return target;
		}

		try
		{
			if(length > 0)
			{
				for(EN e = first; length-- > 0; e = e.next)
				{
					if(predicate.test(e.key()))
					{
						target.accept(e.key());
					}
				}
			}
			else
			{
				for(EN e = first; length++ < 0; e = e.prev)
				{
					if(predicate.test(e.key()))
					{
						target.accept(e.key());
					}
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return target;
	}

	// data - array transformation //

	@Override
	public final Object[] keyToArray()
	{
		final Object[] array;
		this.keyCopyToArray(0, Jadoth.to_int(this.parent.size()), array = new Object[Jadoth.to_int(this.parent.size())], 0);
		return array;
	}

	@Override
	public final      K[] keyToArray(final Class<K> type)
	{
		final K[] array;
		this.keyCopyToArray(0, Jadoth.to_int(this.parent.size()), array = JadothArrays.newArray(type, Jadoth.to_int(this.parent.size())), 0);
		return array;
	}

	@Override
	public final Object[] keyRngToArray(final int offset, final int length)
	{
		final Object[] array;
		this.keyCopyToArray(offset, length, array = new Object[length < 0 ? -length : length], 0);
		return array;
	}

	@Override
	public final      K[] keyRngToArray(final int offset, final int length, final Class<K> type)
	{
		final K[] array;
		this.keyCopyToArray(offset, length, array = JadothArrays.newArray(type, length < 0 ? -length : length), 0);
		return array;
	}



	///////////////////////////////////////////////////////////////////////////
	//    querying      //
	/////////////////////

	@Override
	public final K keyFirst()
	{
		return this.head.next == null ? null : this.head.next.key();
	}

	@Override
	public final K keyLast()
	{
		return this.head.prev == this.head ? null : this.head.prev.key();
	}

	@Override
	public final K keyGet(final long index)
	{
		return this.getChainEntry(index).key();
	}



	///////////////////////////////////////////////////////////////////////////
	//    searching     //
	/////////////////////

	// searching - sample //

	@Override
	public final K keySeek(final K sample, final Equalator<? super K> equalator)
	{
		for(EN e = this.head.next; e != null; e = e.next)
		{
			if(equalator.equal(e.key(), sample))
			{
				return e.key();
			}
		}
		return null;
	}

	// searching - predicate //

	@Override
	public final K keySeek(final K sample)
	{
		for(EN e = this.head.next; e != null; e = e.next)
		{
			if(sample == e.key())
			{
				return e.key();
			}
		}
		return null;
	}

	@Override
	public final K keySearch(final Predicate<? super K> predicate)
	{
		try
		{
			for(EN e = this.head.next; e != null; e = e.next)
			{
				if(predicate.test(e.key()))
				{
					return e.key();
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return null;
	}

	@Override
	public final K keyRngSearch(final int offset, int length, final Predicate<? super K> predicate)
	{
		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return null;
		}
		try
		{
			if(length > 0)
			{
				for(; length-- > 0; e = e.next)
				{
					if(predicate.test(e.key()))
					{
						return e.key();
					}
				}
			}
			else
			{
				for(; length++ < 0; e = e.prev)
				{
					if(predicate.test(e.key()))
					{
						return e.key();
					}
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return null;
	}

	// searching - min max //

	@Override
	public final K keyMin(final Comparator<? super K> comparator)
	{
		EN e;
		if((e = this.head.next) == null)
		{
			return null;
		}

		K element, loopMinElement = e.key();
		for(e = e.next; e != null; e = e.next)
		{
			if(comparator.compare(loopMinElement, element = e.key()) > 0)
			{
				loopMinElement = element;
			}
		}
		return loopMinElement;
	}

	@Override
	public final K keyMax(final Comparator<? super K> comparator)
	{
		EN e;
		if((e = this.head.next) == null)
		{
			return null;
		}

		K element, loopMaxElement = e.key();
		for(e = e.next; e != null; e = e.next)
		{
			if(comparator.compare(loopMaxElement, element = e.key()) < 0)
			{
				loopMaxElement = element;
			}
		}
		return loopMaxElement;
	}

	@Override
	public final K keyRngMin(final int offset, int length, final Comparator<? super K> comparator)
	{
		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return null;
		}

		K element, loopMinElement = e.key();
		if(length > 0)
		{
			for(e = e.next, length--; length-- > 0; e = e.next)
			{
				if(comparator.compare(loopMinElement, element = e.key()) > 0)
				{
					loopMinElement = element;
				}
			}
		}
		else
		{
			for(e = e.prev, length++; length++ < 0; e = e.prev)
			{
				if(comparator.compare(loopMinElement, element = e.key()) > 0)
				{
					loopMinElement = element;
				}
			}
		}
		return loopMinElement;
	}

	@Override
	public final K keyRngMax(final int offset, int length, final Comparator<? super K> comparator)
	{
		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return null;
		}

		K element, loopMaxElement = e.key();
		if(length > 0)
		{
			for(e = e.next, length--; length-- > 0; e = e.next)
			{
				if(comparator.compare(loopMaxElement, element = e.key()) < 0)
				{
					loopMaxElement = element;
				}
			}
		}
		else
		{
			for(e = e.prev, length++; length++ < 0; e = e.prev)
			{
				if(comparator.compare(loopMaxElement, element = e.key()) < 0)
				{
					loopMaxElement = element;
				}
			}
		}
		return loopMaxElement;
	}



	///////////////////////////////////////////////////////////////////////////
	//    executing     //
	/////////////////////

	// executing - procedure //

	@Override
	public final void keyIterate(final Consumer<? super K> procedure)
	{
		try
		{
			for(EN entry = this.head; (entry = entry.next) != null;)
			{
				procedure.accept(entry.key());
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
	}

	@Override
	public final <A> void keyJoin(final BiProcedure<? super K, A> joiner, final A aggregate)
	{
		try
		{
			for(EN entry = this.head; (entry = entry.next) != null;)
			{
				joiner.accept(entry.key(), aggregate);
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
	}

	@Override
	public final void keyRngIterate(final int offset, int length, final Consumer<? super K> procedure)
	{
		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return;
		}

		try
		{
			if(length > 0)
			{
				for(; length-- > 0; e = e.next)
				{
					procedure.accept(e.key());
				}
			}
			else
			{
				for(; length++ < 0; e = e.prev)
				{
					procedure.accept(e.key());
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
	}

	// executing - indexed procedure //

	@Override
	public final void keyIterateIndexed(final IndexProcedure<? super K> procedure)
	{
		try
		{
			int i = -1;
			for(EN entry = this.head; (entry = entry.next) != null;)
			{
				procedure.accept(entry.key(), ++i);
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
	}

	private static <K, V, EN extends AbstractChainEntry<KeyValue<K, V>, K, V, EN>> void rngIterateForward(
		      EN           entry ,
		      int                       offset,
		final int                       bound ,
		final IndexProcedure<? super K> procedure
	)
	{
		try
		{
			while(offset < bound)
			{
				procedure.accept((entry = entry.next).key(), offset++);
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
	}

	private static <K, V, EN extends AbstractChainEntry<KeyValue<K, V>, K, V, EN>> void rngIterateReverse(
		      EN           entry ,
		      int                       offset,
		final int                       bound ,
		final IndexProcedure<? super K> procedure
	)
	{
		try
		{
			while(offset > bound)
			{
				procedure.accept((entry = entry.prev).key(), offset--);
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
	}

	@Override
	public final void keyRngIterateIndexed(final int offset, final int length, final IndexProcedure<? super K> procedure)
	{
		if(length >= 0)
		{
			rngIterateForward(this.getRangeChainEntry(offset, length), offset, offset + length, procedure);
		}
		else
		{
			rngIterateReverse(this.getRangeChainEntry(offset, length), offset, offset + length, procedure);
		}
	}

	// executing - conditional //

	@Override
	public final void keyIterate(final Predicate<? super K> predicate, final Consumer<? super K> procedure)
	{
		try
		{
			for(EN e = this.head.next; e != null; e = e.next)
			{
				if(predicate.test(e.key()))
				{
					procedure.accept(e.key());
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
	}



	///////////////////////////////////////////////////////////////////////////
	//    indexing      //
	/////////////////////

	// indexing - single //

	@Override
	public final int keyIndexOf(final K element)
	{
		int i = 0;
		for(EN e = this.head.next; e != null; e = e.next, i++)
		{
			if(e.key() == element)
			{
				return i;
			}
		}
		return -1;
	}

	@Override
	public final int keyIndexOf(final K sample, final Equalator<? super K> equalator)
	{
		int i = 0;
		for(EN e = this.head.next; e != null; e = e.next, i++)
		{
			if(equalator.equal(e.key(), sample))
			{
				return i;
			}
		}
		return -1;
	}

	@Override
	public final int keyRngIndexOf(int offset, final int length, final K element)
	{
		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return -1;
		}

		final int bound = offset + length;
		if(length > 0)
		{
			for(; offset != bound; e = e.next, offset++)
			{
				if(e.key() == element)
				{
					return offset;
				}
			}
		}
		else
		{
			for(; offset != bound; e = e.prev, offset--)
			{
				if(e.key() == element)
				{
					return offset;
				}
			}
		}
		return -1;
	}

	@Override
	public final int keyRngIndexOf(int offset, final int length, final K sample, final Equalator<? super K> equalator)
	{
		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return -1;
		}

		final int bound = offset + length;
		if(length > 0)
		{
			for(; offset != bound; e = e.next, offset++)
			{
				if(equalator.equal(e.key(), sample))
				{
					return offset;
				}
			}
		}
		else
		{
			for(; offset != bound; e = e.prev, offset--)
			{
				if(equalator.equal(e.key(), sample))
				{
					return offset;
				}
			}
		}
		return -1;
	}

	// indexing - predicate //

	@Override
	public final int keyIndexOf(final Predicate<? super K> predicate)
	{
		int i = 0;
		try
		{
			for(EN e = this.head.next; e != null; e = e.next, i++)
			{
				if(predicate.test(e.key()))
				{
					return i;
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return -1;
	}

	@Override
	public final int keyRngIndexOf(int offset, final int length, final Predicate<? super K> predicate)
	{
		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return -1;
		}

		final int bound = offset + length;
		try
		{
			if(length > 0)
			{
				for(; offset != bound; e = e.next, offset++)
				{
					if(predicate.test(e.key()))
					{
						return offset;
					}
				}
			}
			else
			{
				for(; offset != bound; e = e.prev, offset--)
				{
					if(predicate.test(e.key()))
					{
						return offset;
					}
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return -1;
	}

	// indexing - min max //

	@Override
	public final int keyMinIndex(final Comparator<? super K> comparator)
	{
		EN e;
		if((e = this.head.next) == null)
		{
			return -1;
		}

		K loopMinElement = e.key();
		int loopMinIndex = 0;
		int i = 1;
		for(e = e.next; e != null; e = e.next, i++)
		{
			final K element;
			if(comparator.compare(loopMinElement, element = e.key()) > 0)
			{
				loopMinElement = element;
				loopMinIndex = i;
			}
		}
		return loopMinIndex;
	}

	@Override
	public final int keyMaxIndex(final Comparator<? super K> comparator)
	{
		EN e;
		if((e = this.head.next) == null)
		{
			return -1;
		}

		K loopMaxElement = e.key();
		int loopMaxIndex = 0;
		int i = 1;
		for(e = e.next; e != null; e = e.next, i++)
		{
			final K element;
			if(comparator.compare(loopMaxElement, element = e.key()) < 0)
			{
				loopMaxElement = element;
				loopMaxIndex = i;
			}
		}
		return loopMaxIndex;
	}

	@Override
	public final int keyRngMinIndex(int offset, final int length, final Comparator<? super K> comparator)
	{
		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return -1;
		}

		K loopMinElement = e.key();
		int loopMinIndex = 0;
		final int bound = offset + length;
		if(length > 0)
		{
			for(K element; offset != bound; e = e.next, offset++)
			{
				if(comparator.compare(loopMinElement, element = e.key()) > 0)
				{
					loopMinElement = element;
					loopMinIndex = offset;
				}
			}
		}
		else
		{
			for(K element; offset != bound; e = e.prev, offset--)
			{
				if(comparator.compare(loopMinElement, element = e.key()) > 0)
				{
					loopMinElement = element;
					loopMinIndex = offset;
				}
			}
		}
		return loopMinIndex;
	}

	@Override
	public final int keyRngMaxIndex(int offset, final int length, final Comparator<? super K> comparator)
	{
		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return -1;
		}

		K loopMaxElement = e.key();
		int loopMaxIndex = 0;
		final int bound = offset + length;
		if(length > 0)
		{
			for(K element; offset != bound; e = e.next, offset++)
			{
				if(comparator.compare(loopMaxElement, element = e.key()) < 0)
				{
					loopMaxElement = element;
					loopMaxIndex = offset;
				}
			}
		}
		else
		{
			for(K element; offset != bound; e = e.prev, offset--)
			{
				if(comparator.compare(loopMaxElement, element = e.key()) < 0)
				{
					loopMaxElement = element;
					loopMaxIndex = offset;
				}
			}
		}
		return loopMaxIndex;
	}

	// indexing - scan //

	@Override
	public final int keyScan(final Predicate<? super K> predicate)
	{
		int i = 0;
		int foundIndex = -1;
		for(EN e = this.head.next; e != null; e = e.next, i++)
		{
			if(predicate.test(e.key()))
			{
				foundIndex = i;
			}
		}
		return foundIndex;
	}

	@Override
	public final int keyRngScan(int offset, final int length, final Predicate<? super K> predicate)
	{
		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return -1;
		}

		final int bound = offset + length;
		int foundIndex = -1;
		if(length > 0)
		{
			for(; offset != bound; e = e.next, offset++)
			{
				if(predicate.test(e.key()))
				{
					foundIndex = offset;
				}
			}
		}
		else
		{
			for(; offset != bound; e = e.prev, offset--)
			{
				if(predicate.test(e.key()))
				{
					foundIndex = offset;
				}
			}
		}
		return foundIndex;
	}



	///////////////////////////////////////////////////////////////////////////
	//   distinction    //
	/////////////////////

	@Override
	public final boolean keyHasDistinctValues()
	{
		for(EN e = this.head.next; e != null; e = e.next)
		{
			final K element = e.key();
			for(EN lookAhead = e.next; lookAhead != null; lookAhead = lookAhead.next)
			{
				if(element == lookAhead.key())
				{
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public final boolean keyHasDistinctValues(final Equalator<? super K> equalator)
	{
		for(EN e = this.head.next; e != null; e = e.next)
		{
			final K element = e.key();
			for(EN lookAhead = e.next; lookAhead != null; lookAhead = lookAhead.next)
			{
				if(equalator.equal(element, lookAhead.key()))
				{
					return false;
				}
			}
		}
		return true;
	}

	// distinction copying //

	@Override
	public final <C extends Consumer<? super K>> C keyDistinct(final C target)
	{
		return this.keyRngDistinct(Jadoth.to_int(this.parent.size()) - 1, Jadoth.to_int(this.parent.size()), target);
	}

	@Override
	public final <C extends Consumer<? super K>> C keyDistinct(final C target, final Equalator<? super K> equalator)
	{
		return this.keyRngDistinct(Jadoth.to_int(this.parent.size()) - 1, Jadoth.to_int(this.parent.size()), target, equalator);
	}

	@Override
	public final <C extends Consumer<? super K>> C keyRngDistinct(final int offset, int length, final C target)
	{
		if(length == 0)
		{
			// required for correctness of direction reversion
			return target;
		}

		EN e;
		if((e = this.getRangeChainEntry(offset + length - (length > 0 ? 1 : -1), -length)) == null)
		{
			return target;
		}

		// hopping direction (reversed for algorighm!)
		final AbstractChainEntry.Hopper ch = length < 0 ? HOP_NEXT : HOP_PREV;
		if(length < 0)
		{
			length = -length;
		}

		// find last distinct element in reverse order: means put first distinct element to target
		mainLoop:
		for(; length > 0; e = ch.hop(e), length--)
		{
			final K element = e.key();
			for(EN lookAhead = ch.hop(e); lookAhead != null; lookAhead = ch.hop(lookAhead))
			{
				if(element == lookAhead.key())
				{
					continue mainLoop;
				}
			}
			target.accept(element);
		}

		return target;
	}

	@Override
	public final <C extends Consumer<? super K>> C keyRngDistinct(
		final int                  offset   ,
		      int                  length   ,
		final C                    target   ,
		final Equalator<? super K> equalator
	)
	{
		if(length == 0)
		{
			// required for correctness of direction reversion
			return target;
		}

		EN e;
		if((e = this.getRangeChainEntry(offset + length - (length > 0 ? 1 : -1), -length)) == null)
		{
			return target;
		}

		// hopping direction (reversed for algorighm!)
		final AbstractChainEntry.Hopper ch = length < 0 ? HOP_NEXT : HOP_PREV;
		if(length < 0)
		{
			length = -length;
		}

		mainLoop: // find last distinct element in reverse order: means put first distinct element to target
		for(; length > 0; e = ch.hop(e), length--)
		{
			final K element = e.key();
			for(EN lookAhead = ch.hop(e); lookAhead != null; lookAhead = ch.hop(lookAhead))
			{
				if(equalator.equal(element, lookAhead.key()))
				{
					continue mainLoop;
				}
			}
			target.accept(element);
		}

		return target;
	}



	///////////////////////////////////////////////////////////////////////////
	//    removing      //
	/////////////////////

	// removing - indexed //

	@Override
	public final K keyRemove(final long index)
	{
		final EN e;
		(e = this.getChainEntry(index)).removeFrom(this.parent);
		return e.key();
	}

	// removing - null //

	@Override
	public final int keyRemoveNull()
	{
		final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent = this.parent;

		int removeCount = 0;
		for(EN e = this.head.next; e != null; e = e.next)
		{
			if(e.hasNullKey())
			{
				e.removeFrom(parent);
				removeCount++;
			}
		}
		return removeCount;
	}

	@Override
	public final int keyRngRemoveNull(long offset, final long length)
	{
		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return 0;
		}

		int removeCount = 0;
		final long bound = offset + length;
		final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent = this.parent;
		if(length > 0)
		{
			for(; offset != bound; e = e.next, offset++)
			{
				if(e.hasNullKey())
				{
					e.removeFrom(parent);
					removeCount++;
				}
			}
		}
		else
		{
			for(; offset != bound; e = e.prev, offset--)
			{
				if(e.hasNullKey())
				{
					e.removeFrom(parent);
					removeCount++;
				}
			}
		}
		return removeCount;
	}

	// removing - one single //

	@Override
	public final K keyRetrieve(final K element)
	{
		final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent = this.parent;
		for(EN e = this.head.next; e != null; e = e.next)
		{
			if(e.key() == element)
			{
				e.removeFrom(parent);
				return e.key();
			}
		}
		return null;
	}

	@Override
	public final K keyRetrieve(final K sample, final Equalator<? super K> equalator)
	{
		final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent = this.parent;
		for(EN e = this.head.next; e != null; e = e.next)
		{
			if(equalator.equal(e.key(), sample))
			{
				e.removeFrom(parent);
				return e.key();
			}
		}
		return null;
	}

	@Override
	public final K keyRetrieve(final Predicate<? super K> predicate)
	{
		final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent = this.parent;
		for(EN e = this.head.next; e != null; e = e.next)
		{
			if(predicate.test(e.key()))
			{
				e.removeFrom(parent);
				return e.key();
			}
		}
		return null;
	}

	@Override
	public final K keyRngRetrieve(final long offset, long length, final K element)
	{
		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return null;
		}

		final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent = this.parent;
		if(length > 0)
		{
			for(; length-- > 0; e = e.next)
			{
				if(e.key() == element)
				{
					e.removeFrom(parent);
					return e.key();
				}
			}
		}
		else
		{
			for(; length++ < 0; e = e.prev)
			{
				if(e.key() == element)
				{
					e.removeFrom(parent);
					return e.key();
				}
			}
		}
		return null;
	}

	@Override
	public final K keyRngRetrieve(final long offset, long length, final K sample, final Equalator<? super K> equalator)
	{
		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return null;
		}

		final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent = this.parent;
		if(length > 0)
		{
			for(; length-- > 0; e = e.next)
			{
				if(equalator.equal(e.key(), sample))
				{
					e.removeFrom(parent);
					return e.key();
				}
			}
		}
		else
		{
			for(; length++ < 0; e = e.prev)
			{
				if(equalator.equal(e.key(), sample))
				{
					e.removeFrom(parent);
					return e.key();
				}
			}
		}
		return null;
	}

	@Override
	public final boolean keyRemoveOne(final K element)
	{
		final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent = this.parent;
		for(EN e = this.head.next; e != null; e = e.next)
		{
			if(e.key() == element)
			{
				e.removeFrom(parent);
				return true;
			}
		}
		return false;
	}

	@Override
	public final boolean keyRemoveOne(final K sample, final Equalator<? super K> equalator)
	{
		final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent = this.parent;
		for(EN e = this.head.next; e != null; e = e.next)
		{
			if(equalator.equal(e.key(), sample))
			{
				e.removeFrom(parent);
				return true;
			}
		}
		return false;
	}

	// removing - multiple single //

	@Override
	public final int keyRemove(final K element)
	{
		final int oldSize = Jadoth.to_int(this.parent.size());
		final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent = this.parent;
		for(EN e = this.head.next; e != null; e = e.next)
		{
			if(e.key() == element)
			{
				e.removeFrom(parent);
			}
		}
		return oldSize - Jadoth.to_int(this.parent.size());
	}

	@Override
	public final int keyRemove(final K sample, final Equalator<? super K> equalator)
	{
		final int oldSize = Jadoth.to_int(this.parent.size());
		final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent = this.parent;
		for(EN e = this.head.next; e != null; e = e.next)
		{
			if(equalator.equal(e.key(), sample))
			{
				e.removeFrom(parent);
			}
		}
		return oldSize - Jadoth.to_int(this.parent.size());
	}

	@Override
	public final int keyRngRemove(int offset, final int length, final K element)
	{
		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return 0;
		}

		int removeCount = 0;
		final int bound = offset + length;
		final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent = this.parent;
		if(length > 0)
		{
			for(; offset != bound; e = e.next, offset++)
			{
				if(e.key() == element)
				{
					e.removeFrom(parent);
					removeCount++;
				}
			}
		}
		else
		{
			for(; offset != bound; e = e.prev, offset--)
			{
				if(e.key() == element)
				{
					e.removeFrom(parent);
					removeCount++;
				}
			}
		}
		return removeCount;
	}

	// removing - multiple all array //

	@Override
	public final int keyRemoveAll(final K[] elements, final int elementsOffset, final int elementsLength)
	{
		final int d;
		if((d = ChainStorageStrong.validateArrayIteration(elements, elementsOffset, elementsLength)) == 0)
		{
			return 0;
		}

		final int elementsBound = elementsOffset + elementsLength;
		final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent = this.parent;

		int removeCount = 0;
		for(EN e = this.head.next; e != null; e = e.next)
		{
			final K element = e.key();
			for(int i = elementsOffset; i != elementsBound; i += d)
			{
				if(element == elements[i])
				{
					e.removeFrom(parent);
					removeCount++;
					break;
				}
			}
		}
		return removeCount;
	}

	@Override
	public final int keyRngRemoveAll(
		      int offset,
		final int length,
		final K[] elements,
		final int elementsOffset,
		final int elementsLength
	)
	{
		final int d;
		if((d = JadothArrays.validateArrayRange(elements, elementsOffset, elementsLength)) == 0)
		{
			return 0;
		}

		final EN first;
		if((first = this.getRangeChainEntry(offset, length)) == null)
		{
			return 0;
		}

		final int elementsBound = elementsOffset + elementsLength;
		final int bound = offset + length;
		int removeCount = 0;
		final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent = this.parent;
		if(length > 0)
		{
			for(EN e = first; offset != bound; e = e.next, offset++)
			{
				for(int i = elementsOffset; i != elementsBound; i += d)
				{
					if(e.key() == elements[i])
					{
						e.removeFrom(parent);
						removeCount++;
						break;
					}
				}
			}
		}
		else
		{
			for(EN e = first; offset != bound; e = e.prev, offset++)
			{
				for(int i = elementsOffset; i != elementsBound; i += d)
				{
					if(e.key() == elements[i])
					{
						e.removeFrom(parent);
						removeCount++;
						break;
					}
				}
			}
		}
		return removeCount;
	}

	// removing - multiple all collection //

	@Override
	public final int keyRemoveAll(final XGettingCollection<? extends K> elements)
	{
		if(elements instanceof AbstractSimpleArrayCollection<?>)
		{
			return this.keyRngRemoveAll(
				0,
				Jadoth.to_int(this.parent.size()),
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)elements),
				0,
				Jadoth.to_int(elements.size())
			);
		}

		return elements.iterate(new Consumer<K>()
		{
			int removeCount;

			@Override
			public void accept(final K e)
			{
				this.removeCount += ChainStrongStrongStorage.this.keyRemove(e);
			}

		}).removeCount;
	}

	@Override
	public final int keyRngRemoveAll(final int offset, final int length, final XGettingCollection<? extends K> elements)
	{
		if(elements instanceof AbstractSimpleArrayCollection<?>)
		{
			return this.keyRngRemoveAll(offset, length,
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)elements), 0, Jadoth.to_int(elements.size())
			);
		}

		return elements.iterate(new Consumer<K>()
		{
			int removeCount;

			@Override
			public void accept(final K e)
			{
				this.removeCount += ChainStrongStrongStorage.this.keyRngRemove(offset, length, e);
			}

		}).removeCount;
	}

	// removing - duplicates //

	@Override
	public final int keyRemoveDuplicates()
	{
		final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent = this.parent;

		int removeCount = 0;
		for(EN e = this.head.next; e != null; e = e.next)
		{
			final K element = e.key();
			for(EN lookAhead = e.next; lookAhead != null; lookAhead = lookAhead.next)
			{
				if(element == lookAhead.key())
				{
					lookAhead.removeFrom(parent);
					removeCount++;
				}
			}
		}
		return removeCount;
	}

	@Override
	public final int keyRemoveDuplicates(final Equalator<? super K> equalator)
	{
		final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent = this.parent;

		int removeCount = 0;
		for(EN e = this.head.next; e != null; e = e.next)
		{
			final K element = e.key();
			for(EN lookAhead = e.next; lookAhead != null; lookAhead = lookAhead.next)
			{
				if(equalator.equal(element, lookAhead.key()))
				{
					lookAhead.removeFrom(parent);
					removeCount++;
				}
			}
		}
		return removeCount;
	}

	@Override
	public final int keyRngRemoveDuplicates(final int offset, int length)
	{
		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return 0;
		}

		final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent = this.parent;

		// hopping direction
		final AbstractChainEntry.Hopper ch = length < 0 ? HOP_PREV : HOP_NEXT;
		if(length < 0)
		{
			length = -length;
		}

		int removeCount = 0;
		for(; length > 0; e = ch.hop(e), length--)
		{
			final K element = e.key();
			for(EN lookAhead = ch.hop(e); lookAhead != null; lookAhead = ch.hop(lookAhead))
			{
				if(element == lookAhead.key())
				{
					lookAhead.removeFrom(parent);
					removeCount++;
				}
			}
		}
		return removeCount;
	}

	@Override
	public final int keyRngRemoveDuplicates(final int offset, int length, final Equalator<? super K> equalator)
	{
		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return 0;
		}

		final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent = this.parent;

		// hopping direction
		final AbstractChainEntry.Hopper ch = length < 0 ? HOP_PREV : HOP_NEXT;
		if(length < 0)
		{
			length = -length;
		}

		int removeCount = 0;
		for(; length > 0; e = ch.hop(e), length--)
		{
			final K element = e.key();
			for(EN lookAhead = ch.hop(e); lookAhead != null; lookAhead = ch.hop(lookAhead))
			{
				if(equalator.equal(element, lookAhead.key()))
				{
					lookAhead.removeFrom(parent);
					removeCount++;
				}
			}
		}
		return removeCount;
	}



	///////////////////////////////////////////////////////////////////////////
	//    reducing      //
	/////////////////////

	// reducing - predicate //

	@Override
	public final int keyReduce(final Predicate<? super K> predicate)
	{
		int removeCount = 0;
		final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent = this.parent;
		for(EN e = this.head.next; e != null; e = e.next)
		{
			if(predicate.test(e.key()))
			{
				e.removeFrom(parent);
				removeCount++;
			}
		}
		return removeCount;
	}

	@Override
	public final int keyRngReduce(int offset, final int length, final Predicate<? super K> predicate)
	{
		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return 0;
		}

		int removeCount = 0;
		final int bound = offset + length;
		final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent = this.parent;
		try
		{
			if(length > 0)
			{
				for(; offset != bound; e = e.next, offset++)
				{
					if(predicate.test(e.key()))
					{
						e.removeFrom(parent);
						removeCount++;
					}
				}
			}
			else
			{
				for(; offset != bound; e = e.prev, offset--)
				{
					if(predicate.test(e.key()))
					{
						e.removeFrom(parent);
						removeCount++;
					}
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return removeCount;
	}



	///////////////////////////////////////////////////////////////////////////
	//    retaining     //
	/////////////////////

	// retaining - array //

	@Override
	public final int keyRetainAll(final K[] elements, final int elementsOffset, final int elementsLength)
	{
		final int d;
		if((d = ChainStorageStrong.validateArrayIteration(elements, elementsOffset, elementsLength)) == 0)
		{
			return 0;
		}

		final int elementsBound = elementsOffset + elementsLength;
		final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent = this.parent;

		int removeCount = 0;
		main:
		for(EN e = this.head.next; e != null; e = e.next)
		{
			final K element = e.key();
			for(int i = elementsOffset; i != elementsBound; i += d)
			{
				if(element == elements[i])
				{
					continue main;
				}
			}
			e.removeFrom(parent);
			removeCount++;
		}
		return removeCount;
	}

	public final int keyRetainAll(
		final K[] samples,
		final int samplesOffset,
		final int samplesLength,
		final Equalator<? super K> equalator
	)
	{
		final int d;
		if((d = ChainStorageStrong.validateArrayIteration(samples, samplesOffset, samplesLength)) == 0)
		{
			return 0;
		}

		final int samplesBound = samplesOffset + samplesLength;
		final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent = this.parent;

		int removeCount = 0;
		main:
		for(EN e = this.head.next; e != null; e = e.next)
		{
			final K element = e.key();
			for(int i = samplesOffset; i != samplesBound; i += d)
			{
				if(equalator.equal(element, samples[i]))
				{
					continue main;
				}
			}
			e.removeFrom(parent);
			removeCount++;
		}
		return removeCount;
	}

	@Override
	public final int keyRngRetainAll(
		      int offset,
		final int length,
		final K[] elements,
		final int elementsOffset,
		final int elementsLength
	)
	{
		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return 0;
		}

		final int d;
		if((d = ChainStorageStrong.validateArrayIteration(elements, elementsOffset, elementsLength)) == 0)
		{
			return 0;
		}

		final int bound = offset + length;
		final int elementsBound = elementsOffset + elementsLength;
		final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent = this.parent;

		int removeCount = 0;
		if(length > 0)
		{
			main:
			for(; offset != bound; e = e.next, offset++)
			{
				final K element = e.key();
				for(int i = elementsOffset; i != elementsBound; i += d)
				{
					if(element == elements[i])
					{
						continue main;
					}
				}
				e.removeFrom(parent);
				removeCount++;
			}
		}
		else
		{
			main:
			for(; offset != bound; e = e.prev, offset--)
			{
				final K element = e.key();
				for(int i = elementsOffset; i != elementsBound; i += d)
				{
					if(element == elements[i])
					{
						continue main;
					}
				}
				e.removeFrom(parent);
				removeCount++;
			}
		}
		return removeCount;
	}

	// retaining - collection //

	@Override
	public final int keyRetainAll(final XGettingCollection<? extends K> elements)
	{
		if(elements instanceof AbstractSimpleArrayCollection<?>)
		{
			// directly check array against array without predicate function or method calls
			return this.keyRetainAll(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)elements),
				0,
				Jadoth.to_int(elements.size())
			);
		}

		final ElementIsContained<K> currentElement = new ElementIsContained<>();
		final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent = this.parent;
		int removeCount = 0;
		for(EN e = this.head.next; e != null; e = e.next)
		{
			currentElement.element = e.key();
			if(!elements.containsSearched(currentElement))
			{
				e.removeFrom(parent);
				removeCount++;
			}
		}
		return removeCount;
	}

	@Override
	public final int keyRetainAll(final XGettingCollection<? extends K> samples, final Equalator<? super K> equalator)
	{
		if(samples instanceof AbstractSimpleArrayCollection<?>)
		{
			// directly check array against array without predicate function or method calls
			return this.keyRetainAll(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)samples),
				0,
				Jadoth.to_int(samples.size()),
				equalator
			);
		}

		final CachedSampleEquality<K> equalCurrentElement = new CachedSampleEquality<>(equalator);
		final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent = this.parent;
		int removeCount = 0;
		for(EN e = this.head.next; e != null; e = e.next)
		{
			equalCurrentElement.sample = e.key();
			if(!samples.containsSearched(equalCurrentElement))
			{
				e.removeFrom(parent);
				removeCount++;
			}
		}
		return removeCount;
	}

	@Override
	public final int keyRngRetainAll(int offset, final int length, final XGettingCollection<? extends K> elements)
	{
		if(elements instanceof AbstractSimpleArrayCollection<?>)
		{
			// directly check array against array without predicate function or method calls
			return this.keyRngRetainAll(offset, length,
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)elements), 0, Jadoth.to_int(elements.size())
			);
		}

		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return 0;
		}

		int removeCount = 0;
		final int bound = offset + length;
		final ElementIsContained<K> currentElement = new ElementIsContained<>();
		final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent = this.parent;
		if(length > 0)
		{
			for(; offset != bound; e = e.next, offset++)
			{
				currentElement.element = e.key();
				if(!elements.containsSearched(currentElement))
				{
					e.removeFrom(parent);
					removeCount++;
				}
			}
		}
		else
		{
			for(; offset != bound; e = e.prev, offset--)
			{
				currentElement.element = e.key();
				if(!elements.containsSearched(currentElement))
				{
					e.removeFrom(parent);
					removeCount++;
				}
			}
		}
		return removeCount;
	}



	///////////////////////////////////////////////////////////////////////////
	//    processing    //
	/////////////////////

	@Override
	public final int keyProcess(final Consumer<? super K> procedure)
	{
		final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent = this.parent;
		int removeCount = 0;
		try
		{
			for(EN e = this.head.next; e != null; e = e.next)
			{
				procedure.accept(e.key());
				e.removeFrom(parent);
				removeCount++;
			}
		}
		catch(final ThrowBreak b)
		{
			removeCount += parent.internalClear();
		}
		return removeCount;
	}

	@Override
	public final int keyRngProcess(int offset, final int length, final Consumer<? super K> procedure)
	{
		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return 0;
		}

		int removeCount = 0;
		final int bound = offset + length;
		final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent = this.parent;
		try
		{
			if(length > 0)
			{
				for(; offset != bound; e = e.next, offset++)
				{
					procedure.accept(e.key());
					e.removeFrom(parent);
					removeCount++;
				}
			}
			else
			{
				for(; offset != bound; e = e.prev, offset--)
				{
					procedure.accept(e.key());
					e.removeFrom(parent);
					removeCount++;
				}
			}
		}
		catch(final ThrowBreak b)
		{
			removeCount += parent.internalClear();
		}
		return removeCount;
	}




	///////////////////////////////////////////////////////////////////////////
	//     moving       //
	/////////////////////

	@Override
	public final int keyMoveRange(int offset, final int length, final Consumer<? super K> target)
	{
		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return 0;
		}

		final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent = this.parent;
		final int bound = offset + length;

		if(length > 0)
		{
			for(; offset != bound; e = e.next, offset++)
			{
				target.accept(e.key());
				e.removeFrom(parent);
			}
		}
		else
		{
			for(; offset != bound; e = e.prev, offset--)
			{
				target.accept(e.key());
				e.removeFrom(parent);
			}
		}
		return length < 0 ? -length : length;
	}

	@Override
	public final int keyMoveSelection(final Consumer<? super K> target, final long... indices)
	{
		final int indicesLength = indices.length, size = Jadoth.to_int(this.parent.size());

		// validate all indices before copying the first element
		for(int i = 0; i < indicesLength; i++)
		{
			if(indices[i] < 0 || indices[i] >= size)
			{
				throw new IndexOutOfBoundsException(exceptionIndexOutOfBounds(size, indices[i]));
			}
		}

		final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent = this.parent;

		// actual copying. Note: can't sort indices as copying order might be relevant
		EN e;
		for(int i = 0; i < indicesLength; i++)
		{
			target.accept((e = this.getChainEntry(indices[i])).key()); // pretty inefficient scrolling here
			e.removeFrom(parent); // remove not until adding to target has been successful
		}

		return indicesLength; // removeCount is equal to index count if no exception occured
	}

	// moving - conditional //

	@Override
	public final int keyMoveTo(final Consumer<? super K> target, final Predicate<? super K> predicate)
	{
		int removeCount = 0;
		final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent = this.parent;
		for(EN e = this.head.next; e != null; e = e.next)
		{
			final K element;
			if(predicate.test(element = e.key()))
			{
				target.accept(element);
				e.removeFrom(parent);
				removeCount++;
			}
		}
		return removeCount;
	}

	@Override
	public final int keyRngMoveTo(
		      int offset,
		final int length,
		final Consumer<? super K> target,
		final Predicate<? super K> predicate
	)
	{
		final int bound = offset + length;
		int removeCount = 0;
		final EN first;
		if((first = this.getRangeChainEntry(offset, length)) == null)
		{
			return 0;
		}

		final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent = this.parent;

		if(length > 0)
		{
			for(EN e = first; offset != bound; e = e.next, offset++)
			{
				if(predicate.test(e.key()))
				{
					target.accept(e.key());
					e.removeFrom(parent);
					removeCount++;
				}
			}
		}
		else
		{
			for(EN e = first; offset != bound; e = e.prev, offset--)
			{
				if(predicate.test(e.key()))
				{
					target.accept(e.key());
					e.removeFrom(parent);
					removeCount++;
				}
			}
		}
		return removeCount;
	}



	///////////////////////////////////////////////////////////////////////////
	//     sorting       //
	//////////////////////

	@Override
	public final void keySort(final Comparator<? super K> comparator)
	{
		// validate comparator before the chain gets splitted
		if(comparator == null)
		{
			throw new NullPointerException();
		}
		if(Jadoth.to_int(this.parent.size()) <= 1)
		{
			return; // empty or trivial chain is always sorted
		}
		keyMergesortHead(this.head, comparator);
	}

	@Override
	public final void keyRngSort(final int offset, int length, final Comparator<? super K> comparator)
	{
		// validate comparator before the chain gets splitted
		if(comparator == null)
		{
			throw new NullPointerException();
		}

		final EN preFirst;
		if(length <= 1 && length >= -1)
		{
			return; // empty or trivial subchain is always sorted
		}
		else if(length > 0)
		{
			preFirst = this.getRangeChainEntry(offset -      1,  length + 1);
		}
		else
		{
			preFirst = this.getRangeChainEntry(offset - length, -length + 1);
			length = -length;
		}

		EN postLast = preFirst.next;
		while(length-- > 0)
		{
			postLast = postLast.next;
		}

		keyMergesortRange(preFirst, postLast, this.head, comparator);
	}

	@Override
	public final boolean keyIsSorted(final Comparator<? super K> comparator)
	{
		EN e;
		if((e = this.head.next) == null)
		{
			return true; // empty chain is sorted
		}

		K loopLastElement = e.key();
		while((e = e.next) != null)
		{
			final K element;
			if(comparator.compare(loopLastElement, element = e.key()) > 0)
			{
				return false;
			}
			loopLastElement = element;
		}
		return true;
	}

	@Override
	public final boolean keyRngIsSorted(final int offset, int length, final Comparator<? super K> comparator)
	{
		EN e = this.getRangeChainEntry(offset, length);
		K loopLastElement = e.key();
		if(length > 0)
		{
			for(; length-- > 0; e = e.next)
			{
				final K element;
				if(comparator.compare(loopLastElement, element = e.key()) > 0)
				{
					return false;
				}
				loopLastElement = element;
			}
		}
		else
		{
			for(; length++ < 0; e = e.prev)
			{
				final K element;
				if(comparator.compare(loopLastElement, element = e.key()) > 0)
				{
					return false;
				}
				loopLastElement = element;
			}
		}
		return true;
	}



	///////////////////////////////////////////////////////////////////////////
	//     setting      //
	/////////////////////

	@SafeVarargs
	@Override
	public final void keySet(final int offset, final K... elements)
	{
		EN e = this.getRangeChainEntry(offset, elements.length);
		for(int i = 0; i < elements.length; i++)
		{
			e.setKey(elements[i]);
			e = e.next;
		}
	}

	@Override
	public final void keySet(final int offset, final K[] elements, final int elementsOffset, final int elementsLength)
	{
		EN e = this.getRangeChainEntry(offset, elementsLength);
		final int d = JadothArrays.validateArrayRange(elements, elementsOffset, elementsLength);
		for(int i = elementsOffset, bound = elementsOffset + elementsLength; i != bound; i += d)
		{
			e.setKey(elements[i]);
			e = e.next;
		}
	}

	@Override
	public final void keyFill(int offset, final int length, final K element)
	{
		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return;
		}
		final int bound = offset + length;
		if(length > 0)
		{
			for(; offset != bound; e = e.next, offset++)
			{
				e.setKey(element);
			}
		}
		else
		{
			for(; offset != bound; e = e.prev, offset--)
			{
				e.setKey(element);
			}
		}
	}



	///////////////////////////////////////////////////////////////////////////
	//    replacing     //
	/////////////////////

	// replacing - one single //

	@Override
	public final int keyReplaceOne(final K element, final K replacement)
	{
		int i = 0;
		for(EN e = this.head.next; e != null; e = e.next)
		{
			if(e.key() == element)
			{
				e.setKey(replacement);
				return i;
			}
			i++;
		}
		return -1;
	}

	@Override
	public final int keyRngReplaceOne(int offset, final int length, final K element, final K replacement)
	{
		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return -1;
		}

		final int bound = offset + length;
		if(length > 0)
		{
			for(; offset != bound; e = e.next, offset++)
			{
				if(e.key() == element)
				{
					e.setKey(replacement);
					return offset;
				}
			}
		}
		else
		{
			for(; offset != bound; e = e.prev, offset--)
			{
				if(e.key() == element)
				{
					e.setKey(replacement);
					return offset;
				}
			}
		}
		return -1;
	}

	// replacing - multiple single //

	@Override
	public final int keyReplace(final K element, final K replacement)
	{
		int replaceCount = 0;
		for(EN e = this.head.next; e != null; e = e.next)
		{
			if(e.key() == element)
			{
				e.setKey(replacement);
				replaceCount++;
			}
		}
		return replaceCount;
	}

	@Override
	public final int keyRngReplace(final int offset, int length, final K element, final K replacement)
	{
		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return 0;
		}

		int replaceCount = 0;
		if(length > 0)
		{
			for(; length-- > 0; e = e.next)
			{
				if(e.key() == element)
				{
					e.setKey(replacement);
					replaceCount++;
				}
			}
		}
		else
		{
			for(; length++ < 0; e = e.prev)
			{
				if(e.key() == element)
				{
					e.setKey(replacement);
					replaceCount++;
				}
			}
		}
		return replaceCount;
	}

	@Override
	public final int keyRngReplace(
		final int offset,
		      int length,
		final K sample,
		final Equalator<? super K> equalator,
		final K replacement
	)
	{
		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return 0;
		}

		int replaceCount = 0;
		if(length > 0)
		{
			for(; length-- > 0; e = e.next)
			{
				if(equalator.equal(e.key(), sample))
				{
					e.setKey(replacement);
					replaceCount++;
				}
			}
		}
		else
		{
			for(; length++ < 0; e = e.prev)
			{
				if(equalator.equal(e.key(), sample))
				{
					e.setKey(replacement);
					replaceCount++;
				}
			}
		}
		return replaceCount;
	}

	// replacing - multiple all array //

	@Override
	public final int keyReplaceAll(final K[] elements, final int elementsOffset, final int elementsLength, final K replacement)
	{
		final int d;
		if((d = ChainStorageStrong.validateArrayIteration(elements, elementsOffset, elementsLength)) == 0)
		{
			return 0;
		}

		final int elementsBound = elementsOffset + elementsLength;

		int replaceCount = 0;
		for(EN e = this.head.next; e != null; e = e.next)
		{
			final K element = e.key();
			for(int i = elementsOffset; i != elementsBound; i += d)
			{
				if(element == elements[i])
				{
					e.setKey(replacement);
					replaceCount++;
					break;
				}
			}
		}
		return replaceCount;
	}

	@Override
	public final int keyRngReplaceAll(
		      int offset,
		final int length,
		final K[] elements,
		final int elementsOffset,
		final int elementsLength,
		final K replacement
	)
	{
		final int d;
		if((d = JadothArrays.validateArrayRange(elements, elementsOffset, elementsLength)) == 0)
		{
			return 0;
		}

		final EN first;
		if((first = this.getRangeChainEntry(offset, length)) == null)
		{
			return 0;
		}

		final int elementsBound = elementsOffset + elementsLength;
		final int bound = offset + length;
		int replaceCount = 0;
		if(length > 0)
		{
			for(EN e = first; offset != bound; e = e.next, offset++)
			{
				for(int i = elementsOffset; i != elementsBound; i += d)
				{
					if(e.key() == elements[i])
					{
						e.setKey(replacement);
						replaceCount++;
						break;
					}
				}
			}
		}
		else
		{
			for(EN e = first; offset != bound; e = e.prev, offset++)
			{
				for(int i = elementsOffset; i != elementsBound; i += d)
				{
					if(e.key() == elements[i])
					{
						e.setKey(replacement);
						replaceCount++;
						break;
					}
				}
			}
		}
		return replaceCount;
	}

	// replacing - multiple all collection //

	@Override
	public final int keyReplaceAll(final XGettingCollection<? extends K> elements, final K replacement)
	{
		if(elements instanceof AbstractSimpleArrayCollection<?>)
		{
			return this.keyRngReplaceAll(
				0,
				Jadoth.to_int(this.parent.size()),
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)elements),
				0,
				Jadoth.to_int(elements.size()),
				replacement
			);
		}

		return elements.iterate(new Consumer<K>()
		{
			int replaceCount;

			@Override
			public void accept(final K e)
			{
				this.replaceCount += ChainStrongStrongStorage.this.keyReplace(e, replacement);
			}

		}).replaceCount;
	}

	@Override
	public final int keyRngReplaceAll(
		final int offset,
		final int length,
		final XGettingCollection<? extends K> elements,
		final K replacement
	)
	{
		if(elements instanceof AbstractSimpleArrayCollection<?>)
		{
			return this.keyRngReplaceAll(
				offset,
				length,
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)elements),
				0,
				Jadoth.to_int(elements.size()),
				replacement
			);
		}

		return elements.iterate(new Consumer<K>()
		{
			int replaceCount;

			@Override
			public void accept(final K e)
			{
				this.replaceCount += ChainStrongStrongStorage.this.keyRngReplace(offset, length, e, replacement);
			}

		}).replaceCount;
	}



	///////////////////////////////////////////////////////////////////////////
	//  substituting    //
	/////////////////////

	@Override
	public final int keySubstituteOne(final Predicate<? super K> predicate, final K substitute)
	{
		try
		{
			int i = 0;
			for(EN e = this.head.next; e != null; e = e.next)
			{
				if(predicate.test(e.key()))
				{
					e.setKey(substitute);
					return i;
				}
				i++;
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return -1;
	}


	@Override
	public final int keyRngSubstituteOne(
		      int offset,
		final int length,
		final Predicate<? super K> predicate,
		final K substitute
	)
	{
		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return -1;
		}

		try
		{
			final int bound = offset + length;
			if(length > 0)
			{
				for(; offset != bound; e = e.next, offset++)
				{
					if(predicate.test(e.key()))
					{
						e.setKey(substitute);
						return offset;
					}
				}
			}
			else
			{
				for(; offset != bound; e = e.prev, offset--)
				{
					if(predicate.test(e.key()))
					{
						e.setKey(substitute);
						return offset;
					}
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return -1;
	}

	// substituting - multiple //

	@Override
	public final int keySubstitute(final Predicate<? super K> predicate, final K substitute)
	{
		int replaceCount = 0;
		for(EN e = this.head.next; e != null; e = e.next)
		{
			if(predicate.test(e.key()))
			{
				e.setKey(substitute);
				replaceCount++;
			}
		}
		return replaceCount;
	}

	@Override
	public final int keyRngSubstitute(final int offset, int length, final Predicate<? super K> predicate, final K substitute)
	{
		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return 0;
		}

		int replaceCount = 0;
		if(length > 0)
		{
			for(; length-- > 0; e = e.next)
			{
				if(predicate.test(e.key()))
				{
					e.setKey(substitute);
					replaceCount++;
				}
			}
		}
		else
		{
			for(; length++ < 0; e = e.prev)
			{
				if(predicate.test(e.key()))
				{
					e.setKey(substitute);
					replaceCount++;
				}
			}
		}
		return replaceCount;
	}

	// replacing - mapped //

	@Override
	public final long keySubstitute(final Function<? super K, ? extends K> mapper, final BiConsumer<EN, K> callback)
	{
		long count = 0;
		try
		{
			for(EN entry = this.head; (entry = entry.next) != null;)
			{
				final K newElement = mapper.apply(entry.key());
				if(newElement != entry.key())
				{
					callback.accept(entry, newElement);
					count++;
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		
		return count;
	}

	@Override
	public final int keySubstitute(final Predicate<? super K> predicate, final Function<? super K, ? extends K> mapper)
	{
		int replaceCount = 0;
		try
		{
			for(EN e = this.head.next; e != null; e = e.next)
			{
				if(predicate.test(e.key()))
				{
					e.setKey(mapper.apply(e.key()));
					replaceCount++;
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return replaceCount;
	}

	@Override
	public final int keyRngSubstitute(final int offset, int length, final Function<? super K, ? extends K> mapper)
	{
		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return 0;
		}

		int replaceCount = 0;
		try
		{
			K replacement;
			if(length > 0)
			{
				for(; length-- > 0; e = e.next)
				{
					if((replacement = mapper.apply(e.key())) != e.key())
					{
						e.setKey(replacement);
						replaceCount++;
					}
				}
			}
			else
			{
				for(; length++ < 0; e = e.prev)
				{
					if((replacement = mapper.apply(e.key())) != e.key())
					{
						e.setKey(replacement);
						replaceCount++;
					}
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return replaceCount;
	}

	@Override
	public final int keyRngSubstitute(final int offset, int length, final Predicate<? super K> predicate, final Function<K, K> mapper)
	{
		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return 0;
		}

		int replaceCount = 0;
		if(length > 0)
		{
			for(; length-- > 0; e = e.next)
			{
				if(predicate.test(e.key()))
				{
					e.setKey(mapper.apply(e.key()));
					replaceCount++;
				}
			}
		}
		else
		{
			for(; length++ < 0; e = e.prev)
			{
				if(predicate.test(e.key()))
				{
					e.setKey(mapper.apply(e.key()));
					replaceCount++;
				}
			}
		}
		return replaceCount;
	}

	@Override
	public final int keyCopyToArray(final int offset, int length, final Object[] target, final int targetOffset)
	{
		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return 0;
		}

		if(targetOffset < 0)
		{
			throw new ArrayIndexOutOfBoundsException(targetOffset);
		}
		if((length < 0 ? -length : length) + targetOffset > target.length)
		{
			throw new ArrayIndexOutOfBoundsException((length < 0 ? -length : length) + targetOffset);
		}

		int t = targetOffset;
		if(length > 0)
		{
			for(; length-- > 0; e = e.next)
			{
				target[t++] = e.key();
			}
		}
		else
		{
			for(; length++ < 0; e = e.prev)
			{
				target[t++] = e.key();
			}
		}
		return t - targetOffset;
	}

	@Override
	public final VarString keyAppendTo(final VarString vc)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ChainKeyValueStorage<K,V,EN>#keyAppendTo()
	}

	@Override
	public final VarString keyAppendTo(final VarString vc, final char separator)
	{
		if(this.head.next == null)
		{
			return vc;
		}
		for(EN entry = this.head; (entry = entry.next) != null;)
		{
			vc.add(entry.key()).add(separator);
		}
		return vc.deleteLast();
	}

	@Override
	public final VarString keyAppendTo(final VarString vc, final String separator)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ChainKeyValueStorage<K,V,EN>#keyAppendTo()
	}

	@Override
	public final VarString keyAppendTo(final VarString vc, final BiProcedure<VarString, ? super K> keyAppender)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ChainKeyValueStorage<K,V,EN>#keyAppendTo()
	}

	@Override
	public final VarString keyAppendTo(final VarString vc, final BiProcedure<VarString, ? super K> keyAppender, final char separator)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ChainKeyValueStorage<K,V,EN>#keyAppendTo()
	}

	@Override
	public final VarString keyAppendTo(final VarString vc, final BiProcedure<VarString, ? super K> keyAppender, final String separator)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ChainKeyValueStorage<K,V,EN>#keyAppendTo()
	}

	@Override
	public final VarString keyRngAppendTo(final int offset, final int length, final VarString vc)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ChainKeyValueStorage<K,V,EN>#keyRngAppendTo()
	}

	@Override
	public final VarString keyRngAppendTo(final int offset, final int length, final VarString vc, final char separator)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ChainKeyValueStorage<K,V,EN>#keyRngAppendTo()
	}

	@Override
	public final VarString keyRngAppendTo(final int offset, final int length, final VarString vc, final String separator)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ChainKeyValueStorage<K,V,EN>#keyRngAppendTo()
	}

	@Override
	public final VarString keyRngAppendTo(final int offset, final int length, final VarString vc, final BiProcedure<VarString, ? super K> keyAppender)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ChainKeyValueStorage<K,V,EN>#keyRngAppendTo()
	}

	@Override
	public final VarString keyRngAppendTo(final int offset, final int length, final VarString vc, final BiProcedure<VarString, ? super K> keyAppender, final char separator)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ChainKeyValueStorage<K,V,EN>#keyRngAppendTo()
	}

	@Override
	public final VarString keyRngAppendTo(final int offset, final int length, final VarString vc, final BiProcedure<VarString, ? super K> keyAppender, final String separator)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ChainKeyValueStorage<K,V,EN>#keyRngAppendTo()
	}


	///////////////////////////////////////////////////////////////////////////
	//  inner classes   //
	/////////////////////

	final class KeyItr implements Iterator<K>
	{
		private EN current = ChainStrongStrongStorage.this.head;

		@Override
		public final boolean hasNext()
		{
			return this.current.next != null;
		}

		@Override
		public final K next()
		{
			if(this.current.next == null)
			{
				throw new NoSuchElementException();
			}
			return (this.current = this.current.next).key();
		}

		@Override
		public final void remove()
		{
			/* (02.12.2011 TM)NOTE:
			 * Dropped support for removal stuff because it would prevent using the iterator in read-only delegates.
			 * As it is an optional operation, no proper code can rely on it anyway and tbh: just use proper
			 * internal iteration means in the first place.
			 */
			throw new UnsupportedOperationException();
		}

	}

	final class ValueItr implements Iterator<V>
	{
		private EN current = ChainStrongStrongStorage.this.head;

		@Override
		public final boolean hasNext()
		{
			return this.current.next != null;
		}

		@Override
		public final V next()
		{
			if(this.current.next == null)
			{
				throw new NoSuchElementException();
			}
			return (this.current = this.current.next).value();
		}

		@Override
		public final void remove()
		{
			/* (02.12.2011 TM)NOTE:
			 * Dropped support for removal stuff because it would prevent using the iterator in read-only delegates.
			 * As it is an optional operation, no proper code can rely on it anyway and tbh: just use proper
			 * internal iteration means in the first place.
			 */
			throw new UnsupportedOperationException();
		}

	}





	///////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////
	//////////////////////////// values methods  //////////////////////////////
	///////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////



	///////////////////////////////////////////////////////////////////////////
	// sorting internals //
	//////////////////////

	private static <K, V, EN extends AbstractChainEntry<KeyValue<K, V>, K, V, EN>> void valuesMergesortHead(final EN head, final Comparator<? super V> comparator)
	{
		EN last, entry;
		try
		{
			entry = valuesMergesort0(head.next, comparator); // sort
		}
		catch(final Throwable e)
		{
			// rollback 8-)
			for(entry = head.prev; (entry = (last = entry).prev) != head;)
			{
				entry.next = last;
			}
			throw e;
		}

		// reattach sorted chain to head and rebuild prev direction
		(head.next = entry).prev = head;              // entry is new start entry
		while((entry = (last = entry).next) != null)
		{
			entry.prev = last;                        // rebuild prev references
		}
		head.prev = last;                             // last entry now is new end entry (obviously)
	}

	private static <K, V, EN extends AbstractChainEntry<KeyValue<K, V>, K, V, EN>> void valuesMergesortRange(
		final EN preFirst,
		final EN postLast,
		final EN head,
		final Comparator<? super V> comparator
	)
	{
		EN last, entry;
		try
		{
			if(postLast == null)
			{
				entry = valuesMergesort0(preFirst.next, comparator); // sort normally
			}
			else
			{
				entry = valuesRngMergesort0(preFirst.next, postLast, comparator); // sort
			}
		}
		catch(final Throwable e)
		{
			// rollback 8-)
			for(entry = (postLast == null ? head : postLast).prev; (entry = (last = entry).prev) != preFirst;)
			{
				entry.next = last;
			}
			throw e;
		}

		// reattach sorted chain to head and rebuild prev direction
		(preFirst.next = entry).prev = preFirst;      // entry is new start entry
		while((entry = (last = entry).next) != null)
		{
			entry.prev = last;                        // rebuild prev references
		}
		if(postLast != null)
		{
			postLast.prev = last;                     // entry now is new end entry
		}
		else
		{
			head.prev = last;
		}
	}

	private static <K, V, EN extends AbstractChainEntry<KeyValue<K, V>, K, V, EN>> EN valuesMergesort0(final EN chain, final Comparator<? super V> comparator)
	{
		// special case handling for empty or trivial chain
		if(chain == null || chain.next == null)
		{
			return chain;
		}

		// inlined iterative splitting
		EN chain2, t1, t2 = chain2 = (t1 = chain).next;
		while(t2 != null && (t1 = t1.next = t2.next) != null)
		{
			t2 = t2.next = t1.next;
		}

		// merging
		return valuesMerge1(valuesMergesort0(chain, comparator), valuesMergesort0(chain2, comparator), comparator);
	}

	private static <K, V, EN extends AbstractChainEntry<KeyValue<K, V>, K, V, EN>> EN valuesRngMergesort0(
		final EN chain,
		final EN end,
		final Comparator<? super V> cmp
	)
	{
		// special case handling for empty or trivial chain
		if(chain == null || chain.next == null)
		{
			return chain;
		}

		// inlined iterative splitting
		EN chain2, t1, t2 = chain2 = (t1 = chain).next;
		while(t2 != end && (t1 = t1.next = t2.next) != end)
		{
			t2 = t2.next = t1.next;
		}

		// merging
		return valuesRngMerge1(valuesRngMergesort0(chain, end, cmp), valuesRngMergesort0(chain2, end, cmp), end, cmp);
	}

	// valuesMerge iterative
	private static <K, V, EN extends AbstractChainEntry<KeyValue<K, V>, K, V, EN>> EN valuesMerge1(
		      EN                    c1 ,
		      EN                    c2 ,
		final Comparator<? super V> cmp
	)
	{
		if(c1 == null)
		{
			return c2;
		}

		if(c2 == null)
		{
			return c1;
		}

		final EN c;
		if(cmp.compare(c1.value(), c2.value()) < 0)
		{
			c1 = (c = c1).next;
		}
		else
		{
			c2 = (c = c2).next;
		}

		for(EN t = c;;)
		{
			if(c1 == null)
			{
				t.next = c2;
				break;
			}
			else if(c2 == null)
			{
				t.next = c1;
				break;
			}
			else if(cmp.compare(c1.value(), c2.value()) < 0)
			{
				c1 = (t = t.next = c1).next;
			}
			else
			{
				c2 = (t = t.next = c2).next;
			}
		}
		return c;
	}

	// merge iterative
	private static <K, V, EN extends AbstractChainEntry<KeyValue<K, V>, K, V, EN>> EN valuesRngMerge1(
		      EN                    c1 ,
		      EN                    c2 ,
		final EN                    end,
		final Comparator<? super V> cmp
	)
	{
		if(c1 == end)
		{
			return c2;
		}

		if(c2 == end)
		{
			return c1;
		}

		final EN c;
		if(cmp.compare(c1.value(), c2.value()) < 0)
		{
			c1 = (c = c1).next;
		}
		else
		{
			c2 = (c = c2).next;
		}

		for(EN t = c;;)
		{
			if(c1 == end)
			{
				t.next = c2;
				break;
			}
			else if(c2 == end)
			{
				t.next = c1;
				break;
			}
			else if(cmp.compare(c1.value(), c2.value()) < 0)
			{
				c1 = (t = t.next = c1).next;
			}
			else
			{
				c2 = (t = t.next = c2).next;
			}
		}
		return c;
	}



	@Override
	public final Iterator<V> valuesIterator()
	{
		return new ValueItr();
	}

	@Override
	public final ListIterator<V> valuesListIterator(final long index)
	{
		return new ValueListItr(checkArrayRange(index));
	}

	@Override
	public final boolean valuesEqualsContent(final XGettingCollection<? extends V> other, final Equalator<? super V> equalator)
	{
		if(Jadoth.to_int(this.parent.size()) != Jadoth.to_int(other.size()))
		{
			return false;
		}

		if(other instanceof AbstractSimpleArrayCollection<?>)
		{
			final int otherSize = Jadoth.to_int(other.size());
			final V[] otherData = AbstractSimpleArrayCollection.internalGetStorageArray(
				(AbstractSimpleArrayCollection<?>)other
			);
			EN entry = this.head.next;
			for(int i = 0; i < otherSize; i++, entry = entry.next)
			{
				if(!equalator.equal(entry.value(), otherData[i]))
				{
					return false;
				}
			}
			return true;
		}

		final Aggregator<V, Boolean> agg = new Aggregator<V, Boolean>()
		{
			private EN entry = ChainStrongStrongStorage.this.head;
			private boolean notEqual; // false by default

			@Override
			public final void accept(final V element)
			{
				if((this.entry = this.entry.next) == null)
				{
					this.notEqual = true;
					throw BREAK; // chain is too short
				}
				if(!equalator.equal(element, this.entry.value()))
				{
					this.notEqual = true;
					throw BREAK; // unequal element found
				}
			}

			@Override
			public final Boolean yield()
			{
				/*
				 * no explicitely unequal pair may have been found (obviously)
				 * current entry may not be null (otherwise chain was too short)
				 * but next entry in chain must be null (otherwise chain is too long)
				 */
				return this.notEqual || this.entry == null || (this.entry = this.entry.next) != null
					? FALSE
					: TRUE
				;
			}
		};

		other.iterate(agg);
		return agg.yield();
	}


	final class ValueListItr implements ListIterator<V>
	{
		private int index;
		private EN current;



		ValueListItr(final int index)
		{
			super();
			this.current = ChainStrongStrongStorage.this.getChainEntry(index);
			this.index = index;
		}

		@Override
		public final void add(final V e)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public final boolean hasNext()
		{
			return this.current.next != null;
		}

		@Override
		public final boolean hasPrevious()
		{
			return this.current.prev != ChainStrongStrongStorage.this.head;
		}

		@Override
		public final V next()
		{
			final KeyValue<K, V> element = this.current;
			this.current = this.current.next;
			this.index++;
			return element.value();
		}

		@Override
		public final int nextIndex()
		{
			return this.index + 1;
		}

		@Override
		public final V previous()
		{
			final KeyValue<K, V> element = this.current;
			this.current = this.current.prev;
			this.index--;
			return element.value();
		}

		@Override
		public final int previousIndex()
		{
			return this.index - 1;
		}

		@Override
		public final void remove()
		{
			final EN next = this.current.next;
			this.current.removeFrom(ChainStrongStrongStorage.this.parent);
			this.current = next;
		}

		@Override
		public final void set(final V value)
		{
			this.current.setValue(value);
		}

	}


	///////////////////////////////////////////////////////////////////////////
	//  content info    //
	/////////////////////

	@Override
	public final boolean hasVolatileValues()
	{
		return false;
	}

	@Override
	public final ReferenceType getValueReferenceType()
	{
		return ReferenceType.STRONG;
	}



	///////////////////////////////////////////////////////////////////////////
	//   containing     //
	/////////////////////

	// containing - null //

	@Override
	public final boolean valuesContainsNull()
	{
		for(EN e = this.head.next; e != null; e = e.next)
		{
			if(e.hasNullKey())
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public final boolean valuesRngContainsNull(final int offset, int length)
	{
		EN e = this.getRangeChainEntry(offset, length); // valuesIdate range and scroll to offset
		if(length < 0)
		{
			for(; length++ < 0; e = e.prev)
			{
				if(e.hasNullKey())
				{
					return true;
				}
			}
		}
		else
		{
			for(; length-- > 0; e = e.next)
			{
				if(e.hasNullKey())
				{
					return true;
				}
			}
		}
		return false;
	}

	// containing - identity //

	@Override
	public final boolean valuesContainsId(final V element)
	{
		for(EN e = this.head.next; e != null; e = e.next)
		{
			if(e.value() == element)
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public final boolean valuesRngContainsId(final int offset, int length, final V element)
	{
		EN e = this.getRangeChainEntry(offset, length); // valuesIdate range and scroll to offset
		if(length < 0)
		{
			for(; length++ < 0; e = e.prev)
			{
				if(e.value() == element)
				{
					return true;
				}
			}
		}
		else
		{
			for(; length-- > 0; e = e.next)
			{
				if(e.value() == element)
				{
					return true;
				}
			}
		}
		return false;
	}

	// containing - logical //

	@Override
	public final boolean valuesContains(final V element)
	{
		for(EN e = this.head.next; e != null; e = e.next)
		{
			if(e.value() == element)
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public final boolean valuesContains(final V sample, final Equalator<? super V> equalator)
	{
		for(EN e = this.head.next; e != null; e = e.next)
		{
			if(equalator.equal(e.value(), sample))
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public final boolean valuesRngContains(final int offset, int length, final V element)
	{
		EN e = this.getRangeChainEntry(offset, length); // valuesIdate range and scroll to offset
		if(length < 0)
		{
			for(; length++ < 0; e = e.prev)
			{
				if(e.value() == element)
				{
					return true;
				}
			}
		}
		else
		{
			for(; length-- > 0; e = e.next)
			{
				if(e.value() == element)
				{
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public final boolean valuesRngContains(final int offset, int length, final V sample, final Equalator<? super V> equalator)
	{
		EN e = this.getRangeChainEntry(offset, length); // valuesIdate range and scroll to offset
		if(length < 0)
		{
			for(; length++ < 0; e = e.prev)
			{
				if(equalator.equal(e.value(), sample))
				{
					return true;
				}
			}
		}
		else
		{
			for(; length-- > 0; e = e.next)
			{
				if(equalator.equal(e.value(), sample))
				{
					return true;
				}
			}
		}
		return false;
	}

	// containing - all array //

	@Override
	public final boolean valuesContainsAll(final V[] elements, final int elementsOffset, final int elementsLength)
	{
		final EN first;
		if((first = this.head.next) == null)
		{
			return false; // size 0
		}

		final int d;
		if((d = ChainStorageStrong.validateArrayIteration(elements, elementsOffset, elementsLength)) == 0)
		{
			return true;
		}

		final int elementsBound = elementsOffset + elementsLength;

		main:
		for(int ei = elementsOffset; ei != elementsBound; ei += d)
		{
			final V element = elements[ei];
			for(EN e = first; e != null; e = e.next)
			{
				if(e.value() == element)
				{
					continue main;
				}
			}
			return false;  // one element was not found in this list, return false
		}
		return true; // all elements have been found, return true
	}

	@Override
	public final boolean valuesContainsAll(
		final V[] elements,
		final int elementsOffset,
		final int elementsLength,
		final Equalator<? super V> equalator
	)
	{
		final EN first;
		if((first = this.head.next) == null)
		{
			return false; // size 0
		}

		final int d;
		if((d = ChainStorageStrong.validateArrayIteration(elements, elementsOffset, elementsLength)) == 0)
		{
			return true;
		}

		final int elementsBound = elementsOffset + elementsLength;

		// cross-iterate, either with or without ignoring nulls
		main:
		for(int ei = elementsOffset; ei != elementsBound; ei += d)
		{
			final V element = elements[ei];
			for(EN e = first; e != null; e = e.next)
			{
				if(equalator.equal(e.value(), element))
				{
					continue main;
				}
			}
			return false;  // one element was not found in this list, return false
		}
		return true; // all elements have been found, return true
	}

	@Override
	public final boolean valuesRngContainsAll(
		final int offset,
		      int length,
		final V[] elements,
		final int elementsOffset,
		final int elementsLength
	)
	{
		final EN first;
		if((first = this.getRangeChainEntry(offset, length)) == null)
		{
			// size 0
			return false;
		}
		final int d;
		if((d = ChainStorageStrong.validateArrayIteration(elements, elementsOffset, elementsLength)) == 0)
		{
			return true;
		}

		final int elementsBound = elementsOffset + elementsLength;

		// hopping direction
		final AbstractChainEntry.Hopper hop;
		if(length < 0)
		{
			hop = HOP_PREV;
			length = -length;
		}
		else
		{
			hop = HOP_NEXT;
		}

		main:
		for(int ei = elementsOffset; ei != elementsBound; ei += d)
		{
			final V element = elements[ei];
			for(EN e = first; e != null; e = hop.hop(e))
			{
				if(e.value() == element)
				{
					continue main;
				}
			}
			return false;  // one element was not found in this list, return false
		}
		return true;  // all elements have been found, return true
	}

	@Override
	public final boolean valuesRngContainsAll(
		final int offset,
		      int length,
		final V[] elements,
		final int elementsOffset,
		final int elementsLength,
		final Equalator<? super V> equalator
	)
	{
		final EN first;
		if((first = this.getRangeChainEntry(offset, length)) == null)
		{
			return false; // size 0
		}

		final int d;
		if((d = ChainStorageStrong.validateArrayIteration(elements, elementsOffset, elementsLength)) == 0)
		{
			return true;
		}

		final int elementsBound = elementsOffset + elementsLength;

		// hopping direction
		final AbstractChainEntry.Hopper hop;
		if(length < 0)
		{
			hop = HOP_PREV;
			length = -length;
		}
		else
		{
			hop = HOP_NEXT;
		}

		// cross-iterate, either with or without ignoring nulls
		loop:
		for(int ei = elementsOffset; ei != elementsBound; ei += d)
		{
			final V element = elements[ei];
			for(EN e = first; e != null; e = hop.hop(e))
			{
				if(equalator.equal(e.value(), element))
				{
					continue loop;
				}
			}
			return false;  // one element was not found in this list, return false
		}
		return true;  // all elements have been found, return true
	}

	// containing - all collection //

	@SuppressWarnings("unchecked")
	@Override
	public final boolean valuesContainsAll(final XGettingCollection<? extends V> elements)
	{
		if(elements instanceof AbstractSimpleArrayCollection<?>)
		{
			// directly check array against array without predicate function or method calls
			return this.valuesContainsAll(
				(V[])AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)elements), 0, Jadoth.to_int(elements.size())
			);
		}

		// iterate by predicate function
		return elements.applies(e ->
		{
			return this.valuesContains(e);
		});
	}

	@SuppressWarnings("unchecked")
	@Override
	public final boolean valuesContainsAll(final XGettingCollection<? extends V> elements, final Equalator<? super V> equalator)
	{
		if(elements instanceof AbstractSimpleArrayCollection<?>)
		{
			// directly check array against array without predicate function or method calls
			return this.valuesContainsAll(
				(V[])AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)elements), 0, Jadoth.to_int(elements.size()),
				equalator
			);
		}

		// iterate by predicate function
		final EN first = this.head.next;
		return elements.applies(e ->
		{
			for(EN entry = first; entry != null; entry = entry.next)
			{
				if(equalator.equal(entry.value(), e))
				{
					return true;
				}
			}
			return false;
		});
	}

	@Override
	public final boolean valuesRngContainsAll(final int offset, final int length, final XGettingCollection<? extends V> elements)
	{
		if(elements instanceof AbstractSimpleArrayCollection<?>)
		{
			return this.valuesRngContainsAll(
				offset,
				length,
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)elements),
				0,
				Jadoth.to_int(elements.size())
			);
		}

		// iterate by predicate function
		final EN first; // valuesIdate range and scroll to offset
		if((first = this.getRangeChainEntry(offset, length)) == null)
		{
			return false;
		}

		// iterate by predicate function
		if(length < 0)
		{
			return elements.applies(e ->
			{
				int len = length;
				for(EN entry = first; len++ < 0; entry = entry.prev)
				{
					if(entry.value() == e)
					{
						return true;
					}
				}
				return false;
			});
		}
		return elements.applies(e ->
		{
			int len = length;
			for(EN entry = first; len-- > 0; entry = entry.next)
			{
				if(entry.value() == e)
				{
					return true;
				}
			}
			return false;
		});
	}

	@Override
	public final boolean valuesRngContainsAll(
		final int offset,
		final int length,
		final XGettingCollection<? extends V> samples,
		final Equalator<? super V> equalator
	)
	{
		if(samples instanceof AbstractSimpleArrayCollection<?>)
		{
			return this.valuesRngContainsAll(
				offset,
				length,
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)samples),
				0,
				Jadoth.to_int(samples.size()),
				equalator
			);
		}

		final EN first; // valuesIdate range and scroll to offset
		if((first = this.getRangeChainEntry(offset, length)) == null)
		{
			return false;
		}

		// iterate by predicate function
		if(length < 0)
		{
			return samples.applies(e ->
			{
				int len = length;
				for(EN entry = first; len++ < 0; entry = entry.prev)
				{
					if(equalator.equal(entry.value(), e))
					{
						return true;
					}
				}
				return false;
			});
		}
		return samples.applies(e ->
		{
			int len = length;
			for(EN entry = first; len-- > 0; entry = entry.next)
			{
				if(equalator.equal(entry.value(), e))
				{
					return true;
				}
			}
			return false;
		});
	}



	///////////////////////////////////////////////////////////////////////////
	//    applying      //
	/////////////////////

	// applying - single //

	@Override
	public final boolean valuesApplies(final Predicate<? super V> predicate)
	{
		try
		{
			for(EN e = this.head.next; e != null; e = e.next)
			{
				if(predicate.test(e.value()))
				{
					return true;
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return false;
	}

	@Override
	public final boolean valuesRngApplies(final int offset, int length, final Predicate<? super V> predicate)
	{
		EN e = this.getRangeChainEntry(offset, length); // valuesIdate range and scroll to offset
		try
		{
			if(length < 0)
			{
				for(; length++ < 0; e = e.prev)
				{
					if(predicate.test(e.value()))
					{
						return true;
					}
				}
			}
			else
			{
				for(; length-- > 0; e = e.next)
				{
					if(predicate.test(e.value()))
					{
						return true;
					}
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return false;
	}

	// applying - all //

	@Override
	public final boolean valuesAppliesAll(final Predicate<? super V> predicate)
	{
		for(EN e = this.head.next; e != null; e = e.next)
		{
			if(!predicate.test(e.value()))
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public final boolean valuesRngAppliesAll(final int offset, int length, final Predicate<? super V> predicate)
	{
		EN e = this.getRangeChainEntry(offset, length); // valuesIdate range and scroll to offset
		try
		{
			if(length < 0)
			{
				for(; length++ < 0; e = e.prev)
				{
					if(!predicate.test(e.value()))
					{
						return false;
					}
				}
			}
			else
			{
				for(; length-- > 0; e = e.next)
				{
					if(!predicate.test(e.value()))
					{
						return false;
					}
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return true;
	}



	///////////////////////////////////////////////////////////////////////////
	//    counting      //
	/////////////////////

	// counting - element //

	@Override
	public final int valuesCount(final V element)
	{
		int count = 0;
		for(EN e = this.head.next; e != null; e = e.next)
		{
			if(e.value() == element)
			{
				count++;
			}
		}
		return count;
	}

	@Override
	public final int valuesCount(final V sample, final Equalator<? super V> equalator)
	{
		int count = 0;
		for(EN e = this.head.next; e != null; e = e.next)
		{
			if(equalator.equal(e.value(), sample))
			{
				count++;
			}
		}
		return count;
	}

	@Override
	public final int valuesRngCount(final int offset, int length, final V element)
	{
		EN e = this.getRangeChainEntry(offset, length); // valuesIdate range and scroll to offset
		int count = 0;
		if(length < 0)
		{
			for(; length++ < 0; e = e.prev)
			{
				if(e.value() == element)
				{
					count++;
				}
			}
		}
		else
		{
			for(; length-- > 0; e = e.next)
			{
				if(e.value() == element)
				{
					count++;
				}
			}
		}
		return count;
	}

	@Override
	public final int valuesRngCount(
		final int offset,
		      int length,
		final V sample,
		final Equalator<? super V> equalator
	)
	{
		EN e = this.getRangeChainEntry(offset, length); // valuesIdate range and scroll to offset
		int count = 0;
		if(length < 0)
		{
			for(; length++ < 0; e = e.prev)
			{
				if(equalator.equal(e.value(), sample))
				{
					count++;
				}
			}
		}
		else
		{
			for(; length-- > 0; e = e.next)
			{
				if(equalator.equal(e.value(), sample))
				{
					count++;
				}
			}
		}
		return count;
	}

	// counting - predicate //

	@Override
	public final int valuesCount(final Predicate<? super V> predicate)
	{
		int count = 0;
		try
		{
			for(EN e = this.head.next; e != null; e = e.next)
			{
				if(predicate.test(e.value()))
				{
					count++;
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return count;
	}

	@Override
	public final int valuesRngCount(final int offset, int length, final Predicate<? super V> predicate)
	{
		EN e = this.getRangeChainEntry(offset, length); // valuesIdate range and scroll to offset
		int count = 0;
		try
		{
			if(length < 0)
			{
				for(; length++ < 0; e = e.prev)
				{
					if(predicate.test(e.value()))
					{
						count++;
					}
				}
			}
			else
			{
				for(; length-- > 0; e = e.next)
				{
					if(predicate.test(e.value()))
					{
						count++;
					}
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return count;
	}



	///////////////////////////////////////////////////////////////////////////
	// data arithmetic  //
	/////////////////////

	// data - data sets //

	@Override
	public final <C extends Consumer<? super V>> C valuesIntersect(
		final XGettingCollection<? extends V> samples,
		final Equalator<? super V> equalator,
		final C target
	)
	{
		if(samples instanceof AbstractSimpleArrayCollection<?>)
		{
			final V[] array = AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)samples);
			final int size = Jadoth.to_int(samples.size());
			ch:
			for(EN entry = this.head.next; entry != null; entry = entry.next)
			{
				final V element = entry.value();
				for(int i = 0; i < size; i++)
				{
					if(equalator.equal(element, array[i]))
					{
						target.accept(element);
						continue ch;
					}
				}
			}
			return target;
		}

		/* has to be the long way around because:
		 * - can't directly pass an instance of type E to a collection of type ? extends E.
		 * - chain's equal element instances must be added, not samples'.
		 */
		final CachedSampleEquality<V> equalCurrentElement = new CachedSampleEquality<>(equalator);
		for(EN entry = this.head.next; entry != null; entry = entry.next)
		{
			equalCurrentElement.sample = entry.value();
			if(samples.containsSearched(equalCurrentElement))
			{
				target.accept(equalCurrentElement.sample);
			}
		}
		return target;
	}

	@Override
	public final <C extends Consumer<? super V>> C valuesExcept(
		final XGettingCollection<? extends V> samples,
		final Equalator<? super V> equalator,
		final C target
	)
	{
		if(samples instanceof AbstractSimpleArrayCollection<?>)
		{
			final V[] array = AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)samples);
			final int size = Jadoth.to_int(samples.size());
			ch:
			for(EN entry = this.head.next; entry != null; entry = entry.next)
			{
				final V element = entry.value();
				for(int i = 0; i < size; i++)
				{
					if(equalator.equal(element, array[i]))
					{
						continue ch;
					}
				}
				target.accept(element);
			}
			return target;
		}

		/* has to be the long way around because:
		 * - can't directly pass an instance of type E to a collection of type ? extends E.
		 * - chain's equal element instances must be added, not samples'.
		 */
		final CachedSampleEquality<V> equalCurrentElement = new CachedSampleEquality<>(equalator);
		ch:
		for(EN entry = this.head.next; entry != null; entry = entry.next)
		{
			equalCurrentElement.sample = entry.value();
			if(samples.containsSearched(equalCurrentElement))
			{
				continue ch;
			}
			target.accept(equalCurrentElement.sample);
		}
		return target;
	}

	@Override
	public final <C extends Consumer<? super V>> C valuesUnion(
		final XGettingCollection<? extends V> samples,
		final Equalator<? super V> equalator,
		final C target
	)
	{
		this.valuesCopyTo(target);
		if(samples instanceof AbstractSimpleArrayCollection<?>)
		{
			final V[] array = AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)samples);
			final int size = Jadoth.to_int(samples.size());
			ch:
			for(int i = 0; i < size; i++)
			{
				final V sample = array[i];
				for(EN entry = this.head.next; entry != null; entry = entry.next)
				{
					if(equalator.equal(entry.value(), sample))
					{
						continue ch;
					}
				}
				target.accept(sample);
			}
			return target;
		}

		/* has to be the long way around because:
		 * - can't directly pass an instance of type E to a collection of type ? extends E.
		 * - chain's equal element instances must be added, not samples'.
		 */
		samples.iterate(e ->
		{
			// local reference to AIC field
			final Equalator<? super V> equalator2 = equalator;

			for(EN entry = ChainStrongStrongStorage.this.head.next; entry != null; entry = entry.next)
			{
				if(equalator2.equal(e, entry.value()))
				{
					return;
				}
			}
			target.accept(e);
		});

		return target;
	}

	@Override
	public final <C extends Consumer<? super V>> C valuesRngIntersect(
		final int                             offset,
		      int                             length,
		final XGettingCollection<? extends V> samples,
		final Equalator<? super V>            equalator,
		final C                               target
	)
	{
		final EN first = this.getRangeChainEntry(offset, length);
		if(length < 0)
		{
			length = -length;
		}
		final AbstractChainEntry.Hopper ch = length < 0 ? AbstractChainEntry.HOP_PREV : AbstractChainEntry.HOP_NEXT;

		if(samples instanceof AbstractSimpleArrayCollection<?>)
		{
			final V[] array = AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)samples);
			final int size = Jadoth.to_int(samples.size());
			ch:
			for(EN entry = first; length-- > 0; entry = ch.hop(entry))
			{
				final V element = entry.value();
				for(int i = 0; i < size; i++)
				{
					if(equalator.equal(element, array[i]))
					{
						target.accept(element);
						continue ch;
					}
				}
			}
			return target;
		}

		/* has to be the long way around because:
		 * - can't directly pass an instance of type E to a collection of type ? extends E.
		 * - chain's equal element instances must be added, not samples'.
		 */
		final CachedSampleEquality<V> equalCurrentElement = new CachedSampleEquality<>(equalator);
		for(EN entry = first; length-- > 0; entry = ch.hop(entry))
		{
			equalCurrentElement.sample = entry.value();
			if(samples.containsSearched(equalCurrentElement))
			{
				target.accept(equalCurrentElement.sample);
			}
		}
		return target;
	}

	@Override
	public final <C extends Consumer<? super V>> C valuesRngExcept(
		final int offset,
		      int length,
		final XGettingCollection<? extends V> samples,
		final Equalator<? super V> equalator,
		final C target
	)
	{

		final EN first = this.getRangeChainEntry(offset, length);
		if(length < 0)
		{
			length = -length;
		}
		final AbstractChainEntry.Hopper ch = length < 0 ? AbstractChainEntry.HOP_PREV : AbstractChainEntry.HOP_NEXT;

		if(samples instanceof AbstractSimpleArrayCollection<?>)
		{
			final V[] array = AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)samples);
			final int size = Jadoth.to_int(samples.size());
			ch:
			for(EN entry = first; length-- > 0; entry = ch.hop(entry))
			{
				final V element = entry.value();
				for(int i = 0; i < size; i++)
				{
					if(equalator.equal(element, array[i]))
					{
						continue ch;
					}
				}
				target.accept(element);
			}
			return target;
		}

		/* has to be the long way around because:
		 * - can't directly pass an instance of type E to a collection of type ? extends E.
		 * - chain's equal element instances must be added, not samples'.
		 */
		final CachedSampleEquality<V> equalCurrentElement = new CachedSampleEquality<>(equalator);
		ch:
		for(EN entry = first; length-- > 0; entry = ch.hop(entry))
		{
			equalCurrentElement.sample = entry.value();
			if(samples.containsSearched(equalCurrentElement))
			{
				continue ch;
			}
			target.accept(equalCurrentElement.sample);
		}
		return target;
	}

	@Override
	public final <C extends Consumer<? super V>> C valuesRngUnion(
		final int offset,
		final int length,
		final XGettingCollection<? extends V> samples,
		final Equalator<? super V> equalator,
		final C target
	)
	{
		final EN first = this.getRangeChainEntry(offset, length);
		final AbstractChainEntry.Hopper ch = length < 0 ? AbstractChainEntry.HOP_PREV : AbstractChainEntry.HOP_NEXT;

		this.valuesRngCopyTo(offset, length, target);
		if(samples instanceof AbstractSimpleArrayCollection<?>)
		{
			final V[] array = AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)samples);
			final int size = Jadoth.to_int(samples.size());
			ar:
			for(int i = 0; i < size; i++)
			{
				final V sample = array[i];
				int len = length;
				for(EN entry = first; len-- > 0; entry = ch.hop(entry))
				{
					if(equalator.equal(entry.value(), sample))
					{
						continue ar;
					}
				}
				target.accept(sample);
			}
			return target;
		}

		final int normalizedLength = length >= 0 ? length : -length;
		samples.iterate(e->
		{
			// local reference to AIC field
			final Equalator<? super V> equalator2 = equalator;

			int len = normalizedLength;
			for(EN entry = first; len-- > 0; entry = ch.hop(entry))
			{
				if(equalator2.equal(e, entry.value()))
				{
					return;
				}
			}
			target.accept(e);
		});
		return target;
	}


	// data - copying //

	@Override
	public final <C extends Consumer<? super V>> C valuesCopyTo(final C target)
	{
		for(EN e = this.head.next; e != null; e = e.next)
		{
			target.accept(e.value());
		}
		return target;
	}

	@Override
	public final <C extends Consumer<? super V>> C valuesRngCopyTo(final int offset, int length, final C target)
	{
		final EN first;
		if((first = this.getRangeChainEntry(offset, length)) == null)
		{
			return target;
		}

		if(length > 0)
		{
			for(EN e = first; length-- > 0; e = e.next)
			{
				target.accept(e.value());
			}
		}
		else
		{
			for(EN e = first; length++ < 0; e = e.prev)
			{
				target.accept(e.value());
			}
		}
		return target;
	}

	@Override
	public final <C extends Consumer<? super V>> C valuesCopySelection(final C target, final long... indices)
	{
		final int length = indices.length, size = Jadoth.to_int(this.parent.size());

		// valuesIdate all indices before copying the first element
		for(int i = 0; i < length; i++)
		{
			if(indices[i] < 0 || indices[i] >= size)
			{
				throw new IndexOutOfBoundsException(exceptionIndexOutOfBounds(size, indices[i]));
			}
		}

		// actual copying. Note: can't sort indices as copying order might be relevant
		for(int i = 0; i < length; i++)
		{
			target.accept(this.getChainEntry(indices[i]).value()); // scrolling is pretty inefficient here :(
		}
		return target;
	}

	@Override
	public final int valuesCopyToArray(final long offset, int length, final Object[] target, final int targetOffset)
	{
		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return 0;
		}

		if(targetOffset < 0)
		{
			throw new ArrayIndexOutOfBoundsException(targetOffset);
		}
		if((length < 0 ? -length : length) + targetOffset > target.length)
		{
			throw new ArrayIndexOutOfBoundsException((length < 0 ? -length : length) + targetOffset);
		}

		int t = targetOffset;
		if(length > 0)
		{
			for(; length-- > 0; e = e.next)
			{
				target[t++] = e.value();
			}
		}
		else
		{
			for(; length++ < 0; e = e.prev)
			{
				target[t++] = e.value();
			}
		}
		return t - targetOffset;
	}

	// data - conditional copying //

	@Override
	public final <C extends Consumer<? super V>> C valuesCopyTo(final C target, final Predicate<? super V> predicate)
	{
		try
		{
			for(EN e = this.head.next; e != null; e = e.next)
			{
				if(predicate.test(e.value()))
				{
					target.accept(e.value());
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return target;
	}

	@Override
	public final <C extends Consumer<? super V>> C valuesRngCopyTo(
		final int offset,
		      int length,
		final C target,
		final Predicate<? super V> predicate
	)
	{
		final EN first;
		if((first = this.getRangeChainEntry(offset, length)) == null)
		{
			return target;
		}

		try
		{
			if(length > 0)
			{
				for(EN e = first; length-- > 0; e = e.next)
				{
					if(predicate.test(e.value()))
					{
						target.accept(e.value());
					}
				}
			}
			else
			{
				for(EN e = first; length++ < 0; e = e.prev)
				{
					if(predicate.test(e.value()))
					{
						target.accept(e.value());
					}
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return target;
	}

	// data - array transformation //

	@Override
	public final Object[] valuesToArray()
	{
		final Object[] array;
		this.valuesCopyToArray(0, Jadoth.to_int(this.parent.size()), array = new Object[Jadoth.to_int(this.parent.size())], 0);
		return array;
	}

	@Override
	public final      V[] valuesToArray(final Class<V> type)
	{
		final V[] array;
		this.valuesCopyToArray(0, Jadoth.to_int(this.parent.size()), array = JadothArrays.newArray(type, Jadoth.to_int(this.parent.size())), 0);
		return array;
	}

	@Override
	public final Object[] valuesRngToArray(final int offset, final int length)
	{
		final Object[] array;
		this.valuesCopyToArray(offset, length, array = new Object[length < 0 ? -length : length], 0);
		return array;
	}

	@Override
	public final      V[] valuesRngToArray(final int offset, final int length, final Class<V> type)
	{
		final V[] array;
		this.valuesCopyToArray(offset, length, array = JadothArrays.newArray(type, length < 0 ? -length : length), 0);
		return array;
	}



	///////////////////////////////////////////////////////////////////////////
	//    querying      //
	/////////////////////

	@Override
	public final V valuesFirst()
	{
		return this.head.next == null ? null : this.head.next.value();
	}

	@Override
	public final V valuesLast()
	{
		return this.head.prev == this.head ? null : this.head.prev.value();
	}

	@Override
	public final V valuesGet(final long index)
	{
		return this.getChainEntry(index).value();
	}



	///////////////////////////////////////////////////////////////////////////
	//    searching     //
	/////////////////////

	// searching - sample //

	@Override
	public final V valuesRngSearch(final int offset, int length, final V sample, final Equalator<? super V> equalator)
	{
		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return null;
		}

		V element; // variable is necessary to defend against changing (e.g. weakly referencing) entries.
		if(length > 0)
		{
			for(; length-- > 0; e = e.next)
			{
				if(equalator.equal(element = e.value(), sample))
				{
					return element;
				}
			}
		}
		else
		{
			for(; length++ < 0; e = e.prev)
			{
				if(equalator.equal(element = e.value(), sample))
				{
					return element;
				}
			}
		}
		return null;
	}

	// searching - predicate //

	@Override
	public final V valuesSeek(final V element)
	{
		for(EN e = this.head.next; e != null; e = e.next)
		{
			if(e.value() == element)
			{
				return e.value();
			}
		}
		return null;
	}

	@Override
	public final V valuesRngSeek(final int offset, int length, final V sample)
	{
		EN e = this.getRangeChainEntry(offset, length); // valuesIdate range and scroll to offset
		if(length < 0)
		{
			for(; length++ < 0; e = e.prev)
			{
				if(e.value() == sample)
				{
					return e.value();
				}
			}
		}
		else
		{
			for(; length-- > 0; e = e.next)
			{
				if(e.value() == sample)
				{
					return e.value();
				}
			}
		}
		return null;
	}

	@Override
	public final V valuesSearch(final V sample, final Equalator<? super V> equalator)
	{
		for(EN e = this.head.next; e != null; e = e.next)
		{
			if(equalator.equal(e.value(), sample))
			{
				return e.value();
			}
		}
		return null;
	}

	@Override
	public final V valuesSearch(final Predicate<? super V> predicate)
	{
		try
		{
			for(EN e = this.head.next; e != null; e = e.next)
			{
				if(predicate.test(e.value()))
				{
					return e.value();
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return null;
	}

	@Override
	public final V valuesRngSearch(final int offset, int length, final Predicate<? super V> predicate)
	{
		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return null;
		}
		try
		{
			if(length > 0)
			{
				for(; length-- > 0; e = e.next)
				{
					if(predicate.test(e.value()))
					{
						return e.value();
					}
				}
			}
			else
			{
				for(; length++ < 0; e = e.prev)
				{
					if(predicate.test(e.value()))
					{
						return e.value();
					}
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return null;
	}

	// searching - min max //

	@Override
	public final V valuesMin(final Comparator<? super V> comparator)
	{
		EN e;
		if((e = this.head.next) == null)
		{
			return null;
		}

		V element, loopMinElement = e.value();
		for(e = e.next; e != null; e = e.next)
		{
			if(comparator.compare(loopMinElement, element = e.value()) > 0)
			{
				loopMinElement = element;
			}
		}
		return loopMinElement;
	}

	@Override
	public final V valuesMax(final Comparator<? super V> comparator)
	{
		EN e;
		if((e = this.head.next) == null)
		{
			return null;
		}

		V element, loopMaxElement = e.value();
		for(e = e.next; e != null; e = e.next)
		{
			if(comparator.compare(loopMaxElement, element = e.value()) < 0)
			{
				loopMaxElement = element;
			}
		}
		return loopMaxElement;
	}

	@Override
	public final V valuesRngMin(final int offset, int length, final Comparator<? super V> comparator)
	{
		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return null;
		}

		V element, loopMinElement = e.value();
		if(length > 0)
		{
			for(e = e.next, length--; length-- > 0; e = e.next)
			{
				if(comparator.compare(loopMinElement, element = e.value()) > 0)
				{
					loopMinElement = element;
				}
			}
		}
		else
		{
			for(e = e.prev, length++; length++ < 0; e = e.prev)
			{
				if(comparator.compare(loopMinElement, element = e.value()) > 0)
				{
					loopMinElement = element;
				}
			}
		}
		return loopMinElement;
	}

	@Override
	public final V valuesRngMax(final int offset, int length, final Comparator<? super V> comparator)
	{
		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return null;
		}

		V element, loopMaxElement = e.value();
		if(length > 0)
		{
			for(e = e.next, length--; length-- > 0; e = e.next)
			{
				if(comparator.compare(loopMaxElement, element = e.value()) < 0)
				{
					loopMaxElement = element;
				}
			}
		}
		else
		{
			for(e = e.prev, length++; length++ < 0; e = e.prev)
			{
				if(comparator.compare(loopMaxElement, element = e.value()) < 0)
				{
					loopMaxElement = element;
				}
			}
		}
		return loopMaxElement;
	}



	///////////////////////////////////////////////////////////////////////////
	//    executing     //
	/////////////////////

	// executing - procedure //

	@Override
	public final void valuesIterate(final Consumer<? super V> procedure)
	{
		try
		{
			for(EN e = this.head.next; e != null; e = e.next)
			{
				procedure.accept(e.value());
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
	}

	@Override
	public final void valuesRngIterate(final int offset, int length, final Consumer<? super V> procedure)
	{
		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return;
		}
		try
		{
			if(length > 0)
			{
				for(; length-- > 0; e = e.next)
				{
					procedure.accept(e.value());
				}
			}
			else
			{
				for(; length++ < 0; e = e.prev)
				{
					procedure.accept(e.value());
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
	}

	// executing - indexed procedure //

	@Override
	public final void valuesIterateIndexed(final IndexProcedure<? super V> procedure)
	{
		try
		{
			int i = -1;
			for(EN entry = this.head; (entry = entry.next) != null;)
			{
				procedure.accept(entry.value(), ++i);
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
	}

	@Override
	public final void valuesRngIterateIndexed(int offset, final int length, final IndexProcedure<? super V> procedure)
	{
		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return;
		}

		final int bound = offset + length;
		if(length > 0)
		{
			for(; offset != bound; e = e.next)
			{
				procedure.accept(e.value(), offset++);
			}
		}
		else
		{
			for(; offset != bound; e = e.prev)
			{
				procedure.accept(e.value(), offset--);
			}
		}
	}

	@Override
	public final <A> void valuesJoin(final BiProcedure<? super V, A> joiner, final A aggregate)
	{
		try
		{
			for(EN entry = this.head; (entry = entry.next) != null;)
			{
				joiner.accept(entry.value(), aggregate);
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
	}

	// executing - conditional //

	@Override
	public final void valuesIterate(final Predicate<? super V> predicate, final Consumer<? super V> procedure)
	{
		try
		{
			for(EN e = this.head.next; e != null; e = e.next)
			{
				if(predicate.test(e.value()))
				{
					procedure.accept(e.value());
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
	}



	///////////////////////////////////////////////////////////////////////////
	//    indexing      //
	/////////////////////

	// indexing - single //

	@Override
	public final int valuesIndexOf(final V element)
	{
		int i = 0;
		for(EN e = this.head.next; e != null; e = e.next, i++)
		{
			if(e.value() == element)
			{
				return i;
			}
		}
		return -1;
	}

	@Override
	public final int valuesIndexOf(final V sample, final Equalator<? super V> equalator)
	{
		int i = 0;
		for(EN e = this.head.next; e != null; e = e.next, i++)
		{
			if(equalator.equal(e.value(), sample))
			{
				return i;
			}
		}
		return -1;
	}

	@Override
	public final int valuesRngIndexOf(int offset, final int length, final V element)
	{
		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return -1;
		}

		final int bound = offset + length;
		if(length > 0)
		{
			for(; offset != bound; e = e.next, offset++)
			{
				if(e.value() == element)
				{
					return offset;
				}
			}
		}
		else
		{
			for(; offset != bound; e = e.prev, offset--)
			{
				if(e.value() == element)
				{
					return offset;
				}
			}
		}
		return -1;
	}

	@Override
	public final int valuesRngIndexOf(int offset, final int length, final V sample, final Equalator<? super V> equalator)
	{
		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return -1;
		}

		final int bound = offset + length;
		if(length > 0)
		{
			for(; offset != bound; e = e.next, offset++)
			{
				if(equalator.equal(e.value(), sample))
				{
					return offset;
				}
			}
		}
		else
		{
			for(; offset != bound; e = e.prev, offset--)
			{
				if(equalator.equal(e.value(), sample))
				{
					return offset;
				}
			}
		}
		return -1;
	}

	// indexing - predicate //

	@Override
	public final int valuesIndexOf(final Predicate<? super V> predicate)
	{
		int i = 0;
		try
		{
			for(EN e = this.head.next; e != null; e = e.next, i++)
			{
				if(predicate.test(e.value()))
				{
					return i;
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return -1;
	}

	@Override
	public final int valuesRngIndexOf(int offset, final int length, final Predicate<? super V> predicate)
	{
		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return -1;
		}

		try
		{
			final int bound = offset + length;
			if(length > 0)
			{
				for(; offset != bound; e = e.next, offset++)
				{
					if(predicate.test(e.value()))
					{
						return offset;
					}
				}
			}
			else
			{
				for(; offset != bound; e = e.prev, offset--)
				{
					if(predicate.test(e.value()))
					{
						return offset;
					}
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return -1;
	}

	// indexing - min max //

	@Override
	public final int valuesMinIndex(final Comparator<? super V> comparator)
	{
		EN e;
		if((e = this.head.next) == null)
		{
			return -1;
		}

		V loopMinElement = e.value();
		int loopMinIndex = 0;
		int i = 1;
		for(e = e.next; e != null; e = e.next, i++)
		{
			final V element;
			if(comparator.compare(loopMinElement, element = e.value()) > 0)
			{
				loopMinElement = element;
				loopMinIndex = i;
			}
		}
		return loopMinIndex;
	}

	@Override
	public final int valuesMaxIndex(final Comparator<? super V> comparator)
	{
		EN e;
		if((e = this.head.next) == null)
		{
			return -1;
		}

		V loopMaxElement = e.value();
		int loopMaxIndex = 0;
		int i = 1;
		for(e = e.next; e != null; e = e.next, i++)
		{
			final V element;
			if(comparator.compare(loopMaxElement, element = e.value()) < 0)
			{
				loopMaxElement = element;
				loopMaxIndex = i;
			}
		}
		return loopMaxIndex;
	}

	@Override
	public final int valuesRngMinIndex(int offset, final int length, final Comparator<? super V> comparator)
	{
		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return -1;
		}

		V loopMinElement = e.value();
		int loopMinIndex = 0;
		final int bound = offset + length;
		if(length > 0)
		{
			for(V element; offset != bound; e = e.next, offset++)
			{
				if(comparator.compare(loopMinElement, element = e.value()) > 0)
				{
					loopMinElement = element;
					loopMinIndex = offset;
				}
			}
		}
		else
		{
			for(V element; offset != bound; e = e.prev, offset--)
			{
				if(comparator.compare(loopMinElement, element = e.value()) > 0)
				{
					loopMinElement = element;
					loopMinIndex = offset;
				}
			}
		}
		return loopMinIndex;
	}

	@Override
	public final int valuesRngMaxIndex(int offset, final int length, final Comparator<? super V> comparator)
	{
		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return -1;
		}

		V loopMaxElement = e.value();
		int loopMaxIndex = 0;
		final int bound = offset + length;
		if(length > 0)
		{
			for(V element; offset != bound; e = e.next, offset++)
			{
				if(comparator.compare(loopMaxElement, element = e.value()) < 0)
				{
					loopMaxElement = element;
					loopMaxIndex = offset;
				}
			}
		}
		else
		{
			for(V element; offset != bound; e = e.prev, offset--)
			{
				if(comparator.compare(loopMaxElement, element = e.value()) < 0)
				{
					loopMaxElement = element;
					loopMaxIndex = offset;
				}
			}
		}
		return loopMaxIndex;
	}

	// indexing - scan //

	@Override
	public final int valuesScan(final Predicate<? super V> predicate)
	{
		int foundIndex = -1;
		try
		{
			int i = 0;
			for(EN e = this.head.next; e != null; e = e.next, i++)
			{
				if(predicate.test(e.value()))
				{
					foundIndex = i;
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return foundIndex;
	}

	@Override
	public final int valuesRngScan(int offset, final int length, final Predicate<? super V> predicate)
	{
		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return -1;
		}

		int foundIndex = -1;
		try
		{
			final int bound = offset + length;
			if(length > 0)
			{
				for(; offset != bound; e = e.next, offset++)
				{
					if(predicate.test(e.value()))
					{
						foundIndex = offset;
					}
				}
			}
			else
			{
				for(; offset != bound; e = e.prev, offset--)
				{
					if(predicate.test(e.value()))
					{
						foundIndex = offset;
					}
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return foundIndex;
	}



	///////////////////////////////////////////////////////////////////////////
	//   distinction    //
	/////////////////////

	@Override
	public final boolean valuesHasDistinctValues()
	{
		for(EN e = this.head.next; e != null; e = e.next)
		{
			final V element = e.value();
			for(EN lookAhead = e.next; lookAhead != null; lookAhead = lookAhead.next)
			{
				if(element == lookAhead.value())
				{
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public final boolean valuesHasDistinctValues(final Equalator<? super V> equalator)
	{
		for(EN e = this.head.next; e != null; e = e.next)
		{
			final V element = e.value();
			for(EN lookAhead = e.next; lookAhead != null; lookAhead = lookAhead.next)
			{
				if(equalator.equal(element, lookAhead.value()))
				{
					return false;
				}
			}
		}
		return true;
	}

	// distinction copying //

	@Override
	public final <C extends Consumer<? super V>> C valuesDistinct(final C target)
	{
		return this.valuesRngDistinct(Jadoth.to_int(this.parent.size()) - 1, Jadoth.to_int(this.parent.size()), target);
	}

	@Override
	public final <C extends Consumer<? super V>> C valuesDistinct(final C target, final Equalator<? super V> equalator)
	{
		return this.valuesRngDistinct(Jadoth.to_int(this.parent.size()) - 1, Jadoth.to_int(this.parent.size()), target, equalator);
	}

	@Override
	public final <C extends Consumer<? super V>> C valuesRngDistinct(final int offset, int length, final C target)
	{
		if(length == 0)
		{
			// required for correctness of direction reversion
			return target;
		}

		EN e;
		if((e = this.getRangeChainEntry(offset + length - (length > 0 ? 1 : -1), -length)) == null)
		{
			return target;
		}

		// hopping direction (reversed for algorighm!)
		final AbstractChainEntry.Hopper ch = length < 0 ? HOP_NEXT : HOP_PREV;
		if(length < 0)
		{
			length = -length;
		}

		mainLoop: // find last distinct element in reverse order: means put first distinct element to target
		for(; length > 0; e = ch.hop(e), length--)
		{
			final V element = e.value();
			for(EN lookAhead = ch.hop(e); lookAhead != null; lookAhead = ch.hop(lookAhead))
			{
				if(element == lookAhead.value())
				{
					continue mainLoop;
				}
			}
			target.accept(element);
		}

		return target;
	}

	@Override
	public final <C extends Consumer<? super V>> C valuesRngDistinct(
		final int offset,
		      int length,
		final C target,
		final Equalator<? super V> equalator
	)
	{
		if(length == 0)
		{
			// required for correctness of direction reversion
			return target;
		}

		EN e;
		if((e = this.getRangeChainEntry(offset + length - (length > 0 ? 1 : -1), -length)) == null)
		{
			return target;
		}

		// hopping direction (reversed for algorighm!)
		final AbstractChainEntry.Hopper ch = length < 0 ? HOP_NEXT : HOP_PREV;
		if(length < 0)
		{
			length = -length;
		}

		mainLoop: // find last distinct element in reverse order: means put first distinct element to target
		for(; length > 0; e = ch.hop(e), length--)
		{
			final V element = e.value();
			for(EN lookAhead = ch.hop(e); lookAhead != null; lookAhead = ch.hop(lookAhead))
			{
				if(equalator.equal(element, lookAhead.value()))
				{
					continue mainLoop;
				}
			}
			target.accept(element);
		}

		return target;
	}



	///////////////////////////////////////////////////////////////////////////
	// VarString appending //
	//////////////////////

	@Override
	public final VarString valuesAppendTo(final VarString vc)
	{
		for(EN entry = this.head; (entry = entry.next) != null;)
		{
			vc.add(entry.value());
		}
		return vc;
	}

	@Override
	public final VarString valuesAppendTo(final VarString vc, final char separator)
	{
		if(this.head.next == null)
		{
			return vc;
		}
		for(EN entry = this.head; (entry = entry.next) != null;)
		{
			vc.add(entry.value()).add(separator);
		}
		return vc.deleteLast();
	}

	@Override
	public final VarString valuesAppendTo(final VarString vc, final String separator)
	{
		if(separator == null || separator.isEmpty())
		{
			return this.valuesAppendTo(vc);
		}

		EN entry;
		if((entry = this.head.next) == null)
		{
			return vc;
		}
		vc.add(entry.value());
		final char[] sepp = separator.toCharArray();
		while((entry = entry.next) != null)
		{
			vc.add(sepp).add(entry.value());
		}
		return vc;
	}

	@Override
	public final VarString valuesAppendTo(final VarString vc, final BiProcedure<VarString, ? super V> appender)
	{
		for(EN entry = this.head.next; entry != null; entry = entry.next)
		{
			appender.accept(vc, entry.value());
		}
		return vc;
	}

	@Override
	public final VarString valuesAppendTo(final VarString vc, final BiProcedure<VarString, ? super V> appender, final char separator)
	{
		EN entry;
		if((entry = this.head.next) == null)
		{
			return vc;
		}
		appender.accept(vc, entry.value());
		while((entry = entry.next) != null)
		{
			appender.accept(vc.append(separator), entry.value());
		}
		return vc;
	}

	@Override
	public final VarString valuesAppendTo(final VarString vc, final BiProcedure<VarString, ? super V> appender, final String separator)
	{
		if(separator == null || separator.isEmpty())
		{
			return this.valuesAppendTo(vc, appender);
		}

		EN entry;
		if((entry = this.head.next) == null)
		{
			return vc;
		}
		appender.accept(vc, entry.value());
		final char[] sepp = separator.toCharArray();
		while((entry = entry.next) != null)
		{
			appender.accept(vc.add(sepp), entry.value());
		}
		return vc;
	}

	@Override
	public final VarString valuesRngAppendTo(int offset, final int length, final VarString vc)
	{
		EN entry;
		if((entry = this.getRangeChainEntry(offset, length)) == null)
		{
			return vc;
		}
		final int bound = offset + length;
		vc.add(entry.value());
		if(length > 0)
		{
			for(; ++offset != bound; entry = entry.next)
			{
				vc.add(entry.value());
			}
		}
		else
		{
			for(; --offset != bound; entry = entry.prev)
			{
				vc.add(entry.value());
			}
		}
		return vc;
	}

	@Override
	public final VarString valuesRngAppendTo(int offset, final int length, final VarString vc, final char separator)
	{
		EN entry;
		if((entry = this.getRangeChainEntry(offset, length)) == null)
		{
			return vc;
		}
		final int bound = offset + length;
		vc.add(entry.value());
		if(length > 0)
		{
			for(; ++offset != bound; entry = entry.next)
			{
				vc.append(separator).add(entry.value());
			}
		}
		else
		{
			for(; --offset != bound; entry = entry.prev)
			{
				vc.append(separator).add(entry.value());
			}
		}
		return vc;
	}

	@Override
	public final VarString valuesRngAppendTo(int offset, final int length, final VarString vc, final String separator)
	{
		if(separator == null || separator.isEmpty())
		{
			return this.valuesRngAppendTo(offset, length, vc);
		}
		EN entry;
		if((entry = this.getRangeChainEntry(offset, length)) == null)
		{
			return vc;
		}
		final char[] sepp = separator.toCharArray();
		final int bound = offset + length;
		vc.add(entry.value());
		if(length > 0)
		{
			for(; ++offset != bound; entry = entry.next)
			{
				vc.add(sepp).add(entry.value());
			}
		}
		else
		{
			for(; --offset != bound; entry = entry.prev)
			{
				vc.add(sepp).add(entry.value());
			}
		}
		return vc;
	}

	@Override
	public final VarString valuesRngAppendTo(int offset, final int length, final VarString vc, final BiProcedure<VarString, ? super V> appender)
	{
		EN entry;
		if((entry = this.getRangeChainEntry(offset, length)) == null)
		{
			return vc;
		}
		final int bound = offset + length;
		appender.accept(vc, entry.value());
		if(length > 0)
		{
			for(; ++offset != bound; entry = entry.next)
			{
				appender.accept(vc, entry.value());
			}
		}
		else
		{
			for(; --offset != bound; entry = entry.prev)
			{
				appender.accept(vc, entry.value());
			}
		}
		return vc;
	}

	@Override
	public final VarString valuesRngAppendTo(
		      int offset,
		final int length,
		final VarString vc,
		final BiProcedure<VarString, ? super V> appender,
		final char separator
	)
	{
		EN entry;
		if((entry = this.getRangeChainEntry(offset, length)) == null)
		{
			return vc;
		}
		final int bound = offset + length;
		appender.accept(vc, entry.value());
		if(length > 0)
		{
			for(; ++offset != bound; entry = entry.next)
			{
				appender.accept(vc.append(separator), entry.value());
			}
		}
		else
		{
			for(; --offset != bound; entry = entry.prev)
			{
				appender.accept(vc.append(separator), entry.value());
			}
		}
		return vc;
	}

	@Override
	public final VarString valuesRngAppendTo(
		      int offset,
		final int length,
		final VarString vc,
		final BiProcedure<VarString, ? super V> appender,
		final String separator
	)
	{
		if(separator == null || separator.isEmpty())
		{
			return this.valuesRngAppendTo(offset, length, vc, appender);
		}
		EN entry;
		if((entry = this.getRangeChainEntry(offset, length)) == null)
		{
			return vc;
		}
		final char[] sepp = separator.toCharArray();
		final int bound = offset + length;
		appender.accept(vc, entry.value());
		if(length > 0)
		{
			for(; ++offset != bound; entry = entry.next)
			{
				appender.accept(vc.add(sepp), entry.value());
			}
		}
		else
		{
			for(; --offset != bound; entry = entry.prev)
			{
				appender.accept(vc.add(sepp), entry.value());
			}
		}
		return vc;
	}

	@Override
	public final String valuesToString()
	{
		final VarString vc = VarString.New((int)(Jadoth.to_int(this.parent.size()) * 5.0f));
		for(EN e = this.head.next; e != null; e = e.next)
		{
			vc.append('(').add(e.value()).add(')', '-');
		}
		if(vc.isEmpty())
		{
			vc.add('(', ')');
		}
		else
		{
			vc.deleteLast();
		}
		return vc.toString();
	}



	///////////////////////////////////////////////////////////////////////////
	//    removing      //
	/////////////////////

	// removing - indexed //

	@Override
	public final V valuesRemove(final long index)
	{
		final EN e;
		(e = this.getChainEntry(index)).removeFrom(this.parent);
		return e.value();
	}

	// removing - null //

	@Override
	public final int valuesRemoveNull()
	{
		final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent = this.parent;

		int removeCount = 0;
		for(EN e = this.head.next; e != null; e = e.next)
		{
			if(e.hasNullKey())
			{
				e.removeFrom(parent);
				removeCount++;
			}
		}
		return removeCount;
	}

	@Override
	public final int valuesRngRemoveNull(int offset, final int length)
	{
		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return 0;
		}

		int removeCount = 0;
		final int bound = offset + length;
		final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent = this.parent;
		if(length > 0)
		{
			for(; offset != bound; e = e.next, offset++)
			{
				if(e.hasNullKey())
				{
					e.removeFrom(parent);
					removeCount++;
				}
			}
		}
		else
		{
			for(; offset != bound; e = e.prev, offset--)
			{
				if(e.hasNullKey())
				{
					e.removeFrom(parent);
					removeCount++;
				}
			}
		}
		return removeCount;
	}

	// removing - one single //

	@Override
	public final V valuesRetrieve(final V element)
	{
		final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent = this.parent;
		for(EN e = this.head.next; e != null; e = e.next)
		{
			if(e.value() == element)
			{
				e.removeFrom(parent);
				return e.value();
			}
		}
		return null;
	}

	@Override
	public final V valuesRetrieve(final Predicate<? super V> predicate)
	{
		final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent = this.parent;
		for(EN e = this.head.next; e != null; e = e.next)
		{
			if(predicate.test(e.value()))
			{
				e.removeFrom(parent);
				return e.value();
			}
		}
		return null;
	}

	@Override
	public final V valuesRngRetrieve(final int offset, int length, final V element)
	{
		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return null;
		}

		final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent = this.parent;
		if(length > 0)
		{
			for(; length-- > 0; e = e.next)
			{
				if(e.value() == element)
				{
					e.removeFrom(parent);
					return e.value();
				}
			}
		}
		else
		{
			for(; length++ < 0; e = e.prev)
			{
				if(e.value() == element)
				{
					e.removeFrom(parent);
					return e.value();
				}
			}
		}
		return null;
	}

	@Override
	public final V valuesRngRetrieve(final int offset, int length, final V sample, final Equalator<? super V> equalator)
	{
		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return null;
		}

		final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent = this.parent;

		if(length > 0)
		{
			for(; length-- > 0; e = e.next)
			{
				if(equalator.equal(e.value(), sample))
				{
					e.removeFrom(parent);
					return e.value();
				}
			}
		}
		else
		{
			for(; length++ < 0; e = e.prev)
			{
				if(equalator.equal(e.value(), sample))
				{
					e.removeFrom(parent);
					return e.value();
				}
			}
		}
		return null;
	}

	// removing - multiple single //

	@Override
	public final int valuesRemove(final V element)
	{
		final int oldSize = Jadoth.to_int(this.parent.size());
		final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent = this.parent;
		for(EN e = this.head.next; e != null; e = e.next)
		{
			if(e.value() == element)
			{
				e.removeFrom(parent);
			}
		}
		return oldSize - Jadoth.to_int(this.parent.size());
	}

	@Override
	public final int valuesRemove(final V sample, final Equalator<? super V> equalator)
	{
		final int oldSize = Jadoth.to_int(this.parent.size());
		final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent = this.parent;
		for(EN e = this.head.next; e != null; e = e.next)
		{
			if(equalator.equal(e.value(), sample))
			{
				e.removeFrom(parent);
			}
		}
		return oldSize - Jadoth.to_int(this.parent.size());
	}

	@Override
	public final int valuesRngRemove(int offset, final int length, final V element)
	{
		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return 0;
		}

		int removeCount = 0;
		final int bound = offset + length;
		final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent = this.parent;
		if(length > 0)
		{
			for(; offset != bound; e = e.next, offset++)
			{
				if(e.value() == element)
				{
					e.removeFrom(parent);
					removeCount++;
				}
			}
		}
		else
		{
			for(; offset != bound; e = e.prev, offset--)
			{
				if(e.value() == element)
				{
					e.removeFrom(parent);
					removeCount++;
				}
			}
		}
		return removeCount;
	}

	@Override
	public final int valuesRngRemove(int offset, final int length, final V sample, final Equalator<? super V> equalator)
	{
		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return 0;
		}

		int removeCount = 0;
		final int bound = offset + length;
		final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent = this.parent;
		if(length > 0)
		{
			for(; offset != bound; e = e.next, offset++)
			{
				if(equalator.equal(e.value(), sample))
				{
					e.removeFrom(parent);
					removeCount++;
				}
			}
		}
		else
		{
			for(; offset != bound; e = e.prev, offset--)
			{
				if(equalator.equal(e.value(), sample))
				{
					e.removeFrom(parent);
					removeCount++;
				}
			}
		}
		return removeCount;
	}

	// removing - multiple all array //

	@Override
	public final int valuesRemoveAll(final V[] elements, final int elementsOffset, final int elementsLength)
	{
		final int d;
		if((d = ChainStorageStrong.validateArrayIteration(elements, elementsOffset, elementsLength)) == 0)
		{
			return 0;
		}

		final int elementsBound = elementsOffset + elementsLength;
		final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent = this.parent;

		int removeCount = 0;
		for(EN e = this.head.next; e != null; e = e.next)
		{
			final V element = e.value();
			for(int i = elementsOffset; i != elementsBound; i += d)
			{
				if(element == elements[i])
				{
					e.removeFrom(parent);
					removeCount++;
					break;
				}
			}
		}
		return removeCount;
	}

	@Override
	public final int valuesRemoveAll(
		final V[] samples,
		final int samplesOffset,
		final int samplesLength,
		final Equalator<? super V> equalator
	)
	{
		final int d;
		if((d = ChainStorageStrong.validateArrayIteration(samples, samplesOffset, samplesLength)) == 0)
		{
			return 0;
		}

		final int elementsBound = samplesOffset + samplesLength;
		final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent = this.parent;

		int removeCount = 0;
		for(EN e = this.head.next; e != null; e = e.next)
		{
			final V element = e.value();
			for(int i = samplesOffset; i != elementsBound; i += d)
			{
				if(equalator.equal(element, samples[i]))
				{
					e.removeFrom(parent);
					removeCount++;
					break;
				}
			}
		}
		return removeCount;
	}

	@Override
	public final int valuesRngRemoveAll(
		      int offset,
		final int length,
		final V[] elements,
		final int elementsOffset,
		final int elementsLength
	)
	{
		final int d;
		if((d = JadothArrays.validateArrayRange(elements, elementsOffset, elementsLength)) == 0)
		{
			return 0;
		}

		final EN first;
		if((first = this.getRangeChainEntry(offset, length)) == null)
		{
			return 0;
		}

		final int elementsBound = elementsOffset + elementsLength;
		final int bound = offset + length;
		int removeCount = 0;
		final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent = this.parent;
		if(length > 0)
		{
			for(EN e = first; offset != bound; e = e.next, offset++)
			{
				for(int i = elementsOffset; i != elementsBound; i += d)
				{
					if(e.value() == elements[i])
					{
						e.removeFrom(parent);
						removeCount++;
						break;
					}
				}
			}
		}
		else
		{
			for(EN e = first; offset != bound; e = e.prev, offset++)
			{
				for(int i = elementsOffset; i != elementsBound; i += d)
				{
					if(e.value() == elements[i])
					{
						e.removeFrom(parent);
						removeCount++;
						break;
					}
				}
			}
		}
		return removeCount;
	}

	@Override
	public final int valuesRngRemoveAll(
		      int offset,
		final int length,
		final V[] samples,
		final int samplesOffset,
		final int samplesLength,
		final Equalator<? super V> equalator
	)
	{
		final int d;
		if((d = JadothArrays.validateArrayRange(samples, samplesOffset, samplesLength)) == 0)
		{
			return 0;
		}

		final EN first;
		if((first = this.getRangeChainEntry(offset, length)) == null)
		{
			return 0;
		}

		final int elementsBound = samplesOffset + samplesLength;
		final int bound = offset + length;
		int removeCount = 0;
		final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent = this.parent;
		if(length > 0)
		{
			for(EN e = first; offset != bound; e = e.next, offset++)
			{
				final V element = e.value();
				for(int i = samplesOffset; i != elementsBound; i += d)
				{
					if(equalator.equal(element, samples[i]))
					{
						e.removeFrom(parent);
						removeCount++;
						break;
					}
				}
			}
		}
		else
		{
			for(EN e = first; offset != bound; e = e.prev, offset++)
			{
				final V element = e.value();
				for(int i = samplesOffset; i != elementsBound; i += d)
				{
					if(equalator.equal(element, samples[i]))
					{
						e.removeFrom(parent);
						removeCount++;
						break;
					}
				}
			}
		}
		return removeCount;
	}

	// removing - multiple all collection //

	@SuppressWarnings("unchecked")
	@Override
	public final int valuesRemoveAll(final XGettingCollection<? extends V> elements)
	{
		if(elements instanceof AbstractSimpleArrayCollection<?>)
		{
			return this.valuesRngRemoveAll(0, Jadoth.to_int(this.parent.size()),
				(V[])AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)elements), 0, Jadoth.to_int(elements.size())
			);
		}

		return elements.iterate(new Consumer<V>()
		{
			int removeCount;

			@Override
			public void accept(final V e)
			{
				this.removeCount += ChainStrongStrongStorage.this.valuesRemove(e);
			}

		}).removeCount;
	}

	@Override
	public final int valuesRemoveAll(final XGettingCollection<? extends V> samples, final Equalator<? super V> equalator)
	{
		if(samples instanceof AbstractSimpleArrayCollection<?>)
		{
			return this.valuesRngRemoveAll(
				0,
				Jadoth.to_int(this.parent.size()),
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)samples),
				0,
				Jadoth.to_int(samples.size()),
				equalator
			);
		}

		return samples.iterate(new Consumer<V>()
		{
			int removeCount;

			@Override
			public void accept(final V e)
			{
				this.removeCount += ChainStrongStrongStorage.this.valuesRemove(e, equalator);
			}

		}).removeCount;
	}

	@SuppressWarnings("unchecked")
	@Override
	public final int valuesRngRemoveAll(final int offset, final int length, final XGettingCollection<? extends V> elements)
	{
		if(elements instanceof AbstractSimpleArrayCollection<?>)
		{
			return this.valuesRngRemoveAll(offset, length,
				(V[])AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)elements), 0, Jadoth.to_int(elements.size())
			);
		}

		return elements.iterate(new Consumer<V>()
		{
			int removeCount;

			@Override
			public void accept(final V e)
			{
				this.removeCount += ChainStrongStrongStorage.this.valuesRngRemove(offset, length, e);
			}

		}).removeCount;
	}

	@Override
	public final int valuesRngRemoveAll(
		final int offset,
		final int length,
		final XGettingCollection<? extends V> samples,
		final Equalator<? super V> equalator
	)
	{
		if(samples instanceof AbstractSimpleArrayCollection<?>)
		{
			return this.valuesRngRemoveAll(
				offset,
				length,
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)samples),
				0,
				Jadoth.to_int(samples.size()),
				equalator
			);
		}

		return samples.iterate(new Consumer<V>()
		{
			int removeCount;

			@Override
			public void accept(final V e)
			{
				this.removeCount += ChainStrongStrongStorage.this.valuesRngRemove(offset, length, e, equalator);
			}

		}).removeCount;
	}

	// removing - duplicates //

	@Override
	public final int valuesRemoveDuplicates()
	{
		final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent = this.parent;

		int removeCount = 0;
		for(EN e = this.head.next; e != null; e = e.next)
		{
			final V element = e.value();
			for(EN lookAhead = e.next; lookAhead != null; lookAhead = lookAhead.next)
			{
				if(element == lookAhead.value())
				{
					lookAhead.removeFrom(parent);
					removeCount++;
				}
			}
		}
		return removeCount;
	}

	@Override
	public final int valuesRemoveDuplicates(final Equalator<? super V> equalator)
	{
		final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent = this.parent;

		int removeCount = 0;
		for(EN e = this.head.next; e != null; e = e.next)
		{
			final V element = e.value();
			for(EN lookAhead = e.next; lookAhead != null; lookAhead = lookAhead.next)
			{
				if(equalator.equal(element, lookAhead.value()))
				{
					lookAhead.removeFrom(parent);
					removeCount++;
				}
			}
		}
		return removeCount;
	}

	@Override
	public final int valuesRngRemoveDuplicates(final int offset, int length)
	{
		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return 0;
		}

		final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent = this.parent;

		// hopping direction
		final AbstractChainEntry.Hopper ch = length < 0 ? HOP_PREV : HOP_NEXT;
		if(length < 0)
		{
			length = -length;
		}

		int removeCount = 0;
		for(; length > 0; e = ch.hop(e), length--)
		{
			final V element = e.value();
			for(EN lookAhead = ch.hop(e); lookAhead != null; lookAhead = ch.hop(lookAhead))
			{
				if(element == lookAhead.value())
				{
					lookAhead.removeFrom(parent);
					removeCount++;
				}
			}
		}
		return removeCount;
	}

	@Override
	public final int valuesRngRemoveDuplicates(final int offset, int length, final Equalator<? super V> equalator)
	{
		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return 0;
		}

		final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent = this.parent;

		// hopping direction
		final AbstractChainEntry.Hopper ch = length < 0 ? HOP_PREV : HOP_NEXT;
		if(length < 0)
		{
			length = -length;
		}

		int removeCount = 0;
		for(; length > 0; e = ch.hop(e), length--)
		{
			final V element = e.value();
			for(EN lookAhead = ch.hop(e); lookAhead != null; lookAhead = ch.hop(lookAhead))
			{
				if(equalator.equal(element, lookAhead.value()))
				{
					lookAhead.removeFrom(parent);
					removeCount++;
				}
			}
		}
		return removeCount;
	}



	///////////////////////////////////////////////////////////////////////////
	//    reducing      //
	/////////////////////

	// reducing - predicate //

	@Override
	public final int valuesReduce(final Predicate<? super V> predicate)
	{
		int removeCount = 0;
		try
		{
			final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent = this.parent;
			for(EN e = this.head.next; e != null; e = e.next)
			{
				if(predicate.test(e.value()))
				{
					e.removeFrom(parent);
					removeCount++;
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return removeCount;
	}

	@Override
	public final int valuesRngReduce(int offset, final int length, final Predicate<? super V> predicate)
	{
		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return 0;
		}

		int removeCount = 0;
		try
		{
			final int bound = offset + length;
			final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent = this.parent;
			if(length > 0)
			{
				for(; offset != bound; e = e.next, offset++)
				{
					if(predicate.test(e.value()))
					{
						e.removeFrom(parent);
						removeCount++;
					}
				}
			}
			else
			{
				for(; offset != bound; e = e.prev, offset--)
				{
					if(predicate.test(e.value()))
					{
						e.removeFrom(parent);
						removeCount++;
					}
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return removeCount;
	}



	///////////////////////////////////////////////////////////////////////////
	//    retaining     //
	/////////////////////

	// retaining - array //

	@Override
	public final int valuesRetainAll(final V[] elements, final int elementsOffset, final int elementsLength)
	{
		final int d;
		if((d = ChainStorageStrong.validateArrayIteration(elements, elementsOffset, elementsLength)) == 0)
		{
			return 0;
		}

		final int elementsBound = elementsOffset + elementsLength;
		final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent = this.parent;

		int removeCount = 0;
		main:
		for(EN e = this.head.next; e != null; e = e.next)
		{
			final V element = e.value();
			for(int i = elementsOffset; i != elementsBound; i += d)
			{
				if(element == elements[i])
				{
					continue main;
				}
			}
			e.removeFrom(parent);
			removeCount++;
		}
		return removeCount;
	}

	@Override
	public final int valuesRetainAll(
		final V[] samples,
		final int samplesOffset,
		final int samplesLength,
		final Equalator<? super V> equalator
	)
	{
		final int d;
		if((d = ChainStorageStrong.validateArrayIteration(samples, samplesOffset, samplesLength)) == 0)
		{
			return 0;
		}

		final int samplesBound = samplesOffset + samplesLength;
		final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent = this.parent;

		int removeCount = 0;
		main:
		for(EN e = this.head.next; e != null; e = e.next)
		{
			final V element = e.value();
			for(int i = samplesOffset; i != samplesBound; i += d)
			{
				if(equalator.equal(element, samples[i]))
				{
					continue main;
				}
			}
			e.removeFrom(parent);
			removeCount++;
		}
		return removeCount;
	}

	@Override
	public final int valuesRngRetainAll(
		      int offset,
		final int length,
		final V[] elements,
		final int elementsOffset,
		final int elementsLength
	)
	{
		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return 0;
		}

		final int d;
		if((d = ChainStorageStrong.validateArrayIteration(elements, elementsOffset, elementsLength)) == 0)
		{
			return 0;
		}

		final int bound = offset + length;
		final int elementsBound = elementsOffset + elementsLength;
		final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent = this.parent;

		int removeCount = 0;
		if(length > 0)
		{
			main:
			for(; offset != bound; e = e.next, offset++)
			{
				final V element = e.value();
				for(int i = elementsOffset; i != elementsBound; i += d)
				{
					if(element == elements[i])
					{
						continue main;
					}
				}
				e.removeFrom(parent);
				removeCount++;
			}
		}
		else
		{
			main:
			for(; offset != bound; e = e.prev, offset--)
			{
				final V element = e.value();
				for(int i = elementsOffset; i != elementsBound; i += d)
				{
					if(element == elements[i])
					{
						continue main;
					}
				}
				e.removeFrom(parent);
				removeCount++;
			}
		}
		return removeCount;
	}

	@Override
	public final int valuesRngRetainAll(
		      int offset,
		final int length,
		final V[] samples,
		final int samplesOffset,
		final int samplesLength,
		final Equalator<? super V> equalator
	)
	{
		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return 0;
		}

		final int d;
		if((d = ChainStorageStrong.validateArrayIteration(samples, samplesOffset, samplesLength)) == 0)
		{
			return 0;
		}

		final int bound = offset + length;
		final int samplesBound = samplesOffset + samplesLength;
		final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent = this.parent;

		int removeCount = 0;
		if(length > 0)
		{
			main:
			for(; offset != bound; e = e.next, offset++)
			{
				final V element = e.value();
				for(int i = samplesOffset; i != samplesBound; i += d)
				{
					if(equalator.equal(element, samples[i]))
					{
						continue main;
					}
				}
				e.removeFrom(parent);
				removeCount++;
			}
		}
		else
		{
			main:
			for(; offset != bound; e = e.prev, offset--)
			{
				final V element = e.value();
				for(int i = samplesOffset; i != samplesBound; i += d)
				{
					if(equalator.equal(element, samples[i]))
					{
						continue main;
					}
				}
				e.removeFrom(parent);
				removeCount++;
			}
		}
		return removeCount;
	}

	// retaining - collection //

	@SuppressWarnings("unchecked")
	@Override
	public final int valuesRetainAll(final XGettingCollection<? extends V> elements)
	{
		if(elements instanceof AbstractSimpleArrayCollection<?>)
		{
			// directly check array against array without predicate function or method calls
			return this.valuesRetainAll(
				(V[])AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)elements), 0, Jadoth.to_int(elements.size())
			);
		}

		final ElementIsContained<V> currentElement = new ElementIsContained<>();
		final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent = this.parent;
		int removeCount = 0;
		for(EN e = this.head.next; e != null; e = e.next)
		{
			currentElement.element = e.value();
			if(!elements.containsSearched(currentElement))
			{
				e.removeFrom(parent);
				removeCount++;
			}
		}
		return removeCount;
	}

	@Override
	public final int valuesRetainAll(final XGettingCollection<? extends V> samples, final Equalator<? super V> equalator)
	{
		if(samples instanceof AbstractSimpleArrayCollection<?>)
		{
			// directly check array against array without predicate function or method calls
			return this.valuesRetainAll(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)samples),
				0,
				Jadoth.to_int(samples.size()),
				equalator
			);
		}

		final CachedSampleEquality<V> equalCurrentElement = new CachedSampleEquality<>(equalator);
		final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent = this.parent;
		int removeCount = 0;
		for(EN e = this.head.next; e != null; e = e.next)
		{
			equalCurrentElement.sample = e.value();
			if(!samples.containsSearched(equalCurrentElement))
			{
				e.removeFrom(parent);
				removeCount++;
			}
		}
		return removeCount;
	}

	@SuppressWarnings("unchecked")
	@Override
	public final int valuesRngRetainAll(int offset, final int length, final XGettingCollection<? extends V> elements)
	{
		if(elements instanceof AbstractSimpleArrayCollection<?>)
		{
			// directly check array against array without predicate function or method calls
			return this.valuesRngRetainAll(offset, length,
				(V[])AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)elements), 0, Jadoth.to_int(elements.size())
			);
		}

		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return 0;
		}

		int removeCount = 0;
		final int bound = offset + length;
		final ElementIsContained<V> currentElement = new ElementIsContained<>();
		final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent = this.parent;
		if(length > 0)
		{
			for(; offset != bound; e = e.next, offset++)
			{
				currentElement.element = e.value();
				if(!elements.containsSearched(currentElement))
				{
					e.removeFrom(parent);
					removeCount++;
				}
			}
		}
		else
		{
			for(; offset != bound; e = e.prev, offset--)
			{
				currentElement.element = e.value();
				if(!elements.containsSearched(currentElement))
				{
					e.removeFrom(parent);
					removeCount++;
				}
			}
		}
		return removeCount;
	}

	@Override
	public final int valuesRngRetainAll(
		      int offset,
		final int length,
		final XGettingCollection<? extends V> samples,
		final Equalator<? super V> equalator
	)
	{
		if(samples instanceof AbstractSimpleArrayCollection<?>)
		{
			// directly check array against array without predicate function or method calls
			return this.valuesRngRetainAll(
				offset,
				length,
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)samples),
				0,
				Jadoth.to_int(samples.size()),
				equalator
			);
		}

		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return 0;
		}
		int removeCount = 0;
		final int bound = offset + length;
		final CachedSampleEquality<V> equalCurrentElement = new CachedSampleEquality<>(equalator);
		final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent = this.parent;
		if(length > 0)
		{
			for(; offset != bound; e = e.next, offset++)
			{
				equalCurrentElement.sample = e.value();
				if(!samples.containsSearched(equalCurrentElement))
				{
					e.removeFrom(parent);
					removeCount++;
				}
			}
		}
		else
		{
			for(; offset != bound; e = e.prev, offset--)
			{
				equalCurrentElement.sample = e.value();
				if(!samples.containsSearched(equalCurrentElement))
				{
					e.removeFrom(parent);
					removeCount++;
				}
			}
		}
		return removeCount;
	}



	///////////////////////////////////////////////////////////////////////////
	//    processing    //
	/////////////////////

	@Override
	public final int valuesProcess(final Consumer<? super V> procedure)
	{
		final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent = this.parent;
		int removeCount = 0;
		try
		{
			for(EN e = this.head.next; e != null; e = e.next)
			{
				procedure.accept(e.value());
				e.removeFrom(parent);
				removeCount++;
			}
		}
		catch(final ThrowBreak b)
		{
			removeCount += parent.internalClear();
		}
		return removeCount;
	}

	@Override
	public final int valuesRngProcess(int offset, final int length, final Consumer<? super V> procedure)
	{
		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return 0;
		}

		int removeCount = 0;
		final int bound = offset + length;
		final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent = this.parent;
		try
		{
			if(length > 0)
			{
				for(; offset != bound; e = e.next, offset++)
				{
					procedure.accept(e.value());
					e.removeFrom(parent);
					removeCount++;
				}
			}
			else
			{
				for(; offset != bound; e = e.prev, offset--)
				{
					procedure.accept(e.value());
					e.removeFrom(parent);
					removeCount++;
				}
			}
		}
		catch(final ThrowBreak b)
		{
			removeCount += parent.internalClear();
		}
		return removeCount;
	}



	///////////////////////////////////////////////////////////////////////////
	//     moving       //
	/////////////////////

	@Override
	public final int valuesMoveRange(int offset, final int length, final Consumer<? super V> target)
	{
		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return 0;
		}

		final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent = this.parent;
		final int bound = offset + length;

		if(length > 0)
		{
			for(; offset != bound; e = e.next, offset++)
			{
				target.accept(e.value());
				e.removeFrom(parent);
			}
		}
		else
		{
			for(; offset != bound; e = e.prev, offset--)
			{
				target.accept(e.value());
				e.removeFrom(parent);
			}
		}
		return length < 0 ? -length : length;
	}

	@Override
	public final int valuesMoveSelection(final Consumer<? super V> target, final long... indices)
	{
		final int indicesLength = indices.length, size = Jadoth.to_int(this.parent.size());

		// valuesIdate all indices before copying the first element
		for(int i = 0; i < indicesLength; i++)
		{
			if(indices[i] < 0 || indices[i] >= size)
			{
				throw new IndexOutOfBoundsException(exceptionIndexOutOfBounds(size, indices[i]));
			}
		}

		final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent = this.parent;

		// actual copying. Note: can't sort indices as copying order might be relevant
		EN e;
		for(int i = 0; i < indicesLength; i++)
		{
			target.accept((e = this.getChainEntry(indices[i])).value()); // pretty inefficient scrolling here
			e.removeFrom(parent); // remove not until adding to target has been successful
		}

		return indicesLength; // removeCount is equal to index count if no exception occured
	}

	// moving - conditional //

	@Override
	public final int valuesMoveTo(final Consumer<? super V> target, final Predicate<? super V> predicate)
	{
		int removeCount = 0;
		try
		{
			final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent = this.parent;
			for(EN e = this.head.next; e != null; e = e.next)
			{
				final V element;
				if(predicate.test(element = e.value()))
				{
					target.accept(element);
					e.removeFrom(parent);
					removeCount++;
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return removeCount;
	}

	@Override
	public final int valuesRngMoveTo(
		      int offset,
		final int length,
		final Consumer<? super V> target,
		final Predicate<? super V> predicate
	)
	{
		final int bound = offset + length;
		int removeCount = 0;
		final EN first;
		if((first = this.getRangeChainEntry(offset, length)) == null)
		{
			return 0;
		}
		try
		{
			final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent = this.parent;
			if(length > 0)
			{
				for(EN e = first; offset != bound; e = e.next, offset++)
				{
					if(predicate.test(e.value()))
					{
						target.accept(e.value());
						e.removeFrom(parent);
						removeCount++;
					}
				}
			}
			else
			{
				for(EN e = first; offset != bound; e = e.prev, offset--)
				{
					if(predicate.test(e.value()))
					{
						target.accept(e.value());
						e.removeFrom(parent);
						removeCount++;
					}
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return removeCount;
	}



	///////////////////////////////////////////////////////////////////////////
	//     sorting       //
	//////////////////////

	@Override
	public final void valuesSort(final Comparator<? super V> comparator)
	{
		// valuesIdate comparator before the chain gets splitted
		if(comparator == null)
		{
			throw new NullPointerException();
		}
		if(Jadoth.to_int(this.parent.size()) <= 1)
		{
			// empty or trivial chain is always sorted
			return;
		}
		valuesMergesortHead(this.head, comparator);
	}

	@Override
	public final void valuesRngSort(final int offset, int length, final Comparator<? super V> comparator)
	{
		// valuesIdate comparator before the chain gets splitted
		if(comparator == null)
		{
			throw new NullPointerException();
		}

		final EN preFirst;
		if(length <= 1 && length >= -1)
		{
			// empty or trivial subchain is always sorted
			return;
		}
		else if(length > 0)
		{
			preFirst = this.getRangeChainEntry(offset -      1,  length + 1);
		}
		else
		{
			preFirst = this.getRangeChainEntry(offset - length, -length + 1);
			length = -length;
		}

		EN postLast = preFirst.next;
		while(length-- > 0)
		{
			postLast = postLast.next;
		}

		valuesMergesortRange(preFirst, postLast, this.head, comparator);
	}

	@Override
	public final boolean valuesIsSorted(final Comparator<? super V> comparator)
	{
		EN e;
		if((e = this.head.next) == null)
		{
			return true; // empty chain is sorted
		}

		V loopLastElement = e.value();
		while((e = e.next) != null)
		{
			final V element;
			if(comparator.compare(loopLastElement, element = e.value()) > 0)
			{
				return false;
			}
			loopLastElement = element;
		}
		return true;
	}

	@Override
	public final boolean valuesRngIsSorted(final int offset, int length, final Comparator<? super V> comparator)
	{
		EN e = this.getRangeChainEntry(offset, length);
		V loopLastElement = e.value();
		if(length > 0)
		{
			for(; length-- > 0; e = e.next)
			{
				final V element;
				if(comparator.compare(loopLastElement, element = e.value()) > 0)
				{
					return false;
				}
				loopLastElement = element;
			}
		}
		else
		{
			for(; length++ < 0; e = e.prev)
			{
				final V element;
				if(comparator.compare(loopLastElement, element = e.value()) > 0)
				{
					return false;
				}
				loopLastElement = element;
			}
		}
		return true;
	}



	///////////////////////////////////////////////////////////////////////////
	//     setting      //
	/////////////////////

	@Override
	public final V valuesSet(final long offset, final V value)
	{
		return this.getChainEntry(offset).setValue(value);
	}

	@Override
	public final void valuesSet(final long offset, final V[] elements)
	{
		EN e = this.getRangeChainEntry(offset, elements.length);
		for(int i = 0; i < elements.length; i++)
		{
			e.setValue(elements[i]);
			e = e.next;
		}
	}

	@Override
	public final void valuesSet(final long offset, final V[] elements, final int elementsOffset, final int elementsLength)
	{
		EN e = this.getRangeChainEntry(offset, elementsLength);
		final int d = JadothArrays.validateArrayRange(elements, elementsOffset, elementsLength);
		for(int i = elementsOffset, bound = elementsOffset + elementsLength; i != bound; i += d)
		{
			e.setValue(elements[i]);
			e = e.next;
		}
	}

	@Override
	public final void valuesFill(long offset, final long length, final V element)
	{
		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return;
		}

		final long bound = offset + length;
		if(length > 0)
		{
			for(; offset != bound; e = e.next, offset++)
			{
				e.setValue(element);
			}
		}
		else
		{
			for(; offset != bound; e = e.prev, offset--)
			{
				e.setValue(element);
			}
		}
	}



	///////////////////////////////////////////////////////////////////////////
	//    replacing     //
	/////////////////////

	// replacing - one single //

	@Override
	public final boolean valuesReplaceOne(final V element, final V replacement)
	{
		for(EN e = this.head.next; e != null; e = e.next)
		{
			if(e.value() == element)
			{
				e.setValue(replacement);
				return true;
			}
		}
		return false;
	}

	@Override
	public final boolean valuesReplaceOne(final V sample, final Equalator<? super V> equalator, final V replacement)
	{
		for(EN e = this.head.next; e != null; e = e.next)
		{
			if(equalator.equal(e.value(), sample))
			{
				e.setValue(replacement);
				return true;
			}
		}
		return false;
	}

	@Override
	public final int valuesRngReplaceOne(int offset, final int length, final V element, final V replacement)
	{
		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return -1;
		}

		final int bound = offset + length;
		if(length > 0)
		{
			for(; offset != bound; e = e.next, offset++)
			{
				if(e.value() == element)
				{
					e.setValue(replacement);
					return offset;
				}
			}
		}
		else
		{
			for(; offset != bound; e = e.prev, offset--)
			{
				if(e.value() == element)
				{
					e.setValue(replacement);
					return offset;
				}
			}
		}
		return -1;
	}

	@Override
	public final int valuesRngReplaceOne(
		      int offset,
		final int length,
		final V sample,
		final Equalator<? super V> equalator,
		final V replacement
	)
	{
		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return -1;
		}

		final int bound = offset + length;
		if(length > 0)
		{
			for(; offset != bound; e = e.next, offset++)
			{
				if(equalator.equal(e.value(), sample))
				{
					e.setValue(replacement);
					return offset;
				}
			}
		}
		else
		{
			for(; offset != bound; e = e.prev, offset--)
			{
				if(equalator.equal(e.value(), sample))
				{
					e.setValue(replacement);
					return offset;
				}
			}
		}
		return -1;
	}

	// replacing - multiple single //

	@Override
	public final int valuesReplace(final V element, final V replacement)
	{
		int replaceCount = 0;
		for(EN e = this.head.next; e != null; e = e.next)
		{
			if(e.value() == element)
			{
				e.setValue(replacement);
				replaceCount++;
			}
		}
		return replaceCount;
	}

	@Override
	public final int valuesReplace(final V sample, final Equalator<? super V> equalator, final V replacement)
	{
		int replaceCount = 0;
		for(EN e = this.head.next; e != null; e = e.next)
		{
			if(equalator.equal(e.value(), sample))
			{
				e.setValue(replacement);
				replaceCount++;
			}
		}
		return replaceCount;
	}

	@Override
	public final int valuesRngReplace(final int offset, int length, final V element, final V replacement)
	{
		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return 0;
		}

		int replaceCount = 0;
		if(length > 0)
		{
			for(; length-- > 0; e = e.next)
			{
				if(e.value() == element)
				{
					e.setValue(replacement);
					replaceCount++;
				}
			}
		}
		else
		{
			for(; length++ < 0; e = e.prev)
			{
				if(e.value() == element)
				{
					e.setValue(replacement);
					replaceCount++;
				}
			}
		}
		return replaceCount;
	}

	@Override
	public final int valuesRngReplace(
		final int offset,
		      int length,
		final V sample,
		final Equalator<? super V> equalator,
		final V replacement
	)
	{
		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return 0;
		}

		int replaceCount = 0;
		if(length > 0)
		{
			for(; length-- > 0; e = e.next)
			{
				if(equalator.equal(e.value(), sample))
				{
					e.setValue(replacement);
					replaceCount++;
				}
			}
		}
		else
		{
			for(; length++ < 0; e = e.prev)
			{
				if(equalator.equal(e.value(), sample))
				{
					e.setValue(replacement);
					replaceCount++;
				}
			}
		}
		return replaceCount;
	}

	// replacing - multiple all array //

	@Override
	public final int valuesReplaceAll(final V[] elements, final int elementsOffset, final int elementsLength, final V replacement)
	{
		final int d;
		if((d = ChainStorageStrong.validateArrayIteration(elements, elementsOffset, elementsLength)) == 0)
		{
			return 0;
		}

		final int elementsBound = elementsOffset + elementsLength;

		int replaceCount = 0;
		for(EN e = this.head.next; e != null; e = e.next)
		{
			final V element = e.value();
			for(int i = elementsOffset; i != elementsBound; i += d)
			{
				if(element == elements[i])
				{
					e.setValue(replacement);
					replaceCount++;
					break;
				}
			}
		}
		return replaceCount;
	}

	@Override
	public final int valuesReplaceAll(
		final V[] samples,
		final int samplesOffset,
		final int samplesLength,
		final Equalator<? super V> equalator,
		final V replacement
	)
	{
		final int d;
		if((d = ChainStorageStrong.validateArrayIteration(samples, samplesOffset, samplesLength)) == 0)
		{
			return 0;
		}

		final int elementsBound = samplesOffset + samplesLength;

		int replaceCount = 0;
		for(EN e = this.head.next; e != null; e = e.next)
		{
			final V element = e.value();
			for(int i = samplesOffset; i != elementsBound; i += d)
			{
				if(equalator.equal(element, samples[i]))
				{
					e.setValue(replacement);
					replaceCount++;
					break;
				}
			}
		}
		return replaceCount;
	}

	@Override
	public final int valuesRngReplaceAll(
		      int offset,
		final int length,
		final V[] elements,
		final int elementsOffset,
		final int elementsLength,
		final V replacement
	)
	{
		final int d;
		if((d = JadothArrays.validateArrayRange(elements, elementsOffset, elementsLength)) == 0)
		{
			return 0;
		}

		final EN first;
		if((first = this.getRangeChainEntry(offset, length)) == null)
		{
			return 0;
		}

		final int elementsBound = elementsOffset + elementsLength;
		final int bound = offset + length;
		int replaceCount = 0;
		if(length > 0)
		{
			for(EN e = first; offset != bound; e = e.next, offset++)
			{
				for(int i = elementsOffset; i != elementsBound; i += d)
				{
					if(e.value() == elements[i])
					{
						e.setValue(replacement);
						replaceCount++;
						break;
					}
				}
			}
		}
		else
		{
			for(EN e = first; offset != bound; e = e.prev, offset++)
			{
				for(int i = elementsOffset; i != elementsBound; i += d)
				{
					if(e.value() == elements[i])
					{
						e.setValue(replacement);
						replaceCount++;
						break;
					}
				}
			}
		}
		return replaceCount;
	}

	@Override
	public final int valuesRngReplaceAll(
		      int offset,
		final int length,
		final V[] samples,
		final int samplesOffset,
		final int samplesLength,
		final Equalator<? super V> equalator,
		final V replacement
	)
	{
		final int d;
		if((d = JadothArrays.validateArrayRange(samples, samplesOffset, samplesLength)) == 0)
		{
			return 0;
		}

		final EN first;
		if((first = this.getRangeChainEntry(offset, length)) == null)
		{
			return 0;
		}

		final int elementsBound = samplesOffset + samplesLength;
		final int bound = offset + length;
		int replaceCount = 0;
		if(length > 0)
		{
			for(EN e = first; offset != bound; e = e.next, offset++)
			{
				final V element = e.value();
				for(int i = samplesOffset; i != elementsBound; i += d)
				{
					if(equalator.equal(element, samples[i]))
					{
						e.setValue(replacement);
						replaceCount++;
						break;
					}
				}
			}
		}
		else
		{
			for(EN e = first; offset != bound; e = e.prev, offset++)
			{
				final V element = e.value();
				for(int i = samplesOffset; i != elementsBound; i += d)
				{
					if(equalator.equal(element, samples[i]))
					{
						e.setValue(replacement);
						replaceCount++;
						break;
					}
				}
			}
		}
		return replaceCount;
	}

	// replacing - multiple all collection //

	@SuppressWarnings("unchecked")
	@Override
	public final int valuesReplaceAll(final XGettingCollection<? extends V> elements, final V replacement)
	{
		if(elements instanceof AbstractSimpleArrayCollection<?>)
		{
			return this.valuesRngReplaceAll(0, Jadoth.to_int(this.parent.size()),
				(V[])AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)elements), 0, Jadoth.to_int(elements.size()),
				replacement
			);
		}

		return elements.iterate(new Consumer<V>()
		{
			int replaceCount;

			@Override
			public void accept(final V e)
			{
				this.replaceCount += ChainStrongStrongStorage.this.valuesReplace(e, replacement);
			}

		}).replaceCount;
	}

	@Override
	public final int valuesReplaceAll(
		final XGettingCollection<? extends V> samples,
		final Equalator<? super V> equalator,
		final V replacement
	)
	{
		if(samples instanceof AbstractSimpleArrayCollection<?>)
		{
			return this.valuesRngReplaceAll(
				0,
				Jadoth.to_int(this.parent.size()),
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)samples),
				0,
				Jadoth.to_int(samples.size()),
				equalator, replacement
			);
		}

		return samples.iterate(new Consumer<V>()
		{
			int replaceCount;

			@Override
			public void accept(final V e)
			{
				this.replaceCount += ChainStrongStrongStorage.this.valuesReplace(e, equalator, replacement);
			}

		}).replaceCount;
	}

	@SuppressWarnings("unchecked")
	@Override
	public final int valuesRngReplaceAll(
		final int offset,
		final int length,
		final XGettingCollection<? extends V> elements,
		final V replacement
	)
	{
		if(elements instanceof AbstractSimpleArrayCollection<?>)
		{
			return this.valuesRngReplaceAll(offset, length,
				(V[])AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)elements), 0, Jadoth.to_int(elements.size()),
				replacement
			);
		}

		return elements.iterate(new Consumer<V>()
		{
			int replaceCount;

			@Override
			public void accept(final V e)
			{
				this.replaceCount += ChainStrongStrongStorage.this.valuesRngReplace(offset, length, e, replacement);
			}

		}).replaceCount;
	}

	@Override
	public final int valuesRngReplaceAll(
		final int offset,
		final int length,
		final XGettingCollection<? extends V> samples,
		final Equalator<? super V> equalator,
		final V replacement
	)
	{
		if(samples instanceof AbstractSimpleArrayCollection<?>)
		{
			return this.valuesRngReplaceAll(
				offset,
				length,
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)samples),
				0,
				Jadoth.to_int(samples.size()),
				equalator, replacement
			);
		}

		return samples.iterate(new Consumer<V>()
		{
			int replaceCount;

			@Override
			public void accept(final V e)
			{
				this.replaceCount += ChainStrongStrongStorage.this.valuesRngReplace(offset, length, e, equalator, replacement);
			}

		}).replaceCount;
	}



	///////////////////////////////////////////////////////////////////////////
	//  substituting    //
	/////////////////////

	@Override
	public final boolean valuesSubstituteOne(final Predicate<? super V> predicate, final V substitute)
	{
		try
		{
			for(EN e = this.head.next; e != null; e = e.next)
			{
				if(predicate.test(e.value()))
				{
					e.setValue(substitute);
					return true;
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return false;
	}

	@Override
	public final int valuesRngSubstituteOne(
		      int offset,
		final int length,
		final Predicate<? super V> predicate,
		final V substitute
	)
	{
		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return -1;
		}
		try
		{
			final int bound = offset + length;
			if(length > 0)
			{
				for(; offset != bound; e = e.next, offset++)
				{
					if(predicate.test(e.value()))
					{
						e.setValue(substitute);
						return offset;
					}
				}
			}
			else
			{
				for(; offset != bound; e = e.prev, offset--)
				{
					if(predicate.test(e.value()))
					{
						e.setValue(substitute);
						return offset;
					}
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return -1;
	}

	// substituting - multiple //

	@Override
	public final int valuesSubstitute(final Predicate<? super V> predicate, final V substitute)
	{
		int replaceCount = 0;
		try
		{
			for(EN e = this.head.next; e != null; e = e.next)
			{
				if(predicate.test(e.value()))
				{
					e.setValue(substitute);
					replaceCount++;
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return replaceCount;
	}

	@Override
	public final int valuesRngSubstitute(final int offset, int length, final Predicate<? super V> predicate, final V substitute)
	{
		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return 0;
		}

		int replaceCount = 0;
		try
		{
			if(length > 0)
			{
				for(; length-- > 0; e = e.next)
				{
					if(predicate.test(e.value()))
					{
						e.setValue(substitute);
						replaceCount++;
					}
				}
			}
			else
			{
				for(; length++ < 0; e = e.prev)
				{
					if(predicate.test(e.value()))
					{
						e.setValue(substitute);
						replaceCount++;
					}
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return replaceCount;
	}

	// replacing - mapped //

	@Override
	public final int valuesSubstitute(final Function<? super V, ? extends V> mapper)
	{
		int replaceCount = 0;
		try
		{
			for(EN e = this.head.next; e != null; e = e.next)
			{
				final V replacement;
				if((replacement = mapper.apply(e.value())) != e.value())
				{
					e.setValue(replacement);
					replaceCount++;
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return replaceCount;
	}

	@Override
	public final int valuesSubstitute(final Predicate<? super V> predicate, final Function<V, V> mapper)
	{
		int replaceCount = 0;
		try
		{
			for(EN e = this.head.next; e != null; e = e.next)
			{
				if(predicate.test(e.value()))
				{
					e.setValue(mapper.apply(e.value()));
					replaceCount++;
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return replaceCount;
	}

	@Override
	public final int valuesRngSubstitute(final int offset, int length, final Function<V, V> mapper)
	{
		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return 0;
		}

		int replaceCount = 0;
		V replacement;
		if(length > 0)
		{
			for(; length-- > 0; e = e.next)
			{
				if((replacement = mapper.apply(e.value())) != e.value())
				{
					e.setValue(replacement);
					replaceCount++;
				}
			}
		}
		else
		{
			for(; length++ < 0; e = e.prev)
			{
				if((replacement = mapper.apply(e.value())) != e.value())
				{
					e.setValue(replacement);
					replaceCount++;
				}
			}
		}
		return replaceCount;
	}

	@Override
	public final int valuesRngSubstitute(final int offset, int length, final Predicate<? super V> predicate, final Function<V, V> mapper)
	{
		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return 0;
		}

		int replaceCount = 0;
		try
		{
			if(length > 0)
			{
				for(; length-- > 0; e = e.next)
				{
					if(predicate.test(e.value()))
					{
						e.setValue(mapper.apply(e.value()));
						replaceCount++;
					}
				}
			}
			else
			{
				for(; length++ < 0; e = e.prev)
				{
					if(predicate.test(e.value()))
					{
						e.setValue(mapper.apply(e.value()));
						replaceCount++;
					}
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return replaceCount;
	}

	@Override
	public final boolean valuesRemoveOne(final V element)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ChainKeyValueStorage<K,V,EN>#valuesRemoveOne()
	}

	@Override
	public final boolean valuesRemoveOne(final V sample, final Equalator<? super V> equalator)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ChainKeyValueStorage<K,V,EN>#valuesRemoveOne()
	}

	@Override
	public final boolean valuesRngRemoveOne(final int offset, final int length, final V value)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ChainKeyValueStorage<K,V,EN>#valuesRngRemoveOne()
	}

	@Override
	public final boolean valuesRngRemoveOne(final int offset, final int length, final V sample, final Equalator<? super V> equalator)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ChainKeyValueStorage<K,V,EN>#valuesRngRemoveOne()
	}

	// CHECKSTYLE.ON: FinalParameter
}
