package net.jadoth.com;

import static net.jadoth.X.notNull;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.function.Consumer;

import net.jadoth.persistence.types.PersistenceFoundation;

public interface ComHostContext<C>
{
	public InetSocketAddress provideAddress();

	public ComChannelAcceptor provideChannelAcceptor();
	
	public ComPersistenceAdaptor<C> providePersistenceAdaptor();
	
	
	
	public static <C> ComHostContext.Builder.Implementation<C> Builder()
	{
		return new ComHostContext.Builder.Implementation<>();
	}
	
	public static <C> ComHostContext.Implementation<C> New(
		final InetSocketAddress        address           ,
		final ComChannelAcceptor       channelAcceptor   ,
		final ComPersistenceAdaptor<C> persistenceAdaptor
	)
	{
		return new ComHostContext.Implementation<>(
			notNull(address),
			notNull(channelAcceptor),
			notNull(persistenceAdaptor)
		);
	}
	
	public class Implementation<C> implements ComHostContext<C>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final InetSocketAddress        address           ;
		private final ComChannelAcceptor       channelAcceptor	 ;
		private final ComPersistenceAdaptor<C> persistenceAdaptor;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(
			final InetSocketAddress        address           ,
			final ComChannelAcceptor       channelAcceptor   ,
			final ComPersistenceAdaptor<C> persistenceAdaptor
		)
		{
			super();
			this.address            = address           ;
			this.channelAcceptor    = channelAcceptor   ;
			this.persistenceAdaptor = persistenceAdaptor;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final InetSocketAddress provideAddress()
		{
			return this.address;
		}

		@Override
		public final ComChannelAcceptor provideChannelAcceptor()
		{
			return this.channelAcceptor;
		}

		@Override
		public final ComPersistenceAdaptor<C> providePersistenceAdaptor()
		{
			return this.persistenceAdaptor;
		}
		
	}
		
	public interface Builder<C>
	{
		public Builder<C> setAddress(InetSocketAddress socketAddress);
		
		public Builder<C> setAddress(InetAddress address);
		
		public Builder<C> setPort(int port);
		
		public default Builder<C> setAddress(final InetAddress address, final int port)
		{
			synchronized(this)
			{
				this.setAddress(address);
				this.setPort(port);
			}
			
			return this;
		}
		
		public Builder<C> setChannelAcceptor(ComChannelAcceptor channelAcceptor);
		
		public Builder<C> setChannelAcceptorLogic(Consumer<? super ComChannel> channelAcceptorLogic);
		
		public Builder<C> setPersistence(ComPersistenceAdaptor<C> persistenceAdaptor);

		public Builder<C> setPersistence(PersistenceFoundation<?, ?> persistenceFoundation);
		
		

		public InetSocketAddress socketAddress();
		
		public InetAddress address();
		
		public int port();
		
		public ComChannelAcceptor channelAcceptor();
		
		public Consumer<? super ComChannel> channelAcceptorLogic();
		
		public ComPersistenceAdaptor<C> persistenceAdaptor();

		public PersistenceFoundation<?, ?> persistenceFoundation();
		
		
		
		public InetSocketAddress getSocketAddress();
		
		public ComChannelAcceptor getChannelAcceptor();
		
		public ComPersistenceAdaptor<C> getPersistenceAdaptor();
		
		
		
		public ComHostContext<C> buildHostContext();
		
		
		public class Implementation<C> implements ComHostContext.Builder<C>
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			private InetSocketAddress            socketAddress        ;
			private InetAddress                  address              ;
			private int                          port                 ;
			private ComChannelAcceptor           channelAcceptor      ;
			private Consumer<? super ComChannel> channelAcceptorLogic ;
			private ComPersistenceAdaptor<C>     persistenceAdaptor   ;
			private PersistenceFoundation<?, ?>  persistenceFoundation;
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////
			
			protected Implementation()
			{
				super();
			}
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// methods //
			////////////

			@Override
			public Builder<C> setAddress(final InetSocketAddress socketAddress)
			{
				this.socketAddress = socketAddress;
				return this;
			}
			
			@Override
			public Builder<C> setAddress(final InetAddress address)
			{
				this.address = address;
				return this;
			}
			
			@Override
			public Builder<C> setPort(final int port)
			{
				this.port = port;
				return this;
			}
			
			@Override
			public Builder<C> setChannelAcceptor(final ComChannelAcceptor channelAcceptor)
			{
				this.channelAcceptor = channelAcceptor;
				return this;
			}
			
			@Override
			public Builder<C> setChannelAcceptorLogic(final Consumer<? super ComChannel> channelAcceptorLogic)
			{
				this.channelAcceptorLogic = channelAcceptorLogic;
				return this;
			}
			
			@Override
			public Builder<C> setPersistence(final ComPersistenceAdaptor<C> persistenceAdaptor)
			{
				this.persistenceAdaptor = persistenceAdaptor;
				return this;
			}

			@Override
			public Builder<C> setPersistence(final PersistenceFoundation<?, ?> persistenceFoundation)
			{
				this.persistenceFoundation = persistenceFoundation;
				return this;
			}
			
			

			@Override
			public InetSocketAddress socketAddress()
			{
				return this.socketAddress;
			}
			
			@Override
			public InetAddress address()
			{
				return this.address;
			}
			
			@Override
			public int port()
			{
				return this.port;
			}
			
			@Override
			public ComChannelAcceptor channelAcceptor()
			{
				return this.channelAcceptor;
			}
			
			@Override
			public Consumer<? super ComChannel> channelAcceptorLogic()
			{
				return this.channelAcceptorLogic;
			}
			
			@Override
			public ComPersistenceAdaptor<C> persistenceAdaptor()
			{
				return this.persistenceAdaptor;
			}

			@Override
			public PersistenceFoundation<?, ?> persistenceFoundation()
			{
				return this.persistenceFoundation;
			}
			
			
			
			@Override
			public InetSocketAddress getSocketAddress()
			{
				return this.socketAddress != null
					? this.socketAddress
					: this.buildSocketAddress()
				;
			}
			
			protected InetSocketAddress buildSocketAddress()
			{
				// both can be null / 0 according to JavaDoc.
				return new InetSocketAddress(this.address, this.port);
			}
			
			@Override
			public ComChannelAcceptor getChannelAcceptor()
			{
				return this.channelAcceptor != null
					? this.channelAcceptor
					: this.buildChannelAcceptor()
				;
			}
			
			protected ComChannelAcceptor buildChannelAcceptor()
			{
				if(this.channelAcceptorLogic != null)
				{
					return ComChannelAcceptor.Wrap(this.channelAcceptorLogic);
				}
				
				// (13.11.2018 TM)EXCP: proper exception
				throw new NullPointerException("No channel acceptor logic set.");
			}
			
			@Override
			public ComPersistenceAdaptor<C> getPersistenceAdaptor()
			{
				return this.persistenceAdaptor != null
					? this.persistenceAdaptor
					: this.buildPersistenceAdaptor()
				;
			}
			
			protected ComPersistenceAdaptor<C> buildPersistenceAdaptor()
			{
				if(this.persistenceFoundation != null)
				{
					return ComPersistenceAdaptor.New(this.persistenceFoundation);
				}
				
				// (13.11.2018 TM)EXCP: proper exception
				throw new NullPointerException("No persistence context set.");
			}
			
			@Override
			public ComHostContext.Implementation<C> buildHostContext()
			{
				return ComHostContext.New(
					this.getSocketAddress()     ,
					this.getChannelAcceptor()   ,
					this.getPersistenceAdaptor()
				);
			}
			
		}
		
	}
	
}
