package one.microstream.persistence.types;

import static one.microstream.X.notNull;

import java.util.function.Consumer;

import one.microstream.collections.HashEnum;
import one.microstream.collections.HashTable;
import one.microstream.collections.types.XAddingEnum;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.collections.types.XGettingEnum;
import one.microstream.equality.Equalator;
import one.microstream.persistence.exceptions.PersistenceExceptionConsistency;
import one.microstream.persistence.exceptions.PersistenceExceptionTypeConsistency;
import one.microstream.persistence.exceptions.PersistenceExceptionTypeHandlerConsistency;
import one.microstream.reflect.XReflect;
import one.microstream.typing.KeyValue;


public interface PersistenceTypeHandlerManager<M> extends PersistenceTypeManager, PersistenceTypeHandlerRegistry<M>
{
	@Override
	public <T> PersistenceTypeHandler<M, T> lookupTypeHandler(T instance);

	@Override
	public <T> PersistenceTypeHandler<M, T> lookupTypeHandler(Class<T> type);

	@Override
	public PersistenceTypeHandler<M, ?> lookupTypeHandler(long typeId);

	public <T> PersistenceTypeHandler<M, T> ensureTypeHandler(T instance);

	public <T> PersistenceTypeHandler<M, T> ensureTypeHandler(Class<T> type);
	
	public <T> PersistenceTypeHandler<M, T> ensureTypeHandler(PersistenceTypeDefinition typeDefinition);
	
	public void ensureTypeHandlers(XGettingEnum<PersistenceTypeDefinition> typeDefinitions);

	public void ensureTypeHandlersByTypeIds(XGettingEnum<Long> typeIds);

	public PersistenceTypeHandlerManager<M> initialize();

	public void update(PersistenceTypeDictionary typeDictionary, long highestTypeId);

	public default void update(final PersistenceTypeDictionary typeDictionary)
	{
		this.update(typeDictionary, 0);
	}
	
	public PersistenceTypeDictionary typeDictionary();
	
	@Override
	public long ensureTypeId(Class<?> type);

	@Override
	public Class<?> ensureType(long typeId);
	
	public void validateTypeHandler(PersistenceTypeHandler<M, ?> typeHandler);
	
	public default void validateTypeHandlers(final Iterable<? extends PersistenceTypeHandler<M, ?>> typeHandlers)
	{
		for(final PersistenceTypeHandler<M, ?> typeHandler : typeHandlers)
		{
			this.validateTypeHandler(typeHandler);
		}
	}



	public static <M> PersistenceTypeHandlerManager.Default<M> New(
		final PersistenceTypeHandlerRegistry<M>           typeHandlerRegistry  ,
		final PersistenceTypeHandlerProvider<M>           typeHandlerProvider  ,
		final PersistenceTypeDictionaryManager            typeDictionaryManager,
		final PersistenceTypeMismatchValidator<M>         typeMismatchValidator,
		final PersistenceLegacyTypeMapper<M>              legacyTypeMapper     ,
		final PersistenceUnreachableTypeHandlerCreator<M> unreachableTypeHandlerCreator
	)
	{
		return new PersistenceTypeHandlerManager.Default<>(
			notNull(typeHandlerRegistry)  ,
			notNull(typeHandlerProvider)  ,
			notNull(typeDictionaryManager),
			notNull(typeMismatchValidator),
			notNull(legacyTypeMapper)     ,
			notNull(unreachableTypeHandlerCreator)
		);
	}

	public final class Default<M> implements PersistenceTypeHandlerManager<M>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		        final PersistenceTypeHandlerRegistry<M>           typeHandlerRegistry          ;
		private final PersistenceTypeHandlerProvider<M>           typeHandlerProvider          ;
		private final PersistenceTypeDictionaryManager            typeDictionaryManager        ;
		private final PersistenceTypeMismatchValidator<M>         typeMismatchValidator        ;
		private final PersistenceLegacyTypeMapper<M>              legacyTypeMapper             ;
		private final PersistenceUnreachableTypeHandlerCreator<M> unreachableTypeHandlerCreator;
		
