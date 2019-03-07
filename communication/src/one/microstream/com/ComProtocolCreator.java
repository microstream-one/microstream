package one.microstream.com;

import java.nio.ByteOrder;

import one.microstream.persistence.types.PersistenceIdStrategy;
import one.microstream.persistence.types.PersistenceTypeDictionaryView;


@FunctionalInterface
public interface ComProtocolCreator
{
	public ComProtocol creatProtocol(
		String                        name          ,
		String                        version       ,
		ByteOrder                     byteOrder     ,
		PersistenceIdStrategy             idStrategy    ,
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
			final PersistenceIdStrategy             idStrategy    ,
			final PersistenceTypeDictionaryView typeDictionary
		)
		{
			return new ComProtocol.Implementation(name, version, byteOrder, idStrategy, typeDictionary);
		}
		
	}
	
}
