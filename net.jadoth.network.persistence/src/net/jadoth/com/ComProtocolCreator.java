package net.jadoth.com;

import java.nio.ByteOrder;

import net.jadoth.persistence.types.PersistenceTypeDictionaryView;
import net.jadoth.swizzling.types.SwizzleIdStrategy;


@FunctionalInterface
public interface ComProtocolCreator
{
	public ComProtocol creatProtocol(
		String                        name          ,
		String                        version       ,
		ByteOrder                     byteOrder     ,
		SwizzleIdStrategy             idStrategy    ,
		PersistenceTypeDictionaryView typeDictionary
	);
	
	
	
	public static ComProtocolCreator New()
	{
		return new ComProtocolCreator.Implementation();
	}
	
	public final class Implementation implements ComProtocolCreator
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
		public ComProtocol creatProtocol(
			final String                        name          ,
			final String                        version       ,
			final ByteOrder                     byteOrder     ,
			final SwizzleIdStrategy             idStrategy    ,
			final PersistenceTypeDictionaryView typeDictionary
		)
		{
			return new ComProtocol.Implementation(name, version, byteOrder, idStrategy, typeDictionary);
		}
		
	}
	
}
