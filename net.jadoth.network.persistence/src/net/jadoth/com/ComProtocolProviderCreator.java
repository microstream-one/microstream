package net.jadoth.com;

import java.nio.ByteOrder;

import net.jadoth.persistence.types.PersistenceTypeDictionaryView;
import net.jadoth.swizzling.types.SwizzleIdStrategy;

public interface ComProtocolProviderCreator
{
	public ComProtocolProvider creatProtocolProvider(
		final String                        name           ,
		final String                        version        ,
		final ByteOrder                     byteOrder      ,
		final SwizzleIdStrategy             idStrategy     ,
		final PersistenceTypeDictionaryView typeDictionary ,
		final ComProtocolCreator            protocolCreator
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
			final String                        name           ,
			final String                        version        ,
			final ByteOrder                     byteOrder      ,
			final SwizzleIdStrategy             idStrategy     ,
			final PersistenceTypeDictionaryView typeDictionary ,
			final ComProtocolCreator            protocolCreator
		)
		{
			return new ComProtocolProvider.Implementation(
				name           ,
				version        ,
				byteOrder      ,
				idStrategy     ,
				typeDictionary ,
				protocolCreator
			);
		}
		
	}
	
}
