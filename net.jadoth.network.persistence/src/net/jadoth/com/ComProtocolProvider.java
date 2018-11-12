package net.jadoth.com;

import static net.jadoth.X.notNull;

import java.nio.ByteOrder;

import net.jadoth.persistence.types.PersistenceTypeDictionaryView;
import net.jadoth.persistence.types.PersistenceTypeDictionaryViewProvider;
import net.jadoth.swizzling.types.SwizzleIdStrategy;
import net.jadoth.typing.Immutable;

public interface ComProtocolProvider extends ComProtocolData
{
	public ComProtocol provideProtocol();
	
	
	
	public static ComProtocolProviderCreator Creator()
	{
		return ComProtocolProviderCreator.New();
	}
	
	public static ComProtocolProvider New(
		final String                                name                  ,
		final String                                version               ,
		final ByteOrder                             byteOrder             ,
		final SwizzleIdStrategy                     idStrategy            ,
		final PersistenceTypeDictionaryViewProvider typeDictionaryProvider,
		final ComProtocolCreator                    protocolCreator
	)
	{
		return new ComProtocolProvider.Implementation(
			notNull(name)                  ,
			notNull(version)               ,
			notNull(byteOrder)             ,
			notNull(idStrategy)            ,
			notNull(typeDictionaryProvider),
			notNull(protocolCreator)
		);
	}
	
	public final class Implementation implements ComProtocolProvider, Immutable
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final String                                name                  ;
		private final String                                version               ;
		private final ByteOrder                             byteOrder             ;
		private final SwizzleIdStrategy                     idStrategy            ;
		private final PersistenceTypeDictionaryViewProvider typeDictionaryProvider;
		private final ComProtocolCreator                    protocolCreator       ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(
			final String                                name                  ,
			final String                                version               ,
			final ByteOrder                             byteOrder             ,
			final SwizzleIdStrategy                     idStrategy            ,
			final PersistenceTypeDictionaryViewProvider typeDictionaryProvider,
			final ComProtocolCreator                    protocolCreator
		)
		{
			super();
			this.name                   = name                  ;
			this.version                = version               ;
			this.byteOrder              = byteOrder             ;
			this.idStrategy             = idStrategy            ;
			this.typeDictionaryProvider = typeDictionaryProvider;
			this.protocolCreator        = protocolCreator       ;
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
			return this.typeDictionaryProvider.provideTypeDictionary();
		}
		
		@Override
		public ComProtocol provideProtocol()
		{
			return this.protocolCreator.creatProtocol(
				this.name()          ,
				this.version()       ,
				this.byteOrder()     ,
				this.idStrategy()    ,
				this.typeDictionary()
			);
		}
		
	}
		
}
