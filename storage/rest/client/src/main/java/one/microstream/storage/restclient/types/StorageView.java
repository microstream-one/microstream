package one.microstream.storage.restclient.types;

/*-
 * #%L
 * microstream-storage-restclient
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

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
import one.microstream.storage.restadapter.types.ViewerObjectDescription;
import one.microstream.storage.restadapter.types.ViewerRootDescription;
import one.microstream.storage.restclient.exceptions.StorageViewExceptionMissingTypeDescription;

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
			
			if(rootDesc.getObjectId() > 0)
			{
				final ViewerObjectDescription    objectDesc = this.client.requestObject(
						this.objectRequestBuilder(rootDesc.getObjectId()).build()
				);
				
				return this.createElement(
					null,
					rootDesc.getName(),
					objectDesc
				);
			}
			//special case for not yet set root
			return new StorageViewValue.Default(
				null,
				null,
				rootDesc.getName(),
				"NOT YET DEFINED",
				null
			);
		}
		
		List<StorageViewElement> members(
			final StorageViewObject parent
		)
		{
			final long                     objectId   = parent.objectId();
			final List<StorageViewElement> members    = new ArrayList<>();
			final ViewerObjectDescription  objectDesc = this.client.requestObject(
				this.objectRequestBuilder(objectId)
					.withReferences()
					.variableLength(0L)
					.build()
			);
			
			final XGettingSequence<? extends PersistenceTypeDescriptionMember>
                                                    typeMembers = this.getTypeMembers(objectDesc);
			final Iterator<ViewerObjectDescription> references  = Arrays.asList(objectDesc.getReferences()).iterator();
			final Object[]                          data        = objectDesc.getData();

			final int length = Integer.parseInt(objectDesc.getLength());
			      int index  = 0;
			for(; index < length; index++)
			{
				final PersistenceTypeDescriptionMember typeMember = typeMembers.at(index);
				members.add(this.createElement(
					parent,
					typeMember.name(),
					references,
					typeMember,
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
					members.addAll(this.variableMembers(parent, objectId, 0, varLength));
				}
				else
				{
					final PersistenceTypeDescriptionMember typeMember = typeMembers.at(index);
					members.add(new StorageViewRange.Default(
						this,
						parent,
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
			final StorageViewElement parent,
			final long objectId,
			final long offset,
			final long length
		)
		{
			final long range;
			if(length > (range = this.configuration.elementRangeMaximumLength()))
			{
				return this.ranges(
					parent,
					objectId,
					offset,
					length,
					range,
					range
				);
			}
			
			final List<StorageViewElement>   members      = new ArrayList<>();
			final ViewerObjectDescription    objectDesc   = this.client.requestObject(
				this.objectRequestBuilder(objectId)
					.withReferences()
					.fixedLength(0L)
					.variableRange(offset, length)
					.build()
			);

			final XGettingSequence<? extends PersistenceTypeDescriptionMember>
			           typeMembers  = this.getTypeMembers(objectDesc);
			final long memberOffset = Long.parseLong(objectDesc.getLength());
			
			final PersistenceTypeDescriptionMemberFieldGenericComplex varMember =
				(PersistenceTypeDescriptionMemberFieldGenericComplex)typeMembers.at(memberOffset);

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
							parent,
							"[" + index++ + "]",
							references,
							elemMember,
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
							parent,
							"[" + index++ + "]",
							this.value(String.valueOf(dataElem), null, elemMember.typeName()),
							elemMember.typeName()
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
								parent,
								elemMember.name(),
								references,
								elemMember,
								subDataElem
							));
						}
						else
						{
							memberMembers.add(new StorageViewValue.Default(
								this,
								parent,
								elemMember.name(),
								this.value(String.valueOf(subDataElem), null, elemMember.typeName()),
								elemMember.typeName()
							));
						}
					}
					
					members.add(new StorageViewComplexRangeEntry.Default(
						this,
						parent,
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
			final StorageViewElement parent,
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
				return this.ranges(
					parent,
					objectId,
					offset,
					length,
					nextRange,
					maxRange
				);
			}
			
			final List<StorageViewElement> ranges = new ArrayList<>(X.checkArrayRange(length / range + 1));
			for(long i = 0; i < length; i += range)
			{
				final long   rangeOffset = i + offset;
				final long   rangeLength = Math.min(range, length - i);
				final long   rangeEnd    = rangeOffset + rangeLength - 1;
				final String name        = "[" + rangeOffset + ".." + rangeEnd + "]";
				ranges.add(new StorageViewRange.Default(
					this,
					parent,
					name,
					objectId,
					rangeOffset,
					rangeLength
				));
			}
			return ranges;
		}
		
		private StorageViewElement createElement(
			final StorageViewElement parent,
			final String name,
			final Iterator<ViewerObjectDescription> references,
			final PersistenceTypeDescriptionMember member,
			final Object data
		)
		{
			final ViewerObjectDescription reference;
			if(references.hasNext() && (reference = references.next()) != null)
			{
				return this.createElement(
					parent,
					name,
					reference
				);
			}
			
			final String dataString = member.isReference()
				? "null"
				: this.value(String.valueOf(data), null, member.typeName());
			return new StorageViewValue.Default(
				this,
				parent,
				name,
				dataString,
				member.typeName()
			);
		}
		
		private StorageViewElement createElement(
			final StorageViewElement parent,
			final String name,
			final ViewerObjectDescription reference
		)
		{
			final PersistenceTypeDescription typeDescription = this.getTypeDescription(reference);
			if(reference.getSimplified())
			{
				final String value = this.value(
					String.valueOf(reference.getData()[0]),
					reference,
					typeDescription.typeName()
				);
				return new StorageViewObject.Simple(
					this,
					parent,
					name,
					value,
					typeDescription,
					Long.parseLong(reference.getObjectId())
				);
			}
			
			return new StorageViewObject.Complex(
				this,
				parent,
				name,
				null,
				typeDescription,
				Long.parseLong(reference.getObjectId())
			);
		}
		
		private String value(
			final String value,
			final ViewerObjectDescription reference,
			final String typeName
		)
		{
			final ValueRenderer valueRenderer = this.configuration.provideValueRenderer(typeName);
			return valueRenderer != null
				? valueRenderer.apply(value, reference)
				: value;
		}

		private XGettingSequence<? extends PersistenceTypeDescriptionMember> getTypeMembers(
			final ViewerObjectDescription objectDesc
		)
		{
			return this.getTypeMembers(
				this.getTypeDescription(objectDesc)
			);
		}

		private XGettingSequence<? extends PersistenceTypeDescriptionMember> getTypeMembers(
			final PersistenceTypeDescription typeDesc
		)
		{
			return typeDesc
				.allMembers()
				.filterTo(
					BulkList.New(),
					m -> !m.isEnumConstant()
				)
			;
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
				throw new StorageViewExceptionMissingTypeDescription(typeId);
			}
			return typeDescription;
		}
		
		private ObjectRequest.Builder objectRequestBuilder(final long objectId)
		{
			return ObjectRequest.Builder(objectId)
				.valueLength(this.configuration.maxValueLength())
			;
		}
	}
	
}
