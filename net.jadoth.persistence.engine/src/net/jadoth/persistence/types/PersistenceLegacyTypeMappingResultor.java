package net.jadoth.persistence.types;

import net.jadoth.collections.HashEnum;
import net.jadoth.collections.HashTable;
import net.jadoth.collections.types.XGettingMap;
import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.typing.KeyValue;
import net.jadoth.util.matching.MultiMatch;


public interface PersistenceLegacyTypeMappingResultor<M>
{
	public <T> PersistenceLegacyTypeMappingResult<M, T> createMappingResult(
		final PersistenceTypeDefinition<?>                                                    legacyTypeDefinition,
		final PersistenceTypeHandler<M, T>                                                    currentTypeHandler  ,
		final XGettingMap<PersistenceTypeDescriptionMember, PersistenceTypeDescriptionMember> explicitMappings    ,
		final MultiMatch<PersistenceTypeDescriptionMember>                                    matchedMembers
	);
	
	
	
	public static <M> PersistenceLegacyTypeMappingResultor<M> New()
	{
		return new PersistenceLegacyTypeMappingResultor.Implementation<>();
	}
	
	public final class Implementation<M> implements PersistenceLegacyTypeMappingResultor<M>
	{

		@Override
		public <T> PersistenceLegacyTypeMappingResult<M, T> createMappingResult(
			final PersistenceTypeDefinition<?>                                                    legacyTypeDefinition,
			final PersistenceTypeHandler<M, T>                                                    currentTypeHandler  ,
			final XGettingMap<PersistenceTypeDescriptionMember, PersistenceTypeDescriptionMember> explicitMappings    ,
			final MultiMatch<PersistenceTypeDescriptionMember>                                    matchedMembers
		)
		{
			final HashTable<PersistenceTypeDescriptionMember, PersistenceTypeDescriptionMember> legacyToCurrentMembers =
				HashTable.New(explicitMappings)
			;
			
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
			
			final HashEnum<PersistenceTypeDescriptionMember> deletedLegacyMembers = HashEnum.New();
			
			// initialized to all current type members and reduced according to the mapping. The remaining are new.
			final HashEnum<PersistenceTypeDescriptionMember> newCurrentMembers = HashEnum.New(currentTypeHandler.members());
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
			
			return PersistenceLegacyTypeMappingResult.New(
				legacyTypeDefinition,
				currentTypeHandler  ,
				explicitMappings    ,
				deletedLegacyMembers,
				newCurrentMembers
			);
		}
		
	}
	
}
