
package one.microstream.storage.restclient;

import static one.microstream.X.notNull;

import java.util.Map;

import one.microstream.persistence.types.PersistenceTypeDescription;
import one.microstream.storage.restadapter.ViewerObjectDescription;


public interface StorageRestConnection
{
	public StorageObject root();
	
	public static StorageRestConnection New(
		final StorageRestClient restClient
	)
	{
		return new Default(restClient);
	}
	
	public static class Default implements StorageRestConnection
	{
		private final StorageRestClient               restClient;
		private Map<Long, PersistenceTypeDescription> typeDictionary;
		
		Default(
			final StorageRestClient restClient
		)
		{
			super();
			this.restClient = notNull(restClient);
		}
		
		private StorageObject createObject(
			final ViewerObjectDescription desc
		)
		{
			final long objectId = Long.parseLong(desc.getObjectId());
			final long typeId   = Long.parseLong(desc.getTypeId());
			final long length   = Long.parseLong(desc.getLength());
			
			PersistenceTypeDescription typeDescription;
			if(this.typeDictionary == null || (typeDescription = this.typeDictionary.get(typeId)) == null)
			{
				this.typeDictionary = this.restClient.requestTypeDictionary();
			}
			typeDescription = this.typeDictionary.get(typeId);
			if(typeDescription == null)
			{
				throw new StorageRestClientException("Missing type description, typeId=" + typeId);
			}
			
			return new StorageObject.Default(objectId, typeDescription, length, desc.getData());
		}
		
		@Override
		public StorageObject root()
		{
			return this.createObject(
				this.restClient.requestObjectWithReferences(
					this.restClient.requestRoot().getObjectId()
				)
			);
		}
		
	}
	
}
