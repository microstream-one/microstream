package one.microstream.memory.android;

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

import one.microstream.functional.DefaultInstantiator;
import one.microstream.memory.DirectBufferDeallocator;
import one.microstream.memory.MemoryAccessorGeneric;
import one.microstream.memory.XMemory;


/**
 * Trivial setup wrapping class to simplify and document the different setup possibilities.
 *
 */
public final class MicroStreamAndroidAdapter
{
	/**
	 * Sets up the memory accessing logic to use {@link MemoryAccessorGeneric}.
	 * <p>
	 * No platform-specific {@link DefaultInstantiator} is set, meaning the behavior
	 * defaults to public API functionality, i.e. requiring a default constructor to be
	 * present for every generically handled class.
	 * <p>
	 * No platform-specific {@link DirectBufferDeallocator} is set, meaning the memory
	 * allocated by direct byte buffers cannot be freed before the direct byte buffer
	 * instance is garbage-collected.
	 * See <a href="http://stackoverflow.com/questions/8462200/examples-of-forcing-freeing-of-native-memory-direct-bytebuffer-has-allocated-us">
	 * this Stack Overflow question</a> for an example why that might be a problem.
	 * 
	 * @see #setupWithInstantiator()
	 * @see #setupFull()
	 */
	public static final void setupBasic()
	{
		XMemory.setMemoryAccessor(
			MemoryAccessorGeneric.New()
		);
	}
	
	/**
	 * Sets up the memory accessing logic to use {@link MemoryAccessorGeneric}.
	 * <p>
	 * {@link AndroidInstantiatorBlank} ist used as the {@link DefaultInstantiator} implementation.
	 * <p>
	 * No platform-specific {@link DirectBufferDeallocator} is set, identical to {@link #setupBasic()}.
	 * 
	 * @see #setupBasic()
	 * @see #setupFull()
	 */
	public static final void setupWithInstantiator()
	{
		XMemory.setMemoryAccessor(
			MemoryAccessorGeneric.New(
				AndroidInternals.InstantiatorBlank()
			)
		);
	}
	
	/**
	 * Sets up the memory accessing logic to use {@link MemoryAccessorGeneric}.
	 * <p>
	 * {@link AndroidInstantiatorBlank} ist used as the {@link DefaultInstantiator} implementation.
	 * <p>
	 * {@link AndroidDirectBufferDeallocator} is used as the {@link DirectBufferDeallocator}.
	 * 
	 * @see #setupBasic()
	 * @see #setupWithInstantiator()
	 */
	public static final void setupFull()
	{
		XMemory.setMemoryAccessor(
			MemoryAccessorGeneric.New(
				AndroidInternals.InstantiatorBlank(),
				AndroidInternals.DirectBufferDeallocator()
			)
		);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Dummy constructor to prevent instantiation of this static-only utility class.
	 * 
	 * @throws UnsupportedOperationException when called
	 */
	private MicroStreamAndroidAdapter()
	{
		// static only
		throw new UnsupportedOperationException();
	}
	
}
