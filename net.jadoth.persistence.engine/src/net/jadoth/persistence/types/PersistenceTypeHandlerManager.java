package net.jadoth.persistence.types;

import static net.jadoth.X.array;
import static net.jadoth.X.notNull;

import java.util.function.Consumer;

import net.jadoth.collections.BulkList;
import net.jadoth.collections.HashEnum;
import net.jadoth.collections.HashTable;
import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.collections.types.XGettingEnum;
import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.collections.types.XGettingTable;
import net.jadoth.equality.Equalator;
import net.jadoth.persistence.exceptions.PersistenceExceptionTypeConsistency;
import net.jadoth.persistence.exceptions.PersistenceExceptionTypeConsistencyDefinitionResolveTypeName;
import net.jadoth.persistence.exceptions.PersistenceExceptionTypeNotPersistable;
import net.jadoth.reflect.XReflect;
import net.jadoth.swizzling.exceptions.SwizzleExceptionConsistency;
import net.jadoth.swizzling.types.SwizzleRegistry;
import net.jadoth.swizzling.types.SwizzleTypeIdentity;
import net.jadoth.swizzling.types.SwizzleTypeLink;
import net.jadoth.swizzling.types.SwizzleTypeManager;
import net.jadoth.typing.KeyValue;
import net.jadoth.util.matching.MultiMatch;
import net.jadoth.util.matching.MultiMatcher;


public interface PersistenceTypeHandlerManager<M> extends SwizzleTypeManager, PersistenceTypeHandlerRegistry<M>
{
	@Override
	public <T> PersistenceTypeHandler<M, T> lookupTypeHandler(T instance);

	@Override
	public <T> PersistenceTypeHandler<M, T> lookupTypeHandler(Class<T> type);

	@Override
	public PersistenceTypeHandler<M, ?> lookupTypeHandler(long typeId);

	public <T> PersistenceTypeHandler<M, T> ensureTypeHandler(T instance);

	public <T> PersistenceTypeHandler<M, T> ensureTypeHandler(Class<T> type);

	public PersistenceTypeHandler<M, ?> ensureTypeHandler(long tid);
	
	public void ensureTypeHandlers(XGettingEnum<Long> tids);

	public void initialize();

	public PersistenceDistrict<M> createDistrict(SwizzleRegistry registry);

	public void update(PersistenceTypeDictionary typeDictionary, long highestTypeId);

	public default void update(final PersistenceTypeDictionary typeDictionary)
	{
		this.update(typeDictionary, 0);
	}
	
	@Override
	public long ensureTypeId(Class<?> type);

	@Override
	public Class<?> ensureType(long typeId);



	public static <M> PersistenceTypeHandlerManager.Implementation<M> New(
		final PersistenceTypeHandlerRegistry<M>       typeHandlerRegistry       ,
		final PersistenceTypeHandlerProvider<M>       typeHandlerProvider       ,
		final PersistenceTypeDictionaryManager        typeDictionaryManager     ,
		final PersistenceTypeEvaluator                typeEvaluator             ,
		final PersistenceTypeMismatchValidator<M>     typeMismatchValidator     ,
		final PersistenceRefactoringMappingProvider   refactoringMappingProvider,
		final PersistenceDeletedTypeHandlerCreator<M> deletedTypeHandlerCreator
	)
	{
		return new PersistenceTypeHandlerManager.Implementation<>(
			notNull(typeHandlerRegistry)       ,
			notNull(typeHandlerProvider)       ,
			notNull(typeDictionaryManager)     ,
			notNull(typeEvaluator)             ,
			notNull(typeMismatchValidator)     ,
			notNull(refactoringMappingProvider),
			notNull(deletedTypeHandlerCreator)
		);
	}



