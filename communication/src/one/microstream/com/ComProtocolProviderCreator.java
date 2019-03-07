package one.microstream.com;

import java.nio.ByteOrder;

import one.microstream.persistence.types.PersistenceIdStrategy;
import one.microstream.persistence.types.PersistenceTypeDictionaryViewProvider;


@FunctionalInterface
public interface ComProtocolProviderCreator<C>
{
	public ComProtocolProvider<C> creatProtocolProvider(
		String                                name                  ,
		String                                version               ,
		ByteOrder                             byteOrder             ,
		PersistenceIdStrategy                 idStrategy            ,
		PersistenceTypeDictionaryViewProvider typeDictionaryProvider,
		ComProtocolCreator                    protocolCreator
	);
	
	
	
	public static <C> ComProtocolProviderCreator<C> New()
	{
		return new ComProtocolProviderCreator.Implementation<>();
	}
	
	public final class Implementation<C> implements ComProtocolProviderCreator<C>
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
		public ComProtocolProvider<C> creatProtocolProvider(
			final String                                name                  ,
			final String                                version               ,
			final ByteOrder                             byteOrder             ,
			final PersistenceIdStrategy                 idStrategy            ,
			final PersistenceTypeDictionaryViewProvider typeDictionaryProvider,
			final ComProtocolCreator                    protocolCreator
		)
		{
			return new ComProtocolProvider.Implementation<>(
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
