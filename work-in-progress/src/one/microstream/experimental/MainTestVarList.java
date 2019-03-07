package one.microstream.experimental;

import one.microstream.collections.VarList;

public class MainTestVarList
{
	static final int SIZE = 100;

	public static void main(final String[] args)
	{
		final VarList<Integer> list = VarList.New();

		for(int i = 0; i < SIZE; i++)
		{
			list.add(i);
		}

		System.out.println(list);

		System.out.println(list.at(0));
		System.out.println(list.at(31));
		System.out.println(list.at(32));
		System.out.println(list.at(90));
		System.out.println(list.at(SIZE - 1));
	}
}
