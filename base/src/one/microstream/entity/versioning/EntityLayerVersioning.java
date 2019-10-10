
package one.microstream.entity.versioning;

import static one.microstream.X.notNull;

import java.util.Comparator;

import one.microstream.collections.EqHashTable;
import one.microstream.collections.types.XGettingTable;
import one.microstream.entity.Entity;
import one.microstream.entity.EntityLayer;
import one.microstream.typing.KeyValue;


public class EntityLayerVersioning<K extends Comparable<? super K>> extends EntityLayer
{
	private final EntityVersionContext<K>             versionContext;
	private final EqHashTable<K, Entity>              versions;
	private transient Comparator<KeyValue<K, Entity>> keyValueComparator;
	
	protected EntityLayerVersioning(final Entity inner, final EntityVersionContext<K> versionContext)
	{
		super(inner);
		
		this.versionContext = notNull(versionContext);
		this.versions       = EqHashTable.New(versionContext.equalator());
	}
	
	protected XGettingTable<K, Entity> versions()
	{
		return this.versions.view();
	}
	
	@Override
	protected synchronized Entity $entityData()
	{
		final K versionKey = this.versionContext.currentVersion();
		if(versionKey == null)
		{
			return super.$entityData();
		}
		
		final Entity versionedData = this.versions.get(versionKey);
		if(versionedData == null)
		{
			throw new EntityVersionException("No data for version " + versionKey);
		}
		
		return versionedData;
	}
	
	@Override
	protected synchronized boolean $updateEntityData(final Entity data)
	{
		final K versionKey = this.versionContext.versionForUpdate();
		if(versionKey == null)
		{
			super.$updateEntityData(data);
		}
		else
		{
			this.versions.put(versionKey, data);
			
			final long maxPreservedVersions = this.versionContext.maxPreservedVersions();
			if(this.versions.size() > maxPreservedVersions)
			{
				this.versions.sort(this.keyValueComparator());
				do
				{
					this.versions.pinch();
				}
				while(this.versions.size() > maxPreservedVersions);
			}
		}
		
		return true;
	}
	
	private Comparator<KeyValue<K, Entity>> keyValueComparator()
	{
		if(this.keyValueComparator == null)
		{
			final Comparator<? super K> comparator = this.versionContext.comparator();
			this.keyValueComparator = (kv1, kv2) -> comparator.compare(kv1.key(), kv2.key());
		}
		
		return this.keyValueComparator;
	}
}
