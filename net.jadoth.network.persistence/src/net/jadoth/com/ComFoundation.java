package net.jadoth.com;

import java.net.InetSocketAddress;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;

import net.jadoth.collections.HashEnum;
import net.jadoth.collections.types.XEnum;
import net.jadoth.exceptions.MissingFoundationPartException;
import net.jadoth.persistence.types.PersistenceIdStrategy;
import net.jadoth.util.InstanceDispatcher;

public interface ComFoundation<C, F extends ComFoundation<C, ?>>
{
	public String getProtocolName();
	
	public String getProtocolVersion();
	
	public ByteOrder getByteOrder();
	
	public PersistenceIdStrategy getClientIdStrategy();
			
	public ComProtocolProvider<C> getProtocolProvider();
	
	public ComProtocolProviderCreator<C> getProtocolProviderCreator();
	
	public ComProtocolCreator getProtocolCreator();
	
	
	public ComProtocolStringConverter getProtocolStringConverter();
	
	public ComHostCreator<C> getHostCreator();
	
	public ComConnectionHandler<C> getConnectionHandler();
	
	public ComConnectionAcceptorCreator<C> getConnectionAcceptorCreator();
		
	public ComHostChannelAcceptor<C> getHostChannelAcceptor();
	
	public ComPersistenceAdaptor<C> getHostPersistenceAdaptor();
	
	public ComPersistenceAdaptor<C> getClientPersistenceAdaptor();
	
	public ComPersistenceAdaptorCreator<C> getPersistenceAdaptorCreator();
	
	public PersistenceIdStrategy getHostInitializationIdStrategy();
	
	public XEnum<Class<?>> getEntityTypes();
	
	public PersistenceIdStrategy getHostIdStrategy();
		
	public ComClientCreator<C> getClientCreator();
	
	public ComConnectionLogicDispatcher<C> getConnectionLogicDispatcher();
	
	
	// the port applies to host and client alike, that's what using a common channel is all about.
	public int getPort();
	
	public InetSocketAddress getHostBindingAddress();
	
	public InetSocketAddress getClientTargetAddress();
	
	
	public F setProtocolName(String protocolName);
	
	public F setProtocolVersion(String protocolVersion);
	
	public F setByteOrder(ByteOrder byteOrder);
	
	public F setClientIdStrategy(PersistenceIdStrategy idStrategy);
	
	public F setProtocolCreator(ComProtocolCreator protocolCreator);
	
	public F setProtocolProvider(ComProtocolProvider<C> protocolProvider);
	
	public F setProtocolProviderCreator(ComProtocolProviderCreator<C> protocolProviderCreator);
	
	
	public F setProtocolStringConverter(ComProtocolStringConverter protocolStringConverter);
	
	public F setHostCreator(ComHostCreator<C> hostCreator);

	public F setConnectionHandler(ComConnectionHandler<C> connectionHandler);
	
	public F setConnectionAcceptorCreator(ComConnectionAcceptorCreator<C> connectionAcceptorCreator);
		
	
	public F setHostChannelAcceptor(ComHostChannelAcceptor<C> channelAcceptor);
	
	public F setHostPersistenceAdaptor(ComPersistenceAdaptor<C> hostPersistenceAdaptor);
	
	public F setClientPersistenceAdaptor(ComPersistenceAdaptor<C> clientPersistenceAdaptor);
	
	public F setPersistenceAdaptorCreator(ComPersistenceAdaptorCreator<C> persistenceAdaptorCreator);
	
	public F setHostInitializationIdStrategy(PersistenceIdStrategy hostInitializationIdStrategy);
	
	public F setEntityTypes(XEnum<Class<?>> entityTypes);
	
	public boolean registerEntityType(Class<?> entityType);
	
	public F registerEntityTypes(Class<?>... entityTypes);
	
	public F registerEntityTypes(final Iterable<Class<?>> entityTypes);
	
	public F setHostIdStrategy(PersistenceIdStrategy hostIdStrategy);
		
	public F setClientCreator(ComClientCreator<C> clientCreator);
	
	public F setConnectionLogicDispatcher(ComConnectionLogicDispatcher<C> connectionLogicDispatcher);
	
