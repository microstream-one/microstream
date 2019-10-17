
package one.microstream.entity;

import static one.microstream.chars.XChars.systemString;

import one.microstream.chars.VarString;


/**
 * 
 * @author FH
 */
public class EntityExceptionUnaccessibleEntityType extends EntityException
{
	private final Entity entity;
	
	public EntityExceptionUnaccessibleEntityType(final Entity entity)
	{
		super();
		
		this.entity = entity;
	}
	
	public final Entity entity()
	{
		return this.entity;
	}
	
	@Override
	public String assembleDetailString()
	{
		return VarString.New()
			.add("Unaccessible entity type: ")
			.add(systemString(this.entity))
			.toString();
	}
}
