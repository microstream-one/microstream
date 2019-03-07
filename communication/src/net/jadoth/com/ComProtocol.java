package net.jadoth.com;

import static net.jadoth.X.notNull;

import java.nio.ByteOrder;

import net.jadoth.persistence.types.PersistenceIdStrategy;
import net.jadoth.persistence.types.PersistenceTypeDictionaryView;
import net.jadoth.typing.Immutable;

public interface ComProtocol extends ComProtocolData
{
	public static String protocolName()
	{
		return "JETSTREAM-COMCHANNEL";
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
		return new ComProtocol.Implementation(
			notNull(name)          ,
			notNull(version)       ,
			notNull(byteOrder)     ,
			notNull(idStrategy)    ,
			notNull(typeDictionary)
		);
	}
	
	public final class Implementation implements ComProtocol, Immutable
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
		
		Implementation(
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
