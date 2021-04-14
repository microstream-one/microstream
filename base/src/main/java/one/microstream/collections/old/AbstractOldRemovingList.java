package one.microstream.collections.old;

import java.util.Collection;

import one.microstream.collections.types.XList;
import one.microstream.collections.types.XProcessingList;
import one.microstream.collections.types.XSet;
import one.microstream.functional.XFunc;
import one.microstream.typing.XTypes;

public abstract class AbstractOldRemovingList<E> extends AbstractOldGettingList<E>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	protected AbstractOldRemovingList(final XProcessingList<E> list)
	{
		super(list);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public XProcessingList<E> parent()
	{
		return (XProcessingList<E>)this.subject;
	}

	@Override
	public void clear()
	{
		((XProcessingList<E>)this.subject).clear();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean remove(final Object o)
	{
		return ((XProcessingList<E>)this.subject).removeBy(XFunc.isEqualTo((E)o)) > 0;
	}

	@Override
	public E remove(final int index)
	{
		return ((XProcessingList<E>)this.subject).removeAt(index);
	}

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
		((XList<E>)this.subject).removeBy(e -> !c.contains(e));
		return oldSize - XTypes.to_int(this.subject.size()) > 0;
	}

	@Override
	public AbstractOldRemovingList<E> subList(final int fromIndex, final int toIndex)
	{
		/* XList implementations always create a SubList instance whose implementation creates an
		 * OldXList bridge instance, so this cast is safe (and inevitable). Savvy :)?
		 */
		return (AbstractOldRemovingList<E>)((XProcessingList<E>)this.subject).range(fromIndex, toIndex).old();
	}
}
