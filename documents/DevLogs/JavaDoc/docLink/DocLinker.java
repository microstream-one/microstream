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
	public default DocLinker processDoc(final String doc)
	{
		return this.processDoc(doc, null);
	}
	
	public default DocLinker processDoc(final String doc, final String qualifiedTypeName)
	{
		return this.processDoc(doc, qualifiedTypeName, null);
	}
	
	public DocLinker processDoc(String doc, String qualifiedTypeName, String parameterName);
	
	
	
	/* (22.05.2019 TM)TODO: DocLink: Make member and parameters omittable, too.
	 * E.g. {@linkDoc TargetType#:)
	 * Meaning "Take the JavaDoc from the TargetType's member that corresponds to the current subject member".
	 */
	
	// (22.05.2019 TM)TODO: DocLink: Make type omittable for local references.
	
	/* (22.05.2019 TM)TODO: DocLink: Proper problem callback instead of simple override-method
	 * (to better modularize warnings/errors and failfast or problem collecting
	 */
	
	/* (24.05.2019 TM)TODO: DocLink: @link tags with local reference have to have their reference transformed to
	 * globally qualified, as well.
	 */
	
	/* (04.06.2019 TM)TODO: DocLink: A way is required to reference specific sentences of a general desctiption,
	 * at least the first one (which has a special meaning in JavaDoc).
	 * E.g. {@linkDoc SomeType:1
	 */
	
	/* (04.06.2019 TM)TODO: DocLink: A way is required to reference ALL tags of one kind, e.g. all @see tags.
	 * E.g. {@linkDoc SomeType@see:all}
	 */
	
	/* (04.06.2019 TM)TODO: DocLink: Simple type names must be resolved to full qualified types using the
	 * subject class' imports.
	 */
	
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
			final char[] input            ,
			final int    start            ,
			final int    bound            ,
			final String qualifiedTypeName,
			final String parameterName
		)
		{
			this.processDocLinkContentTrimmed(
				input,
				UtilsDocLink.skipStartWhiteSpaces(input, start, bound),
				UtilsDocLink.trimBoundWhiteSpaces(input, start, bound),
				qualifiedTypeName,
				parameterName
			);
		}
		
		protected void internalPrepare()
		{
			this.charsBuilder.prepare();
		}
		
		protected String internalYield()
		{
			// no reset here, in case yield is called multiple times before the result is complete (e.g. logging)
			return this.charsBuilder.yield();
		}
		
		protected void processDocLinkContentTrimmed(
			final char[] input            ,
			final int    start            ,
			final int    bound            ,
			final String qualifiedTypeName,
			final String parameterName
		)
		{
			final DocLinkTagParts parsedParts = parseParts(input, start, bound);
			this.handleParsedContent(parsedParts, qualifiedTypeName, parameterName);
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
			final DocLinkTagParts.Default parts = new DocLinkTagParts.Default();
			parts.rawString = String(input, start - 1, bound);
			
			final int iStart  = UtilsDocLink.skipStartWhiteSpaces(input, start, bound);
			final int iBound  = UtilsDocLink.trimBoundWhiteSpaces(input, start, bound);
			final int iMember = UtilsDocLink.indexOf(input, iStart, iBound, DocLink.JAVA_DOC_MEMBER_SEPARATOR);
			final int iParOpn = UtilsDocLink.indexOf(input, iStart, iBound, DocLink.JAVA_DOC_PARENTHESIS_OPEN);
			final int iParCls = UtilsDocLink.indexOf(input, iStart, iBound, DocLink.JAVA_DOC_PARENTHESIS_CLOSE);
			final int iTagSig = UtilsDocLink.indexOf(input, iStart, iBound, DocLink.DOCLINK_TAG_REFERENCE);
			final int iExtraS = UtilsDocLink.indexOf(input, iStart, iBound, DocLink.DOCLINK_EXTRA_SEPARATOR);
			
			final int memberNameBound = firstOccurance(0, iParOpn, iTagSig, iExtraS, iBound);
			final int tagNameBound    = firstOccurance(0, 0, 0, iExtraS, iBound);
			final int extraNameBound  = firstOccurance(0, 0, 0, 0, iBound);
			
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
				// tag signal char must be prepended (explicitely, after white spaces have been removed)
				parts.tagName = DocLink.JAVA_DOC_TAG_SIGNAL + String(input, iTagSig, tagNameBound);
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
			String          parameterName
		);

		@Override
		public DocLinker.Abstract processDoc(final String doc, final String qualifiedTypeName, final String parameterName)
		{
			/* (22.05.2019 TM)FIXME: DocLink: Fix stack-overflowing looping recursion.
			 * Passing this without an alreadyHandled Set will cause a stack overflow on looping recusion
			 */
			DocLink.parseDocLinkContent(
				doc.toCharArray(),
				0                ,
				doc.length()     ,
				qualifiedTypeName,
				parameterName    ,
				this.charsBuilder,
				this
			);
			
			return this;
		}
		
	}
	
}
