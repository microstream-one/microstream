package doclink;

import java.util.Arrays;

public final class DocLinkTagDebugger extends DocLinkTagProcessor.AbstractParserBuffering
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
		super(new StringBuilder());
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	
	@Override
	protected void handleContent(final char[] input, final int start, final int bound)
	{
//		this.DEBUG_passThrough(input, start, bound);
		this.DEBUG_printAndBlacken(input, start, bound);
		super.handleContent(input, start, bound);
	}
	
	@Deprecated
	final void DEBUG_passThrough(final char[] input, final int start, final int bound)
	{
		this.buffer().append(input, start, bound - start);
	}

	@Deprecated
	final void DEBUG_printAndBlacken(final char[] chars, final int offset, final int bound)
	{
		System.out.println("Parsed content: " + String.valueOf(chars, offset, bound - offset));
		this.DEBUG_fillWith(bound - offset, 'x');
	}
	
	private void DEBUG_fillWith(final int amount, final char c)
	{
		final StringBuilder sb = this.buffer();
		for(int i = amount; i --> 0;)
		{
			sb.append(c);
		}
	}

	@Override
	protected void handleParsedContent(final DocLinkTagParts parts)
	{
		System.out.println(""
			+   "-Type       = >" + parts.typeName() + "<"
			+ "\n-IsMember   ? "  + parts.isMember()
			+ "\n-MemberName = >" + parts.memberName() + "<"
			+ "\n-IsMethod   ? "  + parts.isMethod()
			+ "\n-Parameters = "  + Arrays.toString(parts.parameterList())
			+ "\n-TagName    = >" + parts.tagName() + "<"
			+ "\n-Extra      = >" + parts.extraIdentifier() + "<"
		);
	}
	
}
