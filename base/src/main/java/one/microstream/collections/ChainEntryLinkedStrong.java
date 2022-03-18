package one.microstream.collections;

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

import one.microstream.typing.KeyValue;

public final class ChainEntryLinkedStrong<E>
extends AbstractChainEntryLinked<E, E, E, ChainEntryLinkedStrong<E>>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	@SuppressWarnings("unchecked") // compensate java generics type system loophole
	static final <E> ChainEntryLinkedStrong<E>[] array(final int length)
	{
		return new ChainEntryLinkedStrong[length];
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	E element;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	protected ChainEntryLinkedStrong(final E element, final ChainEntryLinkedStrong<E> link)
	{
		super(link);
		this.element = element;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////
	
	@Override
	protected final void setElement0(final E element)
	{
		this.element = element;
	}
	
	@Override
	protected final E setElement(final E element)
	{
		final E old = this.element;
		this.element = element;
		return old;
	}

	@Override
	protected final boolean hasNullElement()
	{
		return this.element == null;
	}

	@Override
	protected final E element()
	{
		return this.element;
	}

	@Override
	public final E getKey()
	{
		return this.element;
	}

	@Override
	public final E getValue()
	{
		return this.element;
	}

	@Override
	protected final void set0(final E key, final E value)
	{
		this.element = key;
	}

	@Override
	public final E key()
	{
		return this.element;
	}

	@Override
	protected final E setKey(final E key)
	{
		final E old = this.element = key;
		this.element = key;
		return old;
	}

	@Override
	protected final void setKey0(final E key)
	{
		this.element = key;
	}

	@Override
	protected final boolean hasNullKey()
	{
		return this.element == null;
	}

	@Override
	public final E value()
	{
		return this.element;
	}

	@Override
	public final E setValue(final E value)
	{
		final E old = this.element = value;
		this.element = value;
		return old;
	}

	@Override
	protected final void setValue0(final E value)
	{
		this.element = value;
	}

	@Override
	protected final boolean hasNullValue()
	{
		return this.element == null;
	}

	@Override
	protected final boolean sameKV(final KeyValue<E, E> other)
	{
		return other.key() == this.element && other.value() == this.element;
	}

}
