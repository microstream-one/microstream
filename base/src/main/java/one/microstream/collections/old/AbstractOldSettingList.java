package one.microstream.collections.old;

import one.microstream.collections.types.XSettingList;

public abstract class AbstractOldSettingList<E> extends AbstractOldGettingList<E>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	protected AbstractOldSettingList(final XSettingList<E> list)
	{
		super(list);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public XSettingList<E> parent()
	{
		return (XSettingList<E>)this.subject;
	}

	@Override
	public E set(final int index, final E element)
	{
		return ((XSettingList<E>)this.subject).setGet(index, element);
	}

	@Override
	public AbstractOldSettingList<E> subList(final int fromIndex, final int toIndex)
	{
		/* XSettingList implementations always create a SubList instance whose implementation creates an
		 * OldSettingList bridge instance, so this cast is safe (and inevitable). Savvy :)?
		 */
		return (AbstractOldSettingList<E>)(((XSettingList<E>)this.subject).range(fromIndex, toIndex).old());
	}

}
