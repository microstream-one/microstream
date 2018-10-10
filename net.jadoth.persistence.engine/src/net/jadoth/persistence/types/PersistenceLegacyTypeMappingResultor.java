package net.jadoth.persistence.types;

import net.jadoth.collections.HashEnum;
import net.jadoth.collections.HashTable;
import net.jadoth.collections.types.XEnum;
import net.jadoth.collections.types.XGettingMap;
import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.collections.types.XTable;
import net.jadoth.typing.KeyValue;
import net.jadoth.util.matching.MultiMatch;


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
		final MultiMatch<PersistenceTypeDefinitionMember>                                   matchedMembers
	)
	{
		return createLegacyTypeMappingResult(legacyTypeDefinition, currentTypeHandler, explicitMappings, matchedMembers);
	}
	
	
	
	public static <M, T> PersistenceLegacyTypeMappingResult<M, T> createLegacyTypeMappingResult(
		final PersistenceTypeDefinition                                                     legacyTypeDefinition,
		final PersistenceTypeHandler<M, T>                                                  currentTypeHandler  ,
		final XGettingMap<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> explicitMappings    ,
		final MultiMatch<PersistenceTypeDefinitionMember>                                   matchedMembers
	)
	{
		final HashTable<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> legacyToCurrentMembers;
		final HashTable<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> currentToLegacyMembers;
		final HashEnum<PersistenceTypeDefinitionMember>                                   deletedLegacyMembers  ;
		final HashEnum<PersistenceTypeDefinitionMember>                                   newCurrentMembers     ;
		
		combineMappings(
			legacyToCurrentMembers = HashTable.New(),
			currentToLegacyMembers = HashTable.New(),
			deletedLegacyMembers   = HashEnum.New() ,
			newCurrentMembers      = HashEnum.New() ,
			currentTypeHandler                      ,
			explicitMappings                        ,
			matchedMembers
		);
		
		return PersistenceLegacyTypeMappingResult.New(
			legacyTypeDefinition  ,
			currentTypeHandler    ,
			legacyToCurrentMembers,
			currentToLegacyMembers,
			deletedLegacyMembers  ,
			newCurrentMembers
		);
	}
	
	public static void combineMappings(
		final XTable<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember>      legacyToCurrentMembers,
		final XTable<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember>      currentToLegacyMembers,
		final XEnum<PersistenceTypeDefinitionMember>                                        deletedLegacyMembers  ,
		final XEnum<PersistenceTypeDefinitionMember>                                        newCurrentMembers     ,
		final PersistenceTypeHandler<?, ?>                                                  currentTypeHandler    ,
		final XGettingMap<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> explicitMappings      ,
		final MultiMatch<PersistenceTypeDefinitionMember>                                   matchedMembers
	)
	{
		legacyToCurrentMembers.addAll(explicitMappings);
		
		final XGettingSequence<KeyValue<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember>> sourceMatches =
			matchedMembers.result().sourceMatches()
		;
		for(final KeyValue<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> match : sourceMatches)
		{
			if(!legacyToCurrentMembers.add(match.key(), match.value()))
			{
				// (04.10.2018 TM)EXCP: proper exception
				throw new RuntimeException("Inconsistency for legacy type member " + match.key().uniqueName());
			}
		}
		
		final XGettingSequence<KeyValue<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember>> targetMatches =
			matchedMembers.result().targetMatches()
		;
		for(final KeyValue<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> match : targetMatches)
		{
			if(!currentToLegacyMembers.add(match.value(), match.key()))
			{
				// (04.10.2018 TM)EXCP: proper exception
				throw new RuntimeException("Inconsistency for current type member " + match.value().uniqueName());
			}
		}
		
		// initialized to all current type members and reduced according to the mapping. The remaining are new.
		newCurrentMembers.addAll(currentTypeHandler.members());
		for(final KeyValue<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> mapping : legacyToCurrentMembers)
		{
			if(mapping.value() == null)
			{
				deletedLegacyMembers.add(mapping.key());
			}
			else
			{
				// remove mapped current member from the set of potentially new current members.
				newCurrentMembers.remove(mapping.value());
			}
		}
	}
		
	public static <M> PersistenceLegacyTypeMappingResultor<M> New()
	{
		return new PersistenceLegacyTypeMappingResultor.Implementation<>();
	}
	
	public final class Implementation<M> implements PersistenceLegacyTypeMappingResultor<M>
	{
		// since default methods, the ability to instantiate stateless instances from interfaces is missing
	}
	
}
