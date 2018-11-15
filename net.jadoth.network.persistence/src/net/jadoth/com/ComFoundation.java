package net.jadoth.com;

import java.net.InetSocketAddress;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;

import net.jadoth.exceptions.MissingFoundationPartException;
import net.jadoth.persistence.types.PersistenceFoundation;
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
	
	public ComHostChannelCreator<C> getChannelCreator();

	
	public ComHostContext<C> getHostContext();
	
	
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
	
	public F setChannelCreator(ComHostChannelCreator<C> channelCreator);
	
	public F setHostContext(ComHostContext<C> hostContext);
	
	public F setHostContext(
		InetSocketAddress           socketAddress        ,
		ComChannelAcceptor          channelAcceptor      ,
		PersistenceFoundation<?, ?> persistenceFoundation
	);
	
	
	public ComHost<C> createHost();
	
	
	
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
		private ComHostChannelCreator<C>        hostChannelCreator       ;
		                                        
		private ComHostContext<C>               hostContext              ;

		
		
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
		public ComHostCreator<C> getHostCreator()
		{
			if(this.hostCreator == null)
			{
				this.hostCreator = this.ensureHostCreator();
			}
			
			return this.hostCreator;
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
		
		@Override
		public ComHostChannelCreator<C> getChannelCreator()
		{
			if(this.hostChannelCreator == null)
			{
				this.hostChannelCreator = this.ensureChannelCreator();
			}
			
			return this.hostChannelCreator;
		}
		
		@Override
		public ComHostContext<C> getHostContext()
		{
			if(this.hostContext == null)
			{
				this.hostContext = this.ensureHostContext();
			}
			
			return this.hostContext;
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
		
		protected ComConnectionAcceptorCreator<C> ensureConnectionAcceptorCreator()
		{
			return ComConnectionAcceptor.Creator();
		}

		protected ComProtocolProvider ensureProtocolProvider()
		{
			final ComProtocolProviderCreator providerCreator = this.getProtocolProviderCreator();
			
			return providerCreator.creatProtocolProvider(
				this.getProtocolName()          ,
				this.getProtocolVersion()       ,
				this.getByteOrder()             ,
				this.getClientIdStrategy()      ,
				this.providePersistenceAdaptor(),
				this.getProtocolCreator()
			);
		}
		
		protected ComProtocolStringConverter ensureProtocolStringConverter()
		{
			final ComPersistenceAdaptor<C> adaptor = this.providePersistenceAdaptor();
			
			return ComProtocolStringConverter.New(
				adaptor.provideTypeDictionaryCompiler()
			);
		}
		
		protected ComHostChannelCreator<C> ensureChannelCreator()
		{
			return ComHostChannelCreator.New(
				this.providePersistenceAdaptor()
			);
		}
				
		protected ComHostContext<C> ensureHostContext()
		{
			// this is the link to the application / framework context and cannot be created here.
			throw new MissingFoundationPartException(ComHostContext.class);
		}
				
		protected ComConnectionHandler<C> ensureConnectionHandler()
		{
			// must be created or set specific to <C>.
			throw new MissingFoundationPartException(ComConnectionHandler.class);
		}
		
		protected ComPersistenceAdaptor<C> providePersistenceAdaptor()
		{
			return this.getHostContext().providePersistenceAdaptor();
		}
		
		protected ComChannelAcceptor provideChannelAcceptor()
		{
			return this.getHostContext().provideChannelAcceptor();
		}
		
		protected InetSocketAddress provideSocktAddress()
		{
			return this.getHostContext().provideAddress();
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
		public F setChannelCreator(final ComHostChannelCreator<C> channelCreator)
		{
			this.hostChannelCreator = channelCreator;
			return this.$();
		}
				
		@Override
		public F setHostContext(final ComHostContext<C> hostContext)
		{
			this.hostContext = hostContext;
			return this.$();
		}
		
		@Override
		public F setHostContext(
			final InetSocketAddress           socketAddress        ,
			final ComChannelAcceptor          channelAcceptor      ,
			final PersistenceFoundation<?, ?> persistenceFoundation
		)
		{
			final ComHostContext.Builder<C> hostContextBuilder = this.getConnectionHandler().createHostContextBuilder();
			
			this.setHostContext(hostContextBuilder
				.setAddress        (socketAddress)
				.setChannelAcceptor(channelAcceptor)
				.setPersistence    (persistenceFoundation)
				.buildHostContext()
			);
			
			return this.$();
		}
		
		@Override
		public ComHost<C> createHost()
		{
			final ComConnectionAcceptorCreator<C> conAccCreator = this.getConnectionAcceptorCreator();
			final ComConnectionAcceptor<C> connectionAcceptor = conAccCreator.createConnectionAcceptor(
				this.getProtocolProvider()       ,
				this.getConnectionHandler()      ,
				this.getProtocolStringConverter(),
				this.getChannelCreator()         ,
				this.provideChannelAcceptor()
			);

			final ComHostCreator<C>  hostCreator = this.getHostCreator();
			return hostCreator.createComHost(
				this.provideSocktAddress() ,
				this.getConnectionHandler(),
				connectionAcceptor
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
