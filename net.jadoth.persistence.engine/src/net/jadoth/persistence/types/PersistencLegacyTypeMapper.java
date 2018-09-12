package net.jadoth.persistence.types;

import static net.jadoth.X.array;
import static net.jadoth.X.notNull;

import net.jadoth.collections.BulkList;
import net.jadoth.collections.HashEnum;
import net.jadoth.collections.HashTable;
import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.collections.types.XGettingTable;
import net.jadoth.persistence.exceptions.PersistenceExceptionTypeConsistency;
import net.jadoth.persistence.exceptions.PersistenceExceptionTypeConsistencyDefinitionResolveTypeName;
import net.jadoth.reflect.XReflect;
import net.jadoth.util.matching.MultiMatch;
import net.jadoth.util.matching.MultiMatcher;

public interface PersistencLegacyTypeMapper<M>
{
	public <T> PersistenceLegacyTypeHandler<M, T> ensureLegacyTypeHandler(
		PersistenceTypeDefinition<T> legacyTypeDefinition ,
		PersistenceTypeDefinition<T> currentTypeDefinition
	);
	
	public <T> Class<T> lookupRuntimeType(
		PersistenceTypeDefinition<T> legacyTypeDefinition
	);
	
	
	
	public static <M> PersistencLegacyTypeMapper<M> New(
		final PersistenceRefactoringMappingProvider   refactoringMappingProvider,
		final PersistenceDeletedTypeHandlerCreator<M> deletedTypeHandlerCreator ,
		final char                                    identifierSeparator
	)
	{
		return new PersistencLegacyTypeMapper.Implementation<>(
			notNull(refactoringMappingProvider),
			notNull(deletedTypeHandlerCreator) ,
			        identifierSeparator
		);
	}

