package net.jadoth.com.binary;

import static net.jadoth.X.notNull;

import java.nio.channels.SocketChannel;

import net.jadoth.com.ComPersistenceAdaptor;
import net.jadoth.com.ComProtocol;
import net.jadoth.persistence.binary.types.BinaryPersistenceFoundation;
import net.jadoth.persistence.types.BufferSizeProvider;
import net.jadoth.persistence.types.PersistenceFoundation;
import net.jadoth.persistence.types.PersistenceTypeDictionaryManager;

public interface ComPersistenceAdaptorBinary<C> extends ComPersistenceAdaptor<C>
{
	public static ComPersistenceAdaptorBinary.Default New(
		final BinaryPersistenceFoundation<?> foundation
	)
	{
		return new ComPersistenceAdaptorBinary.Default(
			notNull(foundation)     ,
			BufferSizeProvider.New()
		);
	}
	
	// (16.11.2018 TM)TODO: set Persistence.typeMismatchValidatorFailing()?
	public static ComPersistenceAdaptorBinary.Default New(
		final BinaryPersistenceFoundation<?> foundation        ,
		final BufferSizeProvider             bufferSizeProvider
	)
	{
		return new ComPersistenceAdaptorBinary.Default(
			notNull(foundation)        ,
			notNull(bufferSizeProvider)
		);
	}

	public final class Default implements ComPersistenceAdaptorBinary<SocketChannel>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final BinaryPersistenceFoundation<?> foundation        ;
		private final BufferSizeProvider             bufferSizeProvider;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Default(
			final BinaryPersistenceFoundation<?> foundation        ,
			final BufferSizeProvider             bufferSizeProvider
		)
		{
			super();
			this.foundation         = foundation        ;
			this.bufferSizeProvider = bufferSizeProvider;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public PersistenceFoundation<?, ?> provideHostPersistenceFoundation(
			final SocketChannel connection
		)
		{
			if(connection == null)
			{
				return this.foundation;
			}
			
			final ComPersistenceChannelBinary.Default channel = ComPersistenceChannelBinary.New(
				connection,
				this.bufferSizeProvider
			);
			this.foundation.setPersistenceChannel(channel);
			
			return this.foundation;
		}
		
		@Override
		public PersistenceFoundation<?, ?> provideClientPersistenceFoundation(
			final SocketChannel connection,
			final ComProtocol   protocol
		)
		{
			final ComPersistenceChannelBinary.Default channel = ComPersistenceChannelBinary.New(
				connection,
				this.bufferSizeProvider
			);
			this.foundation.setPersistenceChannel(channel);
			
			final PersistenceTypeDictionaryManager typeDictionaryManager =
				PersistenceTypeDictionaryManager.Immutable(protocol)
			;
			this.foundation.setTypeDictionaryManager(typeDictionaryManager);
			this.foundation.setObjectIdProvider(protocol.idStrategy().createObjectIdProvider());
			this.foundation.setTypeIdProvider(protocol.idStrategy().createTypeIdProvider());
			
			// (16.11.2018 TM)TODO: JET-49: divergent target ByteOrder not supported yet in BinaryPersistence.
			this.foundation.setTargetByteOrder(protocol.byteOrder());
			
			return this.foundation;
		}
				
	}
	
}
