package one.microstream.storage.restclient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import one.microstream.X;
import one.microstream.collections.BulkList;
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
		
		@Override
		public StorageViewElement root()
		{
			final ViewerRootDescription      rootDesc   = this.client.requestRoot();
			final ViewerObjectDescription    objectDesc = this.client.requestObject(
				ObjectRequest.New(rootDesc.getObjectId())
			);
			return this.createStorageViewObject(
				rootDesc.getName(),
				objectDesc,
				null,
				null
			);
		}
		
		List<StorageViewElement> members(
			final long objectId
		)
		{
			final List<StorageViewElement>   members     = new ArrayList<>();
			final ViewerObjectDescription    objectDesc  = this.client.requestObject(
				ObjectRequest.Builder(objectId)
					.withReferences()
					.variableLength(0L)
					.build()
			);
			
			final XGettingSequence<? extends PersistenceTypeDescriptionMember> typeMembers =
				this.getTypeDescription(objectDesc)
					.allMembers()
					.filterTo(
						BulkList.New(),
						m -> !m.isEnumConstant()
					)
			;
			
			final Iterator<ViewerObjectDescription> references = Arrays.asList(objectDesc.getReferences()).iterator();
			final Object[]                          data       = objectDesc.getData();

			final int length = Integer.parseInt(objectDesc.getLength());
			      int index  = 0;
			for(; index < length; index++)
			{
				final PersistenceTypeDescriptionMember typeMember = typeMembers.at(index);
				members.add(this.createElement(
					typeMember,
					typeMember.name(),
					references,
					data[index]
				));
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

			final Iterator<ViewerObjectDescription> references = Arrays.asList(objectDesc.getReferences()).iterator();
			final List<Object>                      dataList   = asList(objectDesc.getData()[0]);
						
			final XGettingSequence<PersistenceTypeDescriptionMemberFieldGeneric> elemMembers = varMember.members();
			if(elemMembers.size() == 1)
			{
				final PersistenceTypeDescriptionMemberFieldGeneric elemMember  = elemMembers.get();
				if(elemMember.isReference())
				{
					long index = offset;
					for(final Object dataElem : dataList)
					{
						members.add(this.createElement(
							elemMember,
							"[" + index++ + "]",
							references,
							dataElem
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
				long index = offset;
				for(final Object dataElem : dataList)
				{
					final List<StorageViewElement> memberMembers = new ArrayList<>();
					
					int subIndex = 0;
					final List<Object> dataElemList = asList(dataElem);
					for(final PersistenceTypeDescriptionMemberFieldGeneric elemMember : elemMembers)
					{
						final Object subDataElem = dataElemList.get(subIndex++);
						if(elemMember.isReference())
						{
							memberMembers.add(this.createElement(
								elemMember,
								elemMember.name(),
								references,
								subDataElem
							));
						}
						else
						{
							memberMembers.add(new StorageViewValue.Default(
								this,
								elemMember.name(),
								String.valueOf(subDataElem)
							));
						}
					}
					
					members.add(new StorageViewComplexRangeEntry.Default(
						this,
						"[" + index++ + "]",
						"",
						memberMembers
					));
				}
			}
			
			return members;
		}

		@SuppressWarnings("unchecked")
		private static List<Object> asList(
			final Object listOrArray
		)
		{
			return listOrArray instanceof List
				? (List<Object>)listOrArray
				: Arrays.asList((Object[])listOrArray);
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
		
		private StorageViewElement createElement(
			final PersistenceTypeDescriptionMember member,
			final String name,
			final Iterator<ViewerObjectDescription> references,
			final Object data
		)
		{
			ViewerObjectDescription reference;
			if(references.hasNext() && (reference = references.next()) != null)
			{
				return this.createStorageViewObject(
					name,
					reference,
					data,
					member
				);
			}
			
			final String dataString = member.isReference()
				? "null"
				: String.valueOf(data);
			return new StorageViewValue.Default(
				this,
				name,
				dataString
			);
		}
		
		private StorageViewObject createStorageViewObject(
			final String name,
			final ViewerObjectDescription reference,
			final Object data,
			final PersistenceTypeDescriptionMember member
		)
		{
			return new StorageViewObject.Complex(
				this,
				name,
				null,
				Long.parseLong(reference.getObjectId()),
				this.getTypeDescription(reference)
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
	}
	
}
