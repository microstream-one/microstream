package doclink.doclet;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.ParamTag;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Tag;

import doclink.DocLink;
import doclink.UtilsDocLink;

/**
 * Central utility class that contains logic for handling {@literal com.sun.javadoc.*} types.<br>
 * See<br>
 * https://docs.oracle.com/javase/8/docs/jdk/api/javadoc/doclet/com/sun/javadoc/RootDoc.html
 * 
 * @author TM
 */
public class DocletJava8DocLink
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
			
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
		final int index = UtilsDocLink.to_int(extraIdentifier);
		if(index >= 0)
		{
			return paramTags[index];
		}
		
		final String effectiveParameterName = DocLink.determineEffectiveParameterName(parameterName, extraIdentifier);
		if(effectiveParameterName == null)
		{
			return null;
		}
		
		for(final ParamTag paramTag : paramTags)
		{
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
