package one.microstream.storage.restadapter.types;

/*-
 * #%L
 * microstream-storage-restadapter
 * %%
 * Copyright (C) 2019 - 2021 MicroStream Software
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

import java.lang.reflect.Array;

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceTypeHandler;

public class ViewerBinaryTypeHandlerNativeArray<T> extends ViewerBinaryTypeHandlerWrapperAbstract<T>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public ViewerBinaryTypeHandlerNativeArray(final PersistenceTypeHandler<Binary, T> nativeHandler)
	{
		super(nativeHandler);
	}

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public ObjectDescription create(final Binary medium, final PersistenceLoadHandler handler)
	{
		final ObjectDescription objectDescription = new ObjectDescription();
		objectDescription.setObjectId(medium.getBuildItemObjectId());
		objectDescription.setPersistenceTypeDefinition(this.nativeHandler);

		final T value = this.nativeHandler.create(medium, handler);
		this.nativeHandler.updateState(medium, value, handler);

		final int length = Array.getLength(value);
		final Object objArray[] = new Object[length];
		for(int i = 0; i < length; i++)
		{
			objArray[i] = Array.get(value, i);
		}

		objectDescription.setValues(new Object[] {objArray});

		objectDescription.setLength(0);
		objectDescription.setVariableLength(new Long[] {(long) length});

		return objectDescription;
	}

}
