package doclink;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.RootDoc;

public class DocLink //extends com.sun.javadoc.Doclet
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////
	
	static final String DOC_LINK_TAG       = "@docLink";
	static final char[] DOC_LINK_TAG_CHARS = DOC_LINK_TAG.toCharArray();
	static final char   JAVA_DOC_TAG_START = '{';
	static final char   JAVA_DOC_TAG_CLOSE = '}';
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	// the weirdly hacky way of doing pseudo-interfaces of the JDK developers themselves
	public static boolean start(final RootDoc root)
	{
		final ClassDoc[] classes = root.classes();
		for(int i = 0; i < classes.length; ++i)
		{
			printMethods(classes[i]);
		}
		
		final MethodDoc md = TEST_searchMethod("Storage", "EntityCacheEvaluator", root, "timeoutMs", "threshold");
		System.out.println("Found it!: " + md.qualifiedName());
		for(final Parameter p : md.parameters())
		{
			System.out.println(p.type().qualifiedTypeName() + " " + p.name());
		}
		
		return true;
	}
	
	private static void printMethods(final ClassDoc cd)
	{
		System.out.println("Class " + cd.name());
		for(final MethodDoc md : cd.methods())
		{
			System.out.println("  Method " + md.name());
			for(final Parameter p : md.parameters())
			{
				System.out.println("  > " + p.type().qualifiedTypeName() + " " + p.name());
			}
		}
	}
	
	private static MethodDoc TEST_searchMethod(
		final String className ,
		final String methodName,
		final RootDoc root,
		final String... parameterTypesOrNames
	)
	{
		final ClassDoc cd = UtilsDocLink.revolveClass(className, root);
		return UtilsDocLink.revolveMethod(methodName, cd, parameterTypesOrNames);
	}
	
	public static final <P extends DocLinkTagProcessor> P parseDocLinkContent(
		final String input,
		final P      logic
	)
	{
		// quick check before creating a char array
		final int curlyIndex = input.indexOf(JAVA_DOC_TAG_START);
		if(containsDocLink(input, curlyIndex))
		{
			parseDocLinkContent(input.toCharArray(), logic);
		}
		
		return logic;
	}

	public static final void parseDocLinkContent(
		final char[]              input,
		final DocLinkTagProcessor logic
	)
	{
		parseDocLinkContent(input, 0, input.length, logic);
	}
	
	public static final void parseDocLinkContent(
		final char[]                  input,
		final int                     start,
		final int                     bound,
		final DocLinkTagProcessor logic
	)
	{
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
				logic.signalTagStart(input, i);
				logic.processDocLinkContent(input, j + DOC_LINK_TAG_CHARS.length, tagBound);
				logic.signalTagEnd(input, tagBound + 1);
			}
			
			i = tagBound;
		}
		
		// give processing logic a chance to handle the trailing part after the last tag occurance.
		logic.signalInputEnd(input, bound);
	}
		
	static final boolean containsDocLink(final String s, final int firstCurlyBraceIndex)
	{
		if(firstCurlyBraceIndex < 0)
		{
			return false;
		}
		if(s.indexOf(DOC_LINK_TAG, firstCurlyBraceIndex + 1) < 0)
		{
			return false;
		}
		
		return true;
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
