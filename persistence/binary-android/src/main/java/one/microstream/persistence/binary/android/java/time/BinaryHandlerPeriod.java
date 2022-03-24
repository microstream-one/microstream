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

import java.time.Period;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomNonReferentialFixedLength;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerPeriod extends AbstractBinaryHandlerCustomNonReferentialFixedLength<Period>
{
	static final long BINARY_OFFSET_YEARS  =                                   0L;
	static final long BINARY_OFFSET_MONTHS = BINARY_OFFSET_YEARS  + Integer.BYTES;
	static final long BINARY_OFFSET_DAYS   = BINARY_OFFSET_MONTHS + Integer.BYTES;
	static final long BINARY_LENGTH        = BINARY_OFFSET_DAYS   + Integer.BYTES;
	
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerPeriod New()
	{
		return new BinaryHandlerPeriod();
	}

	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerPeriod()
	{
		super(
			Period.class,
			CustomFields(
				CustomField(int.class, "years" ),
				CustomField(int.class, "months"),
				CustomField(int.class, "days"  )
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	

	@Override
	public final void store(
		final Binary                          data    ,
		final Period                          instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		data.storeEntityHeader(BINARY_LENGTH, this.typeId(), objectId);
		
		data.store_int(BINARY_OFFSET_YEARS , instance.getYears());
		data.store_int(BINARY_OFFSET_MONTHS, instance.getMonths());
		data.store_int(BINARY_OFFSET_DAYS  , instance.getDays());
	}

	@Override
	public final Period create(final Binary data, final PersistenceLoadHandler handler)
	{
		return Period.of(
			data.read_int(BINARY_OFFSET_YEARS),
			data.read_int(BINARY_OFFSET_MONTHS),
			data.read_int(BINARY_OFFSET_DAYS)
		);
	}

	@Override
	public final void updateState(final Binary data, final Period instance, final PersistenceLoadHandler handler)
	{
		// no-op
	}

}
