package net.jadoth.persistence.types;

import static net.jadoth.X.array;
import static net.jadoth.X.notNull;

import net.jadoth.collections.BulkList;
import net.jadoth.collections.EqHashTable;
import net.jadoth.collections.HashEnum;
import net.jadoth.collections.HashTable;
import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.collections.types.XGettingTable;
import net.jadoth.equality.Equalator;
import net.jadoth.functional.Similator;
import net.jadoth.persistence.exceptions.PersistenceExceptionTypeConsistency;
import net.jadoth.persistence.exceptions.PersistenceExceptionTypeConsistencyDefinitionResolveTypeName;
import net.jadoth.reflect.XReflect;
import net.jadoth.typing.TypeMapping;
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
		final TypeMapping<Float>                      typeSimilarity            ,
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
		private final TypeMapping<Float>                      typeSimilarity            ;
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
			final TypeMapping<Float>                      typeSimilarity            ,
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
			// helper variables
			final PersistenceRefactoringMapping refacMapping = this.ensureRefactoringMapping();
			final char                          separator    = this.identifierSeparator;
			
			// mapping structures
			final EqHashTable<String, PersistenceTypeDescriptionMember>                         refacTrgStrings = EqHashTable.New();
			final HashEnum<PersistenceTypeDescriptionMember>                                    refacDeletes    = HashEnum.New();
			final HashTable<PersistenceTypeDescriptionMember, PersistenceTypeDescriptionMember> resolvedMembers = HashTable.New();
			
			// mapping logic
			collectTargetStrings(legacyTypeDefinition, refacTrgStrings, refacMapping.entries(), refacDeletes, separator);
			resolveToTargetMembers(refacTrgStrings, currentTypeHandler, resolvedMembers, separator);
			addDeletionMembers(refacDeletes, resolvedMembers);
			
			// heuristics matching
			final MultiMatch<PersistenceTypeDescriptionMember> match = match(
				legacyTypeDefinition,
				currentTypeHandler  ,
				resolvedMembers
			);
			
			// bundeling everything into a result
			final PersistenceLegacyTypeMappingResult<M, T> result = this.resultor.createMappingResult(
				legacyTypeDefinition,
				currentTypeHandler  ,
				resolvedMembers     ,
				refacDeletes        ,
				match
			);
			
			// creating a type handler from the finished result
			return this.legacyTypeHandlerCreator.createLegacyTypeHandler(result);
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
			final PersistenceRefactoringMapping                    mapping   = this.ensureRefactoringMapping();
			final PersistenceMemberMatchingProvider                provider  = this.memberMatchingProvider;
			final TypeMapping<Float>                               typeSimis = this.typeSimilarity;
			final Equalator<PersistenceTypeDescriptionMember>      equalator = provider.provideMemberMatchingEqualator();
			final Similator<PersistenceTypeDescriptionMember>      similator = provider.provideMemberMatchingSimilator(
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
			

		
		private static void resolveToTargetMembers(
			final XGettingTable<String, PersistenceTypeDescriptionMember>                       refacTargetStrings,
			final PersistenceTypeDefinition<?>                                                  targetTypeDef     ,
			final HashTable<PersistenceTypeDescriptionMember, PersistenceTypeDescriptionMember> resolvedMembers   ,
			final char                                                                          separator
		)
		{
			final IdentifierBuilder[] identifierBuilders = createTargetIdentifierBuilders(separator);
			
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
		
		private static void collectTargetStrings(
			final PersistenceTypeDefinition<?>                          typeDefinition    ,
			final EqHashTable<String, PersistenceTypeDescriptionMember> refacTargetStrings,
			final XGettingTable<String, String>                         refacMapping      ,
			final HashEnum<PersistenceTypeDescriptionMember>            refacDeletes      ,
			final char                                                  separator
		)
		{
			final IdentifierBuilder[] identifierBuilders = createSourceIdentifierBuilders(separator);
			
			for(final PersistenceTypeDescriptionMember member : typeDefinition.members())
			{
				for(final IdentifierBuilder identifierBuilder : identifierBuilders)
				{
					final String identifier = identifierBuilder.buildIdentifier(typeDefinition, member);
					if(check(member, identifier, refacMapping, refacTargetStrings, refacDeletes))
					{
						continue;
					}
				}
			}
		}
		
		private static boolean check(
			final PersistenceTypeDescriptionMember                      member                    ,
			final String                                                lookupString              ,
			final XGettingTable<String, String>                         refactoringEntries        ,
			final EqHashTable<String, PersistenceTypeDescriptionMember> refactoringTargetStrings  ,
			final HashEnum<PersistenceTypeDescriptionMember>            refactoringDeletionMembers
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
		public <T> Class<T> lookupRuntimeType(final PersistenceTypeDefinition<?> legacyTypeDefinition)
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
