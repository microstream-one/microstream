package one.microstream.persistence.types;

import java.util.Iterator;

import one.microstream.collections.types.XGettingMap;
import one.microstream.util.similarity.Similarity;

public interface PersistenceLegacyTypeHandlerCreator<M>
{
	public <T> PersistenceLegacyTypeHandler<M, T> createLegacyTypeHandler(
		PersistenceLegacyTypeMappingResult<M, T> mappingResult
	);
	
	
	
	public abstract class Abstract<M> implements PersistenceLegacyTypeHandlerCreator<M>
	{
		@Override
		public <T> PersistenceLegacyTypeHandler<M, T> createLegacyTypeHandler(
			final PersistenceLegacyTypeMappingResult<M, T> result
		)
		{
			if(isUnchangedStructure(result))
			{
				/*
				 * special case: structure didn't change, only namings, so the current type handler can be used.
				 * Note that this applies to custom handlers, too. Even ones with variable length instances.
				 */
				return PersistenceLegacyTypeHandler.Wrap(result.legacyTypeDefinition(), result.currentTypeHandler());
			}
			
			if(result.currentTypeHandler() instanceof PersistenceTypeHandlerReflective<?, ?>)
			{
				return this.deriveReflectiveHandler(
					result,
					(PersistenceTypeHandlerReflective<M, T>)result.currentTypeHandler()
				);
			}

			return this.deriveCustomWrappingHandler(result);
		}
			
		
		private static boolean isUnchangedStructure(final PersistenceLegacyTypeMappingResult<?, ?> result)
		{
			if(result.legacyTypeDefinition().instanceMembers().size() != result.currentTypeHandler().instanceMembers().size())
			{
				// if there are differing members counts, the structure cannot be unchanged.
				return false;
			}

			final XGettingMap<PersistenceTypeDefinitionMember, Similarity<PersistenceTypeDefinitionMember>> map =
				result.legacyToCurrentMembers()
			;
			final Iterator<? extends PersistenceTypeDefinitionMember> legacy =
				result.legacyTypeDefinition().instanceMembers().iterator()
			;
			final Iterator<? extends PersistenceTypeDefinitionMember> current =
				result.currentTypeHandler().instanceMembers().iterator()
			;
			
			// check as long as both collections have order-wise corresponding entries (ensured by size check above)
			while(legacy.hasNext())
			{
				final PersistenceTypeDefinitionMember legacyMember  = legacy.next() ;
				final PersistenceTypeDefinitionMember currentMember = current.next();
				
				// all legacy members must be directly mapped to their order-wise corresponding current member.
				if(map.get(legacyMember) != currentMember)
				{
					return false;
				}
				
				// and the types must be the same, of course. Member names are sound and smoke.
				if(!legacyMember.typeName().equals(currentMember.typeName()))
				{
					return false;
				}
			}
			
			// no need to check for remaining elements since size was checked above
			return true;
		}
				
		protected abstract <T> PersistenceLegacyTypeHandler<M, T> deriveCustomWrappingHandler(
			PersistenceLegacyTypeMappingResult<M, T> mappingResult
		);
		
		protected abstract <T> PersistenceLegacyTypeHandler<M, T> deriveReflectiveHandler(
			PersistenceLegacyTypeMappingResult<M, T> mappingResult,
			PersistenceTypeHandlerReflective<M, T>   typeHandler
		);
	}
	
}
