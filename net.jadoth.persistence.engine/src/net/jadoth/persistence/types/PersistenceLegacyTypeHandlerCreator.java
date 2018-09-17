package net.jadoth.persistence.types;

public interface PersistenceLegacyTypeHandlerCreator<M>
{
	public <T> PersistenceLegacyTypeHandler<M, T> createLegacyTypeHandler(
		PersistenceLegacyTypeMappingResult<M, T> mappingResult
	);
	
	
	public abstract class AbstractImplementation<M> implements PersistenceLegacyTypeHandlerCreator<M>
	{
		@Override
		public <T> PersistenceLegacyTypeHandler<M, T> createLegacyTypeHandler(
			final PersistenceLegacyTypeMappingResult<M, T> result
		)
		{
			if(isUnchangedStructure(result))
			{
				// special case: structure didn't change, only namings, so the current type handler can be used.
				return PersistenceLegacyTypeHandler.Wrap(result.legacyTypeDefinition(), result.currentTypeHandler());
			}
			
			if(isCustom(result.currentTypeHandler()))
			{
				return this.deriveCustomWrappingHandler(result);
			}
			
			return this.deriveReflectiveHandler(result);
		}
			
		
		private static boolean isUnchangedStructure(final PersistenceLegacyTypeMappingResult<?, ?> result)
		{
			// (14.09.2018 TM)FIXME: OGS-3: isUnchangedStructure()
			throw new net.jadoth.meta.NotImplementedYetError();
		}
		
		private static boolean isCustom(final PersistenceTypeHandler<?, ?> currentTypeHandler)
		{
			// (14.09.2018 TM)FIXME: OGS-3: isCustom()
			throw new net.jadoth.meta.NotImplementedYetError();
		}
		
		protected abstract <T> PersistenceLegacyTypeHandler<M, T> deriveCustomWrappingHandler(
			PersistenceLegacyTypeMappingResult<?, ?> result
		);
		
		protected abstract <T> PersistenceLegacyTypeHandler<M, T> deriveReflectiveHandler(
			PersistenceLegacyTypeMappingResult<?, ?> result
		);
	}
	
}
