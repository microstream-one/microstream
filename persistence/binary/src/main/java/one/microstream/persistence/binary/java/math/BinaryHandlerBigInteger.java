package one.microstream.persistence.binary.java.math;

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

import java.math.BigInteger;
import java.util.Arrays;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomValueVariableLength;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerBigInteger extends AbstractBinaryHandlerCustomValueVariableLength<BigInteger, byte[]>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerBigInteger New()
	{
		return new BinaryHandlerBigInteger();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerBigInteger()
	{
		super(
			BigInteger.class,
			CustomFields(
				bytes("value")
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	private static byte[] instanceState(final BigInteger instance)
	{
		return instance.toByteArray();
	}
	
	private static byte[] binaryState(final Binary data)
	{
		return data.build_bytes();
	}

	@Override
	public void store(
		final Binary                          data    ,
		final BigInteger                      instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		data.store_bytes(this.typeId(), objectId, instance.toByteArray());
	}

	@Override
	public BigInteger create(final Binary data, final PersistenceLoadHandler handler)
	{
		return new BigInteger(data.build_bytes());
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// validation //
	///////////////
	
	// actually never called, just to satisfy the interface
	@Override
	public byte[] getValidationStateFromInstance(final BigInteger instance)
	{
		return instanceState(instance);
	}

	// actually never called, just to satisfy the interface
	@Override
	public byte[] getValidationStateFromBinary(final Binary data)
	{
		return binaryState(data);
	}
	
	@Override
	public void validateState(
		final Binary                 data    ,
		final BigInteger             instance,
		final PersistenceLoadHandler handler
	)
	{
		final byte[] instanceState = instanceState(instance);
		final byte[] binaryState   = binaryState(data);
		
		if(Arrays.equals(instanceState, binaryState))
		{
			return;
		}
		
		this.throwInconsistentStateException(instance, Arrays.toString(instanceState), Arrays.toString(binaryState));
	}
	
}
