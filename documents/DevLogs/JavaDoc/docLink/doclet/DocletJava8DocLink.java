package doclink.doclet;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.ParamTag;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.ProgramElementDoc;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Tag;

import doclink.DocLink;
import doclink.UtilsDocLink;

public class DocletJava8DocLink
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
		final String processedString = linker.processDoc(commentText, parameterName);
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
		
	public static ClassDoc resolveClass(
		final String  className,
		final RootDoc rootDoc
	)
	{
		for(final ClassDoc cd : rootDoc.classes())
		{
			// (15.05.2019 TM)TODO: not sure if supporting unqualified name is a good idea
			if(cd.qualifiedName().equals(className) || cd.name().equals(className))
			{
				return cd;
			}
		}
		
		return null;
	}
	
	public static FieldDoc resolveField(final String fieldName, final ClassDoc classDoc)
	{
		final FieldDoc[] fields = classDoc.fields(false);
		if(fields == null)
		{
			return null;
		}
		
		for(final FieldDoc fd : fields)
		{
			// refreshingly simple: only declared fields can be meant in this context.
			if(fd.name().equals(fieldName))
			{
				return fd;
			}
		}
	
		return null;
	}
	
	public static MethodDoc resolveMethod(
		final RootDoc   root                 ,
		final String    className            ,
		final String    methodName           ,
		final String... parameterTypesOrNames
	)
	{
		final ClassDoc cd = resolveClass(className, root);
		return resolveMethod(cd, methodName, parameterTypesOrNames);
	}
	
	public static MethodDoc resolveMethod(
		final ClassDoc  classDoc  ,
		final String    methodName,
		final String... parameterTypesOrNames
	)
	{
		final MethodDoc methodByTypes = resolveMethodByParameterTypes(methodName, classDoc, parameterTypesOrNames);
		
		return methodByTypes != null
			? methodByTypes
			: resolveMethodByParameterNames(methodName, classDoc, parameterTypesOrNames)
		;
	}
	
	public static MethodDoc resolveMethodByParameterTypes(
		final String    methodName,
		final ClassDoc  classDoc,
		final String... parameterTypes
	)
	{
		final MethodDoc[] methods = classDoc.methods();
		if(methods == null)
		{
			return null;
		}
		
		scanMethods:
		for(final MethodDoc md : methods)
		{
			if(!md.name().equals(methodName))
			{
				continue;
			}
			
			final Parameter[] parameters = md.parameters();
//			System.out.println("resolveMethodByParameterTypes: parameterTypes.length = " + parameterTypes.length + " <-> parameters.length = " + parameters.length);
			if(parameters.length != parameterTypes.length)
			{
				continue;
			}
			
			for(int i = 0; i < parameters.length; i++)
			{
				// the method should be specific enough for unqualified names to not cause problems.
				if(!parameters[i].type().qualifiedTypeName().equals(parameterTypes[i])
					&& !parameters[i].typeName().equals(parameterTypes[i])
				)
				{
					continue scanMethods;
				}
			}
			
			return md;
		}
	
		return null;
	}
	
	public static MethodDoc resolveMethodByParameterNames(
		final String    methodName,
		final ClassDoc  classDoc,
		final String... parameterNames
	)
	{
		final MethodDoc[] methods = classDoc.methods();
		if(methods == null)
		{
			return null;
		}
		
		scanMethods:
		for(final MethodDoc md : methods)
		{
			if(!md.name().equals(methodName))
			{
				continue;
			}
			
			final Parameter[] parameters = md.parameters();
			if(parameters.length != parameterNames.length)
			{
				continue;
			}
			
			for(int i = 0; i < parameters.length; i++)
			{
				if(!parameters[i].name().equals(parameterNames[i]))
				{
					continue scanMethods;
				}
			}
			
			return md;
		}
	
		return null;
	}
	
	public static Tag searchNonParamTag(
		final Tag[]  tags           ,
		final String tagName        ,
		final String extraIdentifier
	)
	{
		/*
		 * for general tags, extraIdentifier is used as an optional index for the tags of the same name,
		 * with 0 ("use the first found") as the default.
		 */
		int index = UtilsDocLink.to_int(extraIdentifier, 0);

		for(final Tag tag : tags)
		{
			if(tag instanceof ParamTag)
			{
				// must be handled by a prior call due to more parameters being needed
				continue;
			}
			if(tag.name().equals(tagName) && index-- == 0)
			{
				return tag;
			}
		}
		
		return null;
	}
	
	public static ParamTag searchParamTag(
		final ParamTag[] paramTags      ,
		final String     parameterName  ,
		final String     extraIdentifier
	)
	{
//		System.out.println("searchParamTag: extra = " + extraIdentifier);
		final int index = UtilsDocLink.to_int(extraIdentifier);
		if(index >= 0)
		{
			return paramTags[index];
		}
		
		final String effectiveParameterName = DocLink.determineEffectiveParameterName(parameterName, extraIdentifier);
		
//		System.out.println("searchParamTag: eff = " + effectiveParameterName);
		
		if(effectiveParameterName == null)
		{
			return null;
		}
		
		for(final ParamTag paramTag : paramTags)
		{
//			System.out.println("searchParamTag: paramTag = " + paramTag.name());
			if(paramTag.parameterName().equals(effectiveParameterName))
			{
				return paramTag;
			}
		}
		
		return null;
	}
	

	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	private DocletJava8DocLink()
	{
		// static only
		throw new UnsupportedOperationException();
	}
	
}
