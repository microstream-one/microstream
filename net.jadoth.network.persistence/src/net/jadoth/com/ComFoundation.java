package net.jadoth.com;

import java.net.InetSocketAddress;
import java.nio.ByteOrder;

import net.jadoth.exceptions.MissingFoundationPartException;
import net.jadoth.persistence.types.PersistenceFoundation;
import net.jadoth.persistence.types.PersistenceTypeDictionaryView;
import net.jadoth.persistence.types.PersistenceTypeDictionaryViewProvider;
import net.jadoth.swizzling.types.SwizzleIdStrategy;
import net.jadoth.util.InstanceDispatcher;

public interface ComFoundation<C, F extends ComFoundation<C, ?>>
{
	public String getProtocolName();
	
	public String getProtocolVersion();
	
	public ByteOrder getByteOrder();
	
	public SwizzleIdStrategy getClientIdStrategy();
	
	public PersistenceTypeDictionaryView getTypeDictionary();
	
	public PersistenceTypeDictionaryViewProvider getTypeDictionaryProvider();
	
	public ComProtocolProvider getProtocolProvider();
	
	public ComProtocolProviderCreator getProtocolProviderCreator();
	
	public ComProtocolCreator getProtocolCreator();
	
	public ComProtocolSender<C> getProtocolSender();
	
	
	public InetSocketAddress getAddress();
	
	public ComProtocolStringConverter getProtocolStringConverter();
	
	public ComHostCreator<C> getHostCreator();
	
	public ComConnectionListenerCreator<C> getConnectionListenerCreator();
	
	public ComConnectionAcceptorCreator<C> getConnectionAcceptorCreator();
	
	public ComChannelCreator<C> getChannelCreator();
	
	public ComChannelAcceptor getChannelAcceptor();
	
	public PersistenceFoundation<?, ?> getPersistenceFoundation();
	
	
	
	public F setProtocolName(String protocolName);
	
	public F setProtocolVersion(String protocolVersion);
	
	public F setByteOrder(ByteOrder byteOrder);
	
	public F setClientIdStrategy(SwizzleIdStrategy idStrategy);
	
	public F setTypeDictionary(PersistenceTypeDictionaryView typeDictionary);
	
	public F setTypeDictionaryProvider(PersistenceTypeDictionaryViewProvider typeDictionaryProvider);

	public F setProtocolCreator(ComProtocolCreator protocolCreator);
	
	public F setProtocolProvider(ComProtocolProvider protocolProvider);
	
	public F setProtocolProviderCreator(ComProtocolProviderCreator protocolProviderCreator);
	
	public F setProtocolSender(ComProtocolSender<C> protocolSender);
	
	
	public F setAddress(InetSocketAddress address);
	
	public F setProtocolStringConverter(ComProtocolStringConverter protocolStringConverter);
	
	public F setHostCreator(ComHostCreator<C> hostCreator);

	public F setConnectionListenerCreator(ComConnectionListenerCreator<C> connectionListenerCreator);
	
	public F setConnectionAcceptorCreator(ComConnectionAcceptorCreator<C> connectionAcceptorCreator);
	
	public F setChannelCreator(ComChannelCreator<C> channelCreator);
	
	public F setChannelAcceptor(ComChannelAcceptor channelAcceptor);

	public F setPersistenceFoundation(PersistenceFoundation<?, ?> persistenceFoundation);
	
	
	public ComHost<C> createHost();
	
	
	
	public static <C> ComFoundation<C, ?> New()
	{
		return new ComFoundation.Implementation<>();
	}
	
	public class Implementation<C, F extends ComFoundation.Implementation<C, ?>>
	extends InstanceDispatcher.Implementation implements ComFoundation<C, F>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private String                                protocolName             ;
		private String                                protocolVersion          ;
		private ByteOrder                             byteOrder                ;
		private SwizzleIdStrategy                     clientIdStrategy               ;
		private PersistenceTypeDictionaryView         typeDictionary           ;
		private PersistenceTypeDictionaryViewProvider typeDictionaryProvider   ;
		private ComProtocolCreator                    protocolCreator          ;
		private ComProtocolProvider                   protocolProvider         ;
		private ComProtocolProviderCreator            protocolProviderCreator  ;
                                                      
		private InetSocketAddress                     address                  ;
		private ComProtocolStringConverter            protocolStringConverter  ;
		private ComHostCreator<C>                     hostCreator              ;
		private ComConnectionListenerCreator<C>       connectionListenerCreator;
		private ComConnectionAcceptorCreator<C>       connectionAcceptorCreator;
		private ComProtocolSender<C>                  protocolSender           ;
		private ComChannelCreator<C>                  channelCreator           ;
		private ComChannelAcceptor                    channelAcceptor          ;
		
		private PersistenceFoundation<?, ?>           persistenceFoundation    ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@SuppressWarnings("unchecked") // magic self-type.
		protected final F $()
		{
			return (F)this;
		}
		
		@Override
		public PersistenceTypeDictionaryView getTypeDictionary()
		{
			if(this.typeDictionary == null)
			{
				this.typeDictionary = this.ensureTypeDictionary();
			}
			
			return this.typeDictionary;
		}
		
