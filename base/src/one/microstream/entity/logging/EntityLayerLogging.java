
package one.microstream.entity.logging;

import static one.microstream.X.notNull;

import one.microstream.entity.Entity;
import one.microstream.entity.EntityLayer;


public class EntityLayerLogging extends EntityLayer
{
	private final EntityLogger logger;
	
	protected EntityLayerLogging(final Entity inner, final EntityLogger logger)
	{
		super(inner);
		
		this.logger = notNull(logger);
	}
	
	@Override
	protected Entity $entityData()
	{
		final Entity data = super.$entityData();
		
		this.logger.afterRead(this.$entityIdentity(), data);
		
		return data;
	}
	
	@Override
	protected boolean $updateEntityData(final Entity newData)
	{
		final Entity identity = this.$entityIdentity();
		
		this.logger.beforeUpdate(identity, newData);
		
		final boolean success = super.$updateEntityData(newData);
		
		this.logger.afterUpdate(identity, newData, success);
		
		return success;
	}
}
