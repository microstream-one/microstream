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

import java.time.MonthDay;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomNonReferentialFixedLength;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerMonthDay extends AbstractBinaryHandlerCustomNonReferentialFixedLength<MonthDay>
{
	static final long BINARY_OFFSET_MONTH =                                  0L;
	static final long BINARY_OFFSET_DAY   = BINARY_OFFSET_MONTH + Integer.BYTES;
	static final long BINARY_LENGTH       = BINARY_OFFSET_DAY   + Integer.BYTES;
	
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerMonthDay New()
	{
		return new BinaryHandlerMonthDay();
	}

	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerMonthDay()
	{
		super(
			MonthDay.class,
			CustomFields(
				CustomField(int.class, "month"),
				CustomField(int.class, "day"  )
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	

	@Override
	public final void store(
		final Binary                          data    ,
		final MonthDay                        instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		data.storeEntityHeader(BINARY_LENGTH, this.typeId(), objectId);
		
		data.store_int(BINARY_OFFSET_MONTH, instance.getMonthValue());
		data.store_int(BINARY_OFFSET_DAY  , instance.getDayOfMonth());
	}

	@Override
	public final MonthDay create(final Binary data, final PersistenceLoadHandler handler)
	{
		return MonthDay.of(
			data.read_int(BINARY_OFFSET_MONTH),
			data.read_int(BINARY_OFFSET_DAY)
		);
	}

	@Override
	public final void updateState(final Binary data, final MonthDay instance, final PersistenceLoadHandler handler)
	{
		// no-op
	}

}
