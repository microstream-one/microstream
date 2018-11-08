package net.jadoth.com;

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
	
	public SwizzleIdStrategy getIdStrategy();
	
	public PersistenceTypeDictionaryView getTypeDictionary();
	
	public PersistenceTypeDictionaryViewProvider getTypeDictionaryProvider();
	
//	public PersistenceTypeDictionaryCompiler getTypeDictionaryCompiler();
	
	public ComProtocol getProtocol();
	
	public ComProtocolCreator getProtocolCreator();
	
	public ComProtocolSender<C> getProtocolSender();
	
	
	public int getComPort();
	
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
	
	public F setIdStrategy(SwizzleIdStrategy idStrategy);
	
	public F setTypeDictionary(PersistenceTypeDictionaryView typeDictionary);
	
	public F setTypeDictionaryProvider(PersistenceTypeDictionaryViewProvider typeDictionaryProvider);

//	public F setTypeDictionaryCompiler(PersistenceTypeDictionaryCompiler typeDictionaryCompiler);
	
	public F setProtocolCreator(ComProtocolCreator protocolCreator);
	
	public F setProtocolSender(ComProtocolSender<C> protocolSender);
	
	
	public F setComPort(int comPort);
	
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
		private SwizzleIdStrategy                     idStrategy               ;
		private PersistenceTypeDictionaryView         typeDictionary           ;
		private PersistenceTypeDictionaryViewProvider typeDictionaryProvider   ;
		private ComProtocolCreator                    protocolCreator          ;
		private ComProtocolSender<C>                  protocolSender           ;
		private transient ComProtocol                 cachedProtocol           ;
                                                      
		private int                                   comPort                  ;
		private ComProtocolStringConverter            protocolStringConverter  ;
		private ComHostCreator<C>                     hostCreator              ;
		private ComConnectionListenerCreator<C>       connectionListenerCreator;
		private ComConnectionAcceptorCreator<C>       connectionAcceptorCreator;
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
				this.byteOrder = this.defineByteOrder();
			}
			
			return this.byteOrder;
		}
		
		@Override
		public String getProtocolVersion()
		{
			if(this.protocolVersion == null)
			{
				this.protocolVersion = this.defineProtocolVersion();
			}
			
			return this.protocolVersion;
		}
		
		@Override
		public String getProtocolName()
		{
			if(this.protocolName == null)
			{
				this.protocolName = this.defineProtocolName();
			}
			
			return this.protocolName;
		}
		
		@Override
		public SwizzleIdStrategy getIdStrategy()
		{
			if(this.idStrategy == null)
			{
				this.idStrategy = this.ensureIdStrategy();
			}
			
			return this.idStrategy;
		}
		
		@Override
		public int getComPort()
		{
			if(this.comPort <= 0)
			{
				this.comPort = this.defineComPort();
			}
			
			return this.comPort;
		}
		
		@Override
		public ComProtocol getProtocol()
		{
			if(this.cachedProtocol == null)
			{
				this.cachedProtocol = this.createProtocol();
			}
			
			return this.cachedProtocol;
		}
		
		@Override
		public ComProtocolCreator getProtocolCreator()
		{
			if(this.protocolCreator == null)
			{
				this.protocolCreator = this.createProtocolCreator();
				this.clearCachedProtocol();
			}
			
			return this.protocolCreator;
		}
		
		@Override
		public ComProtocolSender<C> getProtocolSender()
		{
			if(this.protocolSender == null)
			{
				this.protocolSender = this.createProtocolSender();
			}
			
			return this.protocolSender;
		}
		
		@Override
		public ComProtocolStringConverter getProtocolStringConverter()
		{
			if(this.protocolStringConverter == null)
			{
				this.protocolStringConverter = this.createProtocolStringConverter();
			}
			
			return this.protocolStringConverter;
		}
		
		@Override
		public ComHostCreator<C> getHostCreator()
		{
			if(this.hostCreator == null)
			{
				this.hostCreator = this.createHostCreator();
			}
			
			return this.hostCreator;
		}
		
		@Override
		public ComConnectionListenerCreator<C> getConnectionListenerCreator()
		{
			if(this.connectionListenerCreator == null)
			{
				this.connectionListenerCreator = this.createConnectionListenerCreator();
			}
			
			return this.connectionListenerCreator;
		}
		
		@Override
		public ComConnectionAcceptorCreator<C> getConnectionAcceptorCreator()
		{
			if(this.connectionAcceptorCreator == null)
			{
				this.connectionAcceptorCreator = this.createConnectionAcceptorCreator();
			}
			
			return this.connectionAcceptorCreator;
		}
		
		@Override
		public ComChannelCreator<C> getChannelCreator()
		{
			if(this.channelCreator == null)
			{
				this.channelCreator = this.createChannelCreator();
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
				this.persistenceFoundation = this.createPersistenceFoundation();
			}
			
			return this.persistenceFoundation;
		}

		
		
		public int defineComPort()
		{
			return Com.defaultPort();
		}

		public ByteOrder defineByteOrder()
		{
			return Com.byteOrder();
		}

		public String defineProtocolVersion()
		{
			return ComProtocol.protocolVersion();
		}

		public String defineProtocolName()
		{
			return ComProtocol.protocolName();
		}
		
		public ComProtocolCreator createProtocolCreator()
		{
			return ComProtocol.Creator();
		}

		public ComProtocol createProtocol()
		{
			final ComProtocolCreator protocolCreator = this.getProtocolCreator();
			return protocolCreator.creatProtocol(
				this.getProtocolName()   ,
				this.getProtocolVersion(),
				this.getByteOrder()      ,
				this.getIdStrategy()     ,
				this.getTypeDictionary()
			);
		}
		
		public ComProtocolStringConverter createProtocolStringConverter()
		{
			final PersistenceFoundation<?, ?> pf = this.getPersistenceFoundation();
			
			return ComProtocolStringConverter.New(
				pf.getTypeDictionaryCompiler()
			);
		}

		public ComHostCreator<C> createHostCreator()
		{
			return ComHost.Creator();
		}
		
		public ComConnectionListenerCreator<C> createConnectionListenerCreator()
		{
			throw new MissingFoundationPartException(ComConnectionListenerCreator.class);
		}
		
		public ComConnectionAcceptorCreator<C> createConnectionAcceptorCreator()
		{
			return ComConnectionAcceptor.Creator(
				this.getProtocolStringConverter()
			);
		}
		
		public ComChannelCreator<C> createChannelCreator()
		{
			throw new MissingFoundationPartException(ComChannelCreator.class);
		}
		
		public ComProtocolSender<C> createProtocolSender()
		{
			throw new MissingFoundationPartException(ComProtocolSender.class);
		}
		
		public PersistenceFoundation<?, ?> createPersistenceFoundation()
		{
			throw new MissingFoundationPartException(PersistenceFoundation.class);
		}
		
		public PersistenceTypeDictionaryView ensureTypeDictionary()
		{
			// the type dictionary initialization might be deferred beyond infrastructure initialization
			final PersistenceTypeDictionaryViewProvider tdProvider = this.getTypeDictionaryProvider();
			return tdProvider.provideTypeDictionary();
		}

		public SwizzleIdStrategy ensureIdStrategy()
		{
			// (01.11.2018 TM)TODO: JET-43: really exception? Maybe default transient strategy?
			// (08.11.2018 TM)NOTE: Maybe server or client depending on wether a InetAddress is set?
			throw new MissingFoundationPartException(SwizzleIdStrategy.class);
		}
		
		public PersistenceTypeDictionaryViewProvider ensureTypeDictionaryProvider()
		{
			// ultimately, the type dictionary must be supplied from the application context and cannot be created here.
			throw new MissingFoundationPartException(PersistenceTypeDictionaryView.class);
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
			this.clearCachedProtocol();
			return this.$();
		}
		
		@Override
		public F setProtocolVersion(final String protocolVersion)
		{
			this.protocolVersion = protocolVersion;
			this.clearCachedProtocol();
			return this.$();
		}
		
		@Override
		public F setByteOrder(final ByteOrder byteOrder)
		{
			this.byteOrder = byteOrder;
			this.clearCachedProtocol();
			return this.$();
		}
		
		@Override
		public F setIdStrategy(final SwizzleIdStrategy idStrategy)
		{
			this.idStrategy = idStrategy;
			this.clearCachedProtocol();
			return this.$();
		}
		
		@Override
		public F setTypeDictionary(final PersistenceTypeDictionaryView typeDictionary)
		{
			this.typeDictionary = typeDictionary;
			this.clearCachedProtocol();
			return this.$();
		}
		
		@Override
		public F setTypeDictionaryProvider(final PersistenceTypeDictionaryViewProvider typeDictionaryProvider)
		{
			this.typeDictionaryProvider = typeDictionaryProvider;
			this.clearCachedProtocol();
			return this.$();
		}
		
		@Override
		public F setProtocolCreator(final ComProtocolCreator protocolCreator)
		{
			this.protocolCreator = protocolCreator;
			this.clearCachedProtocol();
			return this.$();
		}
		
		@Override
		public F setProtocolSender(final ComProtocolSender<C> protocolSender)
		{
			this.protocolSender = protocolSender;
			return this.$();
		}
		
		private void clearCachedProtocol()
		{
			this.cachedProtocol = null;
		}
		
		@Override
		public F setComPort(final int comPort)
		{
			this.comPort = comPort;
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
			final ComConnectionListenerCreator<C> conLisCreator = this.getConnectionListenerCreator();
			final ComConnectionAcceptorCreator<C> conAccCreator = this.getConnectionAcceptorCreator();
			final ComHostCreator<C>               hostCreator   = this.getHostCreator();
			
			final ComConnectionAcceptor<C> connectionAcceptor = conAccCreator.createConnectionAcceptor(
				this.getProtocol()       ,
				this.getProtocolSender() ,
				this.getChannelCreator() ,
				this.getChannelAcceptor()
			);
			
			return hostCreator.createComHost(
				this.getComPort() ,
				conLisCreator     ,
				connectionAcceptor
			);
		}
		
	}
	
}
