package net.jadoth.com.binary;

import static net.jadoth.X.mayNull;
import static net.jadoth.X.notNull;

import java.nio.channels.SocketChannel;

import net.jadoth.collections.types.XGettingEnum;
import net.jadoth.com.ComPersistenceAdaptor;
import net.jadoth.com.ComPersistenceAdaptorCreator;
import net.jadoth.com.ComProtocol;
import net.jadoth.persistence.binary.types.BinaryPersistenceFoundation;
import net.jadoth.persistence.types.PersistenceContextDispatcher;
import net.jadoth.persistence.types.PersistenceFoundation;
import net.jadoth.persistence.types.PersistenceIdStrategy;
import net.jadoth.persistence.types.PersistenceTypeDictionaryViewProvider;
import net.jadoth.util.BufferSizeProvider;

public interface ComPersistenceAdaptorBinary<C> extends ComPersistenceAdaptor<C>
{
	@Override
	public default ComPersistenceAdaptorBinary<C> initializePersistenceFoundation(
		final PersistenceTypeDictionaryViewProvider typeDictionaryProvider,
		final PersistenceIdStrategy                     idStrategy
	)
	{
		ComPersistenceAdaptor.super.initializePersistenceFoundation(typeDictionaryProvider, idStrategy);
		return this;
	}
	
	@Override
	public BinaryPersistenceFoundation<?> persistenceFoundation();
		
	
	
	public static ComPersistenceAdaptorBinary.Default New(
		final BinaryPersistenceFoundation<?> foundation        ,
		final BufferSizeProvider             bufferSizeProvider,
		final PersistenceIdStrategy              hostInitIdStrategy,
		final XGettingEnum<Class<?>>         entityTypes       ,
		final PersistenceIdStrategy              hostIdStrategy
	)
	{
		return new ComPersistenceAdaptorBinary.Default(
			notNull(foundation)        ,
			notNull(bufferSizeProvider),
			mayNull(hostInitIdStrategy), // null for client persistence. Checked for host persistence beforehand.
			mayNull(entityTypes)       , // null for client persistence. Checked for host persistence beforehand.
			mayNull(hostIdStrategy)      // null for client persistence. Checked for host persistence beforehand.
		);
	}
	
	public abstract class Abstract<C>
	extends ComPersistenceAdaptor.Abstract<C>
	implements ComPersistenceAdaptorBinary<C>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final BinaryPersistenceFoundation<?> foundation        ;
		private final BufferSizeProvider             bufferSizeProvider;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Abstract(
			final BinaryPersistenceFoundation<?> foundation        ,
			final BufferSizeProvider             bufferSizeProvider,
			final PersistenceIdStrategy              hostInitIdStrategy,
			final XGettingEnum<Class<?>>         entityTypes       ,
			final PersistenceIdStrategy              hostIdStrategy
		)
		{
			super(hostInitIdStrategy, entityTypes, hostIdStrategy);
			this.foundation         = foundation        ;
			this.bufferSizeProvider = bufferSizeProvider;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final BinaryPersistenceFoundation<?> persistenceFoundation()
		{
			return this.foundation;
		}
		
		@Override
		public final BinaryPersistenceFoundation<?> createInitializationFoundation()
		{
			return this.foundation.Clone();
		}
		
		public BufferSizeProvider bufferSizeProvider()
		{
			return this.bufferSizeProvider;
		}
						
	}

