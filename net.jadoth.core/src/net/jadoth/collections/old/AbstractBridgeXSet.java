package net.jadoth.collections.old;

import java.util.Collection;

import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.collections.types.XSet;
import net.jadoth.functional.XFunc;
import net.jadoth.typing.XTypes;

public abstract class AbstractBridgeXSet<E> extends AbstractOldGettingSet<E>
{
	protected AbstractBridgeXSet(final XSet<E> set)
	{
		super(set);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public XSet<E> parent()
	{
		return (XSet<E>)super.parent();
	}

	@Override
	public boolean add(final E e)
	{
		return ((XSet<E>)this.subject).add(e);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean addAll(final Collection<? extends E> c)
	{
		if(c instanceof XGettingCollection<?>)
		{
			((XSet<E>)this.subject).addAll((XGettingCollection<? extends E>)c);
			return true;
		}

		final XSet<E> list = (XSet<E>)this.subject;
		for(final E e : c)
		{
			list.add(e);
		}
		return true;
	}

	@Override
	public void clear()
	{
		((XSet<E>)this.subject).clear();
	}

//	@SuppressWarnings("unchecked")
//	@Override
//	public boolean remove(final Object o)
//	{
//		return ((XSet<E>)this.subject).remove((E)o, Jadoth.equals) > 0;
//	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean removeAll(final Collection<?> c)
	{
		int removeCount = 0;
		final XSet<E> list = (XSet<E>)this.subject;

		// even xcollections have to be handled that way because of the missing type info (argh)
		for(final Object o : c)
		{
			removeCount += list.removeBy(XFunc.isEqualTo((E)o));
		}
		return removeCount > 0;
	}

	@Override
	public boolean retainAll(final Collection<?> c)
	{
		final int oldSize = XTypes.to_int(this.subject.size());
		((XSet<E>)this.subject).removeBy(e -> !c.contains(e));
		return oldSize - XTypes.to_int(this.subject.size()) > 0;
	}

}
