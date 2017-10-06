package net.jadoth.persistence.types;

import static net.jadoth.Jadoth.notNull;

import java.util.function.Consumer;

import net.jadoth.Jadoth;
import net.jadoth.collections.EqHashTable;
import net.jadoth.collections.HashTable;
import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.persistence.exceptions.PersistenceExceptionTypeNotPersistable;
import net.jadoth.swizzling.exceptions.SwizzleExceptionConsistency;
import net.jadoth.swizzling.types.SwizzleRegistry;
import net.jadoth.swizzling.types.SwizzleTypeIdentity;
import net.jadoth.swizzling.types.SwizzleTypeLink;
import net.jadoth.swizzling.types.SwizzleTypeManager;
import net.jadoth.util.Equalator;
import net.jadoth.util.KeyValue;


public interface PersistenceTypeHandlerManager<M> extends SwizzleTypeManager, PersistenceTypeHandlerRegistry<M>
{
	@Override
	public <T> PersistenceTypeHandler<M, T> lookupTypeHandler(T instance);

	@Override
	public <T> PersistenceTypeHandler<M, T> lookupTypeHandler(Class<T> type);

	@Override
	public <T> PersistenceTypeHandler<M, T> lookupTypeHandler(long typeId);
	
	/* (27.09.2017 TM)XXX: not sure if all these ensuring methods still make sense,
	 * since the new concept is the the type lineage provider automatically creates a runtime definition
	 * which potentially is a handler.
	 */
	
	public <T> PersistenceTypeHandler<M, T> ensureTypeHandler(T instance);

	public <T> PersistenceTypeHandler<M, T> ensureTypeHandler(Class<T> type);

	public <T> PersistenceTypeHandler<M, T> ensureTypeHandler(long tid);

	public void initialize();

	public void reinitialize();

	public PersistenceDistrict<M> createDistrict(SwizzleRegistry registry);

	public void update(PersistenceTypeDictionary typeDictionary, long highestTypeId);

	public default void update(final PersistenceTypeDictionary typeDictionary)
	{
		this.update(typeDictionary, 0);
	}



	/*
	 * (02.04.2013 TM)TODO: type slave alternative to request type id at master
	 * (and refresh type dictionary in the process)
	 */
	@Override
	public long ensureTypeId(Class<?> type);

	@Override
	public <T> Class<T> ensureType(long typeId);

	
	
	public static <M> PersistenceTypeHandlerManager.Implementation<M> New(
		final PersistenceTypeHandlerRegistry<M> typeHandlerRegistry        ,
		final PersistenceTypeHandlerProvider<M> typeHandlerProvider        ,
		final PersistenceTypeDictionaryManager  typeDictionaryManager      ,
		final PersistenceTypeEvaluator          typeEvaluatorTypeIdMappable,
		final PersistenceTypeChangeCallback     typeChangeCallback
	)
	{
		return new PersistenceTypeHandlerManager.Implementation<>(
			notNull(typeHandlerRegistry)        ,
			notNull(typeHandlerProvider)        ,
			notNull(typeDictionaryManager)      ,
			notNull(typeEvaluatorTypeIdMappable),
			notNull(typeChangeCallback)
		);
	}
	


