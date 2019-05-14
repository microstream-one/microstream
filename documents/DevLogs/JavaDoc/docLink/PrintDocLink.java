package doclink;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.Doclet;
import com.sun.javadoc.ExecutableMemberDoc;
import com.sun.javadoc.ParamTag;
import com.sun.javadoc.RootDoc;

// source: https://docs.oracle.com/javase/8/docs/jdk/api/javadoc/doclet/index.html
public class PrintDocLink extends Doclet
{

	public static boolean start(final RootDoc root)
	{
		final ClassDoc[] classes = root.classes();
		for(int i = 0; i < classes.length; ++i)
		{
			final ClassDoc cd = classes[i];
			printMembers(cd.constructors());
			printMembers(cd.methods());
		}
		
		return true;
	}

	static void printMembers(final ExecutableMemberDoc[] mems)
	{
		for(int i = 0; i < mems.length; ++i)
		{
			final ParamTag[] params = mems[i].paramTags();
//			System.out.println(mems[i].qualifiedName());
			
			for(int j = 0; j < params.length; ++j)
			{
				System.out.println("   " + params[j].parameterName() + " - " + params[j].parameterComment());
			}
		}
	}
	
	static final String DOC_LINK_TAG       = "@docLink";
	static final char[] DOC_LINK_TAG_CHARS = DOC_LINK_TAG.toCharArray();
		
	static void parseDocLinkContent(final String comment, final DocLinkContentProcessor logic)
	{
		// quick check before creating a char array
		final int curlyIndex = comment.indexOf('{');
		if(!containsDocLink(comment, curlyIndex))
		{
			return;
		}
		
		final char[] chars = comment.toCharArray();
		for(int i = curlyIndex; i < chars.length; i++)
		{
			// skip until potentical tag
			if(chars[i] != '{')
			{
				continue;
			}
			
			// check potentical tag for docLink tag
			i = skipWhiteSpacesEoFSafe(chars, i + 1);
			if(equalsCharSequence(chars, i, DOC_LINK_TAG_CHARS))
			{
				// scroll to closing curly brace. Failing to find it means no valid tag.
				i = skipWhiteSpacesEoFSafe(chars, i + DOC_LINK_TAG_CHARS.length);
				final int endIndex = skipToCharEoFSafe(chars, i, '}');
				if(endIndex != i)
				{
					logic.processDocLinkContent(chars, i, endIndex - i - 1);
				}
				i = endIndex;
			}
		}
	}
	
	static boolean containsDocLink(final String content, final int firstCurlyBraceIndex)
	{
		if(firstCurlyBraceIndex < 0)
		{
			return false;
		}
		if(content.indexOf(DOC_LINK_TAG, firstCurlyBraceIndex + 1) < 0)
		{
			return false;
		}
		
		return true;
	}
	
	private static boolean equalsCharSequence(final char[] input, final int i, final char[] sample)
	{
		if(input.length - i < sample.length)
		{
			return false;
		}
		
		for(int s = 0; s < sample.length; s++)
		{
			if(input[i + s] != sample[s])
			{
				return false;
			}
		}
		return true;
	}
	
	private static int skipWhiteSpacesEoFSafe(final char[] input, int i)
	{
		while(i < input.length && input[i] <= ' ')
		{
			i++;
		}
		return i;
	}
	
	private static int skipToCharEoFSafe(final char[] input, int i, final char c)
	{
		while(i < input.length)
		{
			if(input[i++] == c)
			{
				return i;
			}
		}
		
		return i;
	}
	
	public interface DocLinkContentProcessor
	{
		public void processDocLinkContent(char[] chars, int offset, int length);
	}
	
}