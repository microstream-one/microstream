package one.microstream.persistence.binary.android.java.time;

/*-
 * #%L
 * microstream-persistence-binary-android
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

import java.time.LocalTime;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomNonReferentialFixedLength;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerLocalTime extends AbstractBinaryHandlerCustomNonReferentialFixedLength<LocalTime>
{
	static final long BINARY_OFFSET_HOUR   =                                   0L;
	static final long BINARY_OFFSET_MINUTE = BINARY_OFFSET_HOUR   + Byte   .BYTES;
	static final long BINARY_OFFSET_SECOND = BINARY_OFFSET_MINUTE + Byte   .BYTES;
	static final long BINARY_OFFSET_NANO   = BINARY_OFFSET_SECOND + Byte   .BYTES;
	static final long BINARY_LENGTH        = BINARY_OFFSET_NANO   + Integer.BYTES;
	
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerLocalTime New()
	{
		return new BinaryHandlerLocalTime();
	}

	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerLocalTime()
	{
		super(
			LocalTime.class,
			CustomFields(
				CustomField(byte.class, "hour"  ),
				CustomField(byte.class, "minute"),
				CustomField(byte.class, "second"),
				CustomField(int.class , "nano"  )
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	

	@Override
	public final void store(
		final Binary                          data    ,
		final LocalTime                       instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		data.storeEntityHeader(BINARY_LENGTH, this.typeId(), objectId);
		
		data.store_byte(BINARY_OFFSET_HOUR  , (byte)instance.getHour());
		data.store_byte(BINARY_OFFSET_MINUTE, (byte)instance.getMinute());
		data.store_byte(BINARY_OFFSET_SECOND, (byte)instance.getSecond());
		data.store_int (BINARY_OFFSET_NANO  , instance.getNano());
	}

	@Override
	public final LocalTime create(final Binary data, final PersistenceLoadHandler handler)
	{
		return LocalTime.of(
			data.read_byte(BINARY_OFFSET_HOUR),
			data.read_byte(BINARY_OFFSET_MINUTE),
			data.read_byte(BINARY_OFFSET_SECOND),
			data.read_int (BINARY_OFFSET_NANO)
		);
	}

	@Override
	public final void updateState(final Binary data, final LocalTime instance, final PersistenceLoadHandler handler)
	{
		// no-op
	}

}