	public F setPort(int port);

	public F setHostBindingAddress(InetSocketAddress hostBindingAddress);
	
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
		
		private int                             port                     ;
		private InetSocketAddress               hostBindingAddress       ;
		private InetSocketAddress               clientTargetAddress      ;
		
		private String                          protocolName             ;
		private String                          protocolVersion          ;
		private ByteOrder                       byteOrder                ;
		private PersistenceIdStrategy           clientIdStrategy         ;
		private ComProtocolCreator              protocolCreator          ;
		private ComProtocolProvider<C>          protocolProvider         ;
		private ComProtocolProviderCreator<C>   protocolProviderCreator  ;
                                                
		private ComProtocolStringConverter      protocolStringConverter  ;
		
		private ComHostCreator<C>               hostCreator              ;
		private ComConnectionHandler<C>         connectionHandler        ;
		private ComConnectionAcceptorCreator<C> connectionAcceptorCreator;
		private ComHostChannelAcceptor<C>       hostChannelAcceptor      ;
		
		private ComPersistenceAdaptorCreator<C> persistenceAdaptorCreator;
		private ComPersistenceAdaptor<C>        hostPersistenceAdaptor   ;
		private ComPersistenceAdaptor<C>        clientPersistenceAdaptor ;
		
		private PersistenceIdStrategy           hostInitIdStrategy       ;
		private XEnum<Class<?>>                 entityTypes              ;
		private PersistenceIdStrategy           hostIdStrategy           ;
		
		private ComClientCreator<C>             clientCreator            ;
		private ComConnectionLogicDispatcher<C> connectionLogicDispatcher;

		
		
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
		public PersistenceIdStrategy getClientIdStrategy()
		{
			if(this.clientIdStrategy == null)
			{
				this.clientIdStrategy = this.ensureClientIdStrategy();
			}
			
			return this.clientIdStrategy;
		}
		
		@Override
		public ComProtocolProvider<C> getProtocolProvider()
		{
			if(this.protocolProvider == null)
			{
				this.protocolProvider = this.ensureProtocolProvider();
			}
			
			return this.protocolProvider;
		}
		
		@Override
		public ComProtocolProviderCreator<C> getProtocolProviderCreator()
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
		public ComHostChannelAcceptor<C> getHostChannelAcceptor()
		{
			if(this.hostChannelAcceptor == null)
			{
				this.hostChannelAcceptor = this.ensureHostChannelAcceptor();
			}

			return this.hostChannelAcceptor;
		}

		@Override
		public ComPersistenceAdaptor<C> getHostPersistenceAdaptor()
		{
			if(this.hostPersistenceAdaptor == null)
			{
				this.hostPersistenceAdaptor = this.ensureHostPersistenceAdaptor();
			}

			return this.hostPersistenceAdaptor;
		}

		@Override
		public ComPersistenceAdaptor<C> getClientPersistenceAdaptor()
		{
			if(this.clientPersistenceAdaptor == null)
			{
				this.clientPersistenceAdaptor = this.ensureClientPersistenceAdaptor();
			}

			return this.clientPersistenceAdaptor;
		}
		
		@Override
		public ComPersistenceAdaptorCreator<C> getPersistenceAdaptorCreator()
		{
			if(this.persistenceAdaptorCreator == null)
			{
				this.persistenceAdaptorCreator = this.ensurePersistenceAdaptorCreator();
			}

			return this.persistenceAdaptorCreator;
		}
		
		@Override
		public PersistenceIdStrategy getHostInitializationIdStrategy()
		{
			if(this.hostInitIdStrategy == null)
			{
				this.hostInitIdStrategy = this.ensureHostInitializationIdStrategy();
			}

			return this.hostInitIdStrategy;
		}
		
		@Override
		public XEnum<Class<?>> getEntityTypes()
		{
			if(this.entityTypes == null)
			{
				this.entityTypes = this.ensureEntityTypes();
			}

			return this.entityTypes;
		}
		
