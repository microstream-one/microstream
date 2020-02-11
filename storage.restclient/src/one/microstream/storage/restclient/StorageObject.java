
package one.microstream.storage.restclient;

import one.microstream.persistence.types.PersistenceTypeDescription;


public interface StorageObject
{
	public long objectId();
	
	public PersistenceTypeDescription typeDescription();
	
	public long length();
	
	public Object[] data();
		
	
	public static class Default implements StorageObject
	{
		private final long                       objectId;
		private final PersistenceTypeDescription typeDescription;
		private final long                       length;
		private final Object[]                   data;
		
		Default(
			final long objectId,
			final PersistenceTypeDescription typeDescription,
			final long length,
			final Object[] data
		)
		{
			super();
			this.objectId        = objectId;
			this.typeDescription = typeDescription;
			this.length          = length;
			this.data            = data;
		}
		
		@Override
		public long objectId()
		{
			return this.objectId;
		}
		
		@Override
		public PersistenceTypeDescription typeDescription()
		{
			return this.typeDescription;
		}
		
		@Override
		public long length()
		{
			return this.length;
		}
		
		@Override
		public Object[] data()
		{
			return this.data;
		}
		
	}
	
}
