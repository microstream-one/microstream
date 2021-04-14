package one.microstream.storage.types;

import java.util.function.Consumer;

import one.microstream.collections.HashMapIdObject;
import one.microstream.collections.types.XGettingTable;
import one.microstream.persistence.exceptions.PersistenceExceptionTypeHandlerConsistencyUnhandledTypeId;
import one.microstream.persistence.types.PersistenceTypeDefinition;
import one.microstream.persistence.types.PersistenceTypeDefinitionRegistrationObserver;
import one.microstream.persistence.types.PersistenceTypeDescription;
import one.microstream.persistence.types.PersistenceTypeDictionary;
import one.microstream.persistence.types.PersistenceTypeDictionaryView;
import one.microstream.persistence.types.PersistenceTypeLineage;
import one.microstream.storage.exceptions.StorageException;
import one.microstream.storage.exceptions.StorageExceptionConsistency;
import one.microstream.storage.exceptions.StorageExceptionInitialization;


public interface StorageTypeDictionary extends PersistenceTypeDictionary, PersistenceTypeDefinitionRegistrationObserver
{
	public <P extends Consumer<? super StorageEntityTypeHandler>> P iterateTypeHandlers(P procedure);

	public StorageEntityTypeHandler lookupTypeHandler(long typeId);
	
	public default StorageEntityTypeHandler lookupTypeHandlerChecked(final long typeId)
	{
		final StorageEntityTypeHandler typeHandler = this.lookupTypeHandler(typeId);
		if(typeHandler != null)
		{
			return typeHandler;
		}
		
		throw new StorageException("TypeId not resolvable via type dictionary: " + typeId);
	}

	public void validateEntityTypeId(long typeId);

	public StorageEntityTypeHandler validateEntity(long length, long typeId, long objectId);

	public void validate(PersistenceTypeDictionary typeDictionary);
	
	public StorageTypeDictionary initialize(PersistenceTypeDictionary typeDictionary);
	
	@Override
	public StorageTypeDictionary setTypeDescriptionRegistrationObserver(
		PersistenceTypeDefinitionRegistrationObserver observer
	);



