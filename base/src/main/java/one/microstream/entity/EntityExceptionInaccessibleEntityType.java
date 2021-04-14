
package one.microstream.entity;

import static one.microstream.chars.XChars.systemString;

import one.microstream.chars.VarString;


/**
 * 
 * 
 */
public class EntityExceptionInaccessibleEntityType extends EntityException
{
	private final Entity entity;
	
	public EntityExceptionInaccessibleEntityType(final Entity entity)
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
			.add("Inaccessible entity type: ")
			.add(systemString(this.entity))
			.toString();
	}
}
