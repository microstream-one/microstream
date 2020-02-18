package one.microstream.storage.restclient;

import java.util.List;
import java.util.Map;

import one.microstream.persistence.types.PersistenceTypeDescription;
import one.microstream.storage.restadapter.ViewerObjectDescription;
import one.microstream.storage.restadapter.ViewerRootDescription;

public interface StorageView
{
	public StorageObject root();
	
	
	public static StorageView New(
		final StorageViewConfiguration configuration,
		final StorageRestClient client
	)
	{
		return new StorageView.Default(
			configuration,
			client
		);
	}
	
	
	public static class Default implements StorageView
	{
		private final StorageViewConfiguration        configuration;
		private final StorageRestClient               client;
		private Map<Long, PersistenceTypeDescription> typeDictionary;

		Default(
			final StorageViewConfiguration configuration,
			final StorageRestClient client
		)
		{
			super();
			this.configuration = configuration;
			this.client        = client;
		}
		
		private List<StorageViewElement> getMembers(final StorageViewElement parent)
		{
			
			
			
			
			return null;
		}
		
		private StorageObject getObject(
			final StorageViewElement parent,
			final ViewerObjectDescription obj,
			final String name
		)
		{
			final long                       objectId        = Long.parseLong(obj.getObjectId());
			final PersistenceTypeDescription typeDescription = this.getTypeDescription(obj);
			return new StorageObject.Default(
				parent,
				name,
				null,
				objectId,
				typeDescription,
				this::getMembers
			);
		}

		private PersistenceTypeDescription getTypeDescription(
			final ViewerObjectDescription obj
		)
		{
			final long typeId = Long.parseLong(obj.getTypeId());
			
			PersistenceTypeDescription typeDescription;
			if(this.typeDictionary == null || (typeDescription = this.typeDictionary.get(typeId)) == null)
			{
				this.typeDictionary = this.client.requestTypeDictionary();
			}
			typeDescription = this.typeDictionary.get(typeId);
			if(typeDescription == null)
			{
				// TODO proper exception
				throw new RuntimeException("Missing type description, typeId=" + typeId);
			}
			return typeDescription;
		}
		
		
		@Override
		public StorageObject root()
		{
			final ViewerRootDescription   rootDesc       = this.client.requestRoot();
			final ViewerObjectDescription rootObjectDesc = this.client.requestObjectWithReferences(
				rootDesc.getObjectId()
			);
			
			return this.getObject(
				null, // root has no parent
				rootObjectDesc,
				rootDesc.getName()
			);
		}
		
	}
	
}
