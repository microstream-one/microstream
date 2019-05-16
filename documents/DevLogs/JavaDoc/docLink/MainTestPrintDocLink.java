package doclink;

public class MainTestPrintDocLink
{
	public static void main(final String[] args)
	{
		System.out.println("\n\nCorrect cases:\n\n");
		process("start of comment {@docLink MyType} end of comment blabla.");
		process("start of comment {@docLink MyType#doStuff} end of comment blabla.");
		process("start of comment {@docLink MyType#doStuff()} end of comment blabla.");
		process("start of comment {@docLink MyType#doStuff(int, long, double)} end of comment blabla.");
		process("start of comment {@docLink MyType#doStuff(int, long, double):paramName} end of comment blabla.");
		process("start of comment {@docLink MyType#doStuff(int, long, double)@return} end of comment blabla.");
		process("start of comment {@docLink MyType#doStuff(int, long, double)@return:paramName} end of comment blabla.");
		process("start {@docLink   MyType  #  doStuff ( int , long , double  )  @  return  :  paramName  } end.");
		process("start of comment {@docLink MyType#do1()} and {@docLink MyType#do2(int)} end of comment blabla.");

		System.out.println("\n\nInvalid cases:\n\n");
		process("start of comment {@docLink MyType(int, long, double)} end of comment blabla.");
		process("start of comment {@docLink MyType#doStuff@return(int, long, double):paramName} end of comment blabla.");
		process("start of comment {@docLink MyType#doStuff:paramName(int, long, double)@return} end of comment blabla.");
	}
	
	static void process(final String s)
	{
		System.out.println("Input : " + s);
		final String result = DocLink.parseDocLinkContent(s,
//			MainTestPrintDocLink::print
			DocLinkTagDebugger.New()
		).yieldBuffer();
		System.out.println("Result: " + result + "\n\n");
	}
	
	public static void print(final char[] chars, final int offset, final int length)
	{
		System.out.println(">" + String.copyValueOf(chars, offset, length) + "<");
	}

}
