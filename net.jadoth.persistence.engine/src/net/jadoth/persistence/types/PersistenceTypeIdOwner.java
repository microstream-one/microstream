package net.jadoth.persistence.types;

import net.jadoth.collections.XUtilsCollection;
import net.jadoth.collections.types.XSortableSequence;

public interface PersistenceTypeIdOwner
{
	public long typeId();



	public static int orderAscending(final PersistenceTypeIdOwner o1, final PersistenceTypeIdOwner o2)
	{
		return o2.typeId() >= o1.typeId() ? o2.typeId() > o1.typeId() ? -1 : 0 : +1;
	}


	public static <E extends PersistenceTypeIdOwner, C extends XSortableSequence<E>>
	C sortByTypeIdAscending(final C elements)
	{
		return XUtilsCollection.valueSort(elements, PersistenceTypeIdOwner::orderAscending);
	}

}
