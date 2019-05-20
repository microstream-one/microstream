package doclink;

import java.util.Arrays;

public final class DocLinkTagDebugger extends DocLinker.Abstract
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static final DocLinkTagDebugger New()
	{
		return new DocLinkTagDebugger();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	DocLinkTagDebugger()
	{
		super();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Deprecated
	final void DEBUG_passThrough(final CharsAcceptor charsAcceptor, final char[] input, final int start, final int bound)
	{
		charsAcceptor.acceptChars(input, start, bound - start);
	}

	@Deprecated
	final void DEBUG_printAndBlacken(final CharsAcceptor charsAcceptor, final char[] chars, final int offset, final int bound)
	{
		System.out.println("Parsed content: " + String.valueOf(chars, offset, bound - offset));
		this.DEBUG_fillWith(charsAcceptor, bound - offset, 'x');
	}
	
	private void DEBUG_fillWith(final CharsAcceptor charsAcceptor, final int amount, final char c)
	{
		for(int i = amount; i --> 0;)
		{
			charsAcceptor.acceptChar(c);
		}
	}
	
	@Override
	protected void processDocLinkContentTrimmed(
		final char[]        input        ,
		final int           start        ,
		final int           bound        ,
		final String        parameterName,
		final CharsAcceptor charsAcceptor
	)
	{
//		this.DEBUG_passThrough(charsAcceptor, input, start, bound);
		this.DEBUG_printAndBlacken(charsAcceptor, input, start, bound);
		super.processDocLinkContentTrimmed(input, start, bound, parameterName, charsAcceptor);
	}

	@Override
	protected void handleParsedContent(
		final DocLinkTagParts parts        ,
		final String          parameterName,
		final CharsAcceptor   charsAcceptor
	)
	{
		System.out.println(toDebugString(parts, parameterName));
	}
	
	public static String toDebugString(final DocLinkTagParts parts, final String parameterName)
	{
		return ""
			+   "-Type        = >" + parts.typeName() + "<"
			+ "\n-IsMember    ? "  + parts.isMember()
			+ "\n-MemberName  = >" + parts.memberName() + "<"
			+ "\n-IsMethod    ? "  + parts.isMethod()
			+ "\n-Parameters  = "  + Arrays.toString(parts.parameterList()) + " length = " + parts.parameterList().length
			+ "\n-TagName     = >" + parts.tagName() + "<"
			+ "\n-Extra       = >" + parts.extraIdentifier() + "<"
			+ "\n-PassedParam = >" + parameterName + "<"
		;
	}
	
}
