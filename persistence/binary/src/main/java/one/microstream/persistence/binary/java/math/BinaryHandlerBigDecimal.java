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

import java.math.BigDecimal;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomValueVariableLength;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerBigDecimal extends AbstractBinaryHandlerCustomValueVariableLength<BigDecimal, String>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerBigDecimal New()
	{
		return new BinaryHandlerBigDecimal();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerBigDecimal()
	{
		super(
			BigDecimal.class,
			CustomFields(
				chars("value")
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	private static String instanceState(final BigDecimal instance)
	{
		return instance.toString();
	}
	
	private static String binaryState(final Binary data)
	{
		return data.buildString();
	}

	@Override
	public void store(
		final Binary                          data    ,
		final BigDecimal                      instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		// there's a char[] constructor but no char[] utility method, so there's no other option than this
		data.storeStringSingleValue(this.typeId(), objectId, instanceState(instance));
	}

	@Override
	public BigDecimal create(final Binary data, final PersistenceLoadHandler handler)
	{
		return new BigDecimal(binaryState(data));
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// validation //
	///////////////
	
	@Override
	public String getValidationStateFromInstance(final BigDecimal instance)
	{
		return instanceState(instance);
	}
	
	@Override
	public String getValidationStateFromBinary(final Binary data)
	{
		return binaryState(data);
	}

}
