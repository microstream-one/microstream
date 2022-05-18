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

public final class BinaryHandlerByte extends AbstractBinaryHandlerCustomValueFixedLength<Byte, Byte>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerByte New()
	{
		return new BinaryHandlerByte();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerByte()
	{
		super(Byte.class, defineValueType(byte.class));
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	private static byte instanceState(final Byte instance)
	{
		return instance.byteValue();
	}
	
	private static byte binaryState(final Binary data)
	{
		return data.read_byte(0);
	}

	@Override
	public void store(
		final Binary                          data    ,
		final Byte                            instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		data.storeByte(this.typeId(), objectId, instance.byteValue());
	}

	@Override
	public Byte create(final Binary data, final PersistenceLoadHandler handler)
	{
		return data.buildByte();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// validation //
	///////////////
	
	// actually never called, just to satisfy the interface
	@Override
	public Byte getValidationStateFromInstance(final Byte instance)
	{
		// well, lol
		return instance;
	}

	// actually never called, just to satisfy the interface
	@Override
	public Byte getValidationStateFromBinary(final Binary data)
	{
		return binaryState(data);
	}
	
	@Override
	public void validateState(
		final Binary                 data    ,
		final Byte                   instance,
		final PersistenceLoadHandler handler
	)
	{
		final byte instanceState = instanceState(instance);
		final byte binaryState   = binaryState(data);
		
		if(instanceState == binaryState)
		{
			return;
		}
		
		this.throwInconsistentStateException(instance, instanceState, binaryState);
	}

}
