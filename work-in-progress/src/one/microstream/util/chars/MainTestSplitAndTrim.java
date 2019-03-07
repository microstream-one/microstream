package one.microstream.util.chars;

import one.microstream.chars.XChars;
import one.microstream.collections.BulkList;
import one.microstream.memory.XMemory;

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
