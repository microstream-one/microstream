package doclink;

public class DocLink
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	static final char   JAVA_DOC_TAG_START         = '{';
	static final char   JAVA_DOC_TAG_CLOSE         = '}';
	static final char   JAVA_DOC_MEMBER_SEPARATOR  = '#';
	static final char   JAVA_DOC_PARENTHESIS_OPEN  = '(';
	static final char   JAVA_DOC_PARENTHESIS_CLOSE = ')';
	static final char   JAVA_DOC_TAG_SIGNAL        = '@';
	static final String DOC_LINK_TAG               = "@docLink";
	static final char[] DOC_LINK_TAG_CHARS         = DOC_LINK_TAG.toCharArray();
	static final char   DOCLINK_TAG_REFERENCE      = JAVA_DOC_TAG_SIGNAL;
	static final char   DOCLINK_EXTRA_SEPARATOR    = ':';
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static final void parseDocLinkContent(
		final char[]                   input            ,
		final int                      start            ,
		final int                      bound            ,
		final String                   qualifiedTypeName,
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
			final int j = UtilsDocLink.skipStartWhiteSpaces(input, i + 1, bound);
			if(UtilsDocLink.equalsCharSequence(input, j, DOC_LINK_TAG_CHARS))
			{
				charsBuilder.acceptChars(input, last, i - last);
				tagContentHandler.handleDocLinkContent(
					input, j + DOC_LINK_TAG_CHARS.length, tagBound, qualifiedTypeName, parameterName
				);
			}
			else
			{
				charsBuilder.acceptChars(input, last, tagBound - last + 1);
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
	
	/**
	 * (This JavaDoc is also used for testing. It can be changed, but should not be deleted.)
	 * 
	 * @param parameterName the name of the doc-subject parameter.
	 * @param extraIdentifier the extra identifier optionally defined in the docLink tag.
	 * 
	 * @return the - surprise, surprise - effective parameter name.
	 */
	public static String determineEffectiveParameterName(
		final String parameterName  ,
		final String extraIdentifier
	)
	{
		// an empty extraIdentifier means that just a ":" has been defined, i.e. an omitted parameter name.
		return extraIdentifier != null && extraIdentifier.isEmpty()
			? parameterName
			: extraIdentifier
		;
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
