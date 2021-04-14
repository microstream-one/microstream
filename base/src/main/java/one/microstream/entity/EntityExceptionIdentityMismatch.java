
package one.microstream.entity;

import static one.microstream.chars.XChars.systemString;

import one.microstream.chars.VarString;


/**
 * 
 * 
 */
public class EntityExceptionIdentityMismatch extends EntityException
{
	private final Entity entity1;
	private final Entity entity2;
	
	public EntityExceptionIdentityMismatch(final Entity entity1, final Entity entity2)
	{
		super();
		
		this.entity1 = entity1;
		this.entity2 = entity2;
	}
	
	public final Entity entity1()
	{
		return this.entity1;
	}
	
	public final Entity entity2()
	{
		return this.entity2;
	}
	
	@Override
	public String assembleDetailString()
	{
		return VarString.New()
			.add("Entity identity mismatch: ")
			.add(systemString(this.entity1))
			.add(" != ")
			.add(systemString(this.entity2))
			.toString();
	}
}
