package net.jadoth.com;

import static net.jadoth.X.notNull;

import java.nio.ByteOrder;

import net.jadoth.persistence.types.PersistenceIdStrategy;
import net.jadoth.persistence.types.PersistenceTypeDictionaryView;
import net.jadoth.persistence.types.PersistenceTypeDictionaryViewProvider;
import net.jadoth.typing.Immutable;


public interface ComProtocolProvider<C> extends ComProtocolData
{
	public ComProtocol provideProtocol(C connection);
	
	
	
	public static <C> ComProtocolProviderCreator<C> Creator()
	{
		return ComProtocolProviderCreator.New();
	}
	
	public static <C> ComProtocolProvider<C> New(
		final String                                name                  ,
		final String                                version               ,
		final ByteOrder                             byteOrder             ,
		final PersistenceIdStrategy                     idStrategy            ,
		final PersistenceTypeDictionaryViewProvider typeDictionaryProvider,
		final ComProtocolCreator                    protocolCreator
	)
	{
		return new ComProtocolProvider.Implementation<>(
			notNull(name)                  ,
			notNull(version)               ,
			notNull(byteOrder)             ,
			notNull(idStrategy)            ,
			notNull(typeDictionaryProvider),
			notNull(protocolCreator)
		);
	}
	
	public final class Implementation<C> implements ComProtocolProvider<C>, Immutable
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final String                                name                  ;
		private final String                                version               ;
		private final ByteOrder                             byteOrder             ;
		private final PersistenceIdStrategy                 idStrategy            ;
		private final PersistenceTypeDictionaryViewProvider typeDictionaryProvider;
		private final ComProtocolCreator                    protocolCreator       ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(
			final String                                name                  ,
			final String                                version               ,
			final ByteOrder                             byteOrder             ,
			final PersistenceIdStrategy                 idStrategy            ,
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
		public final PersistenceIdStrategy idStrategy()
		{
			return this.idStrategy;
		}
		
		@Override
		public final PersistenceTypeDictionaryView typeDictionary()
		{
			return this.typeDictionaryProvider.provideTypeDictionary();
		}
		
		@Override
		public ComProtocol provideProtocol(final C connection)
		{
			// the default implementation assigns the same id range to every client
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
