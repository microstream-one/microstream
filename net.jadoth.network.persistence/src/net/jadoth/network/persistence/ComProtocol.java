package net.jadoth.network.persistence;

import static net.jadoth.X.notNull;

import java.nio.ByteOrder;

import net.jadoth.chars.VarString;
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
	
	
	public static ComProtocol.Assembler Assembler()
	{
		return new ComProtocol.Assembler.Implementation();
	}
	
	public interface Assembler
	{
		public static String labelVersion()
		{
			return "Version";
		}
		
		public static String labelByteOrder()
		{
			return "ByteOrder";
		}
		
		public static String labelIdStrategy()
		{
			return "IdStrategy";
		}
		
		public static String labelIdStrategyType()
		{
			return "Type";
		}
		
		public static String labelIdStrategyEntity()
		{
			return "Entity";
		}
		
		public static String labelIdStrategyTypeDictionary()
		{
			return "TypeDictionary";
		}
		
		public static char protocolItemSeparator()
		{
			return ';';
		}
		
		public static char protocolItemAssigner()
		{
			return ':';
		}
		
		public VarString assembleProtocol(VarString vs, ComProtocol protocol);
		
		public default String assembleProtocol(final ComProtocol protocol)
		{
			final VarString vs = VarString.New(10_000);
			this.assembleProtocol(vs, protocol);
			
			return vs.toString();
		}
		
		public final class Implementation implements ComProtocol.Assembler
		{

			@Override
			public VarString assembleProtocol(final VarString vs, final ComProtocol protocol)
			{
				throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ComProtocol.Assembler#assembleProtocol()
			}
			
		}
		
	}
	
}
