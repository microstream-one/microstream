package doclink;

/**
 * Simple JavaDoc linker that processes a JavaDoc input String and returns a processed result.
 * <p>
 * Supports optional processing based on a method parameter name.
 * 
 * @author TM
 */
@FunctionalInterface
public interface DocLinker
{
	public default String processDoc(final String doc)
	{
		return this.processDoc(doc, null);
	}
	
	public default String processDoc(final String doc, final String qualifiedTypeName)
	{
		return this.processDoc(doc, qualifiedTypeName, null);
	}
	
	public String processDoc(String doc, String qualifiedTypeName, String parameterName);
	
	
	
	public abstract class Abstract implements DocLinker, DocLinkTagContentHandler
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final CharsBuilder charsBuilder;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Abstract()
		{
			this(CharsBuilder.New());
		}
		
		protected Abstract(final CharsBuilder charsBuilder)
		{
			super();
			this.charsBuilder = UtilsDocLink.notNull(charsBuilder);
		}

		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public void handleDocLinkContent(
			final char[]        input            ,
			final int           start            ,
			final int           bound            ,
			final String        qualifiedTypeName,
			final String        parameterName    ,
			final CharsAcceptor charsAcceptor
		)
		{
			this.processDocLinkContentTrimmed(
				input,
				UtilsDocLink.skipStartWhiteSpaces(input, start, bound),
				UtilsDocLink.trimBoundWhiteSpaces(input, start, bound),
				qualifiedTypeName,
				parameterName,
				charsAcceptor
			);
		}
		
		protected void processDocLinkContentTrimmed(
			final char[]        input            ,
			final int           start            ,
			final int           bound            ,
			final String        qualifiedTypeName,
			final String        parameterName    ,
			final CharsAcceptor charsAcceptor
		)
		{
			final DocLinkTagParts parsedParts = parseParts(input, start, bound);
			this.handleParsedContent(parsedParts, qualifiedTypeName, parameterName, charsAcceptor);
		}
		
		private static int firstOccurance(final int i1, final int i2, final int i3, final int i4, final int i5)
		{
			int minimum = Integer.MAX_VALUE;
			if(i1 > 0 && i1 < minimum)
			{
				minimum = i1;
			}
			if(i2 > 0 && i2 < minimum)
			{
				minimum = i2;
			}
			if(i3 > 0 && i3 < minimum)
			{
				minimum = i3;
			}
			if(i4 > 0 && i4 < minimum)
			{
				minimum = i4;
			}
			if(i5 > 0 && i5 < minimum)
			{
				minimum = i5;
			}
			
			return minimum == Integer.MAX_VALUE
				? -1
				: minimum
			;
		}
		
		private static DocLinkTagParts parseParts(final char[] input, final int start, final int bound)
		{
			final int iStart  = UtilsDocLink.skipStartWhiteSpaces(input, start, bound);
			final int iBound  = UtilsDocLink.trimBoundWhiteSpaces(input, start, bound);
			final int iMember = UtilsDocLink.indexOf(input, iStart, iBound, DocLink.JAVA_DOC_MEMBER_SEPARATOR);
			final int iParOpn = UtilsDocLink.indexOf(input, iStart, iBound, DocLink.JAVA_DOC_PARENTHESIS_OPEN);
			final int iParCls = UtilsDocLink.indexOf(input, iStart, iBound, DocLink.JAVA_DOC_PARENTHESIS_CLOSE);
			final int iTagSig = UtilsDocLink.indexOf(input, iStart, iBound, DocLink.JAVA_DOC_TAG_SIGNAL);
			final int iExtraS = UtilsDocLink.indexOf(input, iStart, iBound, DocLink.DOCLINK_EXTRA_SEPARATOR);
			
			final int memberNameBound = firstOccurance(0, iParOpn, iTagSig, iExtraS, iBound);
			final int tagNameBound    = firstOccurance(0, 0, 0, iExtraS, iBound);
			final int extraNameBound  = firstOccurance(0, 0, 0, 0, iBound);

			final DocLinkTagParts.Default parts = new DocLinkTagParts.Default();
			
			if(iStart >= 0)
			{
				// any non-whitespace content at all is at least a type name
				final int typeNameBound = firstOccurance(iMember, 0, iTagSig, iExtraS, iBound);
				parts.typeName = String(input, iStart - 1, typeNameBound);
			}
			if(iMember >= 0 && iMember < memberNameBound)
			{
				parts.memberName = String(input, iMember, memberNameBound);
				
				if(iParOpn >= 0 && iParCls >= 0 && iParOpn < iParCls
					&& (iTagSig < 0 || iParCls < iTagSig)
					&& (iExtraS < 0 || iParCls < iExtraS)
				)
				{
					// writing a direct parser for that is not worth the hassle (aaand there's hassle even here)
					parts.parameterList = iParOpn == UtilsDocLink.trimBoundWhiteSpaces(input, iParOpn, iParCls) - 1
						? new String[0]
						: String(input, iParOpn, iParCls).split("\\s*,\\s*")
					;
				}
			}
			if(iTagSig >= 0 && iTagSig < tagNameBound)
			{
				parts.tagName = String(input, iTagSig, tagNameBound);
			}
			if(iExtraS >= 0 && iExtraS < extraNameBound)
			{
				// tag identifier is always the last thing, so no bounds seeking here, any more.
				parts.extraIdentifier = String(input, iExtraS, extraNameBound);
			}
			
			return parts;
		}
				
		static final String String(final char[] input, final int lowBound, final int highBound)
		{
			final int iStringStart = UtilsDocLink.skipStartWhiteSpaces(input, lowBound + 1, highBound);
			final int iStringBound = UtilsDocLink.trimBoundWhiteSpaces(input, iStringStart, highBound);
			
			return String.valueOf(input, iStringStart, iStringBound - iStringStart);
		}
		
		protected abstract void handleParsedContent(
			DocLinkTagParts parts            ,
			String          qualifiedTypeName,
			String          parameterName    ,
			CharsAcceptor   charsAcceptor
		);

		@Override
		public String processDoc(final String doc, final String qualifiedTypeName, final String parameterName)
		{
			// quick check before costly char array creation.
			if(!DocLink.containsDocLink(doc))
			{
				return doc;
			}
			
			this.charsBuilder.prepare();
			DocLink.parseDocLinkContent(
				doc.toCharArray(),
				0                ,
				doc.length()     ,
				qualifiedTypeName,
				parameterName    ,
				this.charsBuilder,
				this
			);
			
			return this.charsBuilder.yield();
		}
		
	}
	
}
