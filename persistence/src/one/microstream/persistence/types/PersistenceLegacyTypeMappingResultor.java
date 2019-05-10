package one.microstream.persistence.types;

import one.microstream.collections.HashEnum;
import one.microstream.collections.HashTable;
import one.microstream.collections.types.XEnum;
import one.microstream.collections.types.XGettingMap;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.collections.types.XGettingSet;
import one.microstream.collections.types.XTable;
import one.microstream.typing.KeyValue;
import one.microstream.util.similarity.MultiMatch;
import one.microstream.util.similarity.Similarity;


//@FunctionalInterface - well, lol.
public interface PersistenceLegacyTypeMappingResultor<M>
{
	/**
	 * Override this method to implement various functions like ...
	 * <ul>
	 * <li>user-callback-based validating/modifying</li>
	 * <li>rule-based automatically validating/modifying</li>
	 * <li>displaying</li>
	 * <li>logging</li>
	 * <li>persisting</li>
	 * </ul>
	 * ... the created mapping.
	 * 
	 * @param legacyTypeDefinition
	 * @param currentTypeHandler
	 * @param explicitMappings
	 * @param matchedMembers
	 * 
	 * @return
	 */
	public default <T> PersistenceLegacyTypeMappingResult<M, T> createMappingResult(
		final PersistenceTypeDefinition                                                     legacyTypeDefinition,
		final PersistenceTypeHandler<M, T>                                                  currentTypeHandler  ,
		final XGettingMap<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> explicitMappings    ,
		final XGettingSet<PersistenceTypeDefinitionMember>                                  explicitNewMembers  ,
		final MultiMatch<PersistenceTypeDefinitionMember>                                   matchedMembers
	)
	{
		return createLegacyTypeMappingResult(
			legacyTypeDefinition,
			currentTypeHandler  ,
			explicitMappings    ,
			explicitNewMembers  ,
			matchedMembers
		);
	}
	
	
	
	public static <M, T> PersistenceLegacyTypeMappingResult<M, T> createLegacyTypeMappingResult(
		final PersistenceTypeDefinition                                                     legacyTypeDefinition,
		final PersistenceTypeHandler<M, T>                                                  currentTypeHandler  ,
		final XGettingMap<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> explicitMappings    ,
		final XGettingSet<PersistenceTypeDefinitionMember>                                  explicitNewMembers  ,
		final MultiMatch<PersistenceTypeDefinitionMember>                                   matchedMembers
	)
	{
		final HashEnum<PersistenceTypeDefinitionMember> discardedLegacyMembers;
		final HashEnum<PersistenceTypeDefinitionMember> newCurrentMembers     ;
		final HashTable<PersistenceTypeDefinitionMember, Similarity<PersistenceTypeDefinitionMember>>
			legacyToCurrentMembers,
			currentToLegacyMembers
		;
		
		combineMappings(
			legacyToCurrentMembers = HashTable.New(),
			currentToLegacyMembers = HashTable.New(),
			discardedLegacyMembers = HashEnum.New() ,
			newCurrentMembers      = HashEnum.New() ,
			legacyTypeDefinition                    ,
			currentTypeHandler                      ,
			explicitMappings                        ,
			explicitNewMembers                      ,
			matchedMembers
		);
		
		return PersistenceLegacyTypeMappingResult.New(
			legacyTypeDefinition  ,
			currentTypeHandler    ,
			legacyToCurrentMembers,
			currentToLegacyMembers,
			discardedLegacyMembers,
			newCurrentMembers
		);
	}
		
	public static void combineMappings(
		final XTable<PersistenceTypeDefinitionMember, Similarity<PersistenceTypeDefinitionMember>> legacyToCurrentMembers,
		final XTable<PersistenceTypeDefinitionMember, Similarity<PersistenceTypeDefinitionMember>> currentToLegacyMembers,
		final XEnum<PersistenceTypeDefinitionMember>                                               discardedLegacyMembers,
		final XEnum<PersistenceTypeDefinitionMember>                                               newCurrentMembers     ,
		final PersistenceTypeDefinition                                                            legacyTypeDefinition  ,
		final PersistenceTypeHandler<?, ?>                                                         currentTypeHandler    ,
		final XGettingMap<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember>        explicitMappings      ,
		final XGettingSet<PersistenceTypeDefinitionMember>                                         explicitNewMembers    ,
		final MultiMatch<PersistenceTypeDefinitionMember>                                          matchedMembers
	)
	{
		// no idea right now why the multi match result ~Matches are not tables, so build them here temporarily
		final HashTable<PersistenceTypeDefinitionMember, Similarity<PersistenceTypeDefinitionMember>> sourceLookup = HashTable.New();
		final HashTable<PersistenceTypeDefinitionMember, Similarity<PersistenceTypeDefinitionMember>> targetLookup = HashTable.New();
		// and another temporary reverse lookup table
		final HashTable<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> targetExplicits = HashTable.New();
		
		Static.fillLookupTables(
			sourceLookup      ,
			targetLookup      ,
			targetExplicits   ,
			explicitMappings  ,
			explicitNewMembers,
			matchedMembers
		);
		Static.buildLegacyToCurrentMembersMapping(
			legacyTypeDefinition  ,
			sourceLookup          ,
			explicitMappings      ,
			legacyToCurrentMembers,
			discardedLegacyMembers
		);
		Static.buildCurrentToLegacyMembersMapping(
			currentTypeHandler    ,
			targetLookup          ,
			targetExplicits       ,
			currentToLegacyMembers,
			newCurrentMembers
		);
	}
		
