package net.jadoth.persistence.types;

import static net.jadoth.Jadoth.notNull;

import java.util.function.Consumer;

import net.jadoth.Jadoth;
import net.jadoth.collections.HashEnum;
import net.jadoth.collections.HashTable;
import net.jadoth.collections.types.XGettingEnum;
import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.persistence.exceptions.PersistenceExceptionTypeNotPersistable;
import net.jadoth.swizzling.exceptions.SwizzleExceptionConsistency;
import net.jadoth.swizzling.types.SwizzleRegistry;
import net.jadoth.swizzling.types.SwizzleTypeIdentity;
import net.jadoth.swizzling.types.SwizzleTypeLink;
import net.jadoth.swizzling.types.SwizzleTypeManager;
import net.jadoth.util.Equalator;
import net.jadoth.util.chars.VarString;


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



	/*
	 * (02.04.2013 TM)TODO: type slave alternative to request type id at master
	 * (and refresh type dictionary in the process)
	 */
	@Override
	public long ensureTypeId(Class<?> type);

	@Override
	public <T> Class<T> ensureType(long typeId);



	public final class Implementation<M> implements PersistenceTypeHandlerManager<M>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		        final PersistenceTypeHandlerRegistry<M> typeHandlerRegistry        ;
		private final PersistenceTypeHandlerProvider<M> typeHandlerProvider        ;
		private final PersistenceTypeDictionaryManager  typeDictionaryManager      ;
		private final PersistenceTypeEvaluator          typeEvaluatorTypeIdMappable;
		private final PersistenceTypeDefinitionResolver typeDefinitionResolver     ;
		private       boolean                           initialized                ;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		public Implementation(
			final PersistenceTypeHandlerRegistry<M> typeHandlerRegistry        ,
			final PersistenceTypeHandlerProvider<M> typeHandlerProvider        ,
			final PersistenceTypeDictionaryManager  typeDictionaryManager      ,
			final PersistenceTypeEvaluator          typeEvaluatorTypeIdMappable,
			final PersistenceTypeDefinitionResolver typeDefinitionResolver
		)
		{
			super();
			this.typeHandlerRegistry         = notNull(typeHandlerRegistry)        ;
			this.typeHandlerProvider         = notNull(typeHandlerProvider)        ;
			this.typeDictionaryManager       = notNull(typeDictionaryManager)      ;
			this.typeEvaluatorTypeIdMappable = notNull(typeEvaluatorTypeIdMappable);
			this.typeDefinitionResolver      = notNull(typeDefinitionResolver)     ;
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
			final PersistenceTypeDescription registeredTd =
				this.typeDictionaryManager.provideDictionary().lookupTypeByName(typeHandler.typeName())
			;
			if(registeredTd == null)
			{
				return; // type not yet registered, hence it can't be invalid
			}
			if(!SwizzleTypeIdentity.Static.equals(registeredTd, typeHandler.typeDescription()))
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

				if(m1.equals(m2, PersistenceTypeDescriptionMember.DESCRIPTION_MEMBER_EQUALATOR))
				{
					return true;
				}
				// (07.04.2013)EXCP proper exception
				throw new RuntimeException(
					"Inconsistent member in type description for type "
					+ typeHandler.typeName() + ": " + m1 + " != " + m2
				);
			};


			if(!PersistenceTypeDescription.equalMembers(registeredTd, typeHandler.typeDescription(), memberValidator))
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
				this.typeDictionaryManager.addTypeDescription(typeHandler.typeDescription());
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


		private XGettingSequence<PersistenceTypeDefinition<?>> resolveTypeDefinitions(
			final PersistenceTypeDictionary         typeDictionary
		)
		{
			final XGettingEnum<PersistenceTypeDescription>         typeDescriptions = typeDictionary.types();
			final HashEnum<PersistenceTypeDefinition<?>>           typeDefinitions  =
				HashEnum.NewCustom(typeDescriptions.size())
			;
			final HashTable<PersistenceTypeDescription, Exception> problems         =
				HashTable.NewCustom(typeDescriptions.intSize())
			;

			this.typeDefinitionResolver.resolveTypeDefinitions(
				typeDescriptions,
				td ->
					typeDefinitions.add(td),
				(td, ex) ->
					problems.add(td, ex)
			);

			if(!problems.isEmpty())
			{
				final String message = PersistenceTypeDefinitionResolver.assembleResolveExceptions(
					problems,
					VarString.New()
				)
				.toString();
				throw new RuntimeException(message);
			}

			return typeDefinitions;
		}

		private void internalInitialize()
		{
//			JadothConsole.debugln("initializing " + Jadoth.systemString(this.typeHandlerRegistry));

			final PersistenceTypeDictionary                      typeDictionary  =
				this.typeDictionaryManager.provideDictionary()
			;
			final XGettingSequence<PersistenceTypeDefinition<?>> typeDefinitions = this.resolveTypeDefinitions(
				typeDictionary
			);

			final PersistenceTypeHandlerRegistry<M> typeRegistry = this.typeHandlerRegistry;

			// validate all type mappings before registering anything
			typeRegistry.validatePossibleTypeMappings(typeDefinitions);

			// register type identities (typeId<->type) first to make all types available for type handler creation
			typeDefinitions.iterate(e ->
				typeRegistry.registerType(e.typeId(), e.type())
			);


			/* (05.05.2015 TM)TODO: /!\ type refactoring:
			 * - ensure a map of runtime type descriptions for all type descriptions in the provided dictionary.
			 * - Then compare them, resulting in a comparison result ("diff")
			 * - Pass the result to a modularized handler (throwing an exception or creating a refactoring plan)
			 * - use a returned refactoring plan for the update:
			 *   All effected entities are loaded, transformed as necessary and stored,
			 *   with a single transaction entry at the end of the whole refactoring.
			 *
			 * (29.11.2016 TM)NOTE:
			 *
			 * Storage:
			 * The refactoring type change should be noted as a string in a comment storage item (negative length)
			 * to maintain the ability to throw in all storage files ever created and process them up to the current
			 * state.
			 * This cannot be don in an outside type dictionary file, as the position and the existence is crucial
			 * for correctly interpreting the data and must be covered by a storage transaction entry.
			 *
			 * The comment would contain the old and the new type definition. All entities lying physically
			 * (which is the same as chronologically) before the type change entry are validated by the old definition,
			 * all after type change the type change entry are validated by the new definition.
			 * The type change entry would be the first item in the refactoring store and would be covered by the
			 * closing transaction entry.
			 * Processing a type change entry on startup would mean to hold a collection of all affected entities'
			 * OIDs of that type and then validate if the entites after the entry replace every last one of them to
			 * guarantee that no entity of the old structure is accessible. This would not be necessary for mere
			 * name changes, only for strutural changes (number and position of fields).
			 * The same String must be contained in every channel and be taged with the same timestamp.
			 * If one or more channels are missing some type change entries for timestamps or the entries differ,
			 * then something is wrong. This must be tested during initialization.
			 *
			 * The need for such a intrinsic type information raises the question, if the type dictionary itself
			 * should always be included in the storage data files. Currently, they only match "by chance", but
			 * are not guaranteed to.
			 * The external type dictionary could still be kept for the following two reasons ...
			 * 1.) conveniently change naming (types and fields), which are non-structural and therefore arbitrary
			 * 2.) provide type information and TIDs in advance, even if the application has no encountered them, yet.
			 * ... but ultimately, it must be optional and derivable from the database files in case it is missing.
			 *
			 * This also means: EVERYTHING except actual DB files is optional:
			 * - if a db is known to be consistent, transaction logs can be deleted (this is already the case)
			 * - OID- and TID-files are optional, as the current highest IDs can be derived from the initialization
			 * - type dictionary is optional, if an initial type entry and type change entries are used
			 *
			 * Hm. However, keeping the tpye information preserved means it cannot be just a storage item gap.
			 * It has to be a proper entity in order to get carried over to new files.
			 * But that would be a special kind of storage item that is no entity and is not reachable by the GC.
			 * Not good :-/.
			 *
			 * Also:
			 * Having a type entry of a previous version of the type being transferred to a position
			 * after a type entry of a newer version of the type would mess up consistency. So every type entry
			 * would have to be exactely only one type and it would have to be replaced by its newer version,
			 * just like new versions of an entity replace the old ones and make them untransferrable gaps.
			 *
			 * Maybe the type change entries should indeed actually be Class entities with a special string as its
			 * content. This would fit together perfectly. There's only the one slight complication, that every
			 * stored Class entity would have to be stored in a Class list referenced by the root.
			 *
			 * Classes - and Number constants - should be persisted anyway for consistency reasons.
			 * Currently, they are omitted, causing an intentional loss of information because it is
			 * reconstructed and runtime inside the JVM. But analyzing the persisted form itself currently has
			 * missing information, which is not good.
			 *
			 * Maybe this is overhinking. Maybe the following is enough:
			 * - Upon initialization, every entry is registered without prior validation.
			 * - Later entities replace prior ones, as is.
			 * - Once all entities are registered, all entities (in their latest version) are validated via the
			 *   latest type dictionary.
			 * (this is already the case, see StorageEntityCache. There is even a comment about refactoring)
			 *
			 * Maybe it makes sense for completeness and consistency to include the current type dictionary
			 * as a comment at the start of every file. If every refactoring mandatorily creates a new file,
			 * then the type consistency is always guaranteed by this header comment, even if it does not get
			 * transferred as a pseudo-entity.
			 * The current type dictionary could still be derived from the raw storage files.
			 * The storage channels' type dictionaries could still be validated against each other.
			 *
			 * That header entry could also contain meta information that may or may not be used for validation during
			 * initialization. Like: creation timestamp of the file, endianess, channel ID, file ID. All strictly with a
			 * never-overwrite algorithm as with the rest of the file. All optional in a comment item (negative length).
			 * Such an - optional - header would be the ultimate definition of the identity and the context of a
			 * storage file and its content and make it perfectly validatable and even correctable in case file names
			 * were messed with.
			 */

			this.update(typeDictionary);

			// ensure type handlers for all types in type dict (even on exception, type mappings have already been set)
			typeDefinitions.iterate(e ->
				this.ensureTypeHandler(e.type())
			);

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
