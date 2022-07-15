package one.microstream.persistence.binary.java.time;

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

import static one.microstream.X.Constant;

import java.time.ZoneOffset;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomValueFixedLength;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

// custom type handler for zone offset, needed because of transient field ZoneOffset#id
public final class BinaryHandlerZoneOffset extends AbstractBinaryHandlerCustomValueFixedLength<ZoneOffset, Integer>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static BinaryHandlerZoneOffset New()
	{
		return new BinaryHandlerZoneOffset();
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerZoneOffset()
	{
		super(
			ZoneOffset.class,
			Constant(
				CustomField(int.class, "totalSeconds")
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	private static int instanceState(final ZoneOffset instance)
	{
		return instance.getTotalSeconds();
	}

	private static int binaryState(final Binary data)
	{
		return data.read_int(0L);
	}

	@Override
	public void store(
		final Binary                          data    ,
		final ZoneOffset                      instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		data.storeInteger(this.typeId(), objectId, instanceState(instance));
	}

	@Override
	public ZoneOffset create(final Binary data, final PersistenceLoadHandler handler)
	{
		return ZoneOffset.ofTotalSeconds(binaryState(data));
	}

	///////////////////////////////////////////////////////////////////////////
	// validation //
	///////////////

	// actually never called, just to satisfy the interface
	@Override
	public Integer getValidationStateFromInstance(final ZoneOffset instance)
	{
		return instance.getTotalSeconds();
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
		final ZoneOffset             instance,
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
