package one.microstream.collections.types;

/*-
 * #%L
 * microstream-base
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

import java.util.function.Consumer;


public interface XIterable<E>
{
	/**
	 * Executes the given procedure for each element of the {@link XIterable}
	 * until all elements have been processed or the action throws an
	 * exception. Unless otherwise specified by the implementing class,
	 * procedures are performed in the order of iteration (if an iteration order
	 * is specified). Exceptions thrown by the procedure are relayed to the
	 * caller.<br>
	 * Should be identical to {@link Iterable#forEach(Consumer)}.
	 *
	 * @param <P> type of procedure
	 * @param procedure The procedure to be performed for each element
	 * @return Given procedure
	 */
	public <P extends Consumer<? super E>> P iterate(P procedure);

	

	/**
	 * Wrapper class that implements {@link XIterable} to wrap a subject of type E that procedures shall be
	 * executed on.
	 * <p>
	 * By using an executor instance, an instance not implementing {@link XIterable} can be passed to a context
	 * expecting an {@link XIterable} instance. Through this abstraction, logic can be written that can be
	 * equally executed on single objects (via this wrapper) or multiple objects (via X-collections).
	 * <p>
	 * <u>Example</u>:<pre> someRegistryLogic.register(persons);
	 * someRegistryLogic.register(new Executor&lt;Person&gt;(singlePerson));
	 * </pre>
	 *
	 * 
	 */
	public final class Executor<E> implements XIterable<E>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final E subject;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Executor(final E subject)
		{
			super();
			this.subject = subject;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public <P extends Consumer<? super E>> P iterate(final P procedure)
		{
			procedure.accept(this.subject);
			return procedure;
		}

	}

}