	public final class Implementation<M> implements PersistenceTypeHandlerManager<M>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		        final PersistenceTypeHandlerRegistry<M> typeHandlerRegistry        ;
		private final PersistenceTypeHandlerProvider<M> typeHandlerProvider        ;
		private final PersistenceTypeDictionaryManager  typeDictionaryManager      ;
		private final PersistenceTypeEvaluator          typeEvaluatorTypeIdMappable;
		private final PersistenceTypeChangeCallback     typeChangeCallback         ;
		private       boolean                           initialized                ;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		Implementation(
			final PersistenceTypeHandlerRegistry<M> typeHandlerRegistry        ,
			final PersistenceTypeHandlerProvider<M> typeHandlerProvider        ,
			final PersistenceTypeDictionaryManager  typeDictionaryManager      ,
			final PersistenceTypeEvaluator          typeEvaluatorTypeIdMappable,
			final PersistenceTypeChangeCallback     typeChangeCallback
		)
		{
			super();
			this.typeHandlerRegistry         = typeHandlerRegistry        ;
			this.typeHandlerProvider         = typeHandlerProvider        ;
			this.typeDictionaryManager       = typeDictionaryManager      ;
			this.typeEvaluatorTypeIdMappable = typeEvaluatorTypeIdMappable;
			this.typeChangeCallback          = typeChangeCallback         ;
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		private <T> PersistenceTypeHandler<M, T> synchEnsureTypeHandler(final Class<T> type)
		{
			PersistenceTypeHandler<M, T> handler;
			if((handler = this.typeHandlerRegistry.lookupTypeHandler(type)) == null)
			{
				// must pass manager instance itself to get a chance to cache new dictionary entry
				handler = this.typeHandlerProvider.provideTypeHandler(this, type);
			}
			return handler;
		}

		private <T> PersistenceTypeHandler<M, T> internalEnsureTypeHandler(final Class<T> type)
		{
			if(!this.typeEvaluatorTypeIdMappable.test(type))
			{
				throw new PersistenceExceptionTypeNotPersistable(type);
			}
			synchronized(this.typeHandlerRegistry)
			{
				if(type.getSuperclass() != null)
				{
					// (03.11.2014)NOTE: changed from synchEnsureTypeHandler to internalEnsureTypeHandler to ensure recursion
					this.internalEnsureTypeHandler(type.getSuperclass());
				}
				/* Note about interfaces:
				 * (Regarding a classe's directly implemented interfaces as well as all super interfaces
				 * of an interface type)
				 * As long as an interface doesn't get passed here explicitely (e.g. as a field's type),
				 * it is ignored intentionally because it can be assumed that it is of no concern for
				 * persistence (i.e. a consistent persistent type hierarchy).
				 * If this will be proved to be wrong, ensuring type's interfaces can be inserted here.
				 */
				return this.synchEnsureTypeHandler(type);
			}
		}

		private <T> PersistenceTypeHandler<M, T> internalEnsureTypeHandler(final long tid)
		{
			synchronized(this.typeHandlerRegistry)
			{
				PersistenceTypeHandler<M, T> handler;
				if((handler = this.typeHandlerRegistry.lookupTypeHandler(tid)) == null)
				{
					// must pass manager instance itself to get a chance to cache new dictionary entry
					handler = this.typeHandlerProvider.provideTypeHandler(this, tid);
				}
				return handler;
			}
		}

		private void validateTypeHandler(final PersistenceTypeHandler<M, ?> typeHandler)
		{
			final PersistenceTypeDefinition<?> registeredTd =
				this.typeDictionaryManager.provideTypeDictionary().lookupTypeByName(typeHandler.typeName())
			;
			if(registeredTd == null)
			{
				return; // type not yet registered, hence it can't be invalid
			}
			if(!SwizzleTypeIdentity.equals(registeredTd, typeHandler))
			{
				// (07.04.2013)TODO proper exception
				throw new RuntimeException("Swizzle inconsistency for " + typeHandler.typeName());
			}


			final Equalator<PersistenceTypeDescriptionMember> memberValidator = (m1, m2) ->
			{
				if(m1 == null || m2 == null)
				{
					// (01.07.2015)EXCP proper exception
					throw new RuntimeException("Member count mismatch of type " + typeHandler.typeName());
				}

				if(m1.equals(m2, PersistenceTypeDescriptionMember.equalator()))
				{
					return true;
				}
				// (07.04.2013)EXCP proper exception
				throw new RuntimeException(
					"Inconsistent member in type description for type "
					+ typeHandler.typeName() + ": " + m1 + " != " + m2
				);
			};


			if(!PersistenceTypeDescriptionMember.equalMembers(registeredTd.members(), typeHandler.members(), memberValidator))
			{
				// throw generic exception in case the equalator returns false instead of throwing an exception
				// (07.04.2013)TODO proper exception
				throw new RuntimeException("member inconsistency for " + typeHandler.typeName());
			}
		}



		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		public final <T> PersistenceTypeHandler<M, T> ensureTypeHandler(final T instance)
		{
			// standard implementation does not consider actual objects
			return this.ensureTypeHandler(Jadoth.getClass(instance));
		}

		@Override
		public final <T> PersistenceTypeHandler<M, T> ensureTypeHandler(final Class<T> type)
		{
//			JadothConsole.debugln("ensureTypeHandler(" + type + ")");
			final PersistenceTypeHandler<M, T> handler; // quick read-only check for already registered type
			if((handler = this.typeHandlerRegistry.lookupTypeHandler(type)) != null)
			{
				return handler;
			}
			return this.internalEnsureTypeHandler(type);
		}

		@Override
		public final <T> PersistenceTypeHandler<M, T> ensureTypeHandler(final long tid)
		{
			final PersistenceTypeHandler<M, T> handler; // quick read-only check for already registered type
			if((handler = this.typeHandlerRegistry.lookupTypeHandler(tid)) != null)
			{
				return handler;
			}
			return this.internalEnsureTypeHandler(tid);
		}

		@Override
		public final <T> PersistenceTypeHandler<M, T> lookupTypeHandler(final Class<T> type)
		{
			return this.typeHandlerRegistry.lookupTypeHandler(type);
		}

		@Override
		public final <T> PersistenceTypeHandler<M, T> lookupTypeHandler(final long typeId)
		{
			return this.typeHandlerRegistry.lookupTypeHandler(typeId);
		}

		@Override
		public final <T> PersistenceTypeHandler<M, T> lookupTypeHandler(final T instance)
		{
			// standard implementation does not consider actual objects
			return this.typeHandlerRegistry.lookupTypeHandler(Jadoth.getClass(instance));
		}

		@Override
		public final <T> PersistenceTypeHandler<M, T> lookupTypeHandler(final long objectId, final long typeId)
		{
			// standard implementation does not consider actual objects
			return this.typeHandlerRegistry.lookupTypeHandler(typeId);
		}

		@Override
		public final long lookupTypeId(final Class<?> type)
		{
			return this.typeHandlerRegistry.lookupTypeId(type);
		}

		@Override
		public final <T> Class<T> lookupType(final long typeId)
		{
			return this.typeHandlerRegistry.lookupType(typeId);
		}
		
		@Override
		public final long typeCount()
		{
			return this.typeHandlerRegistry.typeCount();
		}

		@Override
		public final void validateTypeMapping(final long typeId, final Class<?> type)
		{
			this.typeHandlerRegistry.validateTypeMapping(typeId, type);
		}

		@Override
		public final boolean register(final PersistenceTypeHandler<M, ?> typeHandler)
		{
			this.validateTypeHandler(typeHandler);
			if(this.typeHandlerRegistry.register(typeHandler))
			{
				this.typeDictionaryManager.addTypeDescription(typeHandler);
				return true;
			}
			return false;
		}

		@Override
		public final long ensureTypeId(final Class<?> type)
		{
			/* If the type handler is currently being created, its type<->tid mapping is created in advance
			 * to be available here without calling the handler creation recurringly.
			 */
			final long tid;
			if((tid = this.typeHandlerRegistry.lookupTypeId(type)) != 0L)
			{
				return tid;
			}
			return this.ensureTypeHandler(type).typeId();
		}

		@Override
		public final long currentTypeId()
		{
			synchronized(this.typeHandlerRegistry)
			{
				return this.typeHandlerProvider.currentTypeId();
			}
		}

		@Override
		public final <T> Class<T> ensureType(final long typeId)
		{
			final Class<T> type;
			if((type = this.typeHandlerRegistry.lookupType(typeId)) != null)
			{
				return type;
			}
			return this.<T>ensureTypeHandler(typeId).type();
		}

		@Override
		public final boolean registerType(final long tid, final Class<?> type) throws SwizzleExceptionConsistency
		{
			// if the passed type is new, ensure a handler for it as well
			if(this.typeHandlerRegistry.registerType(tid, type))
			{
				this.ensureTypeHandler(type);
				return true;
			}
			return false;
		}

		@Override
		public final void validateExistingTypeMappings(final XGettingSequence<? extends SwizzleTypeLink<?>> mappings)
			throws SwizzleExceptionConsistency
		{
			this.typeHandlerRegistry.validateExistingTypeMappings(mappings);
		}

		@Override
		public final void validatePossibleTypeMappings(final XGettingSequence<? extends SwizzleTypeLink<?>> mappings)
			throws SwizzleExceptionConsistency
		{
			this.typeHandlerRegistry.validatePossibleTypeMappings(mappings);
		}

		@Override
		public final void iterateTypeHandlers(final Consumer<? super PersistenceTypeHandler<M, ?>> procedure)
		{
			this.typeHandlerRegistry.iterateTypeHandlers(procedure);
		}

		@Override
		public final synchronized void initialize()
		{
			if(this.initialized)
			{
//				JadothConsole.debugln("already initialized");
				return;
			}

			this.internalInitialize();
		}

		@Override
		public final synchronized void reinitialize()
		{
			this.initialized = false;
			// (25.06.2014 TM)TODO: actually clear registry on reinitialize or just perform algorithm again?
			this.initialize();
		}


//		private XGettingSequence<PersistenceTypeDescription<?>> resolveTypeDefinitions(
//			final PersistenceTypeDictionary         typeDictionary
//		)
//		{
//			final XGettingEnum<PersistenceTypeDescription<?>> typeDescriptions = typeDictionary.types();
//			final HashEnum<PersistenceTypeDescription<?>> typeDefinitions  =
//				HashEnum.NewCustom(typeDescriptions.size())
//			;
//			final HashTable<PersistenceTypeDescription<?>, Exception> problems =
//				HashTable.NewCustom(typeDescriptions.intSize())
//			;
//
//			this.typeDefinitionResolver.resolveTypeDefinitions(
//				typeDescriptions,
//				td ->
//					typeDefinitions.add(td),
//				(td, ex) ->
//					problems.add(td, ex)
//			);
//
//			if(!problems.isEmpty())
//			{
//				final String message = PersistenceTypeDescriptionResolver.assembleResolveExceptions(
//					problems,
//					VarString.New()
//				)
//				.toString();
//				throw new RuntimeException(message);
//			}
//
//			return typeDefinitions;
//		}
		
		
		private <T> void createTypeDefinitionInitializer(
			final PersistenceTypeDefinitionInitializerProvider<M>            runtimeTypeDefInitializerProvider,
			final PersistenceTypeDefinition<T>                               latestDictionaryTypeDefinition   ,
			final EqHashTable<Long, PersistenceTypeDefinitionInitializer<?>> matchingTypeDefinitions          ,
			final HashTable<PersistenceTypeDefinition<?> , PersistenceTypeDefinitionInitializer<?>> changedTypeDefinitions
		)
		{
			final Class<T> type = latestDictionaryTypeDefinition.type();
			if(type == null)
			{
				this.typeChangeCallback.validateMissingRuntimeType(latestDictionaryTypeDefinition);
				return;
			}
			
			final PersistenceTypeDefinitionInitializer<T> tdi = runtimeTypeDefInitializerProvider.lookupInitializer(type);
			
			if(PersistenceTypeDescription.isEqualStructure(latestDictionaryTypeDefinition, tdi))
			{
				matchingTypeDefinitions.add(latestDictionaryTypeDefinition.typeId(), tdi);
			}
			else
			{
				this.typeChangeCallback.validateTypeChange(latestDictionaryTypeDefinition, tdi);
				changedTypeDefinitions.add(latestDictionaryTypeDefinition, tdi);
			}
		}

		private void internalInitialize()
		{
//			JadothConsole.debugln("initializing " + Jadoth.systemString(this.typeHandlerRegistry));

			final PersistenceTypeDictionary typeDictionary =
				this.typeDictionaryManager.provideTypeDictionary()
			;
			
			/* (28.09.2017 TM)FIXME: /!\ type dictionary stuff
			 *  v ensure runtime definitions
			 *  v change for checks
			 *  - register latest typeId for each type
			 *  - initialize runtime definitions
			 *  - set/initialize runtime definitions to their type lineages
			 */
			
			final PersistenceTypeHandlerRegistry<M> typeRegistry = this.typeHandlerRegistry;
			
			final PersistenceTypeDefinitionInitializerProvider<M> tdip = PersistenceTypeDefinitionInitializerProvider.New(
				this.typeHandlerProvider,
				this
			);
			final EqHashTable<Long, PersistenceTypeDefinitionInitializer<?>> matchingTypeDefinitions =
				EqHashTable.New()
			;
			final HashTable<PersistenceTypeDefinition<?>, PersistenceTypeDefinitionInitializer<?>> changedTypeDefinitions =
				HashTable.New()
			;
						
			// create type definition initializers and check for type changes / conflicts / mismatches.
			for(final PersistenceTypeDefinition<?> td : typeDictionary.latestTypesById().values())
			{
				this.createTypeDefinitionInitializer(tdip, td, matchingTypeDefinitions, changedTypeDefinitions);
			}
			
			// register unconflicted / unchanged types
			matchingTypeDefinitions.iterate(kv ->
				typeRegistry.registerType(kv.key(), kv.value().type())
			);

			// initialize matched TDIs
			for(final KeyValue<Long, PersistenceTypeDefinitionInitializer<?>> e : matchingTypeDefinitions)
			{
				final PersistenceTypeDefinition<?> newTd = e.value().initialize(e.key());
				
				// replace simple dictionary type definition by runtime type definition (e.g. a TypeHandler instance)
				typeDictionary.registerType(newTd);
			}
						
			// assign new TIDs for changedTypes and initialize changed TDIs with new TIDs
			for(final KeyValue<PersistenceTypeDefinition<?>, PersistenceTypeDefinitionInitializer<?>> e : changedTypeDefinitions)
			{
				final PersistenceTypeDefinition<?>       latestTd = e.key();
				final PersistenceTypeDefinitionInitializer<?> tdi = e.value();
				
				final long newTypeId = this.ensureTypeId(tdi.type());
				final PersistenceTypeDefinition<?> newTd = tdi.initialize(newTypeId);
				
				// register properly initialized new runtime TypeDefinintion (e.g. TypeHandler)
				typeDictionary.registerType(newTd);
				
				// register new deprecated "latest" and new TypeDefinintion for change (e.g. entity type conversion)
				this.typeChangeCallback.registerTypeChange(latestTd, newTd);
			}
			
			/* (29.09.2017 TM)XXX: Unnecessary type dictionary update?
			 * Note sure why the freshly populuted and validated type dictionary has to be upated
			 * into another and validated again twice instead of just updatnig the highest TypeId
			 * and exporting the typeDictionary explicitely.
			 * Maybe the "updating" logic should be unburdened.
			 */
			this.update(typeDictionary);
			
//			// (28.09.2017 TM)NOTE: old from before type refactoring ------------
//			final XGettingSequence<PersistenceTypeDefinition<?>> liveTypeDescriptions =
//				typeDictionary.currentTypesByName().values()
//			;
//
//			final PersistenceTypeHandlerRegistry<M> typeRegistry = this.typeHandlerRegistry;
//
//			// validate all type mappings before registering anything
//			typeRegistry.validatePossibleTypeMappings(liveTypeDescriptions);
//
//			// register type identities (typeId<->type) first to make all types available for type handler creation
//			liveTypeDescriptions.iterate(e ->
//				typeRegistry.registerType(e.typeId(), e.type())
//			);
//
//			this.update(typeDictionary);
//
//			// ensure type handlers for all types in type dict (even on exception, type mappings have already been set)
//			liveTypeDescriptions.iterate(e ->
//				this.ensureTypeHandler(e.type())
//			);
//			// (28.09.2017 TM) ------------- //

			this.initialized = true;
		}

		@Override
		public final PersistenceDistrict<M> createDistrict(final SwizzleRegistry registry)
		{
			return new PersistenceDistrict.Implementation<>(registry, this.typeHandlerRegistry);
		}

		@Override
		public void updateCurrentHighestTypeId(final long highestTypeId)
		{
			this.typeHandlerProvider.updateCurrentHighestTypeId(highestTypeId);
		}

		protected void internalUpdate(final PersistenceTypeDictionary typeDictionary, final long highestTypeId)
		{
			this.typeDictionaryManager.validateTypeDescriptions(typeDictionary);

			// update the highest type id first after validation has been passed successfully to guarantee consistency
			if(this.currentTypeId() < highestTypeId)
			{
				// only update if new value is actually higher. No reason to throw an exception otherwise.
				this.updateCurrentHighestTypeId(highestTypeId);
			}

			// finally add the type descriptions
			this.typeDictionaryManager.addTypeDescriptions(typeDictionary);
		}

		@Override
		public void update(final PersistenceTypeDictionary typeDictionary, final long highestTypeId)
		{
			final long effectiveHighestTypeId = Math.max(typeDictionary.determineHighestTypeId(), highestTypeId);
			this.internalUpdate(typeDictionary, effectiveHighestTypeId);

			/*
			 * inlining the max() call changes the second argument from 0 to something like 43466428.
			 * Unbelievable giant JDK bug!
			 */
//			this.internalUpdate(typeDictionary, Math.max(typeDictionary.determineHighestTypeId(), highestTypeId));
		}

	}

}
