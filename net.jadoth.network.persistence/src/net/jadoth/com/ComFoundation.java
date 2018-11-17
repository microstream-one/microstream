package net.jadoth.com;

import java.net.InetSocketAddress;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;

import net.jadoth.exceptions.MissingFoundationPartException;
import net.jadoth.swizzling.types.SwizzleIdStrategy;
import net.jadoth.util.InstanceDispatcher;

public interface ComFoundation<C, F extends ComFoundation<C, ?>>
{
	public String getProtocolName();
	
	public String getProtocolVersion();
	
	public ByteOrder getByteOrder();
	
	public SwizzleIdStrategy getClientIdStrategy();
			
	public ComProtocolProvider getProtocolProvider();
	
	public ComProtocolProviderCreator getProtocolProviderCreator();
	
	public ComProtocolCreator getProtocolCreator();
	
	
	public ComProtocolStringConverter getProtocolStringConverter();
	
	public ComHostCreator<C> getHostCreator();
	
	public ComConnectionHandler<C> getConnectionHandler();
	
	public ComConnectionAcceptorCreator<C> getConnectionAcceptorCreator();
	
	public InetSocketAddress getHostBindingAddress();
	
	public ComHostChannelAcceptor<C> getChannelAcceptor();
	
	public ComPersistenceAdaptor<C> getPersistenceAdaptor();
	
	
	public ComClientCreator<C> getClientCreator();
	
	public InetSocketAddress getClientTargetAddress();
	
	
	public F setProtocolName(String protocolName);
	
	public F setProtocolVersion(String protocolVersion);
	
	public F setByteOrder(ByteOrder byteOrder);
	
	public F setClientIdStrategy(SwizzleIdStrategy idStrategy);
	
	public F setProtocolCreator(ComProtocolCreator protocolCreator);
	
	public F setProtocolProvider(ComProtocolProvider protocolProvider);
	
	public F setProtocolProviderCreator(ComProtocolProviderCreator protocolProviderCreator);
	
	
	public F setProtocolStringConverter(ComProtocolStringConverter protocolStringConverter);
	
	public F setHostCreator(ComHostCreator<C> hostCreator);

	public F setConnectionHandler(ComConnectionHandler<C> connectionHandler);
	
	public F setConnectionAcceptorCreator(ComConnectionAcceptorCreator<C> connectionAcceptorCreator);
		
	public F setHostBindingAddress(InetSocketAddress hostBindingAddress);
	
	public F setHostChannelAcceptor(ComHostChannelAcceptor<C> channelAcceptor);
	
	public F setPersistenceAdaptor(ComPersistenceAdaptor<C> persistenceAdaptor);
	
	public F setHostContext(
		InetSocketAddress         socketAddress        ,
		ComHostChannelAcceptor<C> channelAcceptor      ,
		ComPersistenceAdaptor<C>  persistenceFoundation
	);
	
	public F setClientCreator(ComClientCreator<C> clientCreator);
	
	public F setClientTargetAddress(InetSocketAddress clientTargetAddress);
	
	
	public ComHost<C> createHost();
	
	public ComClient<C> createClient();
	
	
	
	public static ComFoundation.Default<?> New()
	{
		return new ComFoundation.Default<>();
	}
	
	public abstract class Abstract<C, F extends ComFoundation.Abstract<C, ?>>
	extends InstanceDispatcher.Implementation implements ComFoundation<C, F>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private String                          protocolName             ;
		private String                          protocolVersion          ;
		private ByteOrder                       byteOrder                ;
		private SwizzleIdStrategy               clientIdStrategy         ;
		private ComProtocolCreator              protocolCreator          ;
		private ComProtocolProvider             protocolProvider         ;
		private ComProtocolProviderCreator      protocolProviderCreator  ;
                                                
		private ComProtocolStringConverter      protocolStringConverter  ;
		
		private ComHostCreator<C>               hostCreator              ;
		private ComConnectionHandler<C>         connectionHandler        ;
		private ComConnectionAcceptorCreator<C> connectionAcceptorCreator;
		                                        
