package one.microstream.persistence.binary.java.lang;

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

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomValueFixedLength;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerCharacter extends AbstractBinaryHandlerCustomValueFixedLength<Character, Character>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerCharacter New()
	{
		return new BinaryHandlerCharacter();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerCharacter()
	{
		super(Character.class, defineValueType(char.class));
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	private static char instanceState(final Character instance)
	{
		return instance.charValue();
	}
	
	private static char binaryState(final Binary data)
	{
		return data.read_char(0);
	}

	@Override
	public void store(
		final Binary                          data    ,
		final Character                       instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		data.storeCharacter(this.typeId(), objectId, instance.charValue());
	}

	@Override
	public Character create(final Binary data, final PersistenceLoadHandler handler)
	{
		return data.buildCharacter();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// validation //
	///////////////
	
	// actually never called, just to satisfy the interface
	@Override
	public Character getValidationStateFromInstance(final Character instance)
	{
		return instance;
	}

	// actually never called, just to satisfy the interface
	@Override
	public Character getValidationStateFromBinary(final Binary data)
	{
		return binaryState(data);
	}
	
	@Override
	public void validateState(
		final Binary                 data    ,
		final Character              instance,
		final PersistenceLoadHandler handler
	)
	{
		final char instanceState = instanceState(instance);
		final char binaryState   = binaryState(data);
		
		if(instanceState == binaryState)
		{
			return;
		}
		
		this.throwInconsistentStateException(instance, instanceState, binaryState);
	}

}
