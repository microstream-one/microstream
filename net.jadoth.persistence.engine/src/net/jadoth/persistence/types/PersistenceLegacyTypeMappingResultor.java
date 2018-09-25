package net.jadoth.persistence.types;

import net.jadoth.collections.types.XGettingMap;
import net.jadoth.collections.types.XGettingSet;
import net.jadoth.util.matching.MultiMatch;


public interface PersistenceLegacyTypeMappingResultor<M>
{
	public <T> PersistenceLegacyTypeMappingResult<M, T> createMappingResult(
		final PersistenceTypeDefinition<?>                                                    legacyTypeDefinition,
		final PersistenceTypeHandler<M, T>                                                    currentTypeHandler  ,
		final XGettingMap<PersistenceTypeDescriptionMember, PersistenceTypeDescriptionMember> resolvedMembers     ,
		final XGettingSet<PersistenceTypeDescriptionMember>                                   refacDeletionMembers,
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
			final XGettingMap<PersistenceTypeDescriptionMember, PersistenceTypeDescriptionMember> resolvedMembers     ,
			final XGettingSet<PersistenceTypeDescriptionMember>                                   refacDeletionMembers,
			final MultiMatch<PersistenceTypeDescriptionMember>                                    matchedMembers
		)
		{
			return PersistenceLegacyTypeMappingResult.New(
				legacyTypeDefinition,
				currentTypeHandler  ,
				resolvedMembers     ,
				refacDeletionMembers
			);
		}
		
	}
	
}
