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

import one.microstream.chars.XChars;
import one.microstream.persistence.binary.exceptions.BinaryPersistenceException;
import one.microstream.persistence.types.PersistenceLoadHandler;

public interface ValidatingBinaryHandler<T, S>
{
	public default void validateState(final Binary data, final T instance, final PersistenceLoadHandler handler)
	{
		this.validateStates(
			instance,
			this.getValidationStateFromInstance(instance),
			this.getValidationStateFromBinary(data)
		);
	}
	
	public S getValidationStateFromInstance(T instance);
	
	public S getValidationStateFromBinary(Binary data);
	
	public default void validateStates(
		final T instance     ,
		final S instanceState,
		final S binaryState
	)
	{
		if(instanceState.equals(binaryState))
		{
			return;
		}
		
		this.throwInconsistentStateException(instance, instanceState, binaryState);
	}
	
	public default void throwInconsistentStateException(
		final T      instance                   ,
		final Object instanceStateRepresentation,
		final Object binaryStateRepresentation
	)
	{
		throw new BinaryPersistenceException(
			"Inconsistent state for instance " + XChars.systemString(instance) + ": \""
			+ instanceStateRepresentation + "\" not equal to \"" + binaryStateRepresentation + "\""
		);
	}
	
}
