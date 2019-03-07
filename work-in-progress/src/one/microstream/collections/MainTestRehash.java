package one.microstream.collections;

import one.microstream.collections.EqHashEnum;

public class MainTestRehash
{
	public static void main(final String[] args)
	{
		final EqHashEnum<String> strings = EqHashEnum.New("A", "B", "C", "D");

		System.out.println(strings);
		strings.rehash();
		System.out.println(strings);
	}
}
