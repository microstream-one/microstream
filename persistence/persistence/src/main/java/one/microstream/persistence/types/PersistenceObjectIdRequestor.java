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

public interface PersistenceObjectIdRequestor<D>
{
	// always implemented for guaranteed registration
	public <T> void registerGuaranteed(
		long                         objectId       ,
		T                            instance       ,
		PersistenceTypeHandler<D, T> optionalHandler
	);
	
	// implemented by lazy implementation, no-op otherwise
	public <T> void registerLazyOptional(
		long                         objectId       ,
		T                            instance       ,
		PersistenceTypeHandler<D, T> optionalHandler
	);

	// implemented by eager implementation, no-op otherwise
	public <T> void registerEagerOptional(
		long                         objectId       ,
		T                            instance       ,
		PersistenceTypeHandler<D, T> optionalHandler
	);
	
	
	
	public static <D> PersistenceObjectIdRequestor<D> NoOp()
	{
		return new PersistenceObjectIdRequestor.NoOp<>();
	}
	
	public final class NoOp<D> implements PersistenceObjectIdRequestor<D>
	{

		@Override
		public <T> void registerGuaranteed(
			final long                         objectId       ,
			final T                            instance       ,
			final PersistenceTypeHandler<D, T> optionalHandler
		)
		{
			// no-op
		}

		@Override
		public <T> void registerLazyOptional(
			final long                         objectId       ,
			final T                            instance       ,
			final PersistenceTypeHandler<D, T> optionalHandler
		)
		{
			// no-op
		}

		@Override
		public <T> void registerEagerOptional(
			final long                         objectId       ,
			final T                            instance       ,
			final PersistenceTypeHandler<D, T> optionalHandler
		)
		{
			// no-op
		}
		
	}
	
}
