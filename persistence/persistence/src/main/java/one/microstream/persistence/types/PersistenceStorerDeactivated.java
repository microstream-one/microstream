package one.microstream.persistence.types;

/*-
 * #%L
 * MicroStream Persistence
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

import one.microstream.persistence.exceptions.PersistenceExceptionStorerDeactivated;

/**
 * A {@link one.microstream.persistence.types.PersistenceStorer PersistenceStorer} implementation
 * that always throws {@link one.microstream.persistence.exceptions.PersistenceExceptionStorerDeactivated PersistenceExceptionStorerDeactivated}.
 */
public class PersistenceStorerDeactivated implements PersistenceStorer
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	/**
	 * Constructor
	 */
	public PersistenceStorerDeactivated()
	{
		super();
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public Object commit()
	{
		throw new PersistenceExceptionStorerDeactivated();
	}

	@Override
	public void clear()
	{
		throw new PersistenceExceptionStorerDeactivated();
	}

	@Override
	public boolean skipMapped(final Object instance, final long objectId)
	{
		throw new PersistenceExceptionStorerDeactivated();
	}

	@Override
	public boolean skip(final Object instance)
	{
		throw new PersistenceExceptionStorerDeactivated();
	}

	@Override
	public boolean skipNulled(final Object instance)
	{
		throw new PersistenceExceptionStorerDeactivated();
	}

	@Override
	public long size()
	{
		throw new PersistenceExceptionStorerDeactivated();
	}

	@Override
	public long currentCapacity()
	{
		throw new PersistenceExceptionStorerDeactivated();
	}

	@Override
	public long maximumCapacity()
	{
		throw new PersistenceExceptionStorerDeactivated();
	}

	@Override
	public long store(final Object instance)
	{
		throw new PersistenceExceptionStorerDeactivated();
	}

	@Override
	public long[] storeAll(final Object... instances)
	{
		throw new PersistenceExceptionStorerDeactivated();
	}

	@Override
	public void storeAll(final Iterable<?> instances)
	{
		throw new PersistenceExceptionStorerDeactivated();
	}

	@Override
	public PersistenceStorer reinitialize()
	{
		throw new PersistenceExceptionStorerDeactivated();
	}

	@Override
	public PersistenceStorer reinitialize(final long initialCapacity)
	{
		throw new PersistenceExceptionStorerDeactivated();
	}

	@Override
	public PersistenceStorer ensureCapacity(final long desiredCapacity)
	{
		throw new PersistenceExceptionStorerDeactivated();
	}

}
