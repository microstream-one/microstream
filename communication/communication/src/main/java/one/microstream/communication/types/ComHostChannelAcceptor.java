package one.microstream.communication.types;

import static one.microstream.X.notNull;

import java.util.function.Consumer;

/**
 * Gateway/relay to the actual application/framework communication logic.
 * Potentially in another, maybe even dedicated thread.
 * 
 * 
 *
 */
@FunctionalInterface
public interface ComHostChannelAcceptor<C>
{
	public void acceptChannel(ComHostChannel<C> channel);
	
	
	
	public static <C>ComHostChannelAcceptor.Wrapper<C> Wrap(
		final Consumer<? super ComHostChannel<C>> acceptor
	)
	{
		return new ComHostChannelAcceptor.Wrapper<>(
			notNull(acceptor)
		);
	}
	
	public final class Wrapper<C> implements ComHostChannelAcceptor<C>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final Consumer<? super ComHostChannel<C>> acceptor;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Wrapper(final Consumer<? super ComHostChannel<C>> acceptor)
		{
			super();
			this.acceptor = acceptor;
		}

		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final void acceptChannel(final ComHostChannel<C> channel)
		{
			this.acceptor.accept(channel);
		}
		
	}
	
}
