package one.microstream.persistence.types;

import static one.microstream.X.notNull;

import one.microstream.collections.BulkList;
import one.microstream.collections.HashEnum;
import one.microstream.collections.HashTable;
import one.microstream.equality.Equalator;
import one.microstream.persistence.exceptions.PersistenceExceptionTypeConsistency;
import one.microstream.typing.KeyValue;
import one.microstream.typing.TypeMappingLookup;
import one.microstream.util.similarity.MatchValidator;
import one.microstream.util.similarity.MultiMatch;
import one.microstream.util.similarity.MultiMatchAssembler;
import one.microstream.util.similarity.MultiMatcher;
import one.microstream.util.similarity.Similarity;
import one.microstream.util.similarity.Similator;

public interface PersistenceLegacyTypeMapper<M>
{
	public <T> PersistenceLegacyTypeHandler<M, T> ensureLegacyTypeHandler(
		PersistenceTypeDefinition    legacyTypeDefinition,
		PersistenceTypeHandler<M, T> currentTypeHandler
	);
	
	
	
	public interface Defaults
	{
		public static double defaultExplicitMappingSimilarity()
		{
			// to indicate "super similarity", something beyind a similiary match: an explicit mapping.
			return 2.0;
		}
		
		public static String defaultExplicitMappingString()
		{
			return "[mapped]";
		}
	}
	
	public static String similarityToString(final Similarity<PersistenceTypeDefinitionMember> match)
	{
		return match.similarity() == Defaults.defaultExplicitMappingSimilarity()
			? Defaults.defaultExplicitMappingString()
			: MultiMatchAssembler.Defaults.defaultSimilarityFormatter().format(match.similarity())
		;
	}
	
	public static Similarity<PersistenceTypeDefinitionMember> ExplicitMatch(
		final PersistenceTypeDefinitionMember sourceMember,
		final PersistenceTypeDefinitionMember targetMember
	)
	{
		return Similarity.New(
			notNull(sourceMember),
			Defaults.defaultExplicitMappingSimilarity(),
			notNull(targetMember)
		);
	}
	
	
	public static <M> PersistenceLegacyTypeMapper<M> New(
		final PersistenceTypeDescriptionResolverProvider  typeDescriptionResolverProvider,
		final TypeMappingLookup<Float>                    typeSimilarity             ,
		final PersistenceCustomTypeHandlerRegistry<M>     customTypeHandlerRegistry  ,
		final PersistenceMemberMatchingProvider           memberMatchingProvider     ,
		final PersistenceLegacyTypeMappingResultor<M>     resultor                   ,
		final PersistenceLegacyTypeHandlerCreator<M>      legacyTypeHandlerCreator
	)
	{
		return new PersistenceLegacyTypeMapper.Default<>(
			notNull(typeDescriptionResolverProvider),
			notNull(typeSimilarity)                 ,
			notNull(customTypeHandlerRegistry)      ,
			notNull(memberMatchingProvider)         ,
			notNull(resultor)                       ,
			notNull(legacyTypeHandlerCreator)
		);
	}

