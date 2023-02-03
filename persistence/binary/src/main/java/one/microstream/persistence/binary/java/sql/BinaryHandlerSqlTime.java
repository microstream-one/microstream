package one.microstream.persistence.binary.java.sql;

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

import java.sql.Time;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomNonReferentialFixedLength;
import one.microstream.persistence.binary.java.util.BinaryHandlerDate;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

/**
 * Blunt copy of {@link BinaryHandlerDate} for the as good as superfluous type {@link java.sql.Time}.
 *
 */
@SuppressWarnings("exports")
public final class BinaryHandlerSqlTime extends AbstractBinaryHandlerCustomNonReferentialFixedLength<Time>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerSqlTime New()
	{
		return new BinaryHandlerSqlTime();
	}

	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerSqlTime()
	{
		super(
			Time.class,
			CustomFields(
				CustomField(long.class, "timestamp")
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	private static long instanceState(final Time instance)
	{
		return instance.getTime();
	}
	
	private static long binaryState(final Binary data)
	{
		return data.read_long(0);
	}

	@Override
	public final void store(
		final Binary                          data    ,
		final Time                            instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		data.storeEntityHeader(Long.BYTES, this.typeId(), objectId);
		
		// the data content of a date is simple the timestamp long, nothing else
		data.store_long(instanceState(instance));
	}

	@Override
	public final Time create(final Binary data, final PersistenceLoadHandler handler)
	{
		return new Time(binaryState(data));
	}

	@Override
	public final void updateState(final Binary data, final Time instance, final PersistenceLoadHandler handler)
	{
		instance.setTime(binaryState(data));
	}

}
