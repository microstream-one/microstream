package net.jadoth.persistence.types;

import net.jadoth.collections.types.XGettingMap;
import net.jadoth.collections.types.XGettingSet;
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
			// (04.10.2018 TM)FIXME: OGS-3: determine new current members
			final XGettingSet<PersistenceTypeDescriptionMember> newCurrentMembers = null;
			
			return PersistenceLegacyTypeMappingResult.New(
				legacyTypeDefinition,
				currentTypeHandler  ,
				explicitMappings    ,
				newCurrentMembers
			);
		}
		
	}
	
}
