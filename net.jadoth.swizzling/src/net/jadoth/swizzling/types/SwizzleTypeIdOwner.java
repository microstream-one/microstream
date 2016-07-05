package net.jadoth.swizzling.types;

import net.jadoth.collections.XUtilsCollection;
import net.jadoth.collections.types.XSortableSequence;

public interface SwizzleTypeIdOwner
{
	public long typeId();



	public static int orderAscending(final SwizzleTypeIdOwner o1, final SwizzleTypeIdOwner o2)
	{
		return o2.typeId() >= o1.typeId() ? o2.typeId() > o1.typeId() ? -1 : 0 : +1;
	}


	public static <E extends SwizzleTypeIdOwner, C extends XSortableSequence<E>>
	C sortByTypeIdAscending(final C elements)
	{
		return XUtilsCollection.valueSort(elements, SwizzleTypeIdOwner::orderAscending);
	}

}