	public final class Default implements StorageTypeDictionary
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final HashMapIdObject<StorageEntityTypeHandler> registry        = HashMapIdObject.New();
		private final boolean                                   switchByteOrder;
		private       PersistenceTypeDictionary                 dictionary     ;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		public Default(final boolean switchByteOrder)
		{
			super();
			this.switchByteOrder = switchByteOrder;
		}
		


		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		final void deriveHandler(final PersistenceTypeDefinition typeDefinition)
		{
			synchronized(this.registry)
			{
				this.registry.put(
					typeDefinition.typeId(),
					new StorageEntityTypeHandler.Default(typeDefinition, this.switchByteOrder)
				);
			}
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final <P extends Consumer<? super StorageEntityTypeHandler>> P iterateTypeHandlers(
			final P procedure
		)
		{
			synchronized(this.registry)
			{
				this.registry.iterateValues(procedure);
			}
			return procedure;
		}

		@Override
		public final StorageEntityTypeHandler lookupTypeHandler(final long typeId)
		{
			synchronized(this.registry)
			{
				return this.registry.get(typeId);
			}
		}

		@Override
		public final boolean registerTypeDefinition(final PersistenceTypeDefinition typeDefinition)
		{
			synchronized(this.registry)
			{
				return this.dictionary.registerTypeDefinition(typeDefinition);
			}
		}

		@Override
		public boolean registerRuntimeTypeDefinition(final PersistenceTypeDefinition typeDefinition)
		{
			synchronized(this.registry)
			{
				return this.dictionary.registerRuntimeTypeDefinition(typeDefinition);
			}
		}

		@Override
		public boolean registerRuntimeTypeDefinitions(final Iterable<? extends PersistenceTypeDefinition> typeDefinitions)
		{
			synchronized(this.registry)
			{
				return this.dictionary.registerRuntimeTypeDefinitions(typeDefinitions);
			}
		}

		@Override
		public final boolean registerTypeDefinitions(
			final Iterable<? extends PersistenceTypeDefinition> typeDefinitions
		)
		{
			synchronized(this.registry)
			{
				return this.dictionary.registerTypeDefinitions(typeDefinitions);
			}
		}

		@Override
		public final void validate(final PersistenceTypeDictionary typeDictionary)
		{
			synchronized(this.registry)
			{
				for(final PersistenceTypeDefinition typeDesc : typeDictionary.allTypeDefinitions().values())
				{
					if(PersistenceTypeDescription.equalStructure(typeDesc, this.registry.get(typeDesc.typeId())))
					{
						continue;
					}
					
					throw new StorageException(
						"Invalid type description: " + typeDesc.toTypeIdentifier()
					);
				}
			}
		}

		@Override
		public final void validateEntityTypeId(final long typeId)
		{
			synchronized(this.registry)
			{
				if(this.registry.get(typeId) == null)
				{
					throw new PersistenceExceptionTypeHandlerConsistencyUnhandledTypeId(typeId);
				}
			}
		}

		@Override
		public final StorageEntityTypeHandler validateEntity(
			final long length  ,
			final long typeId  ,
			final long objectId
		)
		{
			synchronized(this.registry)
			{
				final StorageEntityTypeHandler typeHandler = this.lookupTypeHandler(typeId);
				if(typeHandler == null)
				{
					throw new StorageException(
						"Unknown type id " + typeId + " of entity with oid " + objectId + " and length " + length
					);
				}
				typeHandler.validateEntityGuaranteedType(length, objectId);
				return typeHandler;
			}
		}

		@Override
		public XGettingTable<Long, PersistenceTypeDefinition> allTypeDefinitions()
		{
			return this.dictionary.allTypeDefinitions();
		}
		
		@Override
		public final PersistenceTypeDefinition lookupTypeByName(final String typeName)
		{
			return this.dictionary.lookupTypeByName(typeName);
		}

		@Override
		public final PersistenceTypeDefinition lookupTypeById(final long typeId)
		{
			return this.dictionary.lookupTypeById(typeId);
		}

		@Override
		public final long determineHighestTypeId()
		{
			return this.dictionary.determineHighestTypeId();
		}

		@Override
		public final StorageTypeDictionary setTypeDescriptionRegistrationObserver(
			final PersistenceTypeDefinitionRegistrationObserver observer
		)
		{
			// as a storage type dictionary is a registration callback itself, this method is only valid for this
			if(observer != this)
			{
				throw new StorageExceptionConsistency(
					"Inconsistent " + PersistenceTypeDefinitionRegistrationObserver.class.getSimpleName()
				);
			}
			
			return this;
		}

		@Override
		public final PersistenceTypeDefinitionRegistrationObserver getTypeDescriptionRegistrationObserver()
		{
			return this;
		}


		@Override
		public StorageTypeDictionary initialize(final PersistenceTypeDictionary typeDictionary)
		{
			synchronized(this.registry)
			{
				if(this.dictionary != null)
				{
					// ensure idempotency just in case some architecture triggers multiple calls
					if(this.dictionary == typeDictionary)
					{
						return this;
					}
					
					throw new StorageExceptionInitialization("Type dictionary already initialized.");
				}
				
				for(final PersistenceTypeDefinition td : typeDictionary.allTypeDefinitions().values())
				{
					this.deriveHandler(td);
				}
				this.dictionary = typeDictionary;
				
				return this;
			}
		}

		@Override
		public void observeTypeDefinitionRegistration(final PersistenceTypeDefinition typeDefinition)
		{
			this.deriveHandler(typeDefinition);
		}

		@Override
		public XGettingTable<String, ? extends PersistenceTypeLineage> typeLineages()
		{
			return this.dictionary.typeLineages();
		}

		@Override
		public boolean isEmpty()
		{
			return this.registry.isEmpty();
		}

		@Override
		public PersistenceTypeLineage ensureTypeLineage(final Class<?> type)
		{
			return this.dictionary.ensureTypeLineage(type);
		}

		@Override
		public PersistenceTypeLineage lookupTypeLineage(final Class<?> type)
		{
			return this.dictionary.lookupTypeLineage(type);
		}

		@Override
		public PersistenceTypeLineage lookupTypeLineage(final String typeName)
		{
			return this.dictionary.lookupTypeLineage(typeName);
		}
		
		@Override
		public PersistenceTypeDictionaryView view()
		{
			return this.dictionary.view();
		}

	}

}
