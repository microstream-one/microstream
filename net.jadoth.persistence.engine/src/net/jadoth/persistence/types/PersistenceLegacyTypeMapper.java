package net.jadoth.persistence.types;

import static net.jadoth.X.array;
import static net.jadoth.X.notNull;

import net.jadoth.chars.VarString;
import net.jadoth.collections.BulkList;
import net.jadoth.collections.EqHashTable;
import net.jadoth.collections.HashTable;
import net.jadoth.collections.types.XGettingTable;
import net.jadoth.equality.Equalator;
import net.jadoth.functional.Similator;
import net.jadoth.persistence.exceptions.PersistenceExceptionTypeConsistency;
import net.jadoth.persistence.exceptions.PersistenceExceptionTypeConsistencyDefinitionResolveTypeName;
import net.jadoth.reflect.XReflect;
import net.jadoth.typing.KeyValue;
import net.jadoth.typing.TypeMappingLookup;
import net.jadoth.util.matching.MatchValidator;
import net.jadoth.util.matching.MultiMatch;
import net.jadoth.util.matching.MultiMatcher;

public interface PersistenceLegacyTypeMapper<M>
{
	public <T> PersistenceLegacyTypeHandler<M, T> ensureLegacyTypeHandler(
		PersistenceTypeDefinition<?> legacyTypeDefinition,
		PersistenceTypeHandler<M, T> currentTypeHandler
	);
	
	public <T> Class<T> lookupRuntimeType(
		PersistenceTypeDefinition<?> legacyTypeDefinition
	);
	
	
	
	public static <M> PersistenceLegacyTypeMapper<M> New(
		final PersistenceRefactoringMappingProvider   refactoringMappingProvider,
		final TypeMappingLookup<Float>                typeSimilarity            ,
		final PersistenceCustomTypeHandlerRegistry<M> customTypeHandlerRegistry ,
		final PersistenceDeletedTypeHandlerCreator<M> deletedTypeHandlerCreator ,
		final PersistenceMemberMatchingProvider       memberMatchingProvider    ,
		final PersistenceLegacyTypeMappingResultor<M> resultor                  ,
		final PersistenceLegacyTypeHandlerCreator<M>  legacyTypeHandlerCreator  ,
		final char                                    identifierSeparator
	)
	{
		return new PersistenceLegacyTypeMapper.Implementation<>(
			notNull(refactoringMappingProvider),
			notNull(typeSimilarity)            ,
			notNull(customTypeHandlerRegistry) ,
			notNull(deletedTypeHandlerCreator) ,
			notNull(memberMatchingProvider)    ,
			notNull(resultor)                  ,
			notNull(legacyTypeHandlerCreator)  ,
	                identifierSeparator
		);
	}

	public class Implementation<M> implements PersistenceLegacyTypeMapper<M>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final PersistenceRefactoringMappingProvider   refactoringMappingProvider;
		private final TypeMappingLookup<Float>                typeSimilarity            ;
		private final PersistenceCustomTypeHandlerRegistry<M> customTypeHandlerRegistry ;
		private final PersistenceDeletedTypeHandlerCreator<M> deletedTypeHandlerCreator ;
		private final PersistenceMemberMatchingProvider       memberMatchingProvider    ;
		private final PersistenceLegacyTypeMappingResultor<M> resultor                  ;
		private final PersistenceLegacyTypeHandlerCreator<M>  legacyTypeHandlerCreator  ;
		private final char                                    identifierSeparator       ;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected Implementation(
			final PersistenceRefactoringMappingProvider   refactoringMappingProvider,
			final TypeMappingLookup<Float>                typeSimilarity            ,
			final PersistenceCustomTypeHandlerRegistry<M> customTypeHandlerRegistry ,
			final PersistenceDeletedTypeHandlerCreator<M> deletedTypeHandlerCreator ,
			final PersistenceMemberMatchingProvider       memberMatchingProvider    ,
			final PersistenceLegacyTypeMappingResultor<M> resultor                  ,
			final PersistenceLegacyTypeHandlerCreator<M>  legacyTypeHandlerCreator  ,
			final char                                    identifierSeparator
		)
		{
			super();
			this.refactoringMappingProvider = refactoringMappingProvider;
			this.typeSimilarity             = typeSimilarity            ;
			this.customTypeHandlerRegistry  = customTypeHandlerRegistry ;
			this.deletedTypeHandlerCreator  = deletedTypeHandlerCreator ;
			this.memberMatchingProvider     = memberMatchingProvider    ;
			this.resultor                   = resultor                  ;
			this.legacyTypeHandlerCreator   = legacyTypeHandlerCreator  ;
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
			final PersistenceTypeDefinition<?> legacyTypeDefinition,
			final PersistenceTypeHandler<M, T> currentTypeHandler
		)
		{
			// explicit mappings take precedence
			final HashTable<PersistenceTypeDescriptionMember, PersistenceTypeDescriptionMember> explicitMappings =
				this.createExplicitMappings(legacyTypeDefinition, currentTypeHandler)
			;

			// heuristical matching is a applied to the remaining unmapped members
			final MultiMatch<PersistenceTypeDescriptionMember> match = match(
				legacyTypeDefinition,
				currentTypeHandler  ,
				explicitMappings
			);
			
			// bundle the mappings into a result, potentially with user callback, validation, modification, logging, etc.
			final PersistenceLegacyTypeMappingResult<M, T> validResult = this.resultor.createMappingResult(
				legacyTypeDefinition ,
				currentTypeHandler   ,
				explicitMappings,
				match
			);
			
			// creating a type handler from the finalized valid result
			return this.legacyTypeHandlerCreator.createLegacyTypeHandler(validResult);
		}
		
