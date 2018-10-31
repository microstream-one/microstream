package net.jadoth.network.persistence;

import java.nio.ByteOrder;

import net.jadoth.exceptions.MissingFoundationPartException;
import net.jadoth.low.XVM;
import net.jadoth.persistence.types.PersistenceTypeDictionaryView;
import net.jadoth.swizzling.types.SwizzleIdStrategy;
import net.jadoth.util.InstanceDispatcher;

public interface NetworkFoundation<F extends NetworkFoundation<?>>
{
	public static int defaultComPort()
	{
		return 1337;
	}
	
	public static String protocolName()
	{
		return "JETSTREAM-COMCHANNEL";
	}
	
	public static String version()
	{
		// (31.10.2018 TM)TODO: JET-43: Maybe create a "Version" type with multiple sub version numbers?
		return "1.0";
	}
	
	public static ByteOrder byteOrder()
	{
		return XVM.nativeByteOrder();
	}
	
	
	
	public PersistenceTypeDictionaryView getTypeDictionary();
	
	public ByteOrder getByteOrder();
	
	public String getVersion();
	
	public String getProtocolName();
	
	public SwizzleIdStrategy getIdStrategy();
	
	public int getComPort();
	
	public ComHost.Creator getComHostCreator();
	
	public ComConfiguration getConfiguration();
	
	public ComManager getComManager();
	
	
	
	public F setTypeDictionary(PersistenceTypeDictionaryView typeDictionary);
	
	public F setByteOrder(ByteOrder byteOrder);
	
	public F setVersion(String version);
	
	public F setProtocolName(String protocolName);
	
	public F setIdStrategy(SwizzleIdStrategy idStrategy);
	
	public F setComPort(int comPort);
	
	public F setComHostCreator(ComHost.Creator comHostCreator);
	
	public F setConfiguration(ComConfiguration configuration);
	
	public F setComManager(ComManager comManager);
	
	
	public ComHost createHost();
	
	
	
	
	public class Implementation<F extends NetworkFoundation.Implementation<?>>
	extends InstanceDispatcher.Implementation implements NetworkFoundation<F>
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
		private ComHost.Creator               comHostCreator;
		private ComConfiguration              configuration ;
		private ComManager                    comManager    ;
		
		
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
		public ComHost.Creator getComHostCreator()
		{
			if(this.comHostCreator == null)
			{
				this.comHostCreator = this.createComHostCreator();
			}
			
			return this.comHostCreator;
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
		public ComManager getComManager()
		{
			if(this.comManager == null)
			{
				this.comManager = this.createComManager();
			}
			
			return this.comManager;
		}
		
		

		public ByteOrder defineByteOrder()
		{
			return NetworkFoundation.byteOrder();
		}

		public String defineVersion()
		{
			return NetworkFoundation.version();
		}

		public String defineProtocolName()
		{
			return NetworkFoundation.protocolName();
		}

		public int defineComPort()
		{
			return NetworkFoundation.defaultComPort();
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

		public ComHost.Creator createComHostCreator()
		{
			return ComHost.Creator();
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

		public ComManager createComManager()
		{
			// (01.11.2018 TM)TODO: JET-43: really exception?
			throw new MissingFoundationPartException(ComManager.class);
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
		public F setComHostCreator(final ComHost.Creator comHostCreator)
		{
			this.comHostCreator = comHostCreator;
			return this.$();
		}
		
		@Override
		public F setConfiguration(final ComConfiguration configuration)
		{
			this.configuration = configuration;
			return this.$();
		}
		
		@Override
		public F setComManager(final ComManager comManager)
		{
			this.comManager = comManager;
			return this.$();
		}
		
		@Override
		public ComHost createHost()
		{
			final ComHost.Creator hostCreator = this.getComHostCreator();
			
			return hostCreator.createComHost(
				this.getConfiguration(),
				this.getComPort()      ,
				this.getComManager()
			);
		}
		
	}
	
}
