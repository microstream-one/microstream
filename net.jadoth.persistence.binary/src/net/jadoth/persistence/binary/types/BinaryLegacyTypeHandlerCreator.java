package net.jadoth.persistence.binary.types;

import net.jadoth.persistence.types.PersistenceLegacyTypeHandler;
import net.jadoth.persistence.types.PersistenceLegacyTypeHandlerCreator;
import net.jadoth.persistence.types.PersistenceLegacyTypeMappingResult;

public interface BinaryLegacyTypeHandlerCreator extends PersistenceLegacyTypeHandlerCreator<Binary>
{
	
	public static BinaryLegacyTypeHandlerCreator New()
	{
		return new BinaryLegacyTypeHandlerCreator.Implementation();
	}
	
	public final class Implementation
	extends PersistenceLegacyTypeHandlerCreator.AbstractImplementation<Binary>
	implements BinaryLegacyTypeHandlerCreator
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation()
		{
			super();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		protected <T> PersistenceLegacyTypeHandler<Binary, T> deriveCustomWrappingHandler(
			final PersistenceLegacyTypeMappingResult<?, ?> result
		)
		{
			// (17.09.2018 TM)FIXME: OGS-3: deriveCustomWrappingHandler
			throw new net.jadoth.meta.NotImplementedYetError();
		}

		@Override
		protected <T> PersistenceLegacyTypeHandler<Binary, T> deriveReflectiveHandler(
			final PersistenceLegacyTypeMappingResult<?, ?> result
		)
		{
			// (17.09.2018 TM)FIXME: OGS-3: deriveReflectiveHandler
			throw new net.jadoth.meta.NotImplementedYetError();
		}
		
	}
}