	public class Default<M> implements PersistenceLegacyTypeMapper<M>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final PersistenceTypeDescriptionResolverProvider typeDescriptionResolverProvider;
		private final TypeMappingLookup<Float>                   typeSimilarity                 ;
		private final PersistenceCustomTypeHandlerRegistry<M>    customTypeHandlerRegistry      ;
		private final PersistenceMemberMatchingProvider          memberMatchingProvider         ;
		private final PersistenceLegacyTypeMappingResultor<M>    resultor                       ;
		private final PersistenceLegacyTypeHandlerCreator<M>     legacyTypeHandlerCreator       ;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected Default(
			final PersistenceTypeDescriptionResolverProvider typeDescriptionResolverProvider,
			final TypeMappingLookup<Float>                   typeSimilarity                 ,
			final PersistenceCustomTypeHandlerRegistry<M>    customTypeHandlerRegistry      ,
			final PersistenceMemberMatchingProvider          memberMatchingProvider         ,
			final PersistenceLegacyTypeMappingResultor<M>    resultor                       ,
			final PersistenceLegacyTypeHandlerCreator<M>     legacyTypeHandlerCreator
		)
		{
			super();
			this.typeDescriptionResolverProvider = typeDescriptionResolverProvider;
			this.typeSimilarity                  = typeSimilarity                 ;
			this.customTypeHandlerRegistry       = customTypeHandlerRegistry      ;
			this.memberMatchingProvider          = memberMatchingProvider         ;
			this.resultor                        = resultor                       ;
			this.legacyTypeHandlerCreator        = legacyTypeHandlerCreator       ;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
				
		private <T> PersistenceLegacyTypeHandler<M, T> createLegacyTypeHandler(
			final PersistenceTypeDefinition    legacyTypeDefinition,
			final PersistenceTypeHandler<M, T> currentTypeHandler
		)
		{
			// explicit mappings take precedence
			final HashTable<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> explicitMappings  ;
			final HashEnum<PersistenceTypeDefinitionMember>                                   explicitNewMembers;
			
			this.createExplicitMappings(
				explicitMappings   = HashTable.New(),
				explicitNewMembers = HashEnum.New() ,
				legacyTypeDefinition,
				currentTypeHandler
			);

			// heuristical matching is a applied to the remaining unmapped members
			final MultiMatch<PersistenceTypeDefinitionMember> match = this.match(
				legacyTypeDefinition,
				currentTypeHandler  ,
				explicitMappings    ,
				explicitNewMembers
			);
			
			// bundle the mappings into a result, potentially with user callback, validation, modification, logging, etc.
			final PersistenceLegacyTypeMappingResult<M, T> validResult = this.resultor.createMappingResult(
				legacyTypeDefinition,
				currentTypeHandler  ,
				explicitMappings    ,
				explicitNewMembers  ,
				match
			);
			
			// creating a type handler from the finalized valid result
			return this.legacyTypeHandlerCreator.createLegacyTypeHandler(validResult);
		}
		
		private void createExplicitMappings(
			final HashTable<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> explicitMappings    ,
			final HashEnum<PersistenceTypeDefinitionMember>                                   explicitNewMembers  ,
			final PersistenceTypeDefinition                                                   legacyTypeDefinition,
			final PersistenceTypeHandler<M, ?>                                                currentTypeHandler
		)
		{
			final PersistenceTypeDescriptionResolver resolver = this.typeDescriptionResolverProvider.provideTypeDescriptionResolver();
			
			for(final PersistenceTypeDefinitionMember currentMember : currentTypeHandler.allMembers())
			{
				if(resolver.isNewCurrentTypeMember(currentTypeHandler, currentMember))
				{
					explicitNewMembers.add(currentMember);
				}
			}
			
			for(final PersistenceTypeDefinitionMember sourceMember : legacyTypeDefinition.allMembers())
			{
				// value might be null to indicate deletion. Member might not be resolvable (= mapped) at all.
				final KeyValue<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> resolved =
					resolver.resolveMember(legacyTypeDefinition, sourceMember, currentTypeHandler)
				;
				
				if(resolved == null)
				{
					continue;
				}
				if(explicitNewMembers.contains(resolved.value()))
				{
					// (11.10.2018 TM)EXCP: proper exception
					throw new RuntimeException(
						"Duplicate target entry " + resolved.value().identifier()
						+ " for type " + currentTypeHandler.toTypeIdentifier() + "."
					);
				}
				if(!explicitMappings.add(resolved))
				{
					// (10.09.2018 TM)EXCP: proper exception
					throw new PersistenceExceptionTypeConsistency(
						"Duplicate member mapping for legacy/source member \"" + sourceMember.identifier() + "\""
						+ " in legacy type " + legacyTypeDefinition.toTypeIdentifier()
					);
				}
			}
		}
		
		private static boolean hasNoElements(final BulkList<?> list)
		{
			return list.applies(e -> e == null);
		}
				
		private MultiMatch<PersistenceTypeDefinitionMember> match(
			final PersistenceTypeDefinition                                                   legacyTypeDefinition,
			final PersistenceTypeHandler<M, ?>                                                currentTypeHandler  ,
			final HashTable<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> explicitMappings    ,
			final HashEnum<PersistenceTypeDefinitionMember>                                   explicitNewMembers
		)
		{
			final BulkList<? extends PersistenceTypeDefinitionMember> sourceMembers = BulkList.New(
				legacyTypeDefinition.allMembers()
			);
			final BulkList<? extends PersistenceTypeDefinitionMember> targetMembers = BulkList.New(
				currentTypeHandler.allMembers()
			);
			
			// null out all explicitly mapped members before matching
			sourceMembers.replace(m ->
				explicitMappings.keys().contains(m),
				null
			);
			targetMembers.replace(m ->
				explicitNewMembers.contains(m) || explicitMappings.values().contains(m),
				null
			);
			
			// if no more elements are left to be matched, return null to signal no matching at all.
			if(hasNoElements(sourceMembers) || hasNoElements(targetMembers))
			{
				return null;
			}
			
			final MultiMatch<PersistenceTypeDefinitionMember> match = this.match(sourceMembers, targetMembers);
			
			return match;
		}
		
		private MultiMatch<PersistenceTypeDefinitionMember> match(
			final BulkList<? extends PersistenceTypeDefinitionMember> sourceMembers,
			final BulkList<? extends PersistenceTypeDefinitionMember> targetMembers
		)
		{
			final PersistenceMemberMatchingProvider          provider  = this.memberMatchingProvider;
			final TypeMappingLookup<Float>                   typeSimis = this.typeSimilarity;
			final Equalator<PersistenceTypeDefinitionMember> equalator = provider.provideMemberMatchingEqualator();
			final Similator<PersistenceTypeDefinitionMember> similator = provider.provideMemberMatchingSimilator(
				typeSimis
			);
			final MatchValidator<PersistenceTypeDefinitionMember> validator = provider.provideMemberMatchValidator();
			
			final MultiMatcher<PersistenceTypeDefinitionMember> matcher =
				MultiMatcher.<PersistenceTypeDefinitionMember>New()
				.setEqualator(equalator)
				.setSimilator(similator)
				.setValidator(validator)
			;
			
			final MultiMatch<PersistenceTypeDefinitionMember> match = matcher.match(sourceMembers, targetMembers);
			
			return match;
		}
		
		private <T> PersistenceLegacyTypeHandler<M, T> lookupCustomHandler(
			final PersistenceTypeDefinition legacyTypeDefinition
		)
		{
			PersistenceLegacyTypeHandler<M, T> matchingHandler = this.lookupCustomHandlerByTypeId(legacyTypeDefinition);
			if(matchingHandler == null)
			{
				matchingHandler = this.lookupCustomHandlerByStructure(legacyTypeDefinition);
			}
			
			return matchingHandler;
		}
		
		private <T> PersistenceLegacyTypeHandler<M, T> lookupCustomHandlerByTypeId(
			final PersistenceTypeDefinition legacyTypeDefinition
		)
		{
			final Class<?> type   = legacyTypeDefinition.type()  ;
			final long     typeId = legacyTypeDefinition.typeId();
			
			// cast safety ensured by checking the typename, which "is" the T.
			@SuppressWarnings("unchecked")
			final PersistenceLegacyTypeHandler<M, T> legacyTypeHandlerbyId = (PersistenceLegacyTypeHandler<M, T>)
				this.customTypeHandlerRegistry.legacyTypeHandlers()
				.search(h ->
					h.typeId() == typeId
				)
			;
			
			if(legacyTypeHandlerbyId == null)
			{
				return null;
			}
			
			// validate if the found handler with matching explicit typeId also has matching type and structure
			if(type != null && type != legacyTypeDefinition.type()
				|| !PersistenceTypeDescription.equalStructure(legacyTypeHandlerbyId, legacyTypeDefinition)
			)
			{
				// (05.07.2019 TM)EXCP: proper exception
				throw new PersistenceExceptionTypeConsistency(
					"Type handler structure mismatch for " + legacyTypeDefinition.toTypeIdentifier()
				);
			}
			
			return legacyTypeHandlerbyId;
		}
		
		private <T> PersistenceLegacyTypeHandler<M, T> lookupCustomHandlerByStructure(
			final PersistenceTypeDefinition legacyTypeDefinition
		)
		{
			// if runtime type is non-null, the found type handler must have the same type, of course.
			final Class<?> type = legacyTypeDefinition.type();
			
			// cast safety ensured by checking the typename, which "is" the T.
			@SuppressWarnings("unchecked")
			final PersistenceLegacyTypeHandler<M, T> matchingLegacyTypeHandler = (PersistenceLegacyTypeHandler<M, T>)
				this.customTypeHandlerRegistry.legacyTypeHandlers()
				.search(h ->
					(type == null || h.type() == type)
					&& PersistenceTypeDescription.equalStructure(h, legacyTypeDefinition)
				)
			;
			
			// intentionally no validation of 0-TypeId here, since the following initialization already does validation.
			return matchingLegacyTypeHandler;
		}
		
		@Override
		public <T> PersistenceLegacyTypeHandler<M, T> ensureLegacyTypeHandler(
			final PersistenceTypeDefinition    legacyTypeDefinition,
			final PersistenceTypeHandler<M, T> currentTypeHandler
		)
		{
			// check for a custom handler with matching structure
			final PersistenceLegacyTypeHandler<M, T> customHandler = this.lookupCustomHandler(legacyTypeDefinition);
			if(customHandler != null)
			{
				/*
				 * must initialize TypeHandler with given TypeId
				 * (potentially creating an initialized instance from an uninitialized prototype handler instance)
				 * note that #lookupCustomHandler already does member structure validation
				 */
				return customHandler.initialize(legacyTypeDefinition.typeId());
			}
			
			// at this point a legacy handler must be creatable or something went wrong.
			return this.createLegacyTypeHandler(legacyTypeDefinition, currentTypeHandler);
		}
		
	}
	
}
