package doclink;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.RootDoc;

/*
 * See
 * https://docs.oracle.com/javase/8/docs/jdk/api/javadoc/doclet/com/sun/javadoc/RootDoc.html
 * 
 */
public final class DocLinkTagResolver implements DocLinkTagProcessor
	{
		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////
		
		public static final DocLinkTagResolver New(final RootDoc docRoot)
		{
			return new DocLinkTagResolver(
				UtilsDocLink.notNull(docRoot)
			);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final RootDoc docRoot;
		private final StringBuilder sb = new StringBuilder();
		
		private int currentIndex = 0;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		DocLinkTagResolver(final RootDoc docRoot)
		{
			super();
			this.docRoot = docRoot;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public void signalTagStart(final char[] chars, final int index)
		{
			this.checkForCatchUp(chars, index);
		}

		@Override
		public void processDocLinkContent(final char[] input, final int start, final int bound)
		{
			this.handleContent(
				input,
				UtilsDocLink.skipWhiteSpaces(input, start, bound),
				UtilsDocLink.trimWhiteSpaces(input, start, bound)
			);
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
				this.sb.append(input, this.currentIndex, index - this.currentIndex);
			}
		}
		
		private void handleContent(final char[] input, final int start, final int bound)
		{
			// (15.05.2019 TM)FIXME: parse code element
			final String typeName = null;
			final String methodName = null;
			final String[] paramters = null; // types or names
			final String optionalParamterName = null;
			
			final ClassDoc  cd = UtilsDocLink.revolveClass(typeName, this.docRoot);
			final MethodDoc md = UtilsDocLink.revolveMethod(methodName, cd, paramters);
			
			if(md == null)
			{
				// (15.05.2019 TM)TODO: warning
				this.sb.append("{@link " + String.valueOf(input, start, bound) + '}');
			}
			
			// (15.05.2019 TM)FIXME: pass current paramTag's parameter name for an implicit lookup
		}
				
		public final DocLinkTagResolver reset()
		{
			// because a clear() or equivalent was too hard to implement for them ...
			this.sb.setLength(0);
			this.currentIndex = 0;
			
			return this;
		}
		
		public final String yield()
		{
			final String result = this.sb.toString();
			this.reset();
			
			return result;
		}
		
	}