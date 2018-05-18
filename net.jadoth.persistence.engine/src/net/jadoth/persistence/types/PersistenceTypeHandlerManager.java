package net.jadoth.persistence.types;

import static net.jadoth.X.notNull;

import java.util.function.Consumer;

import net.jadoth.collections.HashEnum;
import net.jadoth.collections.HashTable;
import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.equality.Equalator;
import net.jadoth.persistence.exceptions.PersistenceExceptionTypeConsistency;
import net.jadoth.persistence.exceptions.PersistenceExceptionTypeNotPersistable;
import net.jadoth.reflect.XReflect;
import net.jadoth.swizzling.exceptions.SwizzleExceptionConsistency;
import net.jadoth.swizzling.types.SwizzleRegistry;
import net.jadoth.swizzling.types.SwizzleTypeIdentity;
import net.jadoth.swizzling.types.SwizzleTypeLink;
import net.jadoth.swizzling.types.SwizzleTypeManager;
import net.jadoth.typing.KeyValue;


public interface PersistenceTypeHandlerManager<M> extends SwizzleTypeManager, PersistenceTypeHandlerRegistry<M>
{
	@Override
	public <T> PersistenceTypeHandler<M, T> lookupTypeHandler(T instance);

	@Override
	public <T> PersistenceTypeHandler<M, T> lookupTypeHandler(Class<T> type);

	@Override
	public <T> PersistenceTypeHandler<M, T> lookupTypeHandler(long typeId);

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
	
	@Override
	public long ensureTypeId(Class<?> type);

	@Override
	public <T> Class<T> ensureType(long typeId);



	public static <M> PersistenceTypeHandlerManager.Implementation<M> New(
		final PersistenceTypeHandlerRegistry<M>   typeHandlerRegistry  ,
		final PersistenceTypeHandlerProvider<M>   typeHandlerProvider  ,
		final PersistenceTypeDictionaryManager    typeDictionaryManager,
		final PersistenceTypeEvaluator            typeEvaluator        ,
		final PersistenceTypeMismatchValidator<M> typeMismatchValidator
	)
	{
		return new PersistenceTypeHandlerManager.Implementation<>(
			notNull(typeHandlerRegistry)  ,
			notNull(typeHandlerProvider)  ,
			notNull(typeDictionaryManager),
			notNull(typeEvaluator)        ,
			notNull(typeMismatchValidator)
		);
	}



	public final class Implementation<M> implements PersistenceTypeHandlerManager<M>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		        final PersistenceTypeHandlerRegistry<M>   typeHandlerRegistry  ;
		private final PersistenceTypeHandlerProvider<M>   typeHandlerProvider  ;
		private final PersistenceTypeDictionaryManager    typeDictionaryManager;
		private final PersistenceTypeEvaluator            typeEvaluator        ;
		private final PersistenceTypeMismatchValidator<M> typeMismatchValidator;
		private       boolean                             initialized          ;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		Implementation(
			final PersistenceTypeHandlerRegistry<M>   typeHandlerRegistry  ,
			final PersistenceTypeHandlerProvider<M>   typeHandlerProvider  ,
			final PersistenceTypeDictionaryManager    typeDictionaryManager,
			final PersistenceTypeEvaluator            typeEvaluator        ,
			final PersistenceTypeMismatchValidator<M> typeMismatchValidator
		)
		{
			super();
			this.typeHandlerRegistry   = typeHandlerRegistry  ;
			this.typeHandlerProvider   = typeHandlerProvider  ;
			this.typeDictionaryManager = typeDictionaryManager;
			this.typeEvaluator         = typeEvaluator        ;
			this.typeMismatchValidator = typeMismatchValidator;
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

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
		
		private void internalRegisterTypeHandlers(final XGettingCollection<PersistenceTypeHandler<M, ?>> typeHandlers)
		{
			for(final PersistenceTypeHandler<M, ?> typeHandler : typeHandlers)
			{
				this.typeHandlerRegistry.registerTypeHandler(typeHandler);
			}
			
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
			 * apropriate type handler (No-op, etc.) manually beforehand.
			 *
			 * creating new type handlers in the process will eventually end up here again for the new types
			 * until all reachable types are ensured to have type handlers registered.
			 */
			typeHandler.getInstanceReferenceFields().iterate(e ->
			{
				try
				{
					this.ensureTypeHandler(e.getType());
				}
				catch(final RuntimeException t)
				{
					throw t; // debug hook
				}
			});
		}

		private <T> PersistenceTypeHandler<M, T> internalEnsureTypeHandler(final Class<T> type)
		{
			if(!this.typeEvaluator.test(type))
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
					handler = this.typeHandlerProvider.provideTypeHandler(tid);
					this.internalRegisterTypeHandler(handler);
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
				// (07.04.2013)EXCP proper exception
				throw new PersistenceExceptionTypeConsistency("Swizzle inconsistency for " + typeHandler.typeName());
			}

			final Equalator<PersistenceTypeDescriptionMember> memberValidator = (m1, m2) ->
			{
				if(m1 == null || m2 == null)
				{
					// (01.07.2015)EXCP proper exception
					throw new PersistenceExceptionTypeConsistency("Member count mismatch of type " + typeHandler.typeName());
				}

				if(m1.equals(m2, PersistenceTypeDescriptionMember.DESCRIPTION_MEMBER_EQUALATOR))
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



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final <T> PersistenceTypeHandler<M, T> ensureTypeHandler(final T instance)
		{
			// standard implementation does not consider actual objects
			return this.ensureTypeHandler(XReflect.getClass(instance));
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
			return this.typeHandlerRegistry.lookupTypeHandler(XReflect.getClass(instance));
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
		public final void validateExistingTypeMapping(final long typeId, final Class<?> type)
		{
			this.typeHandlerRegistry.validateExistingTypeMapping(typeId, type);
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
		public final void validateExistingTypeMappings(final Iterable<? extends SwizzleTypeLink<?>> mappings)
			throws SwizzleExceptionConsistency
		{
			this.typeHandlerRegistry.validateExistingTypeMappings(mappings);
		}

		@Override
		public final void validatePossibleTypeMappings(final Iterable<? extends SwizzleTypeLink<?>> mappings)
			throws SwizzleExceptionConsistency
		{
			this.typeHandlerRegistry.validatePossibleTypeMappings(mappings);
		}

		@Override
		public <C extends Consumer<? super PersistenceTypeHandler<M, ?>>> C iterateTypeHandlers(final C iterator)
		{
			this.typeHandlerRegistry.iterateTypeHandlers(iterator);
			return iterator;
		}

		@Override
		public final synchronized void initialize()
		{
			if(this.initialized)
			{
//				XDebug.debugln("already initialized");
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
		
		// (18.05.2018 TM)NOTE: old version before OGS-3
//		private void internalInitialize()
//		{
//			final PersistenceTypeDictionary typeDictionary = this.typeDictionaryManager.provideTypeDictionary();
//
//			final BulkList<PersistenceTypeDefinition<?>> runtimeTypes = BulkList.New();
//			typeDictionary.iterateLatestTypes(td ->
//			{
//				if(td.type() != null)
//				{
//					runtimeTypes.add(td);
//				}
//			});
//
//			final PersistenceTypeHandlerRegistry<M> typeRegistry = this.typeHandlerRegistry;
//
//			// register all runtime types (with validity check)
//			typeRegistry.registerTypes(runtimeTypes);
//
//			this.internalUpdateCurrentHighestTypeId(typeDictionary);
//
//			// ensure type handlers for all types in type dict (even on exception, type mappings have already been set)
//			runtimeTypes.iterate(e ->
//				this.ensureTypeHandler(e.type())
//			);
//
//			this.initialized = true;
//		}

		private void internalInitialize()
		{
			final PersistenceTypeDictionaryManager    tdm                 = this.typeDictionaryManager;
			final PersistenceTypeDictionary           typeDictionary      = tdm.provideTypeDictionary();
			final HashEnum<PersistenceTypeLineage<?>> runtimeTypeLineages = HashEnum.New();
			
			this.filterRuntimeTypeLineages(typeDictionary, runtimeTypeLineages);

			
			final HashTable<PersistenceTypeDefinition<?>, PersistenceTypeHandler<M, ?>> matches = HashTable.New();
			final HashEnum<PersistenceTypeHandler<M, ?>>                                misfits = HashEnum.New();

			// derive a type handler for every runtime type lineage and try to match an existing type definition
			for(final PersistenceTypeLineage<?> typeLineage : runtimeTypeLineages)
			{
				this.deriveTypeHandler(typeLineage, matches, misfits);
			}
			
			// pass all misfits and the typeDictionary to a PersistenceTypeMismatchEvaluator
			this.typeMismatchValidator.validateTypeMismatches(typeDictionary, misfits);
			
			// internally update the current hightest type id (you don't say...)
			this.internalUpdateCurrentHighestTypeId(typeDictionary);
			
			
			// initialize all matches to the associated TypeId
			final HashEnum<PersistenceTypeHandler<M, ?>> initializedTypeHandlers = HashEnum.New();
			for(final KeyValue<PersistenceTypeDefinition<?>, PersistenceTypeHandler<M, ?>> match : matches)
			{
				final long typeId = match.key().typeId();
				final PersistenceTypeHandler<M, ?> ith = match.value().initializeTypeId(typeId);
				initializedTypeHandlers.add(ith);
			}
			
			// assign new TypeIds to all misfits
			for(final PersistenceTypeHandler<M, ?> misfit : misfits)
			{
				// must be the TypeHandlerProvider's ensureTypeId in order to circumvent implicit handler creation.
				final long newTypeId = this.typeHandlerProvider.ensureTypeId(misfit.type());
				final PersistenceTypeHandler<M, ?> ith = misfit.initializeTypeId(newTypeId);
				initializedTypeHandlers.add(ith);
			}
			
			// register the current Type<->TypeId mapping
			this.typeHandlerRegistry.registerTypes(initializedTypeHandlers);
			
			// (18.05.2018 TM)TODO: OGS-3: set initialized handlers as runtime definitions for all runtimeTypeLineage
			
			// recursive registratio: initialized handlers themselves plus all handlers required for their field types
			this.internalRegisterTypeHandlers(initializedTypeHandlers);

			this.initialized = true;
		}
		
		private void filterRuntimeTypeLineages(
			final PersistenceTypeDictionary           typeDictionary     ,
			final HashEnum<PersistenceTypeLineage<?>> runtimeTypeLineages
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
		
		private <T> void deriveTypeHandler(
			final PersistenceTypeLineage<T>                                             typeLineage            ,
			final HashTable<PersistenceTypeDefinition<?>, PersistenceTypeHandler<M, ?>> matchedTypeHandlers    ,
			final HashEnum<PersistenceTypeHandler<M, ?>>                                unmatchableTypeHandlers
		)
		{
			final PersistenceTypeHandler<M, ?> handler = this.advanceEnsureTypeHandler(typeLineage.type());
			
			for(final PersistenceTypeDefinition<?> typeDefinition : typeLineage.entries().values())
			{
				// exact match including field order
				final boolean isMatched = PersistenceTypeDescriptionMember.equalMembers(
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
		public final PersistenceDistrict<M> createDistrict(final SwizzleRegistry registry)
		{
			return new PersistenceDistrict.Implementation<>(registry, this.typeHandlerRegistry);
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
