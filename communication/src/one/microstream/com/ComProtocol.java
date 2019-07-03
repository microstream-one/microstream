package one.microstream.com;

import static one.microstream.X.notNull;

import java.nio.ByteOrder;

import one.microstream.persistence.types.PersistenceIdStrategy;
import one.microstream.persistence.types.PersistenceTypeDictionaryView;
import one.microstream.typing.Immutable;

public interface ComProtocol extends ComProtocolData
{
	public static String protocolName()
	{
		return "MICROSTREAM-COMCHANNEL";
	}
	
	public static String protocolVersion()
	{
		// (31.10.2018 TM)TODO: Maybe create a "Version" type with multiple sub version numbers?
		return "1.0";
	}
		
		
		
	public static ComProtocolCreator Creator()
	{
		return ComProtocolCreator.New();
	}
	
	public static ComProtocol New(
		final String                        name          ,
		final String                        version       ,
		final ByteOrder                     byteOrder     ,
		final PersistenceIdStrategy         idStrategy    ,
		final PersistenceTypeDictionaryView typeDictionary
	)
	{
		return new ComProtocol.Default(
			notNull(name)          ,
			notNull(version)       ,
			notNull(byteOrder)     ,
			notNull(idStrategy)    ,
			notNull(typeDictionary)
		);
	}
	
	public final class Default implements ComProtocol, Immutable
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final String                        name          ;
		private final String                        version       ;
		private final ByteOrder                     byteOrder     ;
		private final PersistenceIdStrategy         idStrategy    ;
		private final PersistenceTypeDictionaryView typeDictionary;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
			final String                        name          ,
			final String                        version       ,
			final ByteOrder                     byteOrder     ,
			final PersistenceIdStrategy         idStrategy    ,
			final PersistenceTypeDictionaryView typeDictionary
		)
		{
			super();
			this.name           = name          ;
			this.version        = version       ;
			this.byteOrder      = byteOrder     ;
			this.idStrategy     = idStrategy    ;
			this.typeDictionary = typeDictionary;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final String name()
		{
			return this.name;
		}

		@Override
		public final String version()
		{
			return this.version;
		}

		@Override
		public final ByteOrder byteOrder()
		{
			return this.byteOrder;
		}

		@Override
		public final PersistenceIdStrategy idStrategy()
		{
			return this.idStrategy;
		}
		
		@Override
		public final PersistenceTypeDictionaryView typeDictionary()
		{
			return this.typeDictionary;
		}
		
	}
		
}