		private HashTable<PersistenceTypeDescriptionMember, PersistenceTypeDescriptionMember> createExplicitMappings(
			final PersistenceTypeDefinition<?> legacyTypeDefinition,
			final PersistenceTypeHandler<M, ?> currentTypeHandler
		)
		{
			// helper variables
			final PersistenceRefactoringMapping refacMapping = this.ensureRefactoringMapping();
			final char                          separator    = this.identifierSeparator;
			
			final HashTable<PersistenceTypeDescriptionMember, PersistenceTypeDescriptionMember> explicitMappings =
				HashTable.New()
			;
			
			final EqHashTable<String, PersistenceTypeDescriptionMember> refacTargetStrings =
				collectTargetStrings(explicitMappings, legacyTypeDefinition, refacMapping.entries(), separator)
			;
			
			// resolve and validate the collected mapping
			resolveSourceToTargetMembers(explicitMappings, refacTargetStrings, currentTypeHandler, separator);
							
			return explicitMappings;
		}
		
		
		private MultiMatch<PersistenceTypeDescriptionMember> match(
			final PersistenceTypeDefinition<?>                                                  legacyTypeDefinition,
			final PersistenceTypeHandler<M, ?>                                                  currentTypeHandler  ,
			final HashTable<PersistenceTypeDescriptionMember, PersistenceTypeDescriptionMember> resolvedMembers
		)
		{
			final BulkList<? extends PersistenceTypeDescriptionMember> sourceMembers = BulkList.New(
				legacyTypeDefinition.members()
			);
			final BulkList<? extends PersistenceTypeDescriptionMember> targetMembers = BulkList.New(
				currentTypeHandler.members()
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
			
			final MultiMatch<PersistenceTypeDescriptionMember> match = this.match(sourceMembers, targetMembers);
			
			return match;
		}
		
		private MultiMatch<PersistenceTypeDescriptionMember> match(
			final BulkList<? extends PersistenceTypeDescriptionMember> sourceMembers,
			final BulkList<? extends PersistenceTypeDescriptionMember> targetMembers
		)
		{
			final PersistenceRefactoringMapping               mapping   = this.ensureRefactoringMapping();
			final PersistenceMemberMatchingProvider           provider  = this.memberMatchingProvider;
			final TypeMappingLookup<Float>                    typeSimis = this.typeSimilarity;
			final Equalator<PersistenceTypeDescriptionMember> equalator = provider.provideMemberMatchingEqualator();
			final Similator<PersistenceTypeDescriptionMember> similator = provider.provideMemberMatchingSimilator(
				mapping,
				typeSimis
			);
			final MatchValidator<PersistenceTypeDescriptionMember> validator = provider.provideMemberMatchValidator();
			
			final MultiMatcher<PersistenceTypeDescriptionMember> matcher =
				MultiMatcher.<PersistenceTypeDescriptionMember>New()
				.setEqualator(equalator)
				.setSimilator(similator)
				.setValidator(validator)
			;
			
			final MultiMatch<PersistenceTypeDescriptionMember> match = matcher.match(sourceMembers, targetMembers);
			
			return match;
		}
				
		private <T> PersistenceLegacyTypeHandler<M, T> lookupCustomHandler(
			final PersistenceTypeDefinition<?> legacyTypeDefinition
		)
		{
			// cast safety ensured by checking the typename, which "is" the T.
			@SuppressWarnings("unchecked")
			final PersistenceLegacyTypeHandler<M, T> matchingLegacyTypeHandler = (PersistenceLegacyTypeHandler<M, T>)
				this.customTypeHandlerRegistry.legacyTypeHandlers()
				.search(h ->
					PersistenceTypeDescription.equalDescription(h, legacyTypeDefinition)
				)
			;
			
			return matchingLegacyTypeHandler;
		}
						
		@Override
		public <T> PersistenceLegacyTypeHandler<M, T> ensureLegacyTypeHandler(
			final PersistenceTypeDefinition<?> legacyTypeDefinition,
			final PersistenceTypeHandler<M, T> currentTypeHandler
		)
		{
			// check for a custom handler with matching structure
			final PersistenceLegacyTypeHandler<M, T> customHandler = this.lookupCustomHandler(legacyTypeDefinition);
			if(customHandler != null)
			{
				return customHandler;
			}
			
			if(currentTypeHandler == null)
			{
				// null indicates that the type has explicitely been mapped to nothing, i.e. shall be seen as deleted.
				return this.deletedTypeHandlerCreator.createDeletedTypeHandler(legacyTypeDefinition);
			}
			
			// at this point a legacy handler must be creatable or something went wrong.
			return this.createLegacyTypeHandler(legacyTypeDefinition, currentTypeHandler);
		}
		
		private static IdentifierBuilder[] createSourceIdentifierBuilders(final char separator)
		{
			/* (02.10.2018 TM)TODO: Legacy Type Mapping: Flexible lookup syntax
			 * See other occurance for the description.
			 */
			
			return array(
				(t, m) ->
					toTypeIdIdentifier(t, m, separator),
				(t, m) ->
					toGlobalNameIdentifier(t, m, separator),
				(t, m) ->
					toTypeInternalIdentifier(m)
			);
		}
		
		private static IdentifierBuilder[] createTargetIdentifierBuilders(final char separator)
		{
			/*
			 * identifier building logic in order of priority:
			 * - typeId plus global type name identifier (unique)
			 * - global type name identifier (means most specific)
			 * - internal identifier
			 * - unqualified identifier IF unambiguous (unique) or else null.
			 */
			return array(
				(t, m) ->
					toTypeIdIdentifier(t, m, separator),
				(t, m) ->
					toGlobalNameIdentifier(t, m, separator),
				(t, m) ->
					toTypeInternalIdentifier(m),
				(t, m) ->
					toUniqueUnqualifiedIdentifier(t, m, separator)
			);
		}
					
		private static EqHashTable<String, PersistenceTypeDescriptionMember> collectTargetStrings(
			final HashTable<PersistenceTypeDescriptionMember, PersistenceTypeDescriptionMember> sourceToTargetMapping,
			final PersistenceTypeDefinition<?>                                                  sourceTypeDefinition,
			final XGettingTable<String, String>                                                 refacMapping        ,
			final char                                                                          separator
		)
		{
			final EqHashTable<String, PersistenceTypeDescriptionMember> refacTargetStrings = EqHashTable.New();
			
			final IdentifierBuilder[] identifierBuilders = createSourceIdentifierBuilders(separator);
			
			// for every source (legacy type) member ...
			for(final PersistenceTypeDescriptionMember sourceMember : sourceTypeDefinition.members())
			{
				// ... identifier patterns are checked in priority defined by the builder order ...
				for(final IdentifierBuilder identifierBuilder : identifierBuilders)
				{
					final String identifier = identifierBuilder.buildIdentifier(sourceTypeDefinition, sourceMember);
					if(check(sourceMember, identifier, refacMapping, refacTargetStrings, sourceToTargetMapping))
					{
						// ... and on a match, the remaining builders are skipped for the matched source member.
						break;
					}
				}
			}
			
			// every source member has been handled exactely once, so return the result.
			return refacTargetStrings;
		}
		
		private static boolean check(
			final PersistenceTypeDescriptionMember                                              member                  ,
			final String                                                                        lookupString            ,
			final XGettingTable<String, String>                                                 refactoringEntries      ,
			final EqHashTable<String, PersistenceTypeDescriptionMember>                         refactoringTargetStrings,
			final HashTable<PersistenceTypeDescriptionMember, PersistenceTypeDescriptionMember> sourceToTargetMapping
		)
		{
			// must check keys themselves, as a null value means deletion
			if(refactoringEntries.keys().contains(lookupString))
			{
				// might be null to indicate deletion
				final String targetString = refactoringEntries.get(lookupString);
				if(targetString == null)
				{
					// to be deleted members are registered right away. Important for uniqueness checks later on.
					sourceToTargetMapping.add(member, null);
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
		
		private static void resolveSourceToTargetMembers(
			final HashTable<PersistenceTypeDescriptionMember, PersistenceTypeDescriptionMember> sourceToTargetMapping,
			final XGettingTable<String, PersistenceTypeDescriptionMember>                       refacTargetStrings  ,
			final PersistenceTypeDefinition<?>                                                  targetTypeDefinition,
			final char                                                                          separator
		)
		{
			final IdentifierBuilder[] identifierBuilders = createTargetIdentifierBuilders(separator);
			
			final EqHashTable<String, PersistenceTypeDescriptionMember> unresolvedTargetStrings = EqHashTable.New();

			// for every target (current type) member ...
			for(final PersistenceTypeDescriptionMember targetMember : targetTypeDefinition.members())
			{
				// ... identifier patterns are checked in priority defined by the builder order ...
				for(final IdentifierBuilder identifierBuilder : identifierBuilders)
				{
					final String identifier = identifierBuilder.buildIdentifier(targetTypeDefinition, targetMember);
					if(check(targetTypeDefinition, targetMember, refacTargetStrings, sourceToTargetMapping, identifier))
					{
						unresolvedTargetStrings.keys().remove(identifier);
						// ... and on a match, the remaining builders are skipped for the resolved target member.
						break;
					}
				}
			}
			
			if(!unresolvedTargetStrings.isEmpty())
			{
				final VarString vs = VarString.New();
				for(final KeyValue<String, PersistenceTypeDescriptionMember> unresolved : unresolvedTargetStrings)
				{
					vs.add(unresolved.value().uniqueName()).add(" -> ").add(unresolved.key()).lf();
				}
				
				// (04.10.2018 TM)EXCP: proper exception
				throw new RuntimeException("Unresolved mapping targets: \n" + vs);
			}
		}

		private static boolean check(
			final PersistenceTypeDefinition<?>                                                  targetTypeDefinition ,
			final PersistenceTypeDescriptionMember                                              targetTypeMember     ,
			final XGettingTable<String, PersistenceTypeDescriptionMember>                       sourceLookupTable    ,
			final HashTable<PersistenceTypeDescriptionMember, PersistenceTypeDescriptionMember> sourceToTargetMapping,
			final String                                                                        identifier
		)
		{
			final PersistenceTypeDescriptionMember sourceMember = sourceLookupTable.get(identifier);
			
			if(sourceMember == null)
			{
				return false;
			}
			
			if(sourceToTargetMapping.add(sourceMember, targetTypeMember))
			{
				return true;
			}
			
			// (10.09.2018 TM)EXCP: proper exception
			throw new PersistenceExceptionTypeConsistency(
				"Duplicate member mapping for source member " + sourceMember.uniqueName()
				+ "to target identifier \"" + identifier + "\""
			);
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
		
		static String toTypeIdIdentifier(
			final PersistenceTypeDefinition<?>     typeDefinition,
			final PersistenceTypeDescriptionMember member        ,
			final char                             separator
		)
		{
			/* (04.10.2018 TM)TODO: Legacy Type Mapping: consolidate "toTypeIdIdentifier".
			 * - hardcoded ":".
			 * - consolidate with type resolving lookup logic.
			 * Maybe implement as default custom builder
			 * See "Flexible lookup syntax" for details
			 */
			return typeDefinition + ":" + typeDefinition.typeName() + separator + toTypeInternalIdentifier(member);
		}
		
		static String toGlobalNameIdentifier(
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
		public <T> Class<T> lookupRuntimeType(final PersistenceTypeDefinition<?> legacyTypeDefinition)
		{
			/* (02.10.2018 TM)TODO: Legacy Type Mapping: Flexible lookup syntax
			 * There should be a PersistenceRefactoringEntrySyntaxDeriver with 2 methds that can get passed a type
			 * and a type plus a member and that derives entry strings for the passed values.
			 * The system should use a default (type: "TID:Name" and just "Name") that are tried after the
			 * (potentially empty) custom derivers.
			 * The deriving applies to both source and target side. This means a very specific mapping
			 * can be defined: from one specific type to another and if that other type is no longer the
			 * current type, the mapping is no longer used.
			 * 
			 */
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