		@Override
		public PersistenceTypeDictionaryViewProvider getTypeDictionaryProvider()
		{
			if(this.typeDictionaryProvider == null)
			{
				this.typeDictionaryProvider = this.ensureTypeDictionaryProvider();
			}
			
			return this.typeDictionaryProvider;
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
		public InetSocketAddress getAddress()
		{
			if(this.address == null)
			{
				this.address = this.ensureAddress();
			}

			return this.address;
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
		
		@Override
		public ComChannelAcceptor getChannelAcceptor()
		{
			if(this.channelAcceptor == null)
			{
				this.channelAcceptor = this.ensureChannelAcceptor();
			}
			
			return this.channelAcceptor;
		}
		
		@Override
		public PersistenceFoundation<?, ?> getPersistenceFoundation()
		{
			if(this.persistenceFoundation == null)
			{
				this.persistenceFoundation = this.ensurePersistenceFoundation();
			}
			
			return this.persistenceFoundation;
		}

		/*
		 * "ensure" methods guarantee that a non-null/non-zero value is returned.
		 * Either by returning an existing one (e.g. a constant) or by creating a new instance of the specified type.
		 * If both options are not possible, the method will throw a MissingFoundationPartException.
		 */

		public String ensureProtocolName()
		{
			return ComProtocol.protocolName();
		}

		public String ensureProtocolVersion()
		{
			return ComProtocol.protocolVersion();
		}
		
		public ByteOrder ensureByteOrder()
		{
			return Com.byteOrder();
		}

		public SwizzleIdStrategy ensureClientIdStrategy()
		{
			return Com.DefaultIdStrategyClient();
		}
		
		public ComProtocolCreator ensureProtocolCreator()
		{
			return ComProtocol.Creator();
		}
		
		public ComProtocolProviderCreator ensureProtocolProviderCreator()
		{
			return ComProtocolProviderCreator.New();
		}

		public ComHostCreator<C> ensureHostCreator()
		{
			return ComHost.Creator();
		}
		
		public ComConnectionAcceptorCreator<C> ensureConnectionAcceptorCreator()
		{
			return ComConnectionAcceptor.Creator();
		}
		
		public PersistenceTypeDictionaryView ensureTypeDictionary()
		{
			// the type dictionary initialization might be deferred beyond infrastructure initialization
			final PersistenceTypeDictionaryViewProvider tdProvider = this.getTypeDictionaryProvider();
			return tdProvider.provideTypeDictionary();
		}

		public ComProtocolProvider ensureProtocolProvider()
		{
			final ComProtocolProviderCreator providerCreator = this.getProtocolProviderCreator();
			
			return providerCreator.creatProtocolProvider(
				this.getProtocolName()    ,
				this.getProtocolVersion() ,
				this.getByteOrder()       ,
				this.getClientIdStrategy(),
				this.getTypeDictionary()  ,
				this.getProtocolCreator()
			);
		}
		
		public ComProtocolStringConverter ensureProtocolStringConverter()
		{
			final PersistenceFoundation<?, ?> pf = this.getPersistenceFoundation();
			
			return ComProtocolStringConverter.New(
				pf.getTypeDictionaryCompiler()
			);
		}
		
		public InetSocketAddress ensureAddress()
		{
			throw new MissingFoundationPartException(InetSocketAddress.class);
		}
		
		public PersistenceTypeDictionaryViewProvider ensureTypeDictionaryProvider()
		{
			// ultimately, the type dictionary must be supplied from the application context and cannot be created here.
			throw new MissingFoundationPartException(PersistenceTypeDictionaryView.class);
		}
		
		public ComConnectionListenerCreator<C> ensureConnectionListenerCreator()
		{
			throw new MissingFoundationPartException(ComConnectionListenerCreator.class);
		}
		
		public ComChannelCreator<C> ensureChannelCreator()
		{
			throw new MissingFoundationPartException(ComChannelCreator.class);
		}
		
		public ComProtocolSender<C> ensureProtocolSender()
		{
			throw new MissingFoundationPartException(ComProtocolSender.class);
		}
		
		public PersistenceFoundation<?, ?> ensurePersistenceFoundation()
		{
			throw new MissingFoundationPartException(PersistenceFoundation.class);
		}

		public ComChannelAcceptor ensureChannelAcceptor()
		{
			// the channel acceptor is the link to the application / framework logic and cannot be created here.
			throw new MissingFoundationPartException(ComChannelAcceptor.class);
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
		public F setTypeDictionary(final PersistenceTypeDictionaryView typeDictionary)
		{
			this.typeDictionary = typeDictionary;
			return this.$();
		}
		
		@Override
		public F setTypeDictionaryProvider(final PersistenceTypeDictionaryViewProvider typeDictionaryProvider)
		{
			this.typeDictionaryProvider = typeDictionaryProvider;
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
		public F setAddress(final InetSocketAddress address)
		{
			this.address = address;
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
		
		@Override
		public F setChannelAcceptor(final ComChannelAcceptor channelAcceptor)
		{
			this.channelAcceptor = channelAcceptor;
			return this.$();
		}
		
		@Override
		public F setPersistenceFoundation(final PersistenceFoundation<?, ?> persistenceFoundation)
		{
			this.persistenceFoundation = persistenceFoundation;
			return this.$();
		}
		
		@Override
		public ComHost<C> createHost()
		{
			final ComConnectionAcceptorCreator<C> conAccCreator = this.getConnectionAcceptorCreator();
			final ComConnectionAcceptor<C> connectionAcceptor = conAccCreator.createConnectionAcceptor(
				this.getProtocolProvider()       ,
				this.getProtocolStringConverter(),
				this.getProtocolSender()         ,
				this.getChannelCreator()         ,
				this.getChannelAcceptor()
			);

			final ComHostCreator<C>  hostCreator = this.getHostCreator();
			return hostCreator.createComHost(
				this.getAddress()                  ,
				this.getConnectionListenerCreator(),
				connectionAcceptor
			);
		}
		
	}
	
}
