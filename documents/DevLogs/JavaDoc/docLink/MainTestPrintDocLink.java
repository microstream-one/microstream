package doclink;

public class MainTestPrintDocLink
{
	public static void main(final String[] args)
	{

//		process("TestTargetClass.\n"
//			+ "<p>\n"
//			+ "Some nested docLink tags:<br>\n"
//			+ "{@literal TestTargetClass#method1()} = {@docLink TestTargetClass#method1()}.<br>\n"
//			+ "{@literal TestTargetClass#method1(int):value} = {@docLink TestTargetClass#method1(int):value}.<br>\n"
//			+ "{@literal TestTargetClass#method1(int)@param:0} = {@docLink TestTargetClass#method1(int)@param:0}.<br>"
//		);
		
		System.out.println("\n\nCorrect cases:\n\n");
		process("start of comment {@docLink MyType} end of comment blabla.");
		process("start of comment {@docLink MyType#doStuff} end of comment blabla.");
		process("start of comment {@docLink MyType#doStuff()} end of comment blabla.");
		process("start of comment {@docLink MyType#doStuff()}");
		process("start of comment {@docLink MyType#doStuff(int, long, double)} end of comment blabla.");
		process("start of comment {@docLink MyType#doStuff(int, long, double):paramName} end of comment blabla.");
		process("start of comment {@docLink MyType#doStuff(int, long, double):} end of comment blabla.");
		process("start of comment {@docLink MyType#doStuff(int, long, double)@see} end of comment blabla.");
		process("start of comment {@docLink MyType#doStuff(int, long, double)@see:paramName} end of comment blabla.");
		process("start {@docLink   MyType  #  doStuff ( int , long , double  )  @  see   :  paramName } end.");
		process("start {@docLink   MyType  #  doStuff ( int , long , double  )  @  see   :   } end.");
		process("start of comment {@docLink MyType#do1()} and {@docLink MyType#do2(int)} end of comment blabla.");
		process("start of comment {@literal other stuff} blabla {@docLink MyType#doStuff()} and {@literal again}");

		System.out.println("\n\nInvalid cases:\n\n");
		process("start of comment {@docLink MyType(int, long, double)} end of comment blabla.");
		process("start of comment {@docLink MyType#doStuff(int, long, double):paramName@see} end of comment blabla.");
		process("start of comment {@docLink MyType#doStuff@see(int, long, double):paramName} end of comment blabla.");
		process("start of comment {@docLink MyType#doStuff:paramName(int, long, double)@see} end of comment blabla.");
	}
	
	static void process(final String s)
	{
		System.out.println("Input : " + s);
		final String result = DocLinkTagDebugger.New()
			.processDoc(s)
		;
		System.out.println("Result: " + result + "\n\n");
	}
	
	public static void print(final char[] chars, final int offset, final int length)
	{
		System.out.println(">" + String.copyValueOf(chars, offset, length) + "<");
	}

}
