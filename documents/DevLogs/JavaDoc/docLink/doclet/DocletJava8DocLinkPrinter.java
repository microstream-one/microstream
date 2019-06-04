package doclink.doclet;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.ParamTag;
import com.sun.javadoc.ProgramElementDoc;
import com.sun.javadoc.RootDoc;

import doclink.UtilsDocLink;

/**
 * Type pseudo-implementing the pseudo-interface Doclet for testing / proof-of-concept purposes that prints
 * the @linkDoc replacements to the console.
 * 
 * @author TM
 */
public class DocletJava8DocLinkPrinter // extends com.sun.javadoc.Doclet
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	// the weirdly hacky way of doing pseudo-interfaces of the JDK developers themselves
	public static boolean start(final RootDoc root)
	{
		final DocletJava8DocLinker linker = DocletJava8DocLinker.New(root);
		
		final StringBuilder consoleBuffer = new StringBuilder(1000);
		
		final ClassDoc[] classes = root.classes();
		for(int i = 0; i < classes.length; ++i)
		{
			final ClassDoc cd = classes[i];
			UtilsDocLink.clear(consoleBuffer);
			final String classString = printReplacment(consoleBuffer, linker, cd);

			UtilsDocLink.clear(consoleBuffer);
			final String methodsString = assembleMethods(consoleBuffer, cd, linker);
			UtilsDocLink.clear(consoleBuffer);
			
			if(classString != null || methodsString != null)
			{
				consoleBuffer.append("Class " + cd.qualifiedName() + "\n###");
				if(classString != null)
				{
					consoleBuffer.append("\n").append(classString).append("\n");
				}
				if(methodsString != null)
				{
					consoleBuffer.append("\n").append(methodsString).append("\n");
				}
				consoleBuffer.append("###########################\n");
				System.out.println(consoleBuffer.toString());
			}
		}
		
		return true;
	}
	
	private static String assembleMethods(
		final StringBuilder        consoleBuffer,
		final ClassDoc             cd           ,
		final DocletJava8DocLinker linker
	)
	{
		for(final MethodDoc md : cd.methods())
		{
			final StringBuilder assembleBuffer = new StringBuilder(1000);
			
			final String methodString = printReplacment(assembleBuffer, linker, md);
			UtilsDocLink.clear(assembleBuffer);
			final String paramsString = assembleParameters(assembleBuffer, md, linker);
			
			if(methodString != null || paramsString != null)
			{
				consoleBuffer.append("Method " + md.qualifiedName() + "\n---");
				if(methodString != null)
				{
					consoleBuffer.append("\n").append(methodString);
				}
				if(paramsString != null)
				{
					consoleBuffer.append("\n").append(paramsString);
				}
			}
		}

		return consoleBuffer.length() > 0 ? consoleBuffer.toString() : null;
	}
	
	private static String assembleParameters(
		final StringBuilder        consoleBuffer,
		final MethodDoc            md           ,
		final DocletJava8DocLinker linker
	)
	{
		for(final ParamTag p : md.paramTags())
		{
			printReplacment(consoleBuffer, linker, "@" + p.parameterName(), p.parameterComment(), p.parameterName(), false);
		}
		
		return consoleBuffer.length() > 0 ? consoleBuffer.toString() : null;
	}
	
	private static String printReplacment(
		final StringBuilder        buffer,
		final DocletJava8DocLinker linker,
		final ProgramElementDoc    doc
	)
	{
		return printReplacment(buffer, linker, doc.qualifiedName(), doc.commentText(), null, true);
	}
	
	private static String printReplacment(
		final StringBuilder        buffer       ,
		final DocletJava8DocLinker linker       ,
		final String               name         ,
		final String               commentText  ,
		final String               parameterName,
		final boolean              assemble
	)
	{
		final String processedString = linker
			.prepare()
			.processDoc(commentText, null, parameterName)
			.yield()
		;
		
		if(processedString.equals(commentText))
		{
			return null;
		}
		
		buffer.append(name + ":").append('\n');
		buffer.append(commentText).append('\n');
		buffer.append("->").append('\n');
		buffer.append(processedString).append('\n');
		buffer.append("-----------------------------------").append('\n');
		
		return assemble ? buffer.toString() : null;
	}
		

	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	private DocletJava8DocLinkPrinter()
	{
		// static only
		throw new UnsupportedOperationException();
	}
	
}
