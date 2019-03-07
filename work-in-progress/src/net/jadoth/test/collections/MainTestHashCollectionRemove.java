package net.jadoth.test.collections;

import net.jadoth.collections.EqHashEnum;
import net.jadoth.typing.XTypes;

public class MainTestHashCollectionRemove
{
	public static void main(final String[] args)
	{
		final EqHashEnum<Double> ints = EqHashEnum.New();
		ints.add(1.0);
		ints.add(2.0);
		ints.add(3.0);
		ints.add(4.0);
		System.out.println(XTypes.to_int(ints.size())+": "+ints);

		ints.remove(1.0);
		ints.remove(2.0);
		ints.remove(3.0);
		ints.remove(4.0);
		System.out.println(XTypes.to_int(ints.size())+": "+ints);

		ints.add(1.0);
		ints.add(2.0);
		ints.add(3.0);
		ints.add(4.0);
		System.out.println(XTypes.to_int(ints.size())+": "+ints);
	}

}
