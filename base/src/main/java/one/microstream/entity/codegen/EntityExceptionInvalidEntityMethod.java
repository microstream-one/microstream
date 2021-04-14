
package one.microstream.entity.codegen;

import javax.lang.model.element.ExecutableElement;

import one.microstream.chars.VarString;
import one.microstream.entity.EntityException;


/**
 * 
 * 
 */
public class EntityExceptionInvalidEntityMethod extends EntityException
{
	private final ExecutableElement method;
	
	public EntityExceptionInvalidEntityMethod(final ExecutableElement method)
	{
		super();
		
		this.method = method;
	}
	
	public final ExecutableElement method()
	{
		return this.method;
	}
	
	@Override
	public String assembleDetailString()
	{
		return VarString.New()
			.add("Invalid entity method: ")
			.add(this.method.getEnclosingElement()).add('#').add(this.method)
			.add("; only methods with return type, no type parameters")
			.add(", no parameters and no checked exceptions are supported.")
			.toString();
	}
}
