package one.microstream;

import one.microstream.chars.VarString;

public class TestSortUtils
{
	public static String toString(final int size, final int[] values)
	{
		final VarString vc = VarString.New((int)(values.length*5.0f)).append('[');
		for(int i = 0; i < values.length; i++)
		{
			if(i == size)
			{
				vc.add("...  ");
				break;
			}
			vc.add(values[i]).add(',', ' ');
		}
		vc.deleteLast().setLast(']');
		return vc.toString();
	}
}
