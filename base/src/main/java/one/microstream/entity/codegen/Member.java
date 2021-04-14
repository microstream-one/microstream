
package one.microstream.entity.codegen;

import java.beans.Introspector;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

import one.microstream.chars.VarString;

/**
 * 
 * 
 */
class Member
{
	final ExecutableElement element;
	final String            methodName;
	final String            name;
	final String            setterName;
	final TypeMirror        type;
	
	String                  typeName;
	String                  paddedTypeName;
	String                  paddedMethodName;
	String                  paddedName;
	String                  throwsClause;
	
	Member(final ExecutableElement element, final TypeMirror type)
	{
		super();
		
		this.element    = element;
		this.methodName = element.getSimpleName().toString();
		this.name       = memberName(this.methodName);
		this.setterName = setterName(this.name);
		this.type       = type;
	}
	
	private static String memberName(final String methodName)
	{
		int offset = -1;
		if(methodName.startsWith("get"))
		{
			offset = 3;
		}
		else if(methodName.startsWith("is"))
		{
			offset = 2;
		}
		
		return offset <= 0 || methodName.length() <= offset
			? methodName
			: Introspector.decapitalize(methodName.substring(offset));
	}
	
	private static String setterName(final String name)
	{
		return VarString.New("set")
			.add(Character.toUpperCase(name.charAt(0)))
			.add(name.substring(1))
			.toString();
	}
}
