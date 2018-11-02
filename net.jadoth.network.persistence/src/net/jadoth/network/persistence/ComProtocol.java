package net.jadoth.network.persistence;

import static net.jadoth.X.notNull;

import java.nio.ByteOrder;

import net.jadoth.persistence.types.PersistenceTypeDictionaryView;
import net.jadoth.swizzling.types.SwizzleIdStrategy;
import net.jadoth.typing.Immutable;

public interface ComProtocol
{
	public static String protocolName()
	{
		return "JETSTREAM-COMCHANNEL";
	}
	
	public static String protocolVersion()
	{
		// (31.10.2018 TM)TODO: JET-43: Maybe create a "Version" type with multiple sub version numbers?
		return "1.0";
	}
		
	
	
	public String name();
	
	public String version();
	
	public ByteOrder byteOrder();
	
	public PersistenceTypeDictionaryView typeDictionary();
	
	public SwizzleIdStrategy idStrategy();
	
	
		
	public static ComProtocol.Creator Creator()
	{
		return new ComProtocol.Creator.Implementation();
	}
	
	public interface Creator
	{
		public ComProtocol creatProtocol(
			String                        name          ,
			String                        version       ,
			ByteOrder                     byteOrder     ,
			SwizzleIdStrategy             idStrategy    ,
			PersistenceTypeDictionaryView typeDictionary
		);
		
		public final class Implementation implements ComProtocol.Creator
		{
			Implementation()
			{
				super();
			}

			@Override
			public ComProtocol creatProtocol(
				final String                        name          ,
				final String                        version       ,
				final ByteOrder                     byteOrder     ,
				final SwizzleIdStrategy             idStrategy    ,
				final PersistenceTypeDictionaryView typeDictionary
			)
			{
				return new ComProtocol.Implementation(name, version, byteOrder, idStrategy, typeDictionary);
			}
		}
	}
	
	public static ComProtocol New(
		final String                        name          ,
		final String                        version       ,
		final ByteOrder                     byteOrder     ,
		final SwizzleIdStrategy             idStrategy    ,
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
		private final SwizzleIdStrategy             idStrategy    ;
		private final PersistenceTypeDictionaryView typeDictionary;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(
			final String                        name          ,
			final String                        version       ,
			final ByteOrder                     byteOrder     ,
			final SwizzleIdStrategy             idStrategy    ,
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
		public final SwizzleIdStrategy idStrategy()
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
