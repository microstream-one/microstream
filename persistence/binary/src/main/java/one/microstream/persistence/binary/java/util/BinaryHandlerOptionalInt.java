package one.microstream.persistence.binary.java.util;

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

import java.util.OptionalInt;

import one.microstream.memory.XMemory;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomValueFixedLength;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerOptionalInt
extends AbstractBinaryHandlerCustomValueFixedLength<OptionalInt, Integer>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	static final long
		BINARY_OFFSET_IS_PRESENT =                                        0,
		BINARY_OFFSET_VALUE      = BINARY_OFFSET_IS_PRESENT + Byte   .BYTES, // Boolean.BYTES does not exist
		BINARY_LENGTH            = BINARY_OFFSET_VALUE      + Integer.BYTES
	;

	
	
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerOptionalInt New()
	{
		return new BinaryHandlerOptionalInt();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerOptionalInt()
	{
		/*
		 * Note on fields:
		 * These are not tied to JDK-specific internals. It's merely a sufficient state
		 * (even properly queryable in the implementation, fancy that) to represent an instance.
		 * The identical naming is "by coincidence" and will continue to work even if they change theirs internally.
		 */
		super(
			OptionalInt.class,
			CustomFields(
				CustomField(boolean.class, "isPresent"),
				CustomField(int    .class, "value"    )
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	private static int instanceState(final OptionalInt instance)
	{
		// or ELSE!!!
		return instance.orElse(0);
	}
	
	private static int binaryState(final Binary data)
	{
		return data.read_int(BINARY_OFFSET_VALUE);
	}

	@Override
	public void store(
		final Binary                          data    ,
		final OptionalInt                     instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		data.storeEntityHeader(BINARY_LENGTH, this.typeId(), objectId);
		data.store_boolean(
			BINARY_OFFSET_IS_PRESENT,
			instance.isPresent()
		);

		data.store_int(
			BINARY_OFFSET_VALUE,
			instanceState(instance)
		);
	}

	@Override
	public OptionalInt create(final Binary data, final PersistenceLoadHandler handler)
	{
		final boolean isPresent = data.read_boolean(BINARY_OFFSET_IS_PRESENT);
		
		// luckily, an uninitialized instance (all-zeroes, meaning isPresent == false) is all that is required.
		return isPresent
			? OptionalInt.of(
				data.read_int(BINARY_OFFSET_VALUE)
			)
			: XMemory.instantiateBlank(OptionalInt.class)
		;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// validation //
	///////////////
	
	// actually never called, just to satisfy the interface
	@Override
	public Integer getValidationStateFromInstance(final OptionalInt instance)
	{
		return instanceState(instance);
	}

	// actually never called, just to satisfy the interface
	@Override
	public Integer getValidationStateFromBinary(final Binary data)
	{
		return binaryState(data);
	}
	
	@Override
	public void validateState(
		final Binary                 data    ,
		final OptionalInt            instance,
		final PersistenceLoadHandler handler
	)
	{
		final int instanceState = instanceState(instance);
		final int binaryState   = binaryState(data);
		
		if(instanceState == binaryState)
		{
			return;
		}
		
		this.throwInconsistentStateException(instance, instanceState, binaryState);
	}
	
}