		private InetSocketAddress               hostBindingAddress       ;
		private ComHostChannelAcceptor<C>       channelAcceptor          ;
		private ComPersistenceAdaptor<C>        persistenceAdaptor       ;
		
		private ComClientCreator<C>             clientCreator            ;
		private InetSocketAddress               clientTargetAddress      ;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected Abstract()
		{
			super();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@SuppressWarnings("unchecked") // magic self-type.
		protected final F $()
		{
			return (F)this;
		}
				
		@Override
		public ByteOrder getByteOrder()
		{
			if(this.byteOrder == null)
			{
				this.byteOrder = this.ensureByteOrder();
			}
			
			return this.byteOrder;
		}
		
		@Override
		public String getProtocolVersion()
		{
			if(this.protocolVersion == null)
			{
				this.protocolVersion = this.ensureProtocolVersion();
			}
			
			return this.protocolVersion;
		}
		
		@Override
		public String getProtocolName()
		{
			if(this.protocolName == null)
			{
				this.protocolName = this.ensureProtocolName();
			}
			
			return this.protocolName;
		}
		
		@Override
		public SwizzleIdStrategy getClientIdStrategy()
		{
			if(this.clientIdStrategy == null)
			{
				this.clientIdStrategy = this.ensureClientIdStrategy();
			}
			
			return this.clientIdStrategy;
		}
		
		@Override
		public ComProtocolProvider getProtocolProvider()
		{
			if(this.protocolProvider == null)
			{
				this.protocolProvider = this.ensureProtocolProvider();
			}
			
			return this.protocolProvider;
		}
		
		@Override
		public ComProtocolProviderCreator getProtocolProviderCreator()
		{
			if(this.protocolProviderCreator == null)
			{
				this.protocolProviderCreator = this.ensureProtocolProviderCreator();
			}
			
			return this.protocolProviderCreator;
		}
		
		@Override
		public ComProtocolCreator getProtocolCreator()
		{
			if(this.protocolCreator == null)
			{
				this.protocolCreator = this.ensureProtocolCreator();
			}
			
			return this.protocolCreator;
		}
		
		@Override
		public ComProtocolStringConverter getProtocolStringConverter()
		{
			if(this.protocolStringConverter == null)
			{
				this.protocolStringConverter = this.ensureProtocolStringConverter();
			}
			
			return this.protocolStringConverter;
		}
		
		@Override
		public InetSocketAddress getHostBindingAddress()
		{
			if(this.hostBindingAddress == null)
			{
				this.hostBindingAddress = this.ensureHostBindingAddress();
			}

			return this.hostBindingAddress;
		}

		@Override
		public ComHostChannelAcceptor<C> getChannelAcceptor()
		{
			if(this.channelAcceptor == null)
			{
				this.channelAcceptor = this.ensureChannelAcceptor();
			}

			return this.channelAcceptor;
		}

		@Override
		public ComPersistenceAdaptor<C> getPersistenceAdaptor()
		{
			if(this.persistenceAdaptor == null)
			{
				this.persistenceAdaptor = this.ensurePersistenceAdaptor();
			}

			return this.persistenceAdaptor;
		}
		
		@Override
		public ComHostCreator<C> getHostCreator()
		{
			if(this.hostCreator == null)
			{
				this.hostCreator = this.ensureHostCreator();
			}
			
			return this.hostCreator;
		}
		
		@Override
		public ComClientCreator<C> getClientCreator()
		{
			if(this.clientCreator == null)
			{
				this.clientCreator = this.ensureClientCreator();
			}
			
			return this.clientCreator;
		}
		
		@Override
		public InetSocketAddress getClientTargetAddress()
		{
			if(this.clientTargetAddress == null)
			{
				this.clientTargetAddress = this.ensureClientTargetAddress();
			}
			
			return this.clientTargetAddress;
		}
		
		@Override
		public ComConnectionHandler<C> getConnectionHandler()
		{
			if(this.connectionHandler == null)
			{
				this.connectionHandler = this.ensureConnectionHandler();
			}
			
			return this.connectionHandler;
		}
		
		@Override
		public ComConnectionAcceptorCreator<C> getConnectionAcceptorCreator()
		{
			if(this.connectionAcceptorCreator == null)
			{
				this.connectionAcceptorCreator = this.ensureConnectionAcceptorCreator();
			}
			
			return this.connectionAcceptorCreator;
		}

		/*
		 * "ensure" methods guarantee that a non-null/non-zero value is returned.
		 * Either by returning an existing one (e.g. a constant) or by creating a new instance of the specified type.
		 * If both options are not possible, the method will throw a MissingFoundationPartException.
		 */

		protected String ensureProtocolName()
		{
			return ComProtocol.protocolName();
		}

		protected String ensureProtocolVersion()
		{
			return ComProtocol.protocolVersion();
		}
		
		protected ByteOrder ensureByteOrder()
		{
			return Com.byteOrder();
		}

		protected SwizzleIdStrategy ensureClientIdStrategy()
		{
			return Com.DefaultIdStrategyClient();
		}
		
		protected ComProtocolCreator ensureProtocolCreator()
		{
			return ComProtocol.Creator();
		}
		
		protected ComProtocolProviderCreator ensureProtocolProviderCreator()
		{
			return ComProtocolProviderCreator.New();
		}

		protected ComHostCreator<C> ensureHostCreator()
		{
			return ComHost.Creator();
		}
		
		protected ComClientCreator<C> ensureClientCreator()
		{
			return ComClient.Creator();
		}
		
		protected ComConnectionAcceptorCreator<C> ensureConnectionAcceptorCreator()
		{
			return ComConnectionAcceptor.Creator();
		}

		protected ComProtocolProvider ensureProtocolProvider()
		{
			final ComProtocolProviderCreator providerCreator = this.getProtocolProviderCreator();
			
			return providerCreator.creatProtocolProvider(
				this.getProtocolName()      ,
				this.getProtocolVersion()   ,
				this.getByteOrder()         ,
				this.getClientIdStrategy()  ,
				this.getPersistenceAdaptor(),
				this.getProtocolCreator()
			);
		}
		
		protected ComProtocolStringConverter ensureProtocolStringConverter()
		{
			final ComPersistenceAdaptor<C> adaptor = this.getPersistenceAdaptor();
			
			return ComProtocolStringConverter.New(
				adaptor.provideTypeDictionaryCompiler()
			);
		}
				
		protected InetSocketAddress ensureHostBindingAddress()
		{
			// the address to be used is application-specific and cannot be defined here.
			throw new MissingFoundationPartException(InetSocketAddress.class, "Host Binding Address");
		}

		protected ComHostChannelAcceptor<C> ensureChannelAcceptor()
		{
			// the channel acceptor is the link to the application / framework logic and cannot be created here.
			throw new MissingFoundationPartException(ComHostChannelAcceptor.class);
		}

		protected ComPersistenceAdaptor<C> ensurePersistenceAdaptor()
		{
			// the p.adaptor is the link to the application / framework persistence context and cannot be created here.
			throw new MissingFoundationPartException(ComPersistenceAdaptor.class);
		}
				
		protected ComConnectionHandler<C> ensureConnectionHandler()
		{
			// must be created or set specific to <C>.
			throw new MissingFoundationPartException(ComConnectionHandler.class);
		}
		
		protected InetSocketAddress ensureClientTargetAddress()
		{
			// the address to be used is application-specific and cannot be defined here.
			throw new MissingFoundationPartException(InetSocketAddress.class, "Client Target Address");
		}
				

		
		@Override
		public F setProtocolName(final String protocolName)
		{
			this.protocolName = protocolName;
			return this.$();
		}
		
		@Override
		public F setProtocolVersion(final String protocolVersion)
		{
			this.protocolVersion = protocolVersion;
			return this.$();
		}
		
		@Override
		public F setByteOrder(final ByteOrder byteOrder)
		{
			return this.$();
		}
		
		@Override
		public F setClientIdStrategy(final SwizzleIdStrategy idStrategy)
		{
			this.clientIdStrategy = idStrategy;
			return this.$();
		}
				
		@Override
		public F setProtocolCreator(final ComProtocolCreator protocolCreator)
		{
			this.protocolCreator = protocolCreator;
			return this.$();
		}
		
		@Override
		public F setProtocolProvider(final ComProtocolProvider protocolProvider)
		{
			this.protocolProvider = protocolProvider;
			return this.$();
		}
		
		@Override
		public F setProtocolProviderCreator(final ComProtocolProviderCreator protocolProviderCreator)
		{
			this.protocolProviderCreator = protocolProviderCreator;
			return this.$();
		}
		
		@Override
		public F setProtocolStringConverter(final ComProtocolStringConverter protocolStringConverter)
		{
			this.protocolStringConverter = protocolStringConverter;
			return this.$();
		}
		
		@Override
		public F setHostCreator(final ComHostCreator<C> hostCreator)
		{
			this.hostCreator = hostCreator;
			return this.$();
		}
		
		@Override
		public F setClientCreator(final ComClientCreator<C> clientCreator)
		{
			this.clientCreator = clientCreator;
			return this.$();
		}
		
		@Override
		public F setClientTargetAddress(final InetSocketAddress clientTargetAddress)
		{
			this.clientTargetAddress = clientTargetAddress;
			return this.$();
		}
		
		@Override
		public F setConnectionAcceptorCreator(final ComConnectionAcceptorCreator<C> connectionAcceptorCreator)
		{
			this.connectionAcceptorCreator = connectionAcceptorCreator;
			return this.$();
		}
		
		@Override
		public F setConnectionHandler(final ComConnectionHandler<C> connectionHandler)
		{
			this.connectionHandler = connectionHandler;
			return this.$();
		}
				
		@Override
		public F setHostBindingAddress(final InetSocketAddress hostBindingAddress)
		{
			this.hostBindingAddress = hostBindingAddress;
			return this.$();
		}

		@Override
		public F setHostChannelAcceptor(final ComHostChannelAcceptor<C> channelAcceptor)
		{
			this.channelAcceptor = channelAcceptor;
			return this.$();
		}

		@Override
		public F setPersistenceAdaptor(final ComPersistenceAdaptor<C> persistenceAdaptor)
		{
			this.persistenceAdaptor = persistenceAdaptor;
			return this.$();
		}
		
		@Override
		public F setHostContext(
			final InetSocketAddress         socketAddress        ,
			final ComHostChannelAcceptor<C> channelAcceptor      ,
			final ComPersistenceAdaptor<C>  persistenceFoundation
		)
		{
			this
			.setHostBindingAddress (socketAddress)
			.setHostChannelAcceptor(channelAcceptor)
			.setPersistenceAdaptor (persistenceFoundation)
			;
			
			return this.$();
		}
		
		@Override
		public ComHost<C> createHost()
		{
			final ComConnectionAcceptorCreator<C> conAccCreator = this.getConnectionAcceptorCreator();
			final ComConnectionAcceptor<C> connectionAcceptor = conAccCreator.createConnectionAcceptor(
				this.getProtocolProvider()       ,
				this.getProtocolStringConverter(),
				this.getConnectionHandler()      ,
				this.getPersistenceAdaptor()     ,
				this.getChannelAcceptor()
			);

			final ComHostCreator<C> hostCreator = this.getHostCreator();
			return hostCreator.createComHost(
				this.getHostBindingAddress() ,
				this.getConnectionHandler(),
				connectionAcceptor
			);
		}
		
		@Override
		public ComClient<C> createClient()
		{
			final ComClientCreator<C> clientCreator = this.getClientCreator();
			
			return clientCreator.createClient(
				this.getClientTargetAddress()    ,
				this.getConnectionHandler()      ,
				this.getProtocolStringConverter(),
				this.getPersistenceAdaptor()
			);
		}
		
	}
	
	public class Default<F extends ComFoundation.Default<F>>
	extends ComFoundation.Abstract<SocketChannel, F>
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected Default()
		{
			super();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public ComConnectionHandler.Default ensureConnectionHandler()
		{
			return ComConnectionHandler.Default();
		}
		
	}
	
}