	public class Implementation<M> implements PersistencLegacyTypeMapper<M>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final PersistenceRefactoringMappingProvider   refactoringMappingProvider;
		private final PersistenceDeletedTypeHandlerCreator<M> deletedTypeHandlerCreator ;
		private final char                                    identifierSeparator       ;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected Implementation(
			final PersistenceRefactoringMappingProvider   refactoringMappingProvider,
			final PersistenceDeletedTypeHandlerCreator<M> deletedTypeHandlerCreator ,
			final char                                    identifierSeparator
		)
		{
			super();
			this.refactoringMappingProvider = refactoringMappingProvider;
			this.deletedTypeHandlerCreator  = deletedTypeHandlerCreator ;
			this.identifierSeparator        = identifierSeparator       ;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		public char identifierSeparator()
		{
			return this.identifierSeparator;
		}
		
		private <T> PersistenceLegacyTypeHandler<M, T> createLegacyTypeHandler(
			final PersistenceTypeDefinition<T> legacyTypeDefinition ,
			final PersistenceTypeDefinition<T> currentTypeDefinition
		)
		{
			final HashTable<String, PersistenceTypeDescriptionMember> refacTargetStrings   = HashTable.New();
			final HashEnum<PersistenceTypeDescriptionMember>          refacDeletionMembers = HashEnum.New();
			
			this.collectRefactoringTargetStrings(legacyTypeDefinition, refacTargetStrings ,refacDeletionMembers);
			
			final HashTable<PersistenceTypeDescriptionMember, PersistenceTypeDescriptionMember> resolvedMembers = HashTable.New();
			
			this.resolveToTargetMembers(refacTargetStrings, currentTypeDefinition, resolvedMembers);
			
			addDeletionMembers(refacDeletionMembers, resolvedMembers);
						
			final BulkList<? extends PersistenceTypeDescriptionMember> sourceMembers = BulkList.New(
				legacyTypeDefinition.members()
			);
			final BulkList<? extends PersistenceTypeDescriptionMember> targetMembers = BulkList.New(
				currentTypeDefinition.members()
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
			
			final MultiMatch<PersistenceTypeDescriptionMember> match = matcher.match(sourceMembers, targetMembers);
			
			// (11.09.2018 TM)FIXME: OGS-3: match evaluator callback logic
			/* (11.09.2018 TM)FIXME: OGS-3: Derive PersistenceLegacyTypeHandler from definite Mapping result.
			 * - derive value mapper for each result (including changed field offsets)
			 * - wrap all value mappers in a PersistenceTypeHandler instance.
			 * complex values are not supported for now but cause an exception.
			 */
						
			throw new net.jadoth.meta.NotImplementedYetError();
		}
		
		@Override
		public <T> PersistenceLegacyTypeHandler<M, T> ensureLegacyTypeHandler(
			final PersistenceTypeDefinition<T> legacyTypeDefinition ,
			final PersistenceTypeDefinition<T> currentTypeDefinition
		)
		{
			if(currentTypeDefinition == null)
			{
				// null indicates that the type has explicitely been mapped to nothing, i.e. shall be seen as deleted.
				return this.deletedTypeHandlerCreator.createDeletedTypeHandler(legacyTypeDefinition);
			}
			
			/* (30.05.2018 TM)TODO: OGS-3: custom legacy handler lookup
			 * A way is required to make a lookup in a custom legacy handler registry, first.
			 * Also, the lookup should not only use the TypeId, but prior to that a lookup via the
			 * structure, because it is cumbersome (or logically misplaced) to have to manually assign TypeIds.
			 * The structure is what identifies the handler on a logical level, not an internal technical id.
			 */
			
			// at this point a legacy handler must be creatable or something went wrong.
			return this.createLegacyTypeHandler(legacyTypeDefinition, currentTypeDefinition);
		}
		
		private static IdentifierBuilder[] createSourceIdentifierBuilders(final char separator)
		{
			/*
			 * identifier building logic in order of priority:
			 * - global identifier (means most specific)
			 * - internal identifier
			 */
			return array(
				(t, m) ->
					toGlobalIdentifier(t, m, separator),
				(t, m) ->
					toTypeInternalIdentifier(m)
			);
		}
		
		private static IdentifierBuilder[] createTargetIdentifierBuilders(final char separator)
		{
			/*
			 * identifier building logic in order of priority:
			 * - global identifier (means most specific)
			 * - internal identifier
			 * - unqualified identifier IF unambiguous (unique) or else null.
			 */
			return array(
				(t, m) ->
					toGlobalIdentifier(t, m, separator),
				(t, m) ->
					toTypeInternalIdentifier(m),
				(t, m) ->
					toUniqueUnqualifiedIdentifier(t, m, separator)
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
			

		
		private void resolveToTargetMembers(
			final XGettingTable<String, PersistenceTypeDescriptionMember>                       refacTargetStrings,
			final PersistenceTypeDefinition<?>                                                  targetTypeDef     ,
			final HashTable<PersistenceTypeDescriptionMember, PersistenceTypeDescriptionMember> resolvedMembers
		)
		{
			final IdentifierBuilder[] identifierBuilders = createTargetIdentifierBuilders(this.identifierSeparator());
			
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
			
			final IdentifierBuilder[] identifierBuilders = createSourceIdentifierBuilders(this.identifierSeparator());
			
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
			final PersistenceTypeDescriptionMember member        ,
			final char                             separator
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
			
			return separator + memberSimpleName;
		}
		
		static String toGlobalIdentifier(
			final PersistenceTypeDefinition<?>     typeDefinition,
			final PersistenceTypeDescriptionMember member        ,
			final char                             separator
		)
		{
			return typeDefinition.typeName() + separator + toTypeInternalIdentifier(member);
		}
		
		static String toTypeInternalIdentifier(final PersistenceTypeDescriptionMember member)
		{
			return member.uniqueName();
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public <T> Class<T> lookupRuntimeType(final PersistenceTypeDefinition<T> legacyTypeDefinition)
		{
			return (Class<T>)this.resolveMappedRuntimeType(legacyTypeDefinition.typeName());
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
			 * refactoring mapping. There are not options left to handle the type name, so it is an error.
			 */
			throw new PersistenceExceptionTypeConsistencyDefinitionResolveTypeName(typeName);
		}
			
		
	}


	@FunctionalInterface
	interface IdentifierBuilder
	{
		public String buildIdentifier(PersistenceTypeDefinition<?> type, PersistenceTypeDescriptionMember member);
	}
	
}