		private boolean initialized;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final PersistenceTypeHandlerRegistry<M>           typeHandlerRegistry  ,
			final PersistenceTypeHandlerProvider<M>           typeHandlerProvider  ,
			final PersistenceTypeDictionaryManager            typeDictionaryManager,
			final PersistenceTypeMismatchValidator<M>         typeMismatchValidator,
			final PersistenceLegacyTypeMapper<M>              legacyTypeMapper     ,
			final PersistenceUnreachableTypeHandlerCreator<M> unreachableTypeHandlerCreator
		)
		{
			super();
			this.typeHandlerRegistry           = typeHandlerRegistry  ;
			this.typeHandlerProvider           = typeHandlerProvider  ;
			this.typeDictionaryManager         = typeDictionaryManager;
			this.typeMismatchValidator         = typeMismatchValidator;
			this.legacyTypeMapper              = legacyTypeMapper     ;
			this.unreachableTypeHandlerCreator = unreachableTypeHandlerCreator;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		private <T> PersistenceTypeHandler<M, T> synchEnsureTypeHandler(final Class<T> type)
		{
			PersistenceTypeHandler<M, T> handler;
			if((handler = this.typeHandlerRegistry.lookupTypeHandler(type)) == null)
			{
				handler = this.typeHandlerProvider.provideTypeHandler(type);
				this.internalRegisterTypeHandler(handler);
			}
			
			return handler;
		}
		
		/*
		 * Logic moved from calling it externally in the TypeHandlerProvider to be called internally, where it belongs.
		 */
		private <T> void internalRegisterTypeHandler(final PersistenceTypeHandler<M, T> typeHandler)
		{
			this.registerTypeHandler(typeHandler);
			this.recursiveEnsureTypeHandlers(typeHandler);
		}
		
		private void internalRecursiveEnsureTypeHandlers(
			final Iterable<? extends PersistenceTypeHandler<M, ?>> typeHandlers
		)
		{
			for(final PersistenceTypeHandler<M, ?> typeHandler : typeHandlers)
			{
				this.recursiveEnsureTypeHandlers(typeHandler);
			}
		}
		
		private <T> void recursiveEnsureTypeHandlers(final PersistenceTypeHandler<M, T> typeHandler)
		{
			/*
			 * Must ensure type handlers for all field types as well to keep type definitions consistent.
			 * If some field's type is "too abstract" to be persisted, is has to be registered to an
			 * appropriate type handler (No-op, etc.) manually beforehand.
			 *
			 * creating new type handlers in the process will eventually end up here again for the new types
			 * until all reachable types are ensured to have type handlers registered.
			 */
			typeHandler.iterateMemberTypes(t ->
			{
				try
				{
					this.ensureTypeHandler(t);
				}
				catch(final RuntimeException e)
				{
					throw e; // debug hook
				}
			});
		}

		private <T> PersistenceTypeHandler<M, T> internalEnsureTypeHandler(final Class<T> type)
		{
			synchronized(this.typeHandlerRegistry)
			{
				/* (22.07.2019 TM)NOTE:
				 * With unpersistable types getting an empty dummy type handler ... and actually even since the
				 * very beginning, it is actually superfluous to recursively analyze super classes, as
				 * every class is handled isolated from validity of its super classes.
				 */
//				if(type.getSuperclass() != null)
//				{
//					this.internalEnsureTypeHandler(type.getSuperclass());
//				}
				/* Note about interfaces:
				 * (Regarding a class' directly implemented interfaces as well as all super interfaces
				 * of an interface type)
				 * As long as an interface doesn't get passed here explicitly (e.g. as a field's type),
				 * it is ignored intentionally because it can be assumed that it is of no concern for
				 * persistence (i.e. a consistent persistent type hierarchy).
				 * If this will be proved to be wrong, ensuring type's interfaces can be inserted here.
				 */
				return this.synchEnsureTypeHandler(type);
			}
		}
		
		private void validateTypeHandlerTypeId(final PersistenceTypeHandler<M, ?> typeHandler)
		{
			if(typeHandler.typeId() != Persistence.nullId())
			{
				return;
			}
			
			throw new PersistenceExceptionTypeHandlerConsistency(
				"Invalid 0-TypeId " + PersistenceTypeHandler.class.getSimpleName()
				+ " " + typeHandler.typeName()
			);
		}
		
		@Override
		public final void validateTypeHandler(final PersistenceTypeHandler<M, ?> typeHandler)
		{
			this.validateTypeHandlerTypeId(typeHandler);
			
			final PersistenceTypeDefinition registeredTd =
				this.typeDictionaryManager.provideTypeDictionary().lookupTypeByName(typeHandler.typeName())
			;
			if(registeredTd == null)
			{
				return; // type not yet registered, hence it can't be invalid
			}
			
			if(typeHandler.typeId() != registeredTd.typeId())
			{
				// (07.04.2013)EXCP proper exception
				throw new PersistenceExceptionTypeConsistency(
					"TypeId inconsistency for " + typeHandler.typeName()
					+ ": typeDictionary type definition typeId = " + registeredTd.typeId()
					+ ", validated type handler typeId = " + typeHandler.typeId()
				);
			}

			final Equalator<PersistenceTypeDescriptionMember> memberValidator = (m1, m2) ->
			{
				if(m1 == null || m2 == null)
				{
					// (01.07.2015)EXCP proper exception
					throw new PersistenceExceptionTypeConsistency(
						"Member count mismatch of type " + typeHandler.typeName()
					);
				}

				// structure is enough since qualifiers are just required for intra-type identification
				if(m1.equalsStructure(m2))
				{
					return true;
				}
				
				// (07.04.2013)EXCP proper exception
				throw new PersistenceExceptionTypeConsistency(
					"Inconsistent member in type description for type "
					+ typeHandler.typeName() + ": " + m1 + " != " + m2
				);
			};

			if(!PersistenceTypeDescriptionMember.equalMembers(registeredTd.members(), typeHandler.members(), memberValidator))
			{
				// throw generic exception in case the equalator returns false instead of throwing an exception
				// (07.04.2013)EXCP proper exception
				throw new PersistenceExceptionTypeConsistency("Member inconsistency for " + typeHandler.typeName());
			}
		}

		@Override
		public final <T> PersistenceTypeHandler<M, T> ensureTypeHandler(final T instance)
		{
			// standard implementation does not consider actual objects, only their types
			
			final PersistenceTypeHandler<M, T> typeHandler = this.ensureTypeHandler(
				XReflect.getClass(instance)
			);
			typeHandler.guaranteeInstanceViablity();
			
			return typeHandler;
		}
		
		@Override
		public PersistenceTypeDictionary typeDictionary()
		{
			return this.typeDictionaryManager.provideTypeDictionary();
		}

		@Override
		public final <T> PersistenceTypeHandler<M, T> ensureTypeHandler(final Class<T> type)
		{
//			XDebug.debugln("ensureTypeHandler(" + type + ")");
			final PersistenceTypeHandler<M, T> handler; // quick read-only check for already registered type
			if((handler = this.typeHandlerRegistry.lookupTypeHandler(type)) != null)
			{
				return handler;
			}
			
			return this.internalEnsureTypeHandler(type);
		}
		
		private <T> Class<T> validateExistingType(final PersistenceTypeDefinition typeDefinition)
		{
			@SuppressWarnings("unchecked") // cast safety is ensured by the type itself and the logic handling it.
			final Class<T> runtimeType = (Class<T>)typeDefinition.type();
			if(runtimeType != null)
			{
				return runtimeType;
			}
			
			// (10.10.2018 TM)EXCP: proper exception
			throw new RuntimeException(
				"Missing runtime type for required type handler for type: " + typeDefinition.runtimeTypeName()
			);
		}
		
		private <T> PersistenceTypeHandler<M, T> checkForUnreachableType(final PersistenceTypeDefinition typeDef)
		{
			if(typeDef.runtimeTypeName() != null)
			{
				return null;
			}
			
			synchronized(this.typeHandlerRegistry)
			{
				// must check for an already existing type handler before creating a new one
				final PersistenceTypeHandler<M, ?> alreadyRegisteredTypeHandler = this.lookupTypeHandler(typeDef.typeId());
				if(alreadyRegisteredTypeHandler != null)
				{
					@SuppressWarnings("unchecked")
					final PersistenceTypeHandler<M, T> casted = (PersistenceTypeHandler<M, T>)alreadyRegisteredTypeHandler;
					return casted;
				}
				
				final PersistenceUnreachableTypeHandler<M, T> newHandler =
					this.unreachableTypeHandlerCreator.createUnreachableTypeHandler(typeDef)
				;
				this.registerLegacyTypeHandler(newHandler);
				
				return newHandler;
			}
			
		}
		
		@Override
		public <T> PersistenceTypeHandler<M, T> ensureTypeHandler(final PersistenceTypeDefinition typeDefinition)
		{
			/*
			 * This method must make sure that the passed typeDefinition gets a functional type handler,
			 * which means it must have a non-null runtime type.
			 * Refactoring mappings have already been considered at the type definition's creation time,
			 * meaning if it has no runtime type by now, it is an error. Either a missing refactoring mapping
			 * or maybe even a type that has been deleted in the design without replacement that should not have been.
			 */
						
			final PersistenceTypeHandler<M, T> unreachableHandler = this.checkForUnreachableType(typeDefinition);
			if(unreachableHandler != null)
			{
				return unreachableHandler;
			}
			
			// for all types not explicitly marked as unreachable, the runtime type is essential.
			final Class<T>                     runtimeType        = this.validateExistingType(typeDefinition);
			final PersistenceTypeHandler<M, T> runtimeTypeHandler = this.ensureTypeHandler(runtimeType);
			
			// check if the type definition is up to date or if a legacy type handler is needed
			if(runtimeTypeHandler.typeId() == typeDefinition.typeId())
			{
//				XDebug.println("Up to date type handler : " + typeDefinition.runtimeTypeName());
				return runtimeTypeHandler;
			}
			
//			XDebug.println("Requires legacy type handler : " + typeDefinition.typeName());
			
			// for non-up-to-date type definitions, a legacy type handler must be ensured (looked up or created)
			return this.ensureLegacyTypeHandler(typeDefinition, runtimeTypeHandler);
		}
		
		private <T> PersistenceLegacyTypeHandler<M, T> ensureLegacyTypeHandler(
			final PersistenceTypeDefinition    legacyTypeDefinition,
			final PersistenceTypeHandler<M, T> currentTypeHandler
		)
		{
			final PersistenceLegacyTypeHandler<M, T> legacyTypeHandler = this.legacyTypeMapper.ensureLegacyTypeHandler(
				legacyTypeDefinition,
				currentTypeHandler
			);
			this.registerLegacyTypeHandler(legacyTypeHandler);
			
			return legacyTypeHandler;
		}
				
		@Override
		public void ensureTypeHandlersByTypeIds(final XGettingEnum<Long> typeIds)
		{
			final HashEnum<PersistenceTypeDefinition> resolvedTypeDefinitions = HashEnum.New();
			this.typeDictionaryManager.provideTypeDictionary().resolveTypeIds(typeIds, resolvedTypeDefinitions);
			this.ensureTypeHandlers(resolvedTypeDefinitions);
		}
				
		@Override
		public void ensureTypeHandlers(final XGettingEnum<PersistenceTypeDefinition> typeDefinitions)
		{
			synchronized(this.typeHandlerRegistry)
			{
//				typeDefinitions.iterate(this::ensureTypeHandler);
				typeDefinitions.iterate(typeDefinition ->
					this.ensureTypeHandler(typeDefinition) // debug-friendlier
				);
			}
		}

		@Override
		public final <T> PersistenceTypeHandler<M, T> lookupTypeHandler(final Class<T> type)
		{
			return this.typeHandlerRegistry.lookupTypeHandler(type);
		}

		@Override
		public final PersistenceTypeHandler<M, ?> lookupTypeHandler(final long typeId)
		{
			return this.typeHandlerRegistry.lookupTypeHandler(typeId);
		}

		@Override
		public final <T> PersistenceTypeHandler<M, T> lookupTypeHandler(final T instance)
		{
			// standard implementation does not consider actual objects
			return this.typeHandlerRegistry.lookupTypeHandler(XReflect.getClass(instance));
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
		public boolean validateTypeMapping(final long typeId, final Class<?> type) throws PersistenceExceptionConsistency
		{
			return this.typeHandlerRegistry.validateTypeMapping(typeId, type);
		}
		
		@Override
		public boolean validateTypeMappings(final Iterable<? extends PersistenceTypeLink> mappings)
			throws PersistenceExceptionConsistency
		{
			return this.typeHandlerRegistry.validateTypeMappings(mappings);
		}
				
		@Override
		public boolean registerTypes(final Iterable<? extends PersistenceTypeLink> types)
			throws PersistenceExceptionConsistency
		{
			return this.typeHandlerRegistry.registerTypes(types);
		}

		@Override
		public final boolean registerTypeHandler(final PersistenceTypeHandler<M, ?> typeHandler)
		{
			this.validateTypeHandler(typeHandler);
			if(this.typeHandlerRegistry.registerTypeHandler(typeHandler))
			{
				// a (up to date) handler is always the runtime type definition
				this.typeDictionaryManager.registerRuntimeTypeDefinition(typeHandler);
				return true;
			}
			return false;
		}
		
		@Override
		public final boolean registerLegacyTypeHandler(final PersistenceLegacyTypeHandler<M, ?> legacyTypeHandler)
		{
			this.validateTypeHandlerTypeId(legacyTypeHandler);
			return this.typeHandlerRegistry.registerLegacyTypeHandler(legacyTypeHandler);
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
		public final Class<?> ensureType(final long typeId)
		{
			final Class<?> type;
			if((type = this.typeHandlerRegistry.lookupType(typeId)) != null)
			{
				return type;
			}
			return this.ensureTypeHandler(typeId).type();
		}

		@Override
		public final boolean registerType(final long tid, final Class<?> type) throws PersistenceExceptionConsistency
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
		public <C extends Consumer<? super PersistenceTypeHandler<M, ?>>> C iterateTypeHandlers(final C iterator)
		{
			this.typeHandlerRegistry.iterateTypeHandlers(iterator);
			return iterator;
		}

		@Override
		public final synchronized PersistenceTypeHandlerManager<M> initialize()
		{
			if(this.initialized)
			{
//				XDebug.debugln("already initialized");
				return this;
			}

			this.internalInitialize();
			return this;
		}

		private void internalInitialize()
		{
			final PersistenceTypeDictionary typeDictionary = this.typeDictionaryManager.provideTypeDictionary();
			
			final HashEnum<PersistenceTypeHandler<M, ?>> newTypeHandlers = HashEnum.New();
			final HashEnum<PersistenceTypeHandler<M, ?>> typeRegisteredTypeHandlers = HashEnum.New();
			
			// either fill/initialize an empty type dictionary or initalize from a non-empty dictionary.
			if(typeDictionary.isEmpty())
			{
				this.initializeBlank(newTypeHandlers);
			}
			else
			{
				this.initializeFromDictionary(typeDictionary, newTypeHandlers, typeRegisteredTypeHandlers);
			}
			
			// "new" type Handlers are either generated ones for a blank or misfits for an existing type dictionary.
			this.initializeNewTypeHandlers(newTypeHandlers, typeRegisteredTypeHandlers);
			
			// after all type handler initialization and typeId registration was successful, register all type handlers.
			this.registerTypeHandlers(typeRegisteredTypeHandlers);
			
			// after that, the initialization is complete and marked accordingly.
			this.initialized = true;
		}
		
		private void typeRegisterInitializedTypeHandlers(
			final XGettingEnum<PersistenceTypeHandler<M, ?>> typeUnregisteredInitializedTypeHandlers,
			final XAddingEnum<PersistenceTypeHandler<M, ?>>  typeRegisteredInitializedTypeHandlers
		)
		{
			// register the matched Type<->TypeId mappings
			this.typeHandlerRegistry.registerTypes(typeUnregisteredInitializedTypeHandlers);
			typeRegisteredInitializedTypeHandlers.addAll(typeUnregisteredInitializedTypeHandlers);
		}
		
		
		private void initializeBlank(final HashEnum<PersistenceTypeHandler<M, ?>> newTypeHandlers)
		{
			this.typeHandlerProvider.iterateTypeHandlers(newTypeHandlers);
		}
		
		private void initializeFromDictionary(
			final PersistenceTypeDictionary                 typeDictionary            ,
			final HashEnum<PersistenceTypeHandler<M, ?>>    newTypeHandlers           ,
			final XAddingEnum<PersistenceTypeHandler<M, ?>> typeRegisteredTypeHandlers
		)
		{
			final HashEnum<PersistenceTypeHandler<M, ?>> initializedMatchingTypeHandlers = HashEnum.New();
			final HashEnum<PersistenceTypeLineage> runtimeTypeLineages = HashEnum.New();
			
			this.filterRuntimeTypeLineages(typeDictionary, runtimeTypeLineages);
			
			final HashTable<PersistenceTypeDefinition, PersistenceTypeHandler<M, ?>> matches = HashTable.New();

			// derive a type handler for every runtime type lineage and try to match an existing type definition
			for(final PersistenceTypeLineage typeLineage : runtimeTypeLineages)
			{
				this.deriveRuntimeTypeHandler(typeLineage, matches, newTypeHandlers);
			}
			
			// pass all misfits and the typeDictionary to a PersistenceTypeMismatchEvaluator
			this.typeMismatchValidator.validateTypeMismatches(typeDictionary, newTypeHandlers);
			
			// internally update the current hightest type id, including non-runtime types.
			this.internalUpdateCurrentHighestTypeId(typeDictionary);
			
			// initialize all matches to the associated TypeId
			for(final KeyValue<PersistenceTypeDefinition, PersistenceTypeHandler<M, ?>> match : matches)
			{
				final long typeId = match.key().typeId();
				final PersistenceTypeHandler<M, ?> ith = match.value().initialize(typeId);
				initializedMatchingTypeHandlers.add(ith);
			}
			
			/*
			 * must register the matching type handlers' type-mappings BEFORE initializing the new type handlers
			 * As initializing the new type handlers might ensure a super type's typeId.
			 */
			this.typeRegisterInitializedTypeHandlers(initializedMatchingTypeHandlers, typeRegisteredTypeHandlers);
		}
		
		private void registerTypeHandlers(
			final XGettingCollection<PersistenceTypeHandler<M, ?>> initializedTypeHandlers
		)
		{
			// set initialized handlers as runtime definitions
			this.typeDictionaryManager.registerRuntimeTypeDefinitions(initializedTypeHandlers);
			
			this.internalRegisterTypeHandlers(initializedTypeHandlers);
			
			// recursive registration: initialized handlers themselves plus all handlers required for their field types
			this.internalRecursiveEnsureTypeHandlers(initializedTypeHandlers);
		}
		
		private void internalRegisterTypeHandlers(final Iterable<PersistenceTypeHandler<M, ?>> typeHandlers)
		{
			for(final PersistenceTypeHandler<M, ?> typeHandler : typeHandlers)
			{
				this.typeHandlerRegistry.registerTypeHandler(typeHandler);
			}
		}
		
		private void initializeNewTypeHandlers(
			final XGettingCollection<PersistenceTypeHandler<M, ?>> newTypeHandlers,
			final HashEnum<PersistenceTypeHandler<M, ?>>           typeRegisteredTypeHandlers
		)
		{
			final HashEnum<PersistenceTypeHandler<M, ?>> initializedNewTypeHandlers = HashEnum.New();
			
			// assign new TypeIds to all misfits (TypeIds of matching type handlers must already be registered!)
			for(final PersistenceTypeHandler<M, ?> newTypeHandler : newTypeHandlers)
			{
				// native handlers (e.g. see in class BinaryPersistence) already have their TypeId, even if "new".
				final PersistenceTypeHandler<M, ?> ith = this.ensureInitializedTypeHandler(newTypeHandler);
				initializedNewTypeHandlers.add(ith);
			}

			// register TypeId mappings of all successfully initialized new type handlers.
			this.typeRegisterInitializedTypeHandlers(initializedNewTypeHandlers, typeRegisteredTypeHandlers);
		}
		
		private PersistenceTypeHandler<M, ?> ensureInitializedTypeHandler(
			final PersistenceTypeHandler<M, ?> typeHandler
		)
		{
			if(typeHandler.typeId() != 0)
			{
				return typeHandler;
			}

			// must be the TypeHandlerProvider's ensureTypeId in order to circumvent implicit handler creation.
			final long newTypeId = this.typeHandlerProvider.ensureTypeId(typeHandler.type());
			return typeHandler.initialize(newTypeId);
		}
				
		private void filterRuntimeTypeLineages(
			final PersistenceTypeDictionary           typeDictionary     ,
			final HashEnum<PersistenceTypeLineage> runtimeTypeLineages
		)
		{
			typeDictionary.iterateTypeLineages(td ->
			{
				if(td.type() != null)
				{
					runtimeTypeLineages.add(td);
				}
			});
		}
		
		private <T> void deriveRuntimeTypeHandler(
			final PersistenceTypeLineage                                             typeLineage            ,
			final HashTable<PersistenceTypeDefinition, PersistenceTypeHandler<M, ?>> matchedTypeHandlers    ,
			final HashEnum<PersistenceTypeHandler<M, ?>>                             unmatchableTypeHandlers
		)
		{
			final Class<?> runtimeType = typeLineage.type();
			if(runtimeType == null)
			{
				/*
				 * Type lineage has no runtime type, so there can't be a runtime type handler derived for it.
				 * This does not have to be an error. If the type lineage represents an outdated type that is not
				 * used anymore, it can remain without runtime type. The ensuring of required runtime type handlers
				 * later in the initialization will validate that.
				 */
				return;
			}
			
			final PersistenceTypeHandler<M, ?> handler = this.advanceEnsureTypeHandler(runtimeType);
						
			for(final PersistenceTypeDefinition typeDefinition : typeLineage.entries().values())
			{
				// exact matching structure including field order. no matching of field qualifiers needed
				final boolean isMatched = PersistenceTypeDescriptionMember.equalStructures(
					handler.members(),
					typeDefinition.members()
				);
				
				if(isMatched)
				{
					// matching definition found, register and abort matching.
					matchedTypeHandlers.add(typeDefinition, handler);
					return;
				}
			}

			// no matching definition found
			unmatchableTypeHandlers.add(handler);
		}
		
		private <T> PersistenceTypeHandler<M, T> advanceEnsureTypeHandler(final Class<T> type)
		{
			PersistenceTypeHandler<M, T> handler;
			if((handler = this.typeHandlerRegistry.lookupTypeHandler(type)) == null)
			{
				handler = this.typeHandlerProvider.ensureTypeHandler(type);
			}
			
			return handler;
		}

		@Override
		public void updateCurrentHighestTypeId(final long highestTypeId)
		{
			this.typeHandlerProvider.updateCurrentHighestTypeId(highestTypeId);
		}
		
		final void internalUpdateCurrentHighestTypeId(final PersistenceTypeDictionary typeDictionary)
		{
			this.internalUpdateCurrentHighestTypeId(typeDictionary.determineHighestTypeId());
		}
		
		final void internalUpdateCurrentHighestTypeId(
			final PersistenceTypeDictionary typeDictionary,
			final long                      highestTypeId
		)
		{
			final long effectiveHighestTypeId = Math.max(typeDictionary.determineHighestTypeId(), highestTypeId);
			this.updateCurrentHighestTypeId(effectiveHighestTypeId);
		}
		
		final void internalUpdateCurrentHighestTypeId(final long highestTypeId)
		{
			// update the highest type id first after validation has been passed successfully to guarantee consistency
			if(this.currentTypeId() < highestTypeId)
			{
				// only update if new value is actually higher. No reason to throw an exception otherwise.
				this.updateCurrentHighestTypeId(highestTypeId);
			}
		}

		@Override
		public void update(final PersistenceTypeDictionary typeDictionary, final long highestTypeId)
		{
			this.typeDictionaryManager.validateTypeDefinitions(typeDictionary.allTypeDefinitions().values());

			this.internalUpdateCurrentHighestTypeId(typeDictionary, highestTypeId);

			// finally add the type descriptions
			this.typeDictionaryManager.registerTypeDefinitions(typeDictionary.allTypeDefinitions().values());
		}

	}

}
