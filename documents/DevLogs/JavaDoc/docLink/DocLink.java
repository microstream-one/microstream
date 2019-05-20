package doclink;

public class DocLink
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	static final char   JAVA_DOC_TAG_START         = '{';
	static final char   JAVA_DOC_TAG_CLOSE         = '}';
	static final char   JAVA_DOC_TAG_SIGNAL        = '@';
	static final char   JAVA_DOC_MEMBER_SEPARATOR  = '#';
	static final char   JAVA_DOC_PARENTHESIS_OPEN  = '(';
	static final char   JAVA_DOC_PARENTHESIS_CLOSE = ')';
	static final String DOC_LINK_TAG               = JAVA_DOC_TAG_SIGNAL + "docLink";
	static final char[] DOC_LINK_TAG_CHARS         = DOC_LINK_TAG.toCharArray();
	static final char   DOCLINK_EXTRA_SEPARATOR    = ':';
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static final void parseDocLinkContent(
		final char[]                   input            ,
		final int                      start            ,
		final int                      bound            ,
		final String                   parameterName    ,
		final CharsBuilder             charsBuilder     ,
		final DocLinkTagContentHandler tagContentHandler
	)
	{
		int last = start;
		for(int i = start; i < bound; i++)
		{
			// skip until potential tag
			if(input[i] != JAVA_DOC_TAG_START)
			{
				continue;
			}
			
			/*
			 * Reach forward to tag end.
			 * Note: nested curly braces are not supported. The first closing curly closes the tag. That's it.
			 */
			final int tagBound = UtilsDocLink.skipToChar(input, i, bound, JAVA_DOC_TAG_CLOSE);
			if(tagBound == i)
			{
				continue;
			}
			

			// check tag for docLink tag
			final int j = UtilsDocLink.skipWhiteSpaces(input, i + 1, bound);
			if(UtilsDocLink.equalsCharSequence(input, j, DOC_LINK_TAG_CHARS))
			{
				charsBuilder.acceptChars(input, last, i - last);
				tagContentHandler.handleDocLinkContent(input, j + DOC_LINK_TAG_CHARS.length, tagBound, parameterName, charsBuilder);

			}
			
			i = tagBound;
			last = i + 1;
		}
		
		// handle the trailing part after the last tag occurance.
		charsBuilder.acceptChars(input, last, bound - last);
	}
	
	/**
	 * Quick check (without any object instantiation), if the passed {@link String} contains a docLink tag at all.
	 * 
	 * @param input the {@link String} to be tested.
	 * 
	 * @return whether the passed {@link String} contains a docLink tag.
	 */
	public static final boolean containsDocLink(final String input)
	{
		final int curlyIndex = input.indexOf(JAVA_DOC_TAG_START);
		if(curlyIndex < 0)
		{
			return false;
		}
		if(input.indexOf(DOC_LINK_TAG, curlyIndex + 1) < 0)
		{
			return false;
		}
		
		return true;
	}
	
	public static final String getTagName(final String tagString)
	{
		final int indexOfBracketOpen = tagString.indexOf('[');
		
		return indexOfBracketOpen < 0
			? tagString
			: tagString.substring(0, indexOfBracketOpen)
		;
	}
	
	public static final int getTagIndex(final String tagString)
	{
		final int indexOfBracketOpen = tagString.indexOf('[');
		if(indexOfBracketOpen < 0)
		{
			return -1;
		}
		final int indexOfBracketClose = tagString.lastIndexOf(']');
		if(indexOfBracketClose < 0)
		{
			return -1;
		}
		
		final String indexLiteral = tagString.substring(indexOfBracketOpen, indexOfBracketClose);
		try
		{
			return Integer.parseInt(indexLiteral);
		}
		catch(final NumberFormatException e)
		{
			return -1;
		}
	}
	

	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	private DocLink()
	{
		// static only
		throw new UnsupportedOperationException();
	}
	
}
