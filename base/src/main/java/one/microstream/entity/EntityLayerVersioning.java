
package one.microstream.entity;

import static one.microstream.X.notNull;

import one.microstream.collections.EqHashTable;
import one.microstream.collections.types.XGettingTable;

/**
 * 
 * 
 */
public final class EntityLayerVersioning<K> extends EntityLayer
{
	EntityVersionContext<K> context ;
	EqHashTable<K, Entity>  versions;
	
	protected EntityLayerVersioning(
		final Entity                  inner  ,
		final EntityVersionContext<K> context
	)
	{
		super(inner);
		
		this.context  = notNull(context);
		this.versions = EqHashTable.New(context.equalator());
	}
	
	synchronized XGettingTable<K, Entity> versions()
	{
		return this.versions.immure();
	}
	
	@Override
	protected synchronized Entity entityData()
	{
		final K versionKey = this.context.currentVersion();
		if(versionKey == null)
		{
			return super.entityData();
		}
		
		final Entity versionedData = this.versions.get(versionKey);
		if(versionedData == null)
		{
			throw new EntityExceptionMissingDataForVersion(this.entityIdentity(), versionKey);
		}
		
		return versionedData;
	}
	
	@Override
	protected synchronized void entityCreated()
	{
		final K versionKey = this.context.versionForUpdate();
		if(versionKey != null)
		{
			this.versions.put(versionKey, super.entityData());
		}
		
		super.entityCreated();
	}
	
	@Override
	protected synchronized boolean updateEntityData(final Entity data)
	{
		final K versionKey = this.context.versionForUpdate();
		if(versionKey != null)
		{
			this.versions.put(versionKey, data);
			
			EntityVersionCleaner<K> cleaner;
			if((cleaner = this.context.cleaner()) != null)
			{
				cleaner.cleanVersions(this.versions);
			}
		}
		
		return super.updateEntityData(data);
	}
	
}
