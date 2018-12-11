package net.jadoth.util.chars;

import net.jadoth.chars.XChars;
import net.jadoth.collections.BulkList;
import net.jadoth.low.XMemory;

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
		System.out.println(XChars.splitAndTrimToStrings(XMemory.accessChars(s), ';', new BulkList<String>()));
	}
}
