package net.jadoth.test.collections;

import net.jadoth.util.chars.VarString;

public class MainTestValidateArrayRange
{
	public static void main(final String[] args)
	{
		test(10, 0, 10);
		test(10, 0,  9);
		test(10, 0,  1);
		test(10, 0,  0);
		test(10, 0, -1);
		test(10, 0, -2);
		test(10, 0, 11);
		test(10, 0, 12);
		System.out.println();
		test(10, 1,  9);
		test(10, 1,  8);
		test(10, 1,  1);
		test(10, 1,  0);
		test(10, 1, -2);
		test(10, 1, -3);
		test(10, 1, 10);
		test(10, 1, 11);
		System.out.println();
		test(10, 9,-10);
		test(10, 9,- 9);
		test(10, 9,- 1);
		test(10, 9,- 0);
		test(10, 9,  1);
		test(10, 9,  2);
		test(10, 9,-11);
		test(10, 9,-12);
		System.out.println();
		test(10,10,-10);
		test(10,10,- 9);
		test(10,10,- 1);
		test(10,10,- 0);
		test(10,10,  1);
		test(10,10,  2);
		test(10,10,-11);
		test(10,10,-12);
		System.out.println();
		test(10,-1, 10);
		test(10,-1,  9);
		test(10,-1,  1);
		test(10,-1,  0);
		test(10,-1, -1);
		test(10,-1, -2);
		test(10,-1, 11);
		test(10,-1, 12);

	}
	static void test(final int size, final int offset, final int length)
	{
		final VarString vc = VarString.New();
		vc.add("size=").padLeft(Integer.toString(size), 3, ' ');
		vc.add(", offset=").padLeft(Integer.toString(offset), 3, ' ');
		vc.add(", length=").padLeft(Integer.toString(length), 3, ' ');
		vc.add("  [").padLeft(Integer.toString(offset), 2, ' ');
		vc.add(";").padLeft(Integer.toString(offset+length-(length < 0?-1 : length > 0 ?1 :0)), 2, ' ');
		vc.add("]"+" => ");
		System.out.print(vc);
	}
}
