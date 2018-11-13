package net.jadoth.com;

import static net.jadoth.X.notNull;

import java.util.function.Consumer;

/**
 * Gateway/relay to the actual application/framework communication logic.
 * Potentially in another, maybe even dedicated thread.
 * 
 * @author TM
 *
 */
@FunctionalInterface
public interface ComChannelAcceptor
{
	public void acceptChannel(ComChannel channel);
	
	
	
	public static ComChannelAcceptor.Wrapper Wrap(
		final Consumer<? super ComChannel> acceptor
	)
	{
		return new ComChannelAcceptor.Wrapper(
			notNull(acceptor)
		);
	}
	
	public final class Wrapper implements ComChannelAcceptor
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final Consumer<? super ComChannel> acceptor;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Wrapper(final Consumer<? super ComChannel> acceptor)
		{
			super();
			this.acceptor = acceptor;
		}

		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final void acceptChannel(final ComChannel channel)
		{
			this.acceptor.accept(channel);
		}
		
	}
	
}
