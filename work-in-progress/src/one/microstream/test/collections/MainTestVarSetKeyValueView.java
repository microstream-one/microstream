package one.microstream.test.collections;

import one.microstream.collections.HashEnum;

/**
 * @author Thomas Muenz
 *
 */
public class MainTestVarSetKeyValueView
{
	public static void main(final String[] args)
	{
		final HashEnum<String> strings = HashEnum.<String>New().addAll("A", "B", "C", "D");
		strings.iterate(System.out::println);

//		strings.keyValueView().execute(e -> System.out.println(e.key()));

	}

}
