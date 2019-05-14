package doclink;

public class MainTestPrintDocLink
{
	public static void main(final String[] args)
	{
		
		PrintDocLink.parseDocLinkContent(
			"blabla {@docLink zeh content()} middle bla.\n\r\t {@docLink #1} end bla",
			MainTestPrintDocLink::print
		);
	}
	
	public static void print(final char[] chars, final int offset, final int length)
	{
		System.out.println(">" + String.copyValueOf(chars, offset, length) + "<");
	}

}
