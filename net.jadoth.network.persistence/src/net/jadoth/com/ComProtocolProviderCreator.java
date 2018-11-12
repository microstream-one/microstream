package net.jadoth.com;

import java.nio.ByteOrder;

import net.jadoth.persistence.types.PersistenceTypeDictionaryViewProvider;
import net.jadoth.swizzling.types.SwizzleIdStrategy;

public interface ComProtocolProviderCreator
{
	public ComProtocolProvider creatProtocolProvider(
		String                                name                  ,
		String                                version               ,
		ByteOrder                             byteOrder             ,
		SwizzleIdStrategy                     idStrategy            ,
		PersistenceTypeDictionaryViewProvider typeDictionaryProvider,
		ComProtocolCreator                    protocolCreator
	);
	
	
	
	public static ComProtocolProviderCreator New()
	{
		return new ComProtocolProviderCreator.Implementation();
	}
	
	public final class Implementation implements ComProtocolProviderCreator
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
		public ComProtocolProvider creatProtocolProvider(
			final String                                name                  ,
			final String                                version               ,
			final ByteOrder                             byteOrder             ,
			final SwizzleIdStrategy                     idStrategy            ,
			final PersistenceTypeDictionaryViewProvider typeDictionaryProvider,
			final ComProtocolCreator                    protocolCreator
		)
		{
			return new ComProtocolProvider.Implementation(
				name                  ,
				version               ,
				byteOrder             ,
				idStrategy            ,
				typeDictionaryProvider,
				protocolCreator
			);
		}
		
	}
	
}
