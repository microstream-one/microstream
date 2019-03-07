package one.microstream.batchRefactorer;

import one.microstream.chars.VarString;

public class MainTestLineBreakStrategy
{
	static final String
		TEST = VarString.New()
		.add("only r..").cr()
		.add("both....").crlf()
		.add("only n..").lf()
		.toString()
	;
	
	static final LineBreakNormalizer NORMALIZER = new LineBreakNormalizer.Lf();
	
	static final LineBreakStrategy
		CR    = new LineBreakStrategy.Cr()  ,
		CR_LF = new LineBreakStrategy.CrLf(),
		LF    = new LineBreakStrategy.Lf()
	;
		
	
	public static void main(final String[] args)
	{
		print("raw", TEST);
		
		final String normalized = NORMALIZER.normalize(TEST);
		print("normalized", normalized);
		
		print("CR"   , CR   .restore(normalized));
		print("CR_LF", CR_LF.restore(normalized));
		print("LF"   , LF   .restore(normalized));
	}
	
	static void print(final String caption, final String s)
	{
		System.out.println((caption == null ? "" : caption+": \n") + toOutputForm(s) + "---\n");
	}
	
	static void print(final String s)
	{
		print(null, s);
	}
	
	
	static String toOutputForm(final String s)
	{
		return s
			.replaceAll("\\r\\n", "r\n")
			.replaceAll("\\n", "n\n")
			.replaceAll("\\r", "r\n")
		;
	}
}
