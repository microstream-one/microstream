package net.jadoth.network.persistence;

import java.nio.ByteOrder;

import net.jadoth.exceptions.MissingFoundationPartException;
import net.jadoth.persistence.types.PersistenceTypeDictionaryView;
import net.jadoth.swizzling.types.SwizzleIdStrategy;
import net.jadoth.util.InstanceDispatcher;

public interface ComFoundation<F extends ComFoundation<?>>
{
	public static String protocolName()
	{
		return "JETSTREAM-COMCHANNEL";
	}
	
	public static String version()
	{
		// (31.10.2018 TM)TODO: JET-43: Maybe create a "Version" type with multiple sub version numbers?
		return "1.0";
	}
	
	public PersistenceTypeDictionaryView getTypeDictionary();
	
	public ByteOrder getByteOrder();
	
	public String getVersion();
	
	public String getProtocolName();
	
	public SwizzleIdStrategy getIdStrategy();
	
	public int getComPort();
	
	public ComConfiguration getConfiguration();
	
	public ComHost.Creator getHostCreator();
	
	public ComConnectionAcceptor.Creator getConnectionAcceptorCreator();
	
	public ComChannel.Creator getChannelCreator();
	
	public ComChannelAcceptor getChannelAcceptor();
	
	
	
	public F setTypeDictionary(PersistenceTypeDictionaryView typeDictionary);
	
	public F setByteOrder(ByteOrder byteOrder);
	
	public F setVersion(String version);
	
	public F setProtocolName(String protocolName);
	
	public F setIdStrategy(SwizzleIdStrategy idStrategy);
	
	public F setComPort(int comPort);
	
	public F setConfiguration(ComConfiguration configuration);
	
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
		
		private PersistenceTypeDictionaryView typeDictionary;
		private ByteOrder                     byteOrder     ;
		private String                        version       ;
		private String                        protocolName  ;
		private SwizzleIdStrategy             idStrategy    ;
		private int                           comPort       ;
		private ComConfiguration              configuration ;
		private ComHost.Creator               hostCreator   ;
		private ComConnectionAcceptor.Creator connectionAcceptorCreator;
		private ComChannel.Creator            channelCreator;
		private ComChannelAcceptor            channelAcceptor;
		
		
		
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
				this.typeDictionary = this.createTypeDictionary();
			}
			
			return this.typeDictionary;
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
		public String getVersion()
		{
			if(this.version == null)
			{
				this.version = this.defineVersion();
			}
			
			return this.version;
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
				this.idStrategy = this.createIdStrategy();
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
		public ComConfiguration getConfiguration()
		{
			if(this.configuration == null)
			{
				this.configuration = this.createConfiguration();
			}
			
			return this.configuration;
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
				this.channelAcceptor = this.createChannelAcceptor();
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

		public String defineVersion()
		{
			return ComFoundation.version();
		}

		public String defineProtocolName()
		{
			return ComFoundation.protocolName();
		}

		public ComConfiguration createConfiguration()
		{
			return ComConfiguration.New(
				this.getTypeDictionary(),
				this.getByteOrder()     ,
				this.getVersion()       ,
				this.getProtocolName()  ,
				this.getIdStrategy()
			);
		}

		public ComHost.Creator createHostCreator()
		{
			return ComHost.Creator();
		}
		
		public ComConnectionAcceptor.Creator createConnectionAcceptorCreator()
		{
			return ComConnectionAcceptor.Creator();
		}
		
		public ComChannel.Creator createChannelCreator()
		{
			return ComChannel.Creator();
		}
		
		public PersistenceTypeDictionaryView createTypeDictionary()
		{
			// (01.11.2018 TM)TODO: JET-43: really exception?
			throw new MissingFoundationPartException(PersistenceTypeDictionaryView.class);
		}

		public SwizzleIdStrategy createIdStrategy()
		{
			// (01.11.2018 TM)TODO: JET-43: really exception?
			throw new MissingFoundationPartException(SwizzleIdStrategy.class);
		}

		public ComChannelAcceptor createChannelAcceptor()
		{
			// (01.11.2018 TM)TODO: JET-43: really exception?
			throw new MissingFoundationPartException(ComChannelAcceptor.class);
		}
		
		
		
		@Override
		public F setTypeDictionary(final PersistenceTypeDictionaryView typeDictionary)
		{
			this.typeDictionary = typeDictionary;
			return this.$();
		}
		
		@Override
		public F setByteOrder(final ByteOrder byteOrder)
		{
			this.byteOrder = byteOrder;
			return this.$();
		}
		
		@Override
		public F setVersion(final String version)
		{
			this.version = version;
			return this.$();
		}
		
		@Override
		public F setProtocolName(final String protocolName)
		{
			this.protocolName = protocolName;
			return this.$();
		}
		
		@Override
		public F setIdStrategy(final SwizzleIdStrategy idStrategy)
		{
			this.idStrategy = idStrategy;
			return this.$();
		}
		
		@Override
		public F setComPort(final int comPort)
		{
			this.comPort = comPort;
			return this.$();
		}
		
		@Override
		public F setConfiguration(final ComConfiguration configuration)
		{
			this.configuration = configuration;
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
				this.getConfiguration(),
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
