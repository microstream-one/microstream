
package one.microstream.storage.restclient.types;

import java.util.List;

import one.microstream.persistence.types.PersistenceTypeDescription;


public interface StorageViewObject extends StorageViewValue
{
	public PersistenceTypeDescription typeDescription();
	
	public long objectId();
	
	
	public static class Simple extends StorageViewValue.Default implements StorageViewObject
	{
		final PersistenceTypeDescription typeDescription;
		final long                       objectId;
		
		Simple(
			final StorageView.Default view,
			final StorageViewElement parent,
			final String name,
			final String value,
			final PersistenceTypeDescription typeDescription,
			final long objectId
		)
		{
			super(view, parent, name, value, typeDescription.typeName());
			
			this.typeDescription = typeDescription;
			this.objectId        = objectId;
		}
		
		@Override
		public PersistenceTypeDescription typeDescription()
		{
			return this.typeDescription;
		}

		@Override
		public long objectId()
		{
			return this.objectId;
		}
		
		@Override
		public String toString()
		{
			return super.toString() + " " + this.objectId;
		}
	}
	
	
	public static class Complex extends Simple
	{
		private List<StorageViewElement> members;
		
		Complex(
			final StorageView.Default view,
			final StorageViewElement parent,
			final String name,
			final String data,
			final PersistenceTypeDescription typeDescription,
			final long objectId
		)
		{
			super(view, parent, name, data, typeDescription, objectId);
		}
		
		@Override
		public boolean hasMembers()
		{
			return this.typeDescription.allMembers().size() > 0;
		}
		
		@Override
		public List<StorageViewElement> members(
			final boolean forceRefresh
		)
		{
			if(this.members == null || forceRefresh)
			{
				this.members = this.view().members(this);
			}
			return this.members;
		}
		
	}
	
}
