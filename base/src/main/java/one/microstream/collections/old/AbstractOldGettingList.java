package one.microstream.collections.old;

import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;

import one.microstream.X;
import one.microstream.collections.types.XGettingList;
import one.microstream.functional.XFunc;
import one.microstream.typing.XTypes;
import one.microstream.util.iterables.ReadOnlyListIterator;

public abstract class AbstractOldGettingList<E> implements OldList<E>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	final XGettingList<E> subject;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	protected AbstractOldGettingList(final XGettingList<E> list)
	{
		super();
		this.subject = list;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public XGettingList<E> parent()
	{
		return this.subject;
	}

	@Override
	public boolean add(final E e) throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(final int index, final E element) throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(final Collection<? extends E> c) throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(final int index, final Collection<? extends E> c) throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean contains(final Object o)
	{
		return this.subject.containsSearched(XFunc.isEqualTo((E)o));
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean containsAll(final Collection<?> c)
	{
		for(final Object o : c)
		{
			if(!this.subject.containsSearched(XFunc.isEqualTo((E)o)))
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public E get(final int index)
	{
		return this.subject.at(index);
	}

	@SuppressWarnings("unchecked")
	@Override
	public int indexOf(final Object o)
	{
		return X.checkArrayRange(this.subject.indexBy(XFunc.isEqualTo((E)o)));
	}

	@Override
	public boolean isEmpty()
	{
		return this.subject.isEmpty();
	}

	@Override
	public Iterator<E> iterator()
	{
		return this.subject.iterator();
	}

	@SuppressWarnings("unchecked")
	@Override
	public int lastIndexOf(final Object o)
	{
		return X.checkArrayRange(this.subject.lastIndexBy(XFunc.isEqualTo((E)o)));
	}

	@Override
	public ListIterator<E> listIterator()
	{
		return new ReadOnlyListIterator<>(this.subject);
	}

	@Override
	public ListIterator<E> listIterator(final int index)
	{
		return new ReadOnlyListIterator<>(this.subject, index);
	}

	@Override
	public boolean remove(final Object o) throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public E remove(final int index) throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(final Collection<?> c) throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(final Collection<?> c) throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public E set(final int index, final E element)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public int size()
	{
		return XTypes.to_int(this.subject.size());
	}

	@Override
	public AbstractOldGettingList<E> subList(final int fromIndex, final int toIndex)
	{
		/* XGettingList implementations always create a SubList instance whose implementation creates an
		 * OldGettingList bridge instance, so this cast is safe (and inevitable). Savvy :)?
		 */
		return (AbstractOldGettingList<E>)this.subject.range(fromIndex, toIndex).old();
	}

	@Override
	public Object[] toArray()
	{
		return this.subject.toArray();
	}

	@Override
	public String toString()
	{
		return this.subject.toString();
	}
}
