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
	
	public ComProtocolSender<C> getProtocolSender();
	
	
	public ComProtocolStringConverter getProtocolStringConverter();
	
	public ComHostCreator<C> getHostCreator();
	
	public ComConnectionListenerCreator<C> getConnectionListenerCreator();
	
	public ComConnectionAcceptorCreator<C> getConnectionAcceptorCreator();
	
	public ComChannelCreator<C> getChannelCreator();

	
//	public InetSocketAddress getAddress();
	
//	public ComChannelAcceptor getChannelAcceptor();
	
//	public ComPersistenceAdaptor<C> getPersistenceAdaptor();
	
	public ComHostContext<C> getHostContext();
	
	
	public F setProtocolName(String protocolName);
	
	public F setProtocolVersion(String protocolVersion);
	
	public F setByteOrder(ByteOrder byteOrder);
	
	public F setClientIdStrategy(SwizzleIdStrategy idStrategy);
	
	public F setProtocolCreator(ComProtocolCreator protocolCreator);
	
	public F setProtocolProvider(ComProtocolProvider protocolProvider);
	
	public F setProtocolProviderCreator(ComProtocolProviderCreator protocolProviderCreator);
	
	public F setProtocolSender(ComProtocolSender<C> protocolSender);
	
	
	public F setProtocolStringConverter(ComProtocolStringConverter protocolStringConverter);
	
	public F setHostCreator(ComHostCreator<C> hostCreator);

	public F setConnectionListenerCreator(ComConnectionListenerCreator<C> connectionListenerCreator);
	
	public F setConnectionAcceptorCreator(ComConnectionAcceptorCreator<C> connectionAcceptorCreator);
	
	public F setChannelCreator(ComChannelCreator<C> channelCreator);

	
//	public F setAddress(InetSocketAddress address);
	
//	public F setChannelAcceptor(ComChannelAcceptor channelAcceptor);

//	public F setPersistenceAdaptor(ComPersistenceAdaptor<C> persistenceAdaptor);
	
	public F setHostContext(ComHostContext<C> hostContext);
	
	
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

		private String                                protocolName             ;
		private String                                protocolVersion          ;
		private ByteOrder                             byteOrder                ;
		private SwizzleIdStrategy                     clientIdStrategy         ;
		private ComProtocolCreator                    protocolCreator          ;
		private ComProtocolProvider                   protocolProvider         ;
		private ComProtocolProviderCreator            protocolProviderCreator  ;
                                                      
		private ComProtocolStringConverter            protocolStringConverter  ;
		private ComHostCreator<C>                     hostCreator              ;
		private ComConnectionListenerCreator<C>       connectionListenerCreator;
		private ComConnectionAcceptorCreator<C>       connectionAcceptorCreator;
		private ComProtocolSender<C>                  protocolSender           ;
		private ComChannelCreator<C>                  channelCreator           ;
		
		// (13.11.2018 TM)TODO: clean up single parts if really not needed
