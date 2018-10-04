package net.jadoth.persistence.types;

import net.jadoth.collections.HashEnum;
import net.jadoth.collections.HashTable;
import net.jadoth.collections.types.XGettingMap;
import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.collections.types.XMap;
import net.jadoth.collections.types.XSet;
import net.jadoth.typing.KeyValue;
import net.jadoth.util.matching.MultiMatch;


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
		final PersistenceTypeDefinition<?>                                                    legacyTypeDefinition,
		final PersistenceTypeHandler<M, T>                                                    currentTypeHandler  ,
		final XGettingMap<PersistenceTypeDescriptionMember, PersistenceTypeDescriptionMember> explicitMappings    ,
		final MultiMatch<PersistenceTypeDescriptionMember>                                    matchedMembers
	)
	{
		return createLegacyTypeMappingResult(legacyTypeDefinition, currentTypeHandler, explicitMappings, matchedMembers);
	}
	
	
	
	public static <M, T> PersistenceLegacyTypeMappingResult<M, T> createLegacyTypeMappingResult(
		final PersistenceTypeDefinition<?>                                                    legacyTypeDefinition,
		final PersistenceTypeHandler<M, T>                                                    currentTypeHandler  ,
		final XGettingMap<PersistenceTypeDescriptionMember, PersistenceTypeDescriptionMember> explicitMappings    ,
		final MultiMatch<PersistenceTypeDescriptionMember>                                    matchedMembers
	)
	{
		final HashTable<PersistenceTypeDescriptionMember, PersistenceTypeDescriptionMember> legacyToCurrentMembers;
		final HashEnum<PersistenceTypeDescriptionMember>                                    deletedLegacyMembers  ;
		final HashEnum<PersistenceTypeDescriptionMember>                                    newCurrentMembers     ;
		
		combineMappings(
			legacyToCurrentMembers = HashTable.New(),
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
			deletedLegacyMembers  ,
			newCurrentMembers
		);
	}
	
	public static void combineMappings(
		final XMap<PersistenceTypeDescriptionMember, PersistenceTypeDescriptionMember>        legacyToCurrentMembers,
		final XSet<PersistenceTypeDescriptionMember>                                          deletedLegacyMembers  ,
		final XSet<PersistenceTypeDescriptionMember>                                          newCurrentMembers     ,
		final PersistenceTypeHandler<?, ?>                                                    currentTypeHandler    ,
		final XGettingMap<PersistenceTypeDescriptionMember, PersistenceTypeDescriptionMember> explicitMappings      ,
		final MultiMatch<PersistenceTypeDescriptionMember>                                    matchedMembers
	)
	{
		legacyToCurrentMembers.addAll(explicitMappings);
		
		final XGettingSequence<KeyValue<PersistenceTypeDescriptionMember, PersistenceTypeDescriptionMember>> matches =
			matchedMembers.result().sourceMatches()
		;
		
		for(final KeyValue<PersistenceTypeDescriptionMember, PersistenceTypeDescriptionMember> match : matches)
		{
			if(!legacyToCurrentMembers.add(match.key(), match.value()))
			{
				// (04.10.2018 TM)EXCP: proper exception
				throw new RuntimeException("Inconsistency for legacy type member " + match.key().uniqueName());
			}
		}
		
		// initialized to all current type members and reduced according to the mapping. The remaining are new.
		newCurrentMembers.addAll(currentTypeHandler.members());
		for(final KeyValue<PersistenceTypeDescriptionMember, PersistenceTypeDescriptionMember> mapping : legacyToCurrentMembers)
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