	public final class Implementation<M> implements PersistenceTypeHandlerManager<M>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		        final PersistenceTypeHandlerRegistry<M>       typeHandlerRegistry       ;
		private final PersistenceTypeHandlerProvider<M>       typeHandlerProvider       ;
		private final PersistenceTypeDictionaryManager        typeDictionaryManager     ;
		private final PersistenceTypeEvaluator                typeEvaluator             ;
		private final PersistenceTypeMismatchValidator<M>     typeMismatchValidator     ;
		private final PersistenceRefactoringMappingProvider   refactoringMappingProvider;
		private final PersistenceDeletedTypeHandlerCreator<M> deletedTypeHandlerCreator ;
		private       boolean                                 initialized               ;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		Implementation(
			final PersistenceTypeHandlerRegistry<M>       typeHandlerRegistry       ,
			final PersistenceTypeHandlerProvider<M>       typeHandlerProvider       ,
			final PersistenceTypeDictionaryManager        typeDictionaryManager     ,
			final PersistenceTypeEvaluator                typeEvaluator             ,
			final PersistenceTypeMismatchValidator<M>     typeMismatchValidator     ,
			final PersistenceRefactoringMappingProvider   refactoringMappingProvider,
			final PersistenceDeletedTypeHandlerCreator<M> deletedTypeHandlerCreator
		)
		{
			super();
			this.typeHandlerRegistry        = typeHandlerRegistry       ;
			this.typeHandlerProvider        = typeHandlerProvider       ;
			this.typeDictionaryManager      = typeDictionaryManager     ;
			this.typeEvaluator              = typeEvaluator             ;
			this.typeMismatchValidator      = typeMismatchValidator     ;
			this.refactoringMappingProvider = refactoringMappingProvider;
			this.deletedTypeHandlerCreator  = deletedTypeHandlerCreator ;
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////
		
		public static char memberIdentifierSeparator()
		{
			// (05.09.2018 TM)TODO: centralize and make configurable
			return '#';
		}

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

		private PersistenceTypeHandler<M, ?> internalEnsureTypeHandlerByTypeId(final long tid)
		{
			synchronized(this.typeHandlerRegistry)
			{
				PersistenceTypeHandler<M, ?> handler;
				if((handler = this.typeHandlerRegistry.lookupTypeHandler(tid)) == null)
				{
					handler = this.tryLegacyTypeHandler(tid);
					if(handler == null)
					{
						handler = this.createProperTypeHandler(tid);
					}
				}
				
				return handler;
			}
		}
		
		private PersistenceTypeHandler<M, ?> tryLegacyTypeHandler(final long tid)
		{
			/* (30.05.2018 TM)TODO: OGS-3: custom legacy handler lookup
			 * A way is required to make a lookup in a custom legacy handler registry, first.
			 * Also, the lookup should not only use the TypeId, but prior to that a lookup via the
			 * structure, because it is cumbersome (or logically misplaced) to have to manually assign TypeIds.
			 * The structure is what identifies the handler on a logical level, not an internal technical id.
			 */
			
			final PersistenceTypeDictionary    typeDict = this.typeDictionaryManager.provideTypeDictionary();
			final PersistenceTypeDefinition<?> typeDef  = typeDict.lookupTypeById(tid);
			if(typeDef == null)
			{
				// the type id might refer to a new type that has no handler registered, yet, so this is not an error.
				return null;
			}
			
			final PersistenceTypeLineage<?> typeLin = typeDict.lookupTypeLineage(typeDef.typeName());
			if(typeLin.runtimeDefinition() == typeDef)
			{
				// the tid belongs to the type's runtime type definition, so a legacy type handler is not required.
				return null;
			}
			
			// existing, but not current type version (identified by the typeId), so create a legacy handler.
			return this.createLegacyTypeHandler(typeDef);
			
		}
		private PersistenceTypeHandler<M, ?> createProperTypeHandler(final long tid)
		{
			final PersistenceTypeHandler<M, ?> handler = this.typeHandlerProvider.provideTypeHandler(tid);
			this.internalRegisterTypeHandler(handler);
			
			return handler;
		}
		
		private static IdentifierBuilder[] createSourceIdentifierBuilders()
		{
			/*
			 * identifier building logic in order of priority:
			 * - global identifier (means most specific)
			 * - internal identifier
			 */
			return array(
				(t, m) ->
					toGlobalIdentifier(t, m),
				(t, m) ->
					toTypeInternalIdentifier(m)
			);
		}
		
		private static IdentifierBuilder[] createTargetIdentifierBuilders()
		{
			/*
			 * identifier building logic in order of priority:
			 * - global identifier (means most specific)
			 * - internal identifier
			 * - unqualified identifier IF unambiguous (unique) or else null.
			 */
			return array(
				(t, m) ->
					toGlobalIdentifier(t, m),
				(t, m) ->
					toTypeInternalIdentifier(m),
				(t, m) ->
					toUniqueUnqualifiedIdentifier(t, m)
			);
		}
		
		private static void addDeletionMembers(
			final XGettingSequence<PersistenceTypeDescriptionMember>                            deletionMembers,
			final HashTable<PersistenceTypeDescriptionMember, PersistenceTypeDescriptionMember> resolvedMembers
		)
		{
			for(final PersistenceTypeDescriptionMember deletionMember : deletionMembers)
			{
				if(resolvedMembers.add(deletionMember, null))
				{
					continue;
				}
				
				// (11.09.2018 TM)EXCP: proper exception
				throw new PersistenceExceptionTypeConsistency(
					"Conflicted mapping entry for member " + deletionMember.uniqueName()
				);
			}
		}
						
		private PersistenceTypeHandler<M, ?> createLegacyTypeHandler(
			final PersistenceTypeDefinition<?> typeDefinition
		)
		{
			final Class<?> runtimeType = this.resolveRuntimeType(typeDefinition);
			if(runtimeType == null)
			{
				// null indicates that the type has explicitely been mapped to nothing, i.e. shall be seen as deleted.
				return this.createDeletedTypeHandler(typeDefinition);
			}
			
			final PersistenceTypeHandler<M, ?> runtimeTypeHandler = this.ensureTypeHandler(runtimeType);
						
			final HashTable<String, PersistenceTypeDescriptionMember> refacTargetStrings   = HashTable.New();
			final HashEnum<PersistenceTypeDescriptionMember>          refacDeletionMembers = HashEnum.New();
			
			this.collectRefactoringTargetStrings(
				typeDefinition      ,
				refacTargetStrings  ,
				refacDeletionMembers
			);
			
			final HashTable<PersistenceTypeDescriptionMember, PersistenceTypeDescriptionMember> resolvedMembers = HashTable.New();
			
			this.resolveToTargetMembers(refacTargetStrings, runtimeTypeHandler, resolvedMembers);
			
			addDeletionMembers(refacDeletionMembers, resolvedMembers);
						
			final BulkList<? extends PersistenceTypeDescriptionMember> sourceMembers = BulkList.New(
				typeDefinition.members()
			);
			final BulkList<? extends PersistenceTypeDescriptionMember> targetMembers = BulkList.New(
				runtimeTypeHandler.members()
			);
			
			// null out all explicitely mapped members before matching
			sourceMembers.replace(m ->
				resolvedMembers.keys().contains(m),
				null
			);
			targetMembers.replace(m ->
				resolvedMembers.values().contains(m),
				null
			);
			
			final MultiMatcher<PersistenceTypeDescriptionMember> matcher = MultiMatcher.New();
			
			// (11.09.2018 TM)FIXME: OGS-3: Member similator
			// (11.09.2018 TM)FIXME: OGS-3: Include MatchValidator. Or encapsulate the whole mapping in the first place.
			// (11.09.2018 TM)FIXME: OGS-3: match evaluator callback logic
			
			final MultiMatch<PersistenceTypeDescriptionMember> match = matcher.match(sourceMembers, targetMembers);
			
			/* (11.09.2018 TM)FIXME: OGS-3: Derive PersistenceLegacyTypeHandler from definite Mapping result.
			 * - derive value mapper for each result (including changed field offsets)
			 * - wrapp all value mappers in a PersistenceTypeHandler instance.
			 * complex values are not supported for now but throw an exception.
			 */
						
			throw new net.jadoth.meta.NotImplementedYetError();
		}
		
		private void resolveToTargetMembers(
			final XGettingTable<String, PersistenceTypeDescriptionMember>                       refacTargetStrings,
			final PersistenceTypeDefinition<?>                                                  targetTypeDef     ,
			final HashTable<PersistenceTypeDescriptionMember, PersistenceTypeDescriptionMember> resolvedMembers
		)
		{
			final IdentifierBuilder[] identifierBuilders = createTargetIdentifierBuilders();
			
			targetMembers:
			for(final PersistenceTypeDescriptionMember targetMember : targetTypeDef.members())
			{
				for(final IdentifierBuilder identifierBuilder : identifierBuilders)
				{
					if(check(targetTypeDef, targetMember, refacTargetStrings, resolvedMembers, identifierBuilder))
					{
						continue targetMembers;
					}
				}
			}
		}
		
		@FunctionalInterface
		interface IdentifierBuilder
		{
			public String buildIdentifier(PersistenceTypeDefinition<?> type, PersistenceTypeDescriptionMember member);
		}
		
		private static boolean check(
			final PersistenceTypeDefinition<?>                                                  targetTypeDefinition,
			final PersistenceTypeDescriptionMember                                              targetTypeMember    ,
			final XGettingTable<String, PersistenceTypeDescriptionMember>                       sourceLookupTable   ,
			final HashTable<PersistenceTypeDescriptionMember, PersistenceTypeDescriptionMember> resolvedMembers     ,
			final IdentifierBuilder                                                             identifierBuilder
		)
		{
			final String identifier = identifierBuilder.buildIdentifier(targetTypeDefinition, targetTypeMember);
			final PersistenceTypeDescriptionMember defClassTargetMember = sourceLookupTable.get(identifier);
			
			if(defClassTargetMember == null)
			{
				return false;
			}
			
			if(resolvedMembers.add(defClassTargetMember, targetTypeMember))
			{
				return true;
			}
			
			// (10.09.2018 TM)EXCP: proper exception
			throw new PersistenceExceptionTypeConsistency(
				"Duplicate member mapping for target member \"" + identifier + "\""
			);
		}
		
		private void collectRefactoringTargetStrings(
			final PersistenceTypeDefinition<?>                        typeDefinition      ,
			final HashTable<String, PersistenceTypeDescriptionMember> refacTargetStrings  ,
			final HashEnum<PersistenceTypeDescriptionMember>          refacDeletionMembers
		)
		{
			final XGettingTable<String, String> refacEntries = this.ensureRefactoringMapping().entries();
			
			final IdentifierBuilder[] identifierBuilders = createSourceIdentifierBuilders();
			
			for(final PersistenceTypeDescriptionMember member : typeDefinition.members())
			{
				for(final IdentifierBuilder identifierBuilder : identifierBuilders)
				{
					final String identifier = identifierBuilder.buildIdentifier(typeDefinition, member);
					if(check(member, identifier, refacEntries, refacTargetStrings, refacDeletionMembers))
					{
						continue;
					}
				}
			}
		}
		
		private static boolean check(
			final PersistenceTypeDescriptionMember                    member                    ,
			final String                                              lookupString              ,
			final XGettingTable<String, String>                       refactoringEntries        ,
			final HashTable<String, PersistenceTypeDescriptionMember> refactoringTargetStrings  ,
			final HashEnum<PersistenceTypeDescriptionMember>          refactoringDeletionMembers
		)
		{
			// must check keys themselves, as a null value means deletion
			if(refactoringEntries.keys().contains(lookupString))
			{
				// might be null to indicate deletion
				final String targetString = refactoringEntries.get(lookupString);
				if(targetString == null)
				{
					refactoringDeletionMembers.add(member);
				}
				else
				{
					if(!refactoringTargetStrings.add(targetString, member))
					{
						// (10.09.2018 TM)EXCP: proper exception
						throw new PersistenceExceptionTypeConsistency(
							"Duplicate member mapping for target member \"" + targetString + "\""
						);
					}
				}
				
				return true;
			}
			
			return false;
		}
		
		static String toUniqueUnqualifiedIdentifier(
			final PersistenceTypeDefinition<?>     typeDefinition,
			final PersistenceTypeDescriptionMember member
		)
		{
			final String memberSimpleName = member.name();
			
			for(final PersistenceTypeDescriptionMember m : typeDefinition.members())
			{
				if(m == member)
				{
					continue;
				}
				
				// if the simple name is not unique, it cannot be used as a mapping target
				if(m.name().equals(memberSimpleName))
				{
					return null;
				}
			}
			
			return memberIdentifierSeparator() + memberSimpleName;
		}
		
		static String toGlobalIdentifier(
			final PersistenceTypeDefinition<?>     typeDefinition,
			final PersistenceTypeDescriptionMember member
		)
		{
			return typeDefinition.typeName() + memberIdentifierSeparator() + toTypeInternalIdentifier(member);
		}
		
		static String toTypeInternalIdentifier(final PersistenceTypeDescriptionMember member)
		{
			return member.uniqueName();
		}
		
		private Class<?> resolveRuntimeType(final PersistenceTypeDefinition<?> typeDefinition)
		{
			if(typeDefinition.type() != null)
			{
				return typeDefinition.type();
			}
			
			return this.resolveMappedRuntimeType(typeDefinition.typeName());
		}
		
		private PersistenceRefactoringMapping ensureRefactoringMapping()
		{
			return this.refactoringMappingProvider.provideRefactoringMapping();
		}
		
		private Class<?> resolveMappedRuntimeType(final String typeName)
		{
			final PersistenceRefactoringMapping refactoringMapping = this.ensureRefactoringMapping();
			
			if(refactoringMapping.entries().keys().contains(typeName))
			{
				final String mappedTypeName = refactoringMapping.entries().get(typeName);
				if(mappedTypeName != null)
				{
					try
					{
						return XReflect.classForName(mappedTypeName);
					}
					catch (final ClassNotFoundException e)
					{
						throw new PersistenceExceptionTypeConsistencyDefinitionResolveTypeName(mappedTypeName, e);
					}
				}
				
				// null indicates that the type has explicitely been mapped to nothing, i.e. shall be seen as deleted.
				return null;
			}
			
			/* At this point, the type definition neither has a fitting runtime type nor an entry in the explicit
			 * refactoring mapping. There are not options left to handle the type, so it is an error.
			 */
			throw new PersistenceExceptionTypeConsistencyDefinitionResolveTypeName(typeName);
		}
		
		private PersistenceTypeHandler<M, ?> createDeletedTypeHandler(
			final PersistenceTypeDefinition<?> typeDefinition
		)
		{
			final PersistenceDeletedTypeHandler<M, ?> typeHandler =
				this.deletedTypeHandlerCreator.createDeletedTypeHandler(typeDefinition)
			;
			
			// direct registration without any validation or dictionary entry. This is just a runtime dummy logic.
			this.registerLegacyTypeHandler(typeHandler);
			
			return typeHandler;
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

				if(m1.equalsDescription(m2))
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
		public final PersistenceTypeHandler<M, ?> ensureTypeHandler(final long tid)
		{
			final PersistenceTypeHandler<M, ?> handler; // quick read-only check for already registered type
			if((handler = this.typeHandlerRegistry.lookupTypeHandler(tid)) != null)
			{
				return handler;
			}
			return this.internalEnsureTypeHandlerByTypeId(tid);
		}
		

		@Override
		public void ensureTypeHandlers(final XGettingEnum<Long> tids)
		{
			synchronized(this.typeHandlerRegistry)
			{
				for(final Long tid : tids)
				{
					this.internalEnsureTypeHandlerByTypeId(tid);
				}
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
		public final PersistenceTypeHandler<M, ?> lookupTypeHandler(final long objectId, final long typeId)
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
		public final boolean registerLegacyTypeHandler(final PersistenceLegacyTypeHandler<M, ?> legacyTypeHandler)
		{
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

		private void internalInitialize()
		{
			final PersistenceTypeDictionary typeDictionary = this.typeDictionaryManager.provideTypeDictionary();
			
			final HashEnum<PersistenceTypeHandler<M, ?>> newTypeHandlers      = HashEnum.New();
			final HashEnum<PersistenceTypeHandler<M, ?>> existingTypeHandlers = HashEnum.New();
			
			// either fill/initialize an empty type dictionary or initalize from a non-empty dictionary.
			if(typeDictionary.isEmpty())
			{
				this.initializeBlank(newTypeHandlers);
			}
			else
			{
				this.initializeFromDictionary(typeDictionary, existingTypeHandlers, newTypeHandlers);
			}

			this.initializeNewTypeHandlers(existingTypeHandlers, newTypeHandlers);
		}
		
		private void initializeBlank(final HashEnum<PersistenceTypeHandler<M, ?>> newTypeHandlers)
		{
			this.typeHandlerProvider.iterateTypeHandlers(newTypeHandlers);
		}
		
		private void initializeFromDictionary(
			final PersistenceTypeDictionary              typeDictionary         ,
			final HashEnum<PersistenceTypeHandler<M, ?>> initializedTypeHandlers,
			final HashEnum<PersistenceTypeHandler<M, ?>> newTypeHandlers
		)
		{
			final HashEnum<PersistenceTypeLineage<?>> runtimeTypeLineages = HashEnum.New();
			
			this.filterRuntimeTypeLineages(typeDictionary, runtimeTypeLineages);
			
			final HashTable<PersistenceTypeDefinition<?>, PersistenceTypeHandler<M, ?>> matches = HashTable.New();

			// derive a type handler for every runtime type lineage and try to match an existing type definition
			for(final PersistenceTypeLineage<?> typeLineage : runtimeTypeLineages)
			{
				this.deriveTypeHandler(typeLineage, matches, newTypeHandlers);
			}
			
			// pass all misfits and the typeDictionary to a PersistenceTypeMismatchEvaluator
			this.typeMismatchValidator.validateTypeMismatches(typeDictionary, newTypeHandlers);
			
			// internally update the current hightest type id (you don't say...)
			this.internalUpdateCurrentHighestTypeId(typeDictionary);
			
			// initialize all matches to the associated TypeId
			for(final KeyValue<PersistenceTypeDefinition<?>, PersistenceTypeHandler<M, ?>> match : matches)
			{
				final long typeId = match.key().typeId();
				final PersistenceTypeHandler<M, ?> ith = match.value().initializeTypeId(typeId);
				initializedTypeHandlers.add(ith);
			}
		}
		
		private void initializeNewTypeHandlers(
			final XGettingCollection<PersistenceTypeHandler<M, ?>> existingTypeHandlers,
			final XGettingCollection<PersistenceTypeHandler<M, ?>> newTypeHandlers
		)
		{
			final HashEnum<PersistenceTypeHandler<M, ?>> initializedTypeHandlers = HashEnum.New(existingTypeHandlers);
			
			// assign new TypeIds to all misfits
			for(final PersistenceTypeHandler<M, ?> newTypeHandler : newTypeHandlers)
			{
				// must be the TypeHandlerProvider's ensureTypeId in order to circumvent implicit handler creation.
				final long newTypeId = this.typeHandlerProvider.ensureTypeId(newTypeHandler.type());
				final PersistenceTypeHandler<M, ?> ith = newTypeHandler.initializeTypeId(newTypeId);
				initializedTypeHandlers.add(ith);
			}
			
			// register the current Type<->TypeId mapping
			this.typeHandlerRegistry.registerTypes(initializedTypeHandlers);
			
			// set initialized handlers as runtime definitions
			this.typeDictionaryManager.registerRuntimeTypeDefinitions(initializedTypeHandlers);
						
			// recursive registration: initialized handlers themselves plus all handlers required for their field types
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
				final boolean isMatched = PersistenceTypeDescriptionMember.equalDescriptions(
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
