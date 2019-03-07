package one.microstream.util;

import java.util.Arrays;

import one.microstream.collections.XArrays;

public class MainTestArrayCombine
{
	public static void main(final String[] args)
	{
		final String[][] stringsstrings = {
			{"A", "B", "C"},
			{"D", "E"},
			{"F", "G", "H", "I"}
		};

		System.out.println(Arrays.toString(XArrays.combine(stringsstrings)));
	}
}
