package one.microstream.storage.restclient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import one.microstream.X;
import one.microstream.persistence.types.PersistenceTypeDescription;
import one.microstream.persistence.types.PersistenceTypeDescriptionMember;
import one.microstream.storage.restadapter.ViewerObjectDescription;
import one.microstream.storage.restadapter.ViewerRootDescription;

public interface StorageView
{
	public StorageViewElement root();
	
	public List<StorageViewElement> members(long objectId, long offset, long length);
	
	
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
		
		// TODO handle case when root is value object
		@Override
		public StorageViewElement root()
		{
			final ViewerRootDescription      rootDesc   = this.client.requestRoot();
			final long                       objectId   = rootDesc.getObjectId();
			final ViewerObjectDescription    objectDesc = this.client.requestObject(objectId);
			final long                       length     = Long.parseLong(objectDesc.getLength());
			final PersistenceTypeDescription typeDesc   = this.getTypeDescription(objectDesc);
			return new StorageViewObject.Default(
				this,
				rootDesc.getName(),
				"",
				objectId,
				typeDesc,
				length
			);
		}
		
		@Override
		public List<StorageViewElement> members(
			final long objectId,
			final long offset,
			final long length
		)
		{
			final long range;
			if(length > (range = this.configuration.elementRangeMaximumLength()))
			{
				return this.ranges(objectId, offset, length, range, range);
			}
			
			final List<StorageViewElement> members = new ArrayList<>(X.checkArrayRange(length));
			
			final ViewerObjectDescription    objectDesc = this.client.requestObjectWithReferences(objectId, offset, length);
			final Object[]                   data       = objectDesc.getData();
			final ViewerObjectDescription[]  references = objectDesc.getReferences();
			final PersistenceTypeDescription typeDesc   = this.getTypeDescription(objectDesc);

			int i = 0;
			for(final PersistenceTypeDescriptionMember typeDescMember : typeDesc.allMembers())
			{
				this.createElements(
					members,
					typeDescMember,
					data[i],
					i < references.length ? references[i] : null
				);
				i++;
			}
			
			return members;
		}
		
		private void createElements(
			final List<StorageViewElement> members,
			final PersistenceTypeDescriptionMember typeDescMember,
			final Object data,
			final ViewerObjectDescription reference
		)
		{
			if(typeDescMember.isPrimitive())
			{
				members.add(new StorageViewValue.Default(this, typeDescMember.name(), String.valueOf(data)));
			}
			else if(typeDescMember.isReference())
			{
				if(reference == null)
				{
					members.add(new StorageViewValue.Default(this, typeDescMember.name(), "null"));
				}
				else
				{
					final long                       objectId = Long.parseLong(reference.getObjectId());
					final long                       length   = Long.parseLong(reference.getLength());
					final PersistenceTypeDescription typeDesc = this.getTypeDescription(reference);
					members.add(new StorageViewObject.Default(this, typeDescMember.name(), "", objectId, typeDesc, length));
				}
			}
			else if(typeDescMember.isVariableLength())
			{
				// XXX waiting for fix (HG)
			}
		}
		
		private List<StorageViewElement> ranges(
			final long objectId,
			final long offset,
			final long length,
			final long range,
			final long maxRange
		)
		{
			final long nextRange;
			if(length > (nextRange = range * maxRange))
			{
				return this.ranges(objectId, offset, length, nextRange, maxRange);
			}
			
			final List<StorageViewElement> ranges = new ArrayList<>(X.checkArrayRange(length / range + 1));
			for(long i = 0; i < length; i += range)
			{
				final long   rangeOffset = i + offset;
				final long   rangeLength = Math.min(range, length - i);
				final long   rangeEnd    = rangeOffset + rangeLength - 1;
				final String name        = "[" + rangeOffset + "..." + rangeEnd + "]";
				ranges.add(new StorageViewRange.Default(this, name, objectId, rangeOffset, rangeLength));
			}
			return ranges;
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
		
	}
	
}
