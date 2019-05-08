package one.microstream.com;

import static one.microstream.X.notNull;

import java.net.InetSocketAddress;


@FunctionalInterface
public interface ComClientCreator<C>
{
	public ComClient.Default<C> createClient(
		InetSocketAddress          hostAddress       ,
		ComConnectionHandler<C>    connectionHandler ,
		ComProtocolStringConverter protocolParser    ,
		ComPersistenceAdaptor<C>   persistenceAdaptor
	);
	
	
	public static <C> ComClientCreator.Default<C> New()
	{
		return new ComClientCreator.Default<>();
	}
	
	public final class Default<C> implements ComClientCreator<C>
	{
		@Override
		public ComClient.Default<C> createClient(
			final InetSocketAddress          hostAddress       ,
			final ComConnectionHandler<C>    connectionHandler ,
			final ComProtocolStringConverter protocolParser    ,
			final ComPersistenceAdaptor<C>   persistenceAdaptor
		)
		{
			return ComClient.New(
				notNull(hostAddress)       ,
				notNull(connectionHandler) ,
				notNull(protocolParser)    ,
				notNull(persistenceAdaptor)
			);
		}
		
	}
}
