package net.jadoth.persistence.types;

import static net.jadoth.X.notNull;

import net.jadoth.network.types.NetworkMessageProcessor;
import net.jadoth.network.types.NetworkMessageReceiver;
import net.jadoth.network.types.NetworkSession;

public interface BinaryMessageProcessor<M, S extends NetworkSession<M>> extends NetworkMessageProcessor<S>
{
	@Override
	public void accept(S session);



	public class Implementation<
		M,
		S extends NetworkSession<M>,
		H extends NetworkMessageReceiver<S>
		> implements BinaryMessageProcessor<M, S>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final PersistenceBuilder<M> builder        ;
		private final H                     messageReceiver;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		public Implementation(final PersistenceBuilder<M> builder, final H messageReceiver)
		{
			super();
			this.builder         = notNull(builder        );
			this.messageReceiver = notNull(messageReceiver);
		}



		///////////////////////////////////////////////////////////////////////////
		// getters          //
		/////////////////////

		protected PersistenceBuilder<M> getBuilder()
		{
			return this.builder;
		}

		protected H getRequestHandler()
		{
			return this.messageReceiver;
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		protected Object readRequest(final S session)
		{
//			final M message = session.readMessage();

			throw new net.jadoth.meta.NotImplementedYetError();
			/* (27.04.2013)FIXME: BinaryMessageProcessor.Implementation#readRequest()
			 * This has to be refactored somehow to invert the control flow or to
			 * pass a message to a specialized builder.
			 * Because publishing consistency-critical implementation detail methods
			 * in the public API just for this place here is no solution.
			 */
//			this.builder.addChunks(X.Constant(message));
//			final Object request = this.builder.build();
//			this.builder.commit();
//			return request;
		}

		protected void receiveMessage(final Object request, final S session)
		{
			this.messageReceiver.receiveMessage(request, session);
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public void accept(final S session)
		{
			final Object message = this.readRequest(session);
//			JadothConsole.debugln("Received message " + message);
			this.receiveMessage(message, session);
		}

	}



	public interface Provider<M, S extends NetworkSession<M>, P extends BinaryMessageProcessor<M, S>>
	extends NetworkMessageProcessor.Provider<S, P>
	{
		@Override
		public P provideLogic();

		@Override // generics 4tw! :D
		public void disposeLogic(P processor, Throwable cause);



		public class Implementation<M, S extends NetworkSession<M>, H extends NetworkMessageReceiver<S>>
		implements BinaryMessageProcessor.Provider<M, S, BinaryMessageProcessor.Implementation<M, S, H>>
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields  //
			/////////////////////

			private final NetworkMessageReceiver.Provider<S, H> requestHandlerProvider   ;
			private final PersistenceBuilder.Creator<M>        persistenceBuilderCreator;



			///////////////////////////////////////////////////////////////////////////
			// constructors     //
			/////////////////////

			public Implementation(
				final NetworkMessageReceiver.Provider<S, H> requestHandlerProvider,
				final PersistenceBuilder.Creator<M>        persistenceBuilderCreator
			)
			{
				super();
				this.requestHandlerProvider    = requestHandlerProvider;
				this.persistenceBuilderCreator = persistenceBuilderCreator;
			}



			///////////////////////////////////////////////////////////////////////////
			// override methods //
			/////////////////////

			@Override
			public BinaryMessageProcessor.Implementation<M, S, H> provideLogic()
			{
				return new BinaryMessageProcessor.Implementation<>(
					this.persistenceBuilderCreator.createPersistenceBuilder(),
					this.requestHandlerProvider.provideHandler()
				);
			}

			@Override
			public void disposeLogic(
				final BinaryMessageProcessor.Implementation<M, S, H> processor,
				final Throwable                                      cause
			)
			{
				// builder is always created specifically for local context, doesn't have to be disposed.
				this.requestHandlerProvider.disposeHandler(processor.getRequestHandler(), cause);
			}

		}

	}

}
