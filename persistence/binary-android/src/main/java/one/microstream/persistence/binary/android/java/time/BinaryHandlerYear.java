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

import java.time.Year;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomNonReferentialFixedLength;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerYear extends AbstractBinaryHandlerCustomNonReferentialFixedLength<Year>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerYear New()
	{
		return new BinaryHandlerYear();
	}

	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerYear()
	{
		super(
			Year.class,
			CustomFields(
				CustomField(int.class, "year")
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	

	@Override
	public final void store(
		final Binary                          data    ,
		final Year                            instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		data.storeEntityHeader(Integer.BYTES, this.typeId(), objectId);
		
		data.store_long(instance.getValue());
	}

	@Override
	public final Year create(final Binary data, final PersistenceLoadHandler handler)
	{
		return Year.of(data.read_int(0L));
	}

	@Override
	public final void updateState(final Binary data, final Year instance, final PersistenceLoadHandler handler)
	{
		// no-op
	}

}