		@Override
		public PersistenceIdStrategy getHostIdStrategy()
		{
			if(this.hostIdStrategy == null)
			{
				this.hostIdStrategy = this.ensureHostIdStrategy();
			}

			return this.hostIdStrategy;
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
		public ComConnectionLogicDispatcher<C> getConnectionLogicDispatcher()
		{
			if(this.connectionLogicDispatcher == null)
			{
				this.connectionLogicDispatcher = this.ensureConnectionLogicDispatcher();
			}
			
			return this.connectionLogicDispatcher;
		}
		
		@Override
		public int getPort()
		{
			if(this.port == 0)
			{
				this.port = this.ensurePort();
			}

			return this.port;
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

			return this.getConnectionLogicDispatcher().dispatch(this.connectionHandler);
		}
		
		@Override
		public ComConnectionAcceptorCreator<C> getConnectionAcceptorCreator()
		{
			if(this.connectionAcceptorCreator == null)
			{
				this.connectionAcceptorCreator = this.ensureConnectionAcceptorCreator();
			}
			
			return this.getConnectionLogicDispatcher().dispatch(this.connectionAcceptorCreator);
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

		protected PersistenceIdStrategy ensureClientIdStrategy()
		{
			return Com.DefaultIdStrategyClient();
		}
		
		protected ComProtocolCreator ensureProtocolCreator()
		{
			return ComProtocol.Creator();
		}
		
		protected ComProtocolProviderCreator<C> ensureProtocolProviderCreator()
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
		
		protected ComConnectionLogicDispatcher<C> ensureConnectionLogicDispatcher()
		{
			return ComConnectionLogicDispatcher.New();
		}
		
		protected ComConnectionAcceptorCreator<C> ensureConnectionAcceptorCreator()
		{
			return ComConnectionAcceptor.Creator();
		}

		protected ComProtocolProvider<C> ensureProtocolProvider()
		{
			final ComProtocolProviderCreator<C> providerCreator = this.getProtocolProviderCreator();
			
			return providerCreator.creatProtocolProvider(
				this.getProtocolName()      ,
				this.getProtocolVersion()   ,
				this.getByteOrder()         ,
				this.getClientIdStrategy()  ,
				this.getHostPersistenceAdaptor(),
				this.getProtocolCreator()
			);
		}
		
		protected ComProtocolStringConverter ensureProtocolStringConverter()
		{
			final ComPersistenceAdaptor<C> adaptor = this.getHostPersistenceAdaptor();
			
			return ComProtocolStringConverter.New(
				adaptor.provideTypeDictionaryCompiler()
			);
		}

		protected ComHostChannelAcceptor<C> ensureHostChannelAcceptor()
		{
			// the channel acceptor is the link to the application / framework logic and cannot be created here.
			throw new MissingFoundationPartException(ComHostChannelAcceptor.class);
		}

		protected ComPersistenceAdaptor<C> ensureHostPersistenceAdaptor()
		{
			final ComPersistenceAdaptorCreator<C> creator = this.getPersistenceAdaptorCreator();
			
			return creator.createHostPersistenceAdaptor(
				this.getHostInitializationIdStrategy(),
				this.getEntityTypes()                 ,
				this.getHostIdStrategy()
			);
		}
		
		protected ComPersistenceAdaptor<C> ensureClientPersistenceAdaptor()
		{
			final ComPersistenceAdaptorCreator<C> creator = this.getPersistenceAdaptorCreator();
			
			return creator.createClientPersistenceAdaptor();
		}
		
		protected ComPersistenceAdaptorCreator<C> ensurePersistenceAdaptorCreator()
		{
			// the p.adaptor is the link to the application / framework persistence context and cannot be created here.
			throw new MissingFoundationPartException(ComPersistenceAdaptorCreator.class);
		}
		
		protected PersistenceIdStrategy ensureHostInitializationIdStrategy()
		{
			return Com.DefaultIdStrategyHostInitialization();
		}
		
		protected XEnum<Class<?>> ensureEntityTypes()
		{
			return HashEnum.New();
		}
		
		protected PersistenceIdStrategy ensureHostIdStrategy()
		{
			return Com.DefaultIdStrategyHost();
		}
				
		protected ComConnectionHandler<C> ensureConnectionHandler()
		{
			// must be created or set specific to <C>.
			throw new MissingFoundationPartException(ComConnectionHandler.class);
		}
		
		protected int ensurePort()
		{
			// however meaningful that may be ...
			return Com.defaultPort();
		}
		
		protected InetSocketAddress ensureHostBindingAddress()
		{
			return XSockets.localHostSocketAddress(
				this.getPort()
			);
		}
		
		protected InetSocketAddress ensureClientTargetAddress()
		{
			return XSockets.localHostSocketAddress(
				this.getPort()
			);
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
			this.byteOrder = byteOrder;
			return this.$();
		}
		
		@Override
		public F setClientIdStrategy(final PersistenceIdStrategy idStrategy)
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
		public F setProtocolProvider(final ComProtocolProvider<C> protocolProvider)
		{
			this.protocolProvider = protocolProvider;
			return this.$();
		}
		
		@Override
		public F setProtocolProviderCreator(final ComProtocolProviderCreator<C> protocolProviderCreator)
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
		public F setConnectionLogicDispatcher(final ComConnectionLogicDispatcher<C> connectionLogicDispatcher)
		{
			this.connectionLogicDispatcher = connectionLogicDispatcher;
			return this.$();
		}
		
		@Override
		public F setPort(final int port)
		{
			this.port = port;
			return this.$();
		}
		
		@Override
		public F setHostBindingAddress(final InetSocketAddress hostBindingAddress)
		{
			this.hostBindingAddress = hostBindingAddress;
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
		public F setHostChannelAcceptor(final ComHostChannelAcceptor<C> channelAcceptor)
		{
			this.hostChannelAcceptor = channelAcceptor;
			return this.$();
		}

		@Override
		public F setHostPersistenceAdaptor(final ComPersistenceAdaptor<C> hostPersistenceAdaptor)
		{
			this.hostPersistenceAdaptor = hostPersistenceAdaptor;
			return this.$();
		}

		@Override
		public F setClientPersistenceAdaptor(final ComPersistenceAdaptor<C> clientPersistenceAdaptor)
		{
			this.clientPersistenceAdaptor = clientPersistenceAdaptor;
			return this.$();
		}
		
		@Override
		public F setPersistenceAdaptorCreator(final ComPersistenceAdaptorCreator<C> persistenceAdaptorCreator)
		{
			this.persistenceAdaptorCreator = persistenceAdaptorCreator;
			return this.$();
		}
		
		@Override
		public F setHostInitializationIdStrategy(final PersistenceIdStrategy hostInitializationIdStrategy)
		{
			this.hostInitIdStrategy = hostInitializationIdStrategy;
			return this.$();
		}
		
		@Override
		public F setEntityTypes(final XEnum<Class<?>> entityTypes)
		{
			this.entityTypes = entityTypes;
			return this.$();
		}
		
		@Override
		public boolean registerEntityType(final Class<?> entityType)
		{
			return this.getEntityTypes().add(entityType);
		}
		
		@Override
		public F registerEntityTypes(final Class<?>... entityTypes)
		{
			this.getEntityTypes().addAll(entityTypes);
			
			return this.$();
		}
		
		@Override
		public F registerEntityTypes(final Iterable<Class<?>> entityTypes)
		{
			final XEnum<Class<?>> registeredEntityTypes = this.getEntityTypes();
			
			for(final Class<?> entityType : entityTypes)
			{
				registeredEntityTypes.add(entityType);
			}
			
			return this.$();
		}
		
		@Override
		public F setHostIdStrategy(final PersistenceIdStrategy hostIdStrategy)
		{
			this.hostIdStrategy = hostIdStrategy;
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
				this.getHostPersistenceAdaptor() ,
				this.getHostChannelAcceptor()
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
				this.getClientTargetAddress()     ,
				this.getConnectionHandler()       ,
				this.getProtocolStringConverter() ,
				this.getClientPersistenceAdaptor()
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
		protected ComHostChannelAcceptor<SocketChannel> ensureHostChannelAcceptor()
		{
			return Com::bounce;
		}
		
		@Override
		public ComConnectionHandler.Default ensureConnectionHandler()
		{
			return ComConnectionHandler.Default();
		}
		
	}
	
}
