package one.microstream.persistence.types;

/*-
 * #%L
 * microstream-persistence
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

public interface PersistenceRootsProvider<D>
{
	public PersistenceRoots provideRoots();
	
	public PersistenceRoots peekRoots();
	
	public void updateRuntimeRoots(PersistenceRoots runtimeRoots);
	
	/**
	 * Only the {@link PersistenceRootsProvider} implementation can ensure that the handler fits the instance,
	 * so it has to do the registering as well.
	 *
	 * @param typeHandlerRegistry the type handler registry
	 * @param objectRegistry the object registry
	 */
	public void registerRootsTypeHandlerCreator(
		PersistenceCustomTypeHandlerRegistry<D> typeHandlerRegistry,
		PersistenceObjectRegistry               objectRegistry
	);
	
	
	public static <D> PersistenceRootsProvider<D> Empty()
	{
		return new Empty<>();
	}
	
	
	public final class Empty<D> implements PersistenceRootsProvider<D>
	{
		Empty()
		{
			super();
		}

		@Override
		public PersistenceRoots provideRoots()
		{
			//no-op
			return null;
		}

		@Override
		public PersistenceRoots peekRoots()
		{
			//no-op
			return null;
		}

		@Override
		public void updateRuntimeRoots(final PersistenceRoots runtimeRoots)
		{
			//no-op
		}

		@Override
		public void registerRootsTypeHandlerCreator(
			final PersistenceCustomTypeHandlerRegistry<D> typeHandlerRegistry,
			final PersistenceObjectRegistry               objectRegistry
		)
		{
			//no-op
		}
	}

}
