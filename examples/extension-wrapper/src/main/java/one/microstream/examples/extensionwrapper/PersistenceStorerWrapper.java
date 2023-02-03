package one.microstream.examples.extensionwrapper;

/*-
 * #%L
 * microstream-examples-extension-wrapper
 * %%
 * Copyright (C) 2019 - 2023 MicroStream Software
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

import one.microstream.persistence.types.PersistenceStorer;

/**
 * Wrapper for {@link PersistenceStorer}, used as base for extensions
 *
 */
public class PersistenceStorerWrapper implements PersistenceStorer
{
	private final PersistenceStorer delegate;

	public PersistenceStorerWrapper(final PersistenceStorer delegate)
	{
		super();
		this.delegate = delegate;
	}
 
	@Override
	public PersistenceStorer reinitialize()
	{
		return this.delegate.reinitialize();
	}

	@Override
	public long store(final Object instance)
	{
		return this.delegate.store(instance);
	}

	@Override
	public PersistenceStorer reinitialize(final long initialCapacity)
	{
		return this.delegate.reinitialize(initialCapacity);
	}

	@Override
	public Object commit()
	{
		return this.delegate.commit();
	}

	@Override
	public PersistenceStorer ensureCapacity(final long desiredCapacity)
	{
		return this.delegate.ensureCapacity(desiredCapacity);
	}

	@Override
	public void clear()
	{
		this.delegate.clear();
	}

	@Override
	public long[] storeAll(final Object... instances)
	{
		return this.delegate.storeAll(instances);
	}

	@Override
	public boolean skipMapped(final Object instance, final long objectId)
	{
		return this.delegate.skipMapped(instance, objectId);
	}

	@Override
	public void storeAll(final Iterable<?> instances)
	{
		this.delegate.storeAll(instances);
	}

	@Override
	public boolean skip(final Object instance)
	{
		return this.delegate.skip(instance);
	}

	@Override
	public boolean skipNulled(final Object instance)
	{
		return this.delegate.skipNulled(instance);
	}

	@Override
	public long size()
	{
		return this.delegate.size();
	}

	@Override
	public boolean isEmpty()
	{
		return this.delegate.isEmpty();
	}

	@Override
	public long currentCapacity()
	{
		return this.delegate.currentCapacity();
	}

	@Override
	public long maximumCapacity()
	{
		return this.delegate.maximumCapacity();
	}

	
	
	
}
