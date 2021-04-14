
package one.microstream.entity;

import static one.microstream.chars.XChars.systemString;

import one.microstream.chars.VarString;


/**
 * 
 * 
 */
public class EntityExceptionMissingDataForVersion extends EntityException
{
	private final Entity entity;
	private final Object versionKey;
	
	public EntityExceptionMissingDataForVersion(final Entity entity, final Object versionKey)
	{
		super();
		
		this.entity     = entity;
		this.versionKey = versionKey;
	}
	
	public final Entity entity()
	{
		return this.entity;
	}
	
	public final Object versionKey()
	{
		return this.versionKey;
	}
	
	@Override
	public String assembleDetailString()
	{
		return VarString.New()
			.add("Missing data for version '")
			.add(this.versionKey)
			.add("' in ")
			.add(systemString(this.entity))
			.toString();
	}
}