//		private InetSocketAddress                     address                  ;
//		private ComChannelAcceptor                    channelAcceptor          ;
//		private ComPersistenceAdaptor<C>              persistenceAdaptor       ;
		private ComHostContext<C>                     hostContext              ;

		
		
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
		public ComProtocolSender<C> getProtocolSender()
		{
			if(this.protocolSender == null)
			{
				this.protocolSender = this.ensureProtocolSender();
			}
			
			return this.protocolSender;
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
		public ComConnectionListenerCreator<C> getConnectionListenerCreator()
		{
			if(this.connectionListenerCreator == null)
			{
				this.connectionListenerCreator = this.ensureConnectionListenerCreator();
			}
			
			return this.connectionListenerCreator;
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
		public ComChannelCreator<C> getChannelCreator()
		{
			if(this.channelCreator == null)
			{
				this.channelCreator = this.ensureChannelCreator();
			}
			
			return this.channelCreator;
		}
		
//		@Override
//		public InetSocketAddress getAddress()
//		{
//			if(this.address == null)
//			{
//				this.address = this.ensureAddress();
//			}
//
//			return this.address;
//		}
//
//		@Override
//		public ComChannelAcceptor getChannelAcceptor()
//		{
//			if(this.channelAcceptor == null)
//			{
//				this.channelAcceptor = this.ensureChannelAcceptor();
//			}
//
//			return this.channelAcceptor;
//		}
//
//		@Override
//		public ComPersistenceAdaptor<C> getPersistenceAdaptor()
//		{
//			if(this.persistenceAdaptor == null)
//			{
//				this.persistenceAdaptor = this.ensurePersistenceAdaptor();
//			}
//
//			return this.persistenceAdaptor;
//		}
		
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
		
		protected ComChannelCreator<C> ensureChannelCreator()
		{
			return ComChannelCreator.New(
				this.providePersistenceAdaptor()
			);
		}
				
//		protected InetSocketAddress ensureAddress()
//		{
//			// the address to be used is application-specific and cannot be defined here.
//			throw new MissingFoundationPartException(InetSocketAddress.class);
//		}
//
//		protected ComChannelAcceptor ensureChannelAcceptor()
//		{
//			// the channel acceptor is the link to the application / framework logic and cannot be created here.
//			throw new MissingFoundationPartException(ComChannelAcceptor.class);
//		}
//
//		protected ComPersistenceAdaptor<C> ensurePersistenceAdaptor()
//		{
//			// the p.adaptor is the link to the application / framework persistence context and cannot be created here.
//			throw new MissingFoundationPartException(ComPersistenceAdaptor.class);
//		}
				
		protected ComHostContext<C> ensureHostContext()
		{
			// this is the link to the application / framework context and cannot be created here.
			throw new MissingFoundationPartException(ComHostContext.class);
		}
				
		protected ComConnectionListenerCreator<C> ensureConnectionListenerCreator()
		{
			// must be created or set specific to C.
			throw new MissingFoundationPartException(ComConnectionListenerCreator.class);
		}
		
		protected ComProtocolSender<C> ensureProtocolSender()
		{
			// must be created or set specific to C.
			throw new MissingFoundationPartException(ComProtocolSender.class);
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
		public F setProtocolSender(final ComProtocolSender<C> protocolSender)
		{
			this.protocolSender = protocolSender;
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
		public F setConnectionListenerCreator(final ComConnectionListenerCreator<C> connectionListenerCreator)
		{
			this.connectionListenerCreator = connectionListenerCreator;
			return this.$();
		}
		
		@Override
		public F setChannelCreator(final ComChannelCreator<C> channelCreator)
		{
			this.channelCreator = channelCreator;
			return this.$();
		}
		
//		@Override
//		public F setAddress(final InetSocketAddress address)
//		{
//			this.address = address;
//			return this.$();
//		}
//
//		@Override
//		public F setChannelAcceptor(final ComChannelAcceptor channelAcceptor)
//		{
//			this.channelAcceptor = channelAcceptor;
//			return this.$();
//		}
//
//		@Override
//		public F setPersistenceAdaptor(final ComPersistenceAdaptor<C> persistenceAdaptor)
//		{
//			this.persistenceAdaptor = persistenceAdaptor;
//			return this.$();
//		}
		
		@Override
		public F setHostContext(final ComHostContext<C> hostContext)
		{
			this.hostContext = hostContext;
			return this.$();
		}
		
		@Override
		public ComHost<C> createHost()
		{
			final ComConnectionAcceptorCreator<C> conAccCreator = this.getConnectionAcceptorCreator();
			final ComConnectionAcceptor<C> connectionAcceptor = conAccCreator.createConnectionAcceptor(
				this.getProtocolProvider()   ,
				this.getProtocolSender()     ,
				this.getChannelCreator()     ,
				this.provideChannelAcceptor()
			);

			final ComHostCreator<C>  hostCreator = this.getHostCreator();
			return hostCreator.createComHost(
				this.provideSocktAddress()         ,
				this.getConnectionListenerCreator(),
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
		public ComProtocolSender.Default ensureProtocolSender()
		{
			return ComProtocolSender.New(
				this.getProtocolStringConverter()
			);
		}
		
		@Override
		public ComConnectionListenerCreator.Default ensureConnectionListenerCreator()
		{
			return ComConnectionListenerCreator.New();
		}
		
	}
	
}
