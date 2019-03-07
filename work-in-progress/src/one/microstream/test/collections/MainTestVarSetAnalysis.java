package one.microstream.test.collections;

import one.microstream.collections.HashEnum;
import one.microstream.collections.interfaces.HashCollection;

public class MainTestVarSetAnalysis
{
	static final int SIZE = 100;

	public static void main(final String[] args)
	{
		final HashEnum<String> strings = HashEnum.NewCustom(16, 5f);
		for(int i = 0; i < SIZE; i++)
		{
			strings.add(Integer.toString(i));
		}

		final HashCollection.Analysis<? extends HashEnum<String>> analysis = strings.analyze();
		System.out.println(analysis);
		System.out.println();
	}

}