	public final class Default extends ComPersistenceAdaptorBinary.Abstract<SocketChannel>
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Default(
			final BinaryPersistenceFoundation<?> foundation        ,
			final BufferSizeProvider             bufferSizeProvider,
			final PersistenceIdStrategy              hostInitIdStrategy,
			final XGettingEnum<Class<?>>         entityTypes       ,
			final PersistenceIdStrategy              hostIdStrategy
		)
		{
			super(foundation, bufferSizeProvider, hostInitIdStrategy, entityTypes, hostIdStrategy);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public PersistenceFoundation<?, ?> provideHostPersistenceFoundation(
			final SocketChannel connection
		)
		{
			this.initializeHostPersistenceFoundation();
			
			final BinaryPersistenceFoundation<?> foundation = this.persistenceFoundation();
			
			if(connection != null)
			{
				final ComPersistenceChannelBinary.Default channel = ComPersistenceChannelBinary.New(
					connection,
					this.bufferSizeProvider()
				);
				foundation.setPersistenceChannel(channel);
			}
			
			return foundation;
		}
		
		@Override
		public PersistenceFoundation<?, ?> provideClientPersistenceFoundation(
			final SocketChannel connection,
			final ComProtocol   protocol
		)
		{
			this.initializeClientPersistenceFoundation(protocol);
			
			final BinaryPersistenceFoundation<?> foundation = this.persistenceFoundation();
			
			final ComPersistenceChannelBinary.Default channel = ComPersistenceChannelBinary.New(
				connection,
				this.bufferSizeProvider()
			);
			foundation.setPersistenceChannel(channel);
						
			// (16.11.2018 TM)TODO: JET-49: divergent target ByteOrder not supported yet in BinaryPersistence.
			foundation.setTargetByteOrder(protocol.byteOrder());
			
			return foundation;
		}
				
	}
	
	
	public static ComPersistenceAdaptorBinary.Creator.Default Creator()
	{
		/*
		 * Communication normally doesn't update a central/globale object registry (= object graph) directly,
		 * but uses a local one that is discarded after every message.
		 * In case this shall change, a custom-configured foundation can be passed instead.
		 */
		return new ComPersistenceAdaptorBinary.Creator.Default(
			BinaryPersistenceFoundation.New()
				.setContextDispatcher(
					PersistenceContextDispatcher.LocalObjectRegistry()
				),
			BufferSizeProvider.New()
		);
	}
	
	public static ComPersistenceAdaptorBinary.Creator.Default Creator(
		final BinaryPersistenceFoundation<?> foundation
	)
	{
		return new ComPersistenceAdaptorBinary.Creator.Default(
			notNull(foundation),
			BufferSizeProvider.New()
		);
	}
	
	public static ComPersistenceAdaptorBinary.Creator.Default Creator(
		final BinaryPersistenceFoundation<?> foundation        ,
		final BufferSizeProvider             bufferSizeProvider
	)
	{
		return new ComPersistenceAdaptorBinary.Creator.Default(
			notNull(foundation)        ,
			notNull(bufferSizeProvider)
		);
	}
	
	public interface Creator<C> extends ComPersistenceAdaptorCreator<C>
	{
		public abstract class Abstract<C> implements ComPersistenceAdaptorBinary.Creator<C>
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////

			private final BinaryPersistenceFoundation<?> foundation        ;
			private final BufferSizeProvider             bufferSizeProvider;
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////
			
			protected Abstract(
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
			
			public BinaryPersistenceFoundation<?> foundation()
			{
				return this.foundation;
			}
			
			public BufferSizeProvider bufferSizeProvider()
			{
				return this.bufferSizeProvider;
			}
			
		}
		
		
		public final class Default extends ComPersistenceAdaptorBinary.Creator.Abstract<SocketChannel>
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////

			protected Default(
				final BinaryPersistenceFoundation<?> foundation        ,
				final BufferSizeProvider             bufferSizeProvider
			)
			{
				super(foundation, bufferSizeProvider);
			}
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// methods //
			////////////

			@Override
			public ComPersistenceAdaptor<SocketChannel> createPersistenceAdaptor(
				final PersistenceIdStrategy      hostIdStrategyInitialization,
				final XGettingEnum<Class<?>> entityTypes                 ,
				final PersistenceIdStrategy      hostIdStrategy
			)
			{
				return ComPersistenceAdaptorBinary.New(
					this.foundation()           ,
					this.bufferSizeProvider()   ,
					hostIdStrategyInitialization,
					entityTypes                 ,
					hostIdStrategy
				);
			}
			
		}
		
	}
	
}
