package net.jadoth.network.persistence;

import java.nio.ByteOrder;

import net.jadoth.exceptions.MissingFoundationPartException;
import net.jadoth.network.persistence.ComProtocol.Assembler;
import net.jadoth.persistence.types.PersistenceTypeDictionaryView;
import net.jadoth.swizzling.types.SwizzleIdStrategy;
import net.jadoth.util.InstanceDispatcher;

public interface ComFoundation<F extends ComFoundation<?>>
{
	public int getComPort();
	
	public String getProtocolName();
	
	public String getProtocolVersion();
	
	public ByteOrder getByteOrder();
	
	public SwizzleIdStrategy getIdStrategy();
	
	public PersistenceTypeDictionaryView getTypeDictionary();
	
	public ComProtocol getProtocol();
	
	public ComProtocol.Assembler getProtocolAssembler();
	
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
	
	public F setProtocol(ComProtocol protocol);
	
	public F setProtocolAssembler(ComProtocol.Assembler protocolAssembler);
	
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
		
		private PersistenceTypeDictionaryView typeDictionary           ;
		private ByteOrder                     byteOrder                ;
		private String                        version                  ;
		private String                        protocolName             ;
		private SwizzleIdStrategy             idStrategy               ;
		private int                           comPort                  ;
		private ComProtocol                   protocol                 ;
		private ComProtocol.Assembler         protocolAssembler        ;
		private ComHost.Creator               hostCreator              ;
		private ComConnectionAcceptor.Creator connectionAcceptorCreator;
		private ComChannel.Creator            channelCreator           ;
		private ComChannelAcceptor            channelAcceptor          ;
		
		
		
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
		public String getProtocolVersion()
		{
			if(this.version == null)
			{
				this.version = this.defineProtocolVersion();
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
		public ComProtocol getProtocol()
		{
			if(this.protocol == null)
			{
				this.protocol = this.createProtocol();
			}
			
			return this.protocol;
		}
		
		@Override
		public ComProtocol.Assembler getProtocolAssembler()
		{
			if(this.protocolAssembler == null)
			{
				this.protocolAssembler = this.createProtocolAssembler();
			}
			
			return this.protocolAssembler;
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

		public String defineProtocolVersion()
		{
			return ComProtocol.protocolVersion();
		}

		public String defineProtocolName()
		{
			return ComProtocol.protocolName();
		}

		public ComProtocol createProtocol()
		{
			return ComProtocol.New(
				this.getProtocolName()  ,
				this.getProtocolVersion()       ,
				this.getByteOrder()     ,
				this.getIdStrategy()    ,
				this.getTypeDictionary()
			);
		}
		
		public ComProtocol.Assembler createProtocolAssembler()
		{
			return ComProtocol.Assembler();
		}

		public ComHost.Creator createHostCreator()
		{
			return ComHost.Creator();
		}
		
		public ComConnectionAcceptor.Creator createConnectionAcceptorCreator()
		{
			return ComConnectionAcceptor.Creator(
				this.getProtocolAssembler()
			);
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
		public F setProtocol(final ComProtocol protocol)
		{
			this.protocol = protocol;
			return this.$();
		}
		
		@Override
		public F setProtocolAssembler(final Assembler protocolAssembler)
		{
			this.protocolAssembler = protocolAssembler;
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