	public static <M> PersistenceLegacyTypeMappingResultor<M> New()
	{
		return new PersistenceLegacyTypeMappingResultor.Default<>();
	}
	
	public final class Default<M> implements PersistenceLegacyTypeMappingResultor<M>
	{
		// since default methods, the ability to instantiate stateless instances from interfaces is missing
	}
	
	public final class Static
	{
		// also, it is not comprehensible why non-public static methods of interface logic have to be "hidden" like this
		
		static void fillLookupTables(
			final HashTable<PersistenceTypeDefinitionMember, Similarity<PersistenceTypeDefinitionMember>> sourceToTargetLookup  ,
			final HashTable<PersistenceTypeDefinitionMember, Similarity<PersistenceTypeDefinitionMember>> targetToSourceLookup  ,
			final HashTable<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember>             targetExplicitMappings,
			final XGettingMap<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember>           explicitMappings      ,
			final XGettingSet<PersistenceTypeDefinitionMember>                                            explicitNewMembers    ,
			final MultiMatch<PersistenceTypeDefinitionMember>                                             matchedMembers
		)
		{
			if(matchedMembers != null)
			{
				final XGettingSequence<? extends Similarity<PersistenceTypeDefinitionMember>> matches =
					matchedMembers.result().sourceMatches()
				;
				for(final Similarity<PersistenceTypeDefinitionMember> match : matches)
				{
					sourceToTargetLookup.add(match.sourceElement(), match);
					targetToSourceLookup.add(match.targetElement(), match);
				}
			}
			
			for(final KeyValue<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> e : explicitMappings)
			{
				// (10.05.2019 TM)FIXME: MS-141: reversed explicit mapping
				targetExplicitMappings.add(e.value(), e.key());
			}
			for(final PersistenceTypeDefinitionMember e : explicitNewMembers)
			{
				targetExplicitMappings.add(e, null);
			}
		}
		
		static void buildLegacyToCurrentMembersMapping(
			final PersistenceTypeDefinition                                                               legacyTypeDefinition  ,
			final HashTable<PersistenceTypeDefinitionMember, Similarity<PersistenceTypeDefinitionMember>> sourceToTargetLookup  ,
			final XGettingMap<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember>           sourceExplicitMappings,
			final XTable<PersistenceTypeDefinitionMember, Similarity<PersistenceTypeDefinitionMember>>    legacyToCurrentMembers,
			final XEnum<PersistenceTypeDefinitionMember>                                                  discardedLegacyMembers
		)
		{
			for(final PersistenceTypeDefinitionMember sourceMember : legacyTypeDefinition.members())
			{
				// explicit mappings take precedence
				// (10.05.2019 TM)FIXME: MS-141: reversed explicit mapping (problem here!)
				final KeyValue<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> explicitEntry =
					sourceExplicitMappings.lookup(sourceMember)
				;
				if(explicitEntry != null)
				{
					if(explicitEntry.value() != null)
					{
						legacyToCurrentMembers.add(
							sourceMember,
							PersistenceLegacyTypeMapper.ExplicitMatch(sourceMember, explicitEntry.value())
						);
						continue;
					}
					// else fall through to discarded member registration
				}
				else
				{
					// matching matches are a secondary (fallback / safety net) mapping
					final Similarity<PersistenceTypeDefinitionMember> matchedTargetMember =
						sourceToTargetLookup.get(sourceMember)
					;
					if(matchedTargetMember != null)
					{
						legacyToCurrentMembers.add(sourceMember, matchedTargetMember);
						continue;
					}
					// else fall through to discarded member registration
				}

				// if no mapping was found, the source member gets discarded
				legacyToCurrentMembers.add(sourceMember, null);
				discardedLegacyMembers.add(sourceMember); // just a convenience collection
			}
		}
		
		static void buildCurrentToLegacyMembersMapping(
			final PersistenceTypeHandler<?, ?>                                                            currentTypeHandler    ,
			final HashTable<PersistenceTypeDefinitionMember, Similarity<PersistenceTypeDefinitionMember>> targetToSourceLookup  ,
			final XGettingMap<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember>           targetExplicitMappings,
			final XTable<PersistenceTypeDefinitionMember, Similarity<PersistenceTypeDefinitionMember>>    currentToLegacyMembers,
			final XEnum<PersistenceTypeDefinitionMember>                                                  newCurrentMembers
		)
		{
			for(final PersistenceTypeDefinitionMember trgMember : currentTypeHandler.members())
			{
				// explicit mappings take precedence
				final KeyValue<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> explicitEntry =
					targetExplicitMappings.lookup(trgMember)
				;
				if(explicitEntry != null)
				{
					if(explicitEntry.value() != null)
					{
						currentToLegacyMembers.add(
							trgMember,
							PersistenceLegacyTypeMapper.ExplicitMatch(explicitEntry.value(), trgMember)
						);
						continue;
					}
					// else fall through to new member registration
				}
				else
				{
					// matching matches are a secondary (fallback / safety net) mapping
					final Similarity<PersistenceTypeDefinitionMember> matchedSourceMember = targetToSourceLookup.get(trgMember);
					if(matchedSourceMember != null)
					{
						currentToLegacyMembers.add(trgMember, matchedSourceMember);
						continue;
					}
					// else fall through to new member registration
				}

				// if no mapping was found, the source member gets discarded
				currentToLegacyMembers.add(trgMember, null);
				
				// just a convenience collection
				newCurrentMembers.add(trgMember);
			}
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		private Static()
		{
			// static only
			throw new UnsupportedOperationException();
		}
		
	}
	
}
