package doclink;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.RootDoc;

public final class UtilsDocLink
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static MethodDoc revolveMethod(
		final String    methodName,
		final ClassDoc  classDoc,
		final String... parameterTypesOrNames
	)
	{
		final MethodDoc methodByTypes = revolveMethodByParameterTypes(methodName, classDoc, parameterTypesOrNames);
		
		return methodByTypes != null
			? methodByTypes
			: revolveMethodByParameterNames(methodName, classDoc, parameterTypesOrNames)
		;
	}
	
	public static ClassDoc revolveClass(
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
	
	public static MethodDoc revolveMethodByParameterTypes(
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
		for(final MethodDoc md : classDoc.methods())
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
	
	public static MethodDoc revolveMethodByParameterNames(
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
		for(final MethodDoc md : classDoc.methods())
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
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// general //
	////////////
	
	// (15.05.2019 TM)TODO: Should be replaced by using the microstream base project API
	
	public static final <T> T notNull(final T object) throws NullPointerException
	{
		if(object == null)
		{
			// removing this method's stack trace entry is kind of a hack. On the other hand, it's not.
			throw new NullPointerException();
		}
		return object;
	}

//	static final int indexOf(final char[] input, final int start, final int bound, final char c)
//	{
//		for(int i = start; i < bound; i++)
//		{
//			if(input[i] == c)
//			{
//				return i;
//			}
//		}
//		return -1;
//	}
	
	static final boolean equalsCharSequence(final char[] input, final int i, final char[] sample)
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
	
	static final int skipWhiteSpaces(final char[] input, int i, final int bound)
	{
		while(i < bound && input[i] <= ' ')
		{
			i++;
		}
		return i;
	}
	
	static final int trimWhiteSpaces(final char[] input, final int start, final int bound)
	{
		int i = bound - 1;
		while(i >= start && input[i] <= ' ')
		{
			i--;
		}
		
		return i + 1;
	}
	
	static final int skipToChar(final char[] input, final int start, final int bound, final char c)
	{
		for(int i = start; i < bound; i++)
		{
			if(input[i] == c)
			{
				return i;
			}
		}
		
		// char not found, nowhere to skip to
		return start;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	private UtilsDocLink()
	{
		// static only
		throw new UnsupportedOperationException();
	}
	
}
