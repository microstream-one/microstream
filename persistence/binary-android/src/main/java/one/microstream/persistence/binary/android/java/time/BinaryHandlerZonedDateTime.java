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

import java.time.ZonedDateTime;
import java.time.ZoneId;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomNonReferential;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerZonedDateTime extends AbstractBinaryHandlerCustomNonReferential<ZonedDateTime>
{
	static final long BINARY_OFFSET_YEAR   =                                   0L;
	static final long BINARY_OFFSET_MONTH  = BINARY_OFFSET_YEAR   + Integer.BYTES;
	static final long BINARY_OFFSET_DAY    = BINARY_OFFSET_MONTH  + Short  .BYTES;
	static final long BINARY_OFFSET_HOUR   = BINARY_OFFSET_DAY    + Short  .BYTES;
	static final long BINARY_OFFSET_MINUTE = BINARY_OFFSET_HOUR   + Byte   .BYTES;
	static final long BINARY_OFFSET_SECOND = BINARY_OFFSET_MINUTE + Byte   .BYTES;
	static final long BINARY_OFFSET_NANO   = BINARY_OFFSET_SECOND + Byte   .BYTES;
	static final long BINARY_OFFSET_ID     = BINARY_OFFSET_NANO   + Integer.BYTES;
	
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerZonedDateTime New()
	{
		return new BinaryHandlerZonedDateTime();
	}

	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerZonedDateTime()
	{
		super(
			ZonedDateTime.class,
			CustomFields(
				CustomField(int.class,   "year"  ),
				CustomField(short.class, "month" ),
				CustomField(short.class, "day"   ),
				CustomField(byte.class,  "hour"  ),
				CustomField(byte.class,  "minute"),
				CustomField(byte.class,  "second"),
				CustomField(int.class ,  "nano"  ),
				chars("id")
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	

	@Override
	public final void store(
		final Binary                          data    ,
		final ZonedDateTime                   instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		final String zoneId              = instance.getZone().getId();
		final long   entityContentLength = BINARY_OFFSET_ID + Binary.calculateBinaryLengthChars(zoneId.length());
		
		data.storeEntityHeader(entityContentLength, this.typeId(), objectId);
		
		data.store_int       (BINARY_OFFSET_YEAR  , instance.getYear());
		data.store_short     (BINARY_OFFSET_MONTH , (short)instance.getMonthValue());
		data.store_short     (BINARY_OFFSET_DAY   , (short)instance.getDayOfMonth());
		data.store_byte      (BINARY_OFFSET_HOUR  , (byte)instance.getHour());
		data.store_byte      (BINARY_OFFSET_MINUTE, (byte)instance.getMinute());
		data.store_byte      (BINARY_OFFSET_SECOND, (byte)instance.getSecond());
		data.store_int       (BINARY_OFFSET_NANO  , instance.getNano());
		data.storeStringValue(BINARY_OFFSET_ID    , zoneId);
	}

	@Override
	public final ZonedDateTime create(final Binary data, final PersistenceLoadHandler handler)
	{
		return ZonedDateTime.of(
			data.read_int  (BINARY_OFFSET_YEAR),
			data.read_short(BINARY_OFFSET_MONTH),
			data.read_short(BINARY_OFFSET_DAY),
			data.read_byte (BINARY_OFFSET_HOUR),
			data.read_byte (BINARY_OFFSET_MINUTE),
			data.read_byte (BINARY_OFFSET_SECOND),
			data.read_int  (BINARY_OFFSET_NANO),
			ZoneId.of(data.buildString(BINARY_OFFSET_ID))
		);
	}

	@Override
	public final void updateState(final Binary data, final ZonedDateTime instance, final PersistenceLoadHandler handler)
	{
		// no-op
	}

	@Override
	public final boolean hasPersistedVariableLength()
	{
		return true;
	}
	
	@Override
	public final boolean hasVaryingPersistedLengthInstances()
	{
		return true;
	}

}
