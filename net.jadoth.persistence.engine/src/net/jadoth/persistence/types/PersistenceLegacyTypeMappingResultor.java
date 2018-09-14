package net.jadoth.persistence.types;

import net.jadoth.collections.types.XGettingMap;
import net.jadoth.collections.types.XGettingSet;
import net.jadoth.util.matching.MultiMatch;


public interface PersistenceLegacyTypeMappingResultor<M>
{
	public <T> PersistenceLegacyTypeMappingResult<M, T> createMappingResult(
		final PersistenceTypeDefinition<T>                                                    legacyTypeDefinition,
		final PersistenceTypeHandler<M, T>                                                    currentTypeHandler  ,
		final XGettingMap<PersistenceTypeDescriptionMember, PersistenceTypeDescriptionMember> resolvedMembers     ,
		final XGettingSet<PersistenceTypeDescriptionMember>                                   refacDeletionMembers,
		final MultiMatch<PersistenceTypeDescriptionMember>                                    matchedMembers
	);
	
	
	public static <M> PersistenceLegacyTypeMappingResultor<M> New()
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME OGS-3: PersistenceLegacyTypeMappingResultor#New
	}
}
