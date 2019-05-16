package doclink;

public interface DocLinkTagProcessor
{
	public void signalTagStart(char[] chars, int index);
	
	public void processDocLinkContent(char[] chars, int offset, int bound);
	
	public void signalTagEnd(char[] chars, int index);
	
	public void signalInputEnd(char[] chars, int bound);
	
	
	
	public abstract class AbstractParser implements DocLinkTagProcessor
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected AbstractParser()
		{
			super();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public void processDocLinkContent(final char[] input, final int start, final int bound)
		{
			this.handleContent(
				input,
				UtilsDocLink.skipWhiteSpaces(input, start, bound),
				UtilsDocLink.trimWhiteSpaces(input, start, bound)
			);
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
			final int iStart  = UtilsDocLink.skipWhiteSpaces(input, start, bound);
			final int iBound  = UtilsDocLink.trimWhiteSpaces(input, start, bound);
			final int iMember = UtilsDocLink.indexOf(input, iStart, iBound, DocLink.JAVA_DOC_MEMBER_SEPARATOR);
			final int iTagSig = UtilsDocLink.indexOf(input, iStart, iBound, DocLink.JAVA_DOC_TAG_SIGNAL);
			final int iParOpn = UtilsDocLink.indexOf(input, iStart, iBound, DocLink.JAVA_DOC_PARENTHESIS_OPEN);
			final int iParCls = UtilsDocLink.indexOf(input, iStart, iBound, DocLink.JAVA_DOC_PARENTHESIS_CLOSE);
			final int iExtraS = UtilsDocLink.indexOf(input, iStart, iBound, DocLink.DOCLINK_EXTRA_SEPARATOR);

			final int tagNameBound = firstOccurance(0, 0, 0, iExtraS, iBound);
			final DocLinkTagParts.Default parts = new DocLinkTagParts.Default();
			
			if(iStart >= 0)
			{
				// any non-whitespace content at all is at least a type name
				final int typeNameBound = firstOccurance(iMember, 0, iTagSig, iExtraS, iBound);
				parts.typeName = String(input, iStart - 1, typeNameBound);
			}
			if(iMember >= 0)
			{
				final int memberNameBound = firstOccurance(0, iParOpn, iTagSig, iExtraS, iBound);
				parts.memberName = String(input, iMember, memberNameBound);
				
				if(iParOpn >= 0 && iParCls >= 0 && iParOpn < iParCls
					&& (iTagSig < 0 || iParCls < iTagSig)
					&& (iExtraS < 0 || iParCls < iExtraS)
				)
				{
					// writing a direct parser for that is not worth the hassle
					parts.parameterList = String(input, iParOpn, iParCls).split("\\s*,\\s*");
				}
			}
			if(iTagSig >= 0 && iTagSig < tagNameBound)
			{
				// tag can occur everywhere, so its parsed generally, meaning in here.
				parts.tagName = String(input, iTagSig, tagNameBound);
			}
			if(iExtraS >= 0)
			{
				// extra identifier is always the last thing, so no bounds seeking here, any more.
				parts.extraIdentifier = String(input, iExtraS, iBound);
			}
			
			return parts;
		}
				
		static final String String(final char[] input, final int lowBound, final int highBound)
		{
			final int iStringStart = UtilsDocLink.skipWhiteSpaces(input, lowBound + 1, highBound);
			final int iStringBound = UtilsDocLink.trimWhiteSpaces(input, iStringStart, highBound);
			
			return String.valueOf(input, iStringStart, iStringBound - iStringStart);
		}
		
		protected void handleContent(final char[] input, final int start, final int bound)
		{
			final DocLinkTagParts parsedParts = parseParts(input, start, bound);
			this.handleParsedContent(parsedParts);
		}
		
		protected abstract void handleParsedContent(DocLinkTagParts parts);
		
	}
	
	public abstract class AbstractParserBuffering extends DocLinkTagProcessor.AbstractParser
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final StringBuilder buffer;
		
		private int currentIndex = 0;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		AbstractParserBuffering(final StringBuilder sb)
		{
			super();
			this.buffer = sb;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		public StringBuilder buffer()
		{
			return this.buffer;
		}

		@Override
		public void signalTagStart(final char[] chars, final int index)
		{
			this.checkForCatchUp(chars, index);
		}
		
		@Override
		public void signalTagEnd(final char[] chars, final int index)
		{
			this.currentIndex = index;
		}
		
		@Override
		public void signalInputEnd(final char[] chars, final int bound)
		{
			this.checkForCatchUp(chars, bound);
		}
		
		private void checkForCatchUp(final char[] input, final int index)
		{
			if(this.currentIndex < index)
			{
				this.buffer.append(input, this.currentIndex, index - this.currentIndex);
			}
		}
						
		public AbstractParserBuffering resetBuffer()
		{
			// because a clear() or equivalent was too hard to implement for them ...
			this.buffer.setLength(0);
			this.currentIndex = 0;
			
			return this;
		}
		
		public String yieldBuffer()
		{
			final String result = this.buffer.toString();
			this.resetBuffer();
			
			return result;
		}
		
	}

	
}
