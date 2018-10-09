package net.jadoth.persistence.types;

import static net.jadoth.X.notNull;

import net.jadoth.collections.BulkList;
import net.jadoth.collections.HashTable;
import net.jadoth.equality.Equalator;
import net.jadoth.functional.Similator;
import net.jadoth.persistence.exceptions.PersistenceExceptionTypeConsistency;
import net.jadoth.typing.KeyValue;
import net.jadoth.typing.TypeMappingLookup;
import net.jadoth.util.matching.MatchValidator;
import net.jadoth.util.matching.MultiMatch;
import net.jadoth.util.matching.MultiMatcher;

public interface PersistenceLegacyTypeMapper<M>
{
	public <T> PersistenceLegacyTypeHandler<M, T> ensureLegacyTypeHandler(
		PersistenceTypeDefinition    legacyTypeDefinition,
		PersistenceTypeHandler<M, T> currentTypeHandler
	);
	
	
	
	public static <M> PersistenceLegacyTypeMapper<M> New(
		final PersistenceRefactoringResolverProvider  refactoringResolverProvider,
		final TypeMappingLookup<Float>                typeSimilarity             ,
		final PersistenceCustomTypeHandlerRegistry<M> customTypeHandlerRegistry  ,
		final PersistenceMemberMatchingProvider       memberMatchingProvider     ,
		final PersistenceLegacyTypeMappingResultor<M> resultor                   ,
		final PersistenceLegacyTypeHandlerCreator<M>  legacyTypeHandlerCreator
	)
	{
		return new PersistenceLegacyTypeMapper.Implementation<>(
			notNull(refactoringResolverProvider),
			notNull(typeSimilarity)             ,
			notNull(customTypeHandlerRegistry)  ,
			notNull(memberMatchingProvider)     ,
			notNull(resultor)                   ,
			notNull(legacyTypeHandlerCreator)
		);
	}

	public class Implementation<M> implements PersistenceLegacyTypeMapper<M>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final PersistenceRefactoringResolverProvider  refactoringResolverProvider;
		private final TypeMappingLookup<Float>                typeSimilarity             ;
		private final PersistenceCustomTypeHandlerRegistry<M> customTypeHandlerRegistry  ;
		private final PersistenceMemberMatchingProvider       memberMatchingProvider     ;
		private final PersistenceLegacyTypeMappingResultor<M> resultor                   ;
		private final PersistenceLegacyTypeHandlerCreator<M>  legacyTypeHandlerCreator   ;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected Implementation(
			final PersistenceRefactoringResolverProvider  refactoringResolverProvider,
			final TypeMappingLookup<Float>                typeSimilarity             ,
			final PersistenceCustomTypeHandlerRegistry<M> customTypeHandlerRegistry  ,
			final PersistenceMemberMatchingProvider       memberMatchingProvider     ,
			final PersistenceLegacyTypeMappingResultor<M> resultor                   ,
			final PersistenceLegacyTypeHandlerCreator<M>  legacyTypeHandlerCreator
		)
		{
			super();
			this.refactoringResolverProvider = refactoringResolverProvider;
			this.typeSimilarity              = typeSimilarity             ;
			this.customTypeHandlerRegistry   = customTypeHandlerRegistry  ;
			this.memberMatchingProvider      = memberMatchingProvider     ;
			this.resultor                    = resultor                   ;
			this.legacyTypeHandlerCreator    = legacyTypeHandlerCreator   ;
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
			final HashTable<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> explicitMappings =
				this.createExplicitMappings(legacyTypeDefinition, currentTypeHandler)
			;

			// heuristical matching is a applied to the remaining unmapped members
			final MultiMatch<PersistenceTypeDefinitionMember> match = match(
				legacyTypeDefinition,
				currentTypeHandler  ,
				explicitMappings
			);
			
			// bundle the mappings into a result, potentially with user callback, validation, modification, logging, etc.
			final PersistenceLegacyTypeMappingResult<M, T> validResult = this.resultor.createMappingResult(
				legacyTypeDefinition,
				currentTypeHandler  ,
				explicitMappings    ,
				match
			);
			
			// creating a type handler from the finalized valid result
			return this.legacyTypeHandlerCreator.createLegacyTypeHandler(validResult);
		}
		
		private HashTable<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> createExplicitMappings(
			final PersistenceTypeDefinition legacyTypeDefinition,
			final PersistenceTypeHandler<M, ?> currentTypeHandler
		)
		{
			final PersistenceRefactoringResolver resolver = this.refactoringResolverProvider.provideResolver();

			final HashTable<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> explicitMappings =
				HashTable.New()
			;
			
			for(final PersistenceTypeDefinitionMember sourceMember : legacyTypeDefinition.members())
			{
				// value might be null to indicate deletion. Member might not be resolvable (= mapped) at all.
				final KeyValue<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> resolved =
					resolver.resolveMember(legacyTypeDefinition, sourceMember, currentTypeHandler)
				;
				
				if(resolved == null)
				{
					continue;
				}
				if(!explicitMappings.add(resolved))
				{
					// (10.09.2018 TM)EXCP: proper exception
					throw new PersistenceExceptionTypeConsistency(
						"Duplicate member mapping for legacy/source member \"" + sourceMember.uniqueName() + "\""
						+ " in legacy type " + legacyTypeDefinition.toTypeIdentifier()
					);
				}
			}

			return explicitMappings;
		}
				
		private MultiMatch<PersistenceTypeDefinitionMember> match(
			final PersistenceTypeDefinition                                                   legacyTypeDefinition,
			final PersistenceTypeHandler<M, ?>                                                currentTypeHandler  ,
			final HashTable<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> resolvedMembers
		)
		{
			final BulkList<? extends PersistenceTypeDefinitionMember> sourceMembers = BulkList.New(
				legacyTypeDefinition.members()
			);
			final BulkList<? extends PersistenceTypeDefinitionMember> targetMembers = BulkList.New(
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
			final PersistenceTypeDefinition    legacyTypeDefinition,
			final PersistenceTypeHandler<M, T> currentTypeHandler
		)
		{
			// check for a custom handler with matching structure
			final PersistenceLegacyTypeHandler<M, T> customHandler = this.lookupCustomHandler(legacyTypeDefinition);
			if(customHandler != null)
			{
				return customHandler;
			}
			
			// at this point a legacy handler must be creatable or something went wrong.
			return this.createLegacyTypeHandler(legacyTypeDefinition, currentTypeHandler);
		}
				
	}
	
}
