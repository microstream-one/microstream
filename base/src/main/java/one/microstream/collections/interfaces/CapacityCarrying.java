package one.microstream.collections.interfaces;

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

public interface CapacityCarrying extends Sized
{
	/**
	 * Returns the maximum amount of elements this carrier instance can contain.<br>
	 * The actual value may be depending on the configuration of the concrete instance or may depend only on the
	 * implementation of the carrier (meaning it is constant for all instances of the implementation,
	 * e.g. {@link Integer#MAX_VALUE})
	 *
	 * @return the maximum amount of elements this carrier instance can contain.
	 */
	public long maximumCapacity();

	/**
	 * @return the amount of elements this carrier instance can collect before reaching its maximimum capacity.
	 *
	 */
	public default long remainingCapacity()
	{
		return this.maximumCapacity() - this.size();
	}

	/**
	 * @return true if the current capacity cannot be increased anymore.
	 */
	public default boolean isFull()
	{
		return this.remainingCapacity() == 0L;
	}

}
