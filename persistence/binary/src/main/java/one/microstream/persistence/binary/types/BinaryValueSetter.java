package one.microstream.persistence.binary.types;

/*-
 * #%L
 * microstream-persistence-binary
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

import one.microstream.persistence.types.PersistenceLoadHandler;


public interface BinaryValueSetter
{
	/* (20.09.2018 TM)TODO: BinaryValueSetter performance optimization
	 * A distinction between BinaryValueSetterPrimitive and BinaryValueSetterReference could be made.
	 * Since the persistent form order all references at the start and all primitives afterwards,
	 * the switching overhead from one array iteration to another would be minimal.
	 * As a result, setting a primitive value could omit the reference to the idResolver.
	 * However it is not clear how much performance that would effectively bring.
	 * 
	 * Before a premature optimization is done, this should be tested.
	 * However, if the order of all primitives in instance form and in persistent form is exactly the same,
	 * a shortcut for copying primitives could be made where a low-level loop (Unsafe) copies all primitive
	 * values at one swoop. Or at least all per inheritance level.
	 * However, all these assumptions about memory layout would have to be thoroughly validated at type handler
	 * initialization and if not true, a fallback to the current mechanism would have to be used.
	 * 
	 * In short: here should be a lot of performance optimization potential, but it has to be done properly.
	 */
	
	/**
	 * Sets a single value, read from binary (persisted) form at the absolute memory {@code address}to the memory so that it can be used by common program
	 * logic, usually to the field offset of a target object or an index of a target array.
	 * If {@code target} is null, the {@code targetOffset} is interpreted as an absolute memory address
	 * instead of a relative offset.
	 * 
	 * @param address the absolute source memory address of the value to be set.
	 * @param target the target object to set the value to or {@code null} for absolute memory addressing.
	 * @param targetOffset the target object's relative memory offset or an absolute target memory address.
	 * @param handler a helper instance to resolve OIDs to instance references.
	 * @return absolute source memory address pointing at the first byte following the read value.
	 */
	public long setValueToMemory(
		long                   address     ,
		Object                 target      ,
		long                   targetOffset,
		PersistenceLoadHandler handler
	);
}
