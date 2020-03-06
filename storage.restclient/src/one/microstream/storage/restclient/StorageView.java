package one.microstream.storage.restclient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import one.microstream.X;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.persistence.types.PersistenceTypeDescription;
import one.microstream.persistence.types.PersistenceTypeDescriptionMember;
import one.microstream.persistence.types.PersistenceTypeDescriptionMemberFieldGeneric;
import one.microstream.persistence.types.PersistenceTypeDescriptionMemberFieldGenericComplex;
import one.microstream.storage.restadapter.ViewerObjectDescription;
import one.microstream.storage.restadapter.ViewerRootDescription;

public interface StorageView
{
	public StorageViewElement root();
	
	
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
			final ViewerObjectDescription    objectDesc = this.client.requestObject(
				ObjectRequest.New(objectId)
			);
			final PersistenceTypeDescription typeDesc   = this.getTypeDescription(objectDesc);
			return new StorageViewObject.Default(
				this,
				rootDesc.getName(),
				"",
				objectId,
				Long.parseLong(objectDesc.getLength()),
				typeDesc
			);
		}
		
		List<StorageViewElement> members(
			final long objectId,
			final long fixedLength
		)
		{
			final List<StorageViewElement>   members     = new ArrayList<>();
			final ViewerObjectDescription    objectDesc  = this.client.requestObject(
				ObjectRequest.Builder(objectId)
					.withReferences()
					.referenceLength(fixedLength)
					.variableLength(0L)
					.build()
			);
			final PersistenceTypeDescription typeDesc    = this.getTypeDescription(objectDesc);
			final XGettingSequence<? extends PersistenceTypeDescriptionMember>
			                                 typeMembers = typeDesc.allMembers();
			final Object[]                   data        = objectDesc.getData();
			final ViewerObjectDescription[]  references  = objectDesc.getReferences();
			
			final int length = Integer.parseInt(objectDesc.getLength());
			      int index  = 0;
			for(; index < length; index++)
			{
				final PersistenceTypeDescriptionMember typeMember = typeMembers.at(index);
				if(index < references.length && references[index] != null)
				{
					final ViewerObjectDescription reference = references[index];
					members.add(new StorageViewObject.Default(
						this,
						typeMember.name(),
						"",
						Long.parseLong(reference.getObjectId()),
						Long.parseLong(reference.getLength()),
						this.getTypeDescription(reference)
					));
				}
				else
				{
					final String dataString = typeMember.isReference()
						? "null"
						: String.valueOf(data[index]);
					members.add(new StorageViewValue.Default(
						this,
						typeMember.name(),
						dataString
					));
				}
			}
			
			final String[] varLengthArray = objectDesc.getVariableLength();
			final int      varLength      = varLengthArray != null && varLengthArray.length == 1
				? Integer.parseInt(varLengthArray[0])
				: 0;
			if(varLength > 0)
			{
				if(members.isEmpty())
				{
					members.addAll(this.variableMembers(objectId, 0, varLength));
				}
				else
				{
					final PersistenceTypeDescriptionMember typeMember = typeMembers.at(index);
					members.add(new StorageViewRange.Default(
						this,
						typeMember.name(),
						objectId,
						0,
						varLength
					));
				}
			}
						
			return members;
		}
		
		@SuppressWarnings("unchecked")
		List<StorageViewElement> variableMembers(
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
			
			final List<StorageViewElement>   members      = new ArrayList<>();
			final ViewerObjectDescription    objectDesc   = this.client.requestObject(
				ObjectRequest.Builder(objectId)
					.withReferences()
					.fixedLength(0L)
					.variableRange(offset, length)
					.build()
			);
			final PersistenceTypeDescription typeDesc     = this.getTypeDescription(objectDesc);
			final long                       memberOffset = Long.parseLong(objectDesc.getLength());
			
			final PersistenceTypeDescriptionMemberFieldGenericComplex varMember =
				(PersistenceTypeDescriptionMemberFieldGenericComplex)typeDesc.allMembers().at(memberOffset);
			
			final Object       dataObj  = objectDesc.getData()[0];
			final List<Object> dataList = dataObj instanceof List
				? (List<Object>)dataObj
				: Arrays.asList((Object[])dataObj);
						
			final XGettingSequence<PersistenceTypeDescriptionMemberFieldGeneric> elemMembers = varMember.members();
			if(elemMembers.size() == 1)
			{
				final PersistenceTypeDescriptionMemberFieldGeneric elemMember  = elemMembers.get();
				if(elemMember.isReference())
				{
					final PersistenceTypeDescription elemTypeDesc = this.getTypeDescription(elemMember.typeName());
					
					long fixedLength = 0;
					for(final PersistenceTypeDescriptionMember m : elemTypeDesc.allMembers())
					{
						if(m.isFixedLength())
						{
							fixedLength++;
						}
					}
					
					long index = offset;
					for(final Object dataElem : dataList)
					{
						// TODO resolve via refs
						final long elemObjectId = Long.parseLong(dataElem.toString());
						members.add(new StorageViewObject.Default(
							this,
							"[" + index++ + "]",
							"",
							elemObjectId,
							fixedLength,
							elemTypeDesc
						));
					}
				}
				else
				{
					long index = offset;
					for(final Object dataElem : dataList)
					{
						members.add(new StorageViewValue.Default(
							this,
							"[" + index++ + "]",
							String.valueOf(dataElem)
						));
					}
				}
			}
			else
			{
				
			}
			
			return members;
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

		// TODO externalize to type cache
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
		
		private PersistenceTypeDescription getTypeDescription(
			final String typeName
		)
		{
			PersistenceTypeDescription typeDescription;
			if(this.typeDictionary == null ||
				(typeDescription = this.getTypeDescription(this.typeDictionary, typeName)) == null)
			{
				this.typeDictionary = this.client.requestTypeDictionary();
			}
			typeDescription = this.getTypeDescription(this.typeDictionary, typeName);
			if(typeDescription == null)
			{
				// TODO proper exception
				throw new RuntimeException("Missing type description, typeName=" + typeName);
			}
			return typeDescription;
		}
		
		
		private PersistenceTypeDescription getTypeDescription(
			final Map<Long, PersistenceTypeDescription> typeDictionary,
			final String typeName
		)
		{
			return typeDictionary.values().stream()
				.filter(t -> t.typeName().equals(typeName))
				.findFirst()
				.orElse(null);
		}
	}
	
}
