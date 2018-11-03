package net.jadoth.network.persistence;

import java.nio.ByteOrder;

import net.jadoth.exceptions.MissingFoundationPartException;
import net.jadoth.persistence.types.PersistenceTypeDictionaryView;
import net.jadoth.persistence.types.PersistenceTypeDictionaryViewProvider;
import net.jadoth.swizzling.types.SwizzleIdStrategy;
import net.jadoth.util.InstanceDispatcher;

public interface ComFoundation<F extends ComFoundation<?>>
{
	public String getProtocolName();
	
	public String getProtocolVersion();
	
	public ByteOrder getByteOrder();
	
	public SwizzleIdStrategy getIdStrategy();
	
	public PersistenceTypeDictionaryView getTypeDictionary();
	
	public PersistenceTypeDictionaryViewProvider getTypeDictionaryProvider();
	
	public ComProtocol getProtocol();
	
	public ComProtocol.Creator getProtocolCreator();
	
	
	public int getComPort();
	
	public ComProtocolStringConverter getProtocolStringConverter();
	
	public ComHost.Creator getHostCreator();
	
	public ComConnectionAcceptor.Creator getConnectionAcceptorCreator();
	
	public ComChannel.Creator getChannelCreator();
	
	public ComChannelAcceptor getChannelAcceptor();
	
	
	
	public F setProtocolName(String protocolName);
	
	public F setProtocolVersion(String protocolVersion);
	
	public F setByteOrder(ByteOrder byteOrder);
	
	public F setIdStrategy(SwizzleIdStrategy idStrategy);
	
	public F setTypeDictionary(PersistenceTypeDictionaryView typeDictionary);
	
	public F setTypeDictionaryProvider(PersistenceTypeDictionaryViewProvider typeDictionaryProvider);
	
	public F setProtocolCreator(ComProtocol.Creator protocolCreator);
	
	
	public F setComPort(int comPort);
	
	public F setProtocolStringConverter(ComProtocolStringConverter protocolStringConverter);
	
	public F setHostCreator(ComHost.Creator hostCreator);
	
	public F setConnectionAcceptorCreator(ComConnectionAcceptor.Creator connectionAcceptorCreator);
	
	public F setChannelCreator(ComChannel.Creator channelCreator);
	
	public F setChannelAcceptor(ComChannelAcceptor channelAcceptor);
	
	
	public ComHost createHost();
	
	
	
	
	public class Implementation<F extends ComFoundation.Implementation<?>>
	extends InstanceDispatcher.Implementation implements ComFoundation<F>
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
		private ComProtocol.Creator                   protocolCreator          ;
		private transient ComProtocol                 cachedProtocol           ;
                                                      
		private int                                   comPort                  ;
		private ComProtocolStringConverter            protocolStringConverter  ;
		private ComHost.Creator                       hostCreator              ;
		private ComConnectionAcceptor.Creator         connectionAcceptorCreator;
		private ComChannel.Creator                    channelCreator           ;
		private ComChannelAcceptor                    channelAcceptor          ;
		
		
		
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
		public ComProtocol.Creator getProtocolCreator()
		{
			if(this.protocolCreator == null)
			{
				this.protocolCreator = this.createProtocolCreator();
				this.clearCachedProtocol();
			}
			
			return this.protocolCreator;
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
		public ComHost.Creator getHostCreator()
		{
			if(this.hostCreator == null)
			{
				this.hostCreator = this.createHostCreator();
			}
			
			return this.hostCreator;
		}
		
		@Override
		public ComConnectionAcceptor.Creator getConnectionAcceptorCreator()
		{
			if(this.connectionAcceptorCreator == null)
			{
				this.connectionAcceptorCreator = this.createConnectionAcceptorCreator();
			}
			
			return this.connectionAcceptorCreator;
		}
		
		@Override
		public ComChannel.Creator getChannelCreator()
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
		
		public ComProtocol.Creator createProtocolCreator()
		{
			return ComProtocol.Creator();
		}

		public ComProtocol createProtocol()
		{
			final ComProtocol.Creator protocolCreator = this.getProtocolCreator();
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
			return ComProtocolStringConverter.New();
		}

		public ComHost.Creator createHostCreator()
		{
			return ComHost.Creator();
		}
		
		public ComConnectionAcceptor.Creator createConnectionAcceptorCreator()
		{
			return ComConnectionAcceptor.Creator(
				this.getProtocolStringConverter()
			);
		}
		
		public ComChannel.Creator createChannelCreator()
		{
			return ComChannel.Creator();
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
		public F setProtocolCreator(final ComProtocol.Creator protocolCreator)
		{
			this.protocolCreator = protocolCreator;
			this.clearCachedProtocol();
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
		public F setHostCreator(final ComHost.Creator hostCreator)
		{
			this.hostCreator = hostCreator;
			return this.$();
		}
		
		@Override
		public F setConnectionAcceptorCreator(final ComConnectionAcceptor.Creator connectionAcceptorCreator)
		{
			this.connectionAcceptorCreator = connectionAcceptorCreator;
			return this.$();
		}
		
		@Override
		public F setChannelCreator(final ComChannel.Creator channelCreator)
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
		public ComHost createHost()
		{
			final ComConnectionAcceptor.Creator conAccCreator = this.getConnectionAcceptorCreator();
			final ComHost.Creator               hostCreator   = this.getHostCreator();
			
			final ComConnectionAcceptor connectionAcceptor = conAccCreator.createConnectionAcceptor(
				this.getProtocol(),
				this.getChannelCreator(),
				this.getChannelAcceptor()
			);
			
			return hostCreator.createComHost(
				this.getComPort() ,
				connectionAcceptor
			);
		}
		
	}
	
}
