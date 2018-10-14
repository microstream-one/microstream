package net.jadoth.network.types;

import static net.jadoth.X.notNull;

import java.util.function.Consumer;

import net.jadoth.functional.ParallelProcedure;

/* Architectural note:
 * message processor threads can easily die on any exception (that is not handled by the implementation)
 * because a message processor thread only handles one message at a time. If it dies, a new one will be created.
 * No buggy side effects like with a dying message listener are possible (for the generic context).
 */
/* (02.12.2012)FIXME: MessageProcessor disposal
 * Message processor can easily die, but must be disposed by its provider
 * Or leave it as an implementation detail relation between provider and processor?
 */
public interface NetworkMessageProcessor<S extends NetworkSession<?>> extends Consumer<S>
{
	@Override
	public void accept(S session);



	public interface Provider<S extends NetworkSession<?>, P extends NetworkMessageProcessor<S>>
	extends ParallelProcedure.LogicProvider<S, P>
	{
		@Override
		public P provideLogic();

		@Override // generics 4tw! :D
		public void disposeLogic(P processor, Throwable cause);



		public class TrivialImplementation<S extends NetworkSession<?>, P extends NetworkMessageProcessor<S>>
		implements NetworkMessageProcessor.Provider<S, P>
		{
			private final P messageProcessor;

			public TrivialImplementation(final P messageProcessor)
			{
				super();
				this.messageProcessor = notNull(messageProcessor);
			}

			@Override
			public P provideLogic()
			{
				return this.messageProcessor;
			}

			@Override
			public void disposeLogic(final P processor, final Throwable cause)
			{
				// no-op
			}

		}

	}

	public interface RegulatorThreadCount extends ParallelProcedure.ThreadCountProvider
	{
		@Override
		public int maxThreadCount();
	}

	public interface RegulatorThreadTimeout extends ParallelProcedure.ThreadTimeoutProvider
	{
		@Override
		public int threadTimeout();
	}

}
