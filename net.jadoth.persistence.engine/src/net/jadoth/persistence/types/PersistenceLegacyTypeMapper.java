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
		PersistenceTypeDefinition<?> legacyTypeDefinition,
		PersistenceTypeHandler<M, T> currentTypeHandler
	);
	
	
	
	public static <M> PersistenceLegacyTypeMapper<M> New(
		final PersistenceRefactoringResolverProvider  refactoringResolverProvider,
		final TypeMappingLookup<Float>                typeSimilarity             ,
		final PersistenceCustomTypeHandlerRegistry<M> customTypeHandlerRegistry  ,
		final PersistenceDeletedTypeHandlerCreator<M> deletedTypeHandlerCreator  ,
		final PersistenceMemberMatchingProvider       memberMatchingProvider     ,
		final PersistenceLegacyTypeMappingResultor<M> resultor                   ,
		final PersistenceLegacyTypeHandlerCreator<M>  legacyTypeHandlerCreator
	)
	{
		return new PersistenceLegacyTypeMapper.Implementation<>(
			notNull(refactoringResolverProvider),
			notNull(typeSimilarity)             ,
			notNull(customTypeHandlerRegistry)  ,
			notNull(deletedTypeHandlerCreator)  ,
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
		private final PersistenceDeletedTypeHandlerCreator<M> deletedTypeHandlerCreator  ;
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
			final PersistenceDeletedTypeHandlerCreator<M> deletedTypeHandlerCreator  ,
			final PersistenceMemberMatchingProvider       memberMatchingProvider     ,
			final PersistenceLegacyTypeMappingResultor<M> resultor                   ,
			final PersistenceLegacyTypeHandlerCreator<M>  legacyTypeHandlerCreator
		)
		{
			super();
			this.refactoringResolverProvider = refactoringResolverProvider;
			this.typeSimilarity              = typeSimilarity             ;
			this.customTypeHandlerRegistry   = customTypeHandlerRegistry  ;
			this.deletedTypeHandlerCreator   = deletedTypeHandlerCreator  ;
			this.memberMatchingProvider      = memberMatchingProvider     ;
			this.resultor                    = resultor                   ;
			this.legacyTypeHandlerCreator    = legacyTypeHandlerCreator   ;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
				
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
				legacyTypeDefinition,
				currentTypeHandler  ,
				explicitMappings    ,
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
			final PersistenceRefactoringResolver resolver = this.refactoringResolverProvider.provideResolver();

			final HashTable<PersistenceTypeDescriptionMember, PersistenceTypeDescriptionMember> explicitMappings =
				HashTable.New()
			;
			
			for(final PersistenceTypeDescriptionMember sourceMember : legacyTypeDefinition.members())
			{
				// value might be null to indicate deletion. Member might not be resolvable (= mapped) at all.
				final KeyValue<PersistenceTypeDescriptionMember, PersistenceTypeDescriptionMember> resolved =
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
			final PersistenceRefactoringResolver              resolver  = this.refactoringResolverProvider.provideResolver();
			final PersistenceMemberMatchingProvider           provider  = this.memberMatchingProvider;
			final TypeMappingLookup<Float>                    typeSimis = this.typeSimilarity;
			final Equalator<PersistenceTypeDescriptionMember> equalator = provider.provideMemberMatchingEqualator();
			final Similator<PersistenceTypeDescriptionMember> similator = provider.provideMemberMatchingSimilator(
				resolver,
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
			final PersistenceTypeHandler<M, T> properTypeHandler
		)
		{
			// check for a custom handler with matching structure
			final PersistenceLegacyTypeHandler<M, T> customHandler = this.lookupCustomHandler(legacyTypeDefinition);
			if(customHandler != null)
			{
				return customHandler;
			}
			
			if(properTypeHandler == null)
			{
				// null indicates that the type has explicitely been mapped to nothing, i.e. shall be seen as deleted.
				return this.deletedTypeHandlerCreator.createDeletedTypeHandler(legacyTypeDefinition);
			}
			
			// at this point a legacy handler must be creatable or something went wrong.
			return this.createLegacyTypeHandler(legacyTypeDefinition, properTypeHandler);
		}
				
	}
	
}
