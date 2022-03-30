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

import java.util.Currency;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomValueVariableLength;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerCurrency extends AbstractBinaryHandlerCustomValueVariableLength<Currency, String>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerCurrency New()
	{
		return new BinaryHandlerCurrency();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerCurrency()
	{
		super(
			Currency.class,
			CustomFields(
				chars("currencyCode")
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	private static String instanceState(final Currency instance)
	{
		return instance.getCurrencyCode();
	}
	
	private static String binaryState(final Binary data)
	{
		return data.buildString();
	}

	@Override
	public final void store(
		final Binary                          data    ,
		final Currency                        instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		data.storeStringSingleValue(this.typeId(), objectId, instanceState(instance));
	}

	@Override
	public Currency create(
		final Binary                 data   ,
		final PersistenceLoadHandler handler
	)
	{
		return Currency.getInstance(binaryState(data));
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// validation //
	///////////////
	
	@Override
	public String getValidationStateFromInstance(final Currency instance)
	{
		return instanceState(instance);
	}
	
	@Override
	public String getValidationStateFromBinary(final Binary data)
	{
		return binaryState(data);
	}

}
