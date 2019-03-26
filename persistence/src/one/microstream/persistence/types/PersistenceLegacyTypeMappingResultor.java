package one.microstream.persistence.types;

import one.microstream.collections.HashEnum;
import one.microstream.collections.HashTable;
import one.microstream.collections.types.XEnum;
import one.microstream.collections.types.XGettingMap;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.collections.types.XGettingSet;
import one.microstream.collections.types.XTable;
import one.microstream.typing.KeyValue;
import one.microstream.util.matching.MultiMatch;
import one.microstream.util.matching.MultiMatchResult;


//@FunctionalInterface - well, lol.
public interface PersistenceLegacyTypeMappingResultor<M>
{
	/**
	 * Override this method to a suitable degree to implement various functions like...
	 * <ul>
	 * <li>user-callback-based validateing/modifying</li>
	 * <li>rule-based automatically validating</li>
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
		final HashTable<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> legacyToCurrentMembers;
		final HashTable<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> currentToLegacyMembers;
		final HashEnum<PersistenceTypeDefinitionMember>                                   discardedLegacyMembers;
		final HashEnum<PersistenceTypeDefinitionMember>                                   newCurrentMembers     ;
		
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
		final XTable<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember>      legacyToCurrentMembers,
		final XTable<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember>      currentToLegacyMembers,
		final XEnum<PersistenceTypeDefinitionMember>                                        discardedLegacyMembers,
		final XEnum<PersistenceTypeDefinitionMember>                                        newCurrentMembers     ,
		final PersistenceTypeDefinition                                                     legacyTypeDefinition  ,
		final PersistenceTypeHandler<?, ?>                                                  currentTypeHandler    ,
		final XGettingMap<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> explicitMappings      ,
		final XGettingSet<PersistenceTypeDefinitionMember>                                  explicitNewMembers    ,
		final MultiMatch<PersistenceTypeDefinitionMember>                                   matchedMembers
	)
	{
		// no idea right now why the multi match result ~Matches are not tables, so build them here temporarily
		final HashTable<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> sourceLookup = HashTable.New();
		final HashTable<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> targetLookup = HashTable.New();
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
		return new PersistenceLegacyTypeMappingResultor.Implementation<>();
	}
	
	public final class Implementation<M> implements PersistenceLegacyTypeMappingResultor<M>
	{
		// since default methods, the ability to instantiate stateless instances from interfaces is missing
	}
	
	public final class Static
	{
		// also, it is not comprehensible why non-public static methods of interface logic have to be "hidden" like this
		
		static void fillLookupTables(
			final HashTable<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember>   sourceToTargetLookup  ,
			final HashTable<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember>   targetToSourceLookup  ,
			final HashTable<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember>   targetExplicitMappings,
			final XGettingMap<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> explicitMappings      ,
			final XGettingSet<PersistenceTypeDefinitionMember>                                  explicitNewMembers    ,
			final MultiMatch<PersistenceTypeDefinitionMember>                                   matchedMembers
		)
		{
			if(matchedMembers != null)
			{
				final XGettingSequence<? extends MultiMatchResult.Item<PersistenceTypeDefinitionMember>> matches =
					matchedMembers.result().sourceMatches()
				;
				for(final MultiMatchResult.Item<PersistenceTypeDefinitionMember> match : matches)
				{
					sourceToTargetLookup.add(match.sourceElement(), match.targetElement());
					targetToSourceLookup.add(match.targetElement(), match.sourceElement());
				}
			}
			
			for(final KeyValue<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> e : explicitMappings)
			{
				targetExplicitMappings.add(e.value(), e.key());
			}
			for(final PersistenceTypeDefinitionMember e : explicitNewMembers)
			{
				targetExplicitMappings.add(e, null);
			}
		}
		
		static void buildLegacyToCurrentMembersMapping(
			final PersistenceTypeDefinition                                                     legacyTypeDefinition  ,
			final HashTable<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember>   sourceToTargetLookup  ,
			final XGettingMap<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> sourceExplicitMappings,
			final XTable<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember>      legacyToCurrentMembers,
			final XEnum<PersistenceTypeDefinitionMember>                                        discardedLegacyMembers
		)
		{
			for(final PersistenceTypeDefinitionMember srcMember : legacyTypeDefinition.members())
			{
				// explicit mappings take precedence
				final KeyValue<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> explicitEntry =
					sourceExplicitMappings.lookup(srcMember)
				;
				if(explicitEntry != null)
				{
					if(explicitEntry.value() != null)
					{
						legacyToCurrentMembers.add(srcMember, explicitEntry.value());
						continue;
					}
					// else fall through to discarded member registration
				}
				else
				{
					// matching matches are a secondary (fallback / safety net) mapping
					final PersistenceTypeDefinitionMember matchedTargetMember = sourceToTargetLookup.get(srcMember);
					if(matchedTargetMember != null)
					{
						legacyToCurrentMembers.add(srcMember, matchedTargetMember);
						continue;
					}
					// else fall through to discarded member registration
				}

				// if no mapping was found, the source member gets discarded
				legacyToCurrentMembers.add(srcMember, null);
				discardedLegacyMembers.add(srcMember); // just a convenience collection
			}
		}
		
		static void buildCurrentToLegacyMembersMapping(
			final PersistenceTypeHandler<?, ?>                                                  currentTypeHandler    ,
			final HashTable<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember>   targetToSourceLookup  ,
			final XGettingMap<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> targetExplicitMappings,
			final XTable<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember>      currentToLegacyMembers,
			final XEnum<PersistenceTypeDefinitionMember>                                        newCurrentMembers
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
						currentToLegacyMembers.add(trgMember, explicitEntry.value());
						continue;
					}
					// else fall through to new member registration
				}
				else
				{
					// matching matches are a secondary (fallback / safety net) mapping
					final PersistenceTypeDefinitionMember matchedSourceMember = targetToSourceLookup.get(trgMember);
					if(matchedSourceMember != null)
					{
						currentToLegacyMembers.add(trgMember, matchedSourceMember);
						continue;
					}
					// else fall through to new member registration
				}

				// if no mapping was found, the source member gets discarded
				currentToLegacyMembers.add(trgMember, null);
				newCurrentMembers.add(trgMember); // just a convenience collection
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
