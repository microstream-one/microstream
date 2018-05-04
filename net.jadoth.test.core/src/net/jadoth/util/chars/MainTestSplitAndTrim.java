package net.jadoth.util.chars;

import net.jadoth.chars.JadothChars;
import net.jadoth.collections.BulkList;
import net.jadoth.memory.Memory;

public class MainTestSplitAndTrim
{
	public static void main(final String[] args)
	{
		test("A;B;C");
		test("Alice;Bob;Charly");
		test(" A ; B ; C ");
		test("a");
		test("a;;c;  ;");
		test("");
		test("\t  ");
		test(";");
		test(";;;");
	}


	static void test(final String s)
	{
		System.out.println(JadothChars.splitAndTrimToStrings(Memory.accessChars(s), ';', new BulkList<String>()));
	}
}
