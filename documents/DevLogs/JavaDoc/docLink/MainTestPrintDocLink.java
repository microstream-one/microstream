package doclink;

public class MainTestPrintDocLink
{
	public static void main(final String[] args)
	{
		process("blabla {@docLink zeh content()} middle bla. {@docLink #1} end bla.");
	}
	
	static void process(final String s)
	{
		System.out.println("Input : " + s);
		final String result = DocLink.parseDocLinkContent(s,
//			MainTestPrintDocLink::print
			DocLinkTagDebugger.New()
		).yield();
		System.out.println("Result: " + result);
	}
	
	public static void print(final char[] chars, final int offset, final int length)
	{
		System.out.println(">" + String.copyValueOf(chars, offset, length) + "<");
	}

}
