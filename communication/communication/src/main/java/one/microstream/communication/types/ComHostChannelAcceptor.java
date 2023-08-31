package one.microstream.communication.types;

/*-
 * #%L
 * microstream-communication
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import static one.microstream.X.notNull;

import java.util.function.Consumer;

/**
 * Gateway/relay to the actual application/framework communication logic.
 * Potentially in another, maybe even dedicated thread.
 *
 * @param <C> the communication layer type
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
