package one.microstream.persistence.binary.java.lang;

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

import one.microstream.chars.XChars;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;


public final class BinaryHandlerStringBuffer extends AbstractBinaryHandlerAbstractStringBuilder<StringBuffer>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerStringBuffer New()
	{
		return new BinaryHandlerStringBuffer();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerStringBuffer()
	{
		super(StringBuffer.class);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final void store(
		final Binary                          data    ,
		final StringBuffer                    instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		this.storeData(data, XChars.toCharArray(instance), instance.capacity(), objectId, handler);
	}

	@Override
	public final StringBuffer create(final Binary data, final PersistenceLoadHandler handler)
	{
		return new StringBuffer(this.readCapacity(data));
	}

	@Override
	public void updateState(final Binary data, final StringBuffer instance, final PersistenceLoadHandler handler)
	{
		// because implementing a clear() would have been too hard for the JDK Pros.
		instance.delete(0, instance.length());
		
		instance.ensureCapacity(this.readCapacity(data));
		instance.append(this.readChars(data));
	}

}
