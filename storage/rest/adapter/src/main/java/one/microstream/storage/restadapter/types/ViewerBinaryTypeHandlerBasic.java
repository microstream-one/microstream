package one.microstream.storage.restadapter.types;

/*-
 * #%L
 * microstream-storage-restadapter
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

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceTypeHandler;

public class ViewerBinaryTypeHandlerBasic<T> extends ViewerBinaryTypeHandlerWrapperAbstract<T>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public ViewerBinaryTypeHandlerBasic(final PersistenceTypeHandler<Binary, T> nativeHandler,
			final ViewerBinaryTypeHandlerGeneric genericHandler)
	{
		super(nativeHandler, genericHandler);
	}

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public ObjectDescription create(final Binary medium, final PersistenceLoadHandler handler)
	{
		final ObjectDescription objectDescription = this.genericHandler.create(medium, handler);
		objectDescription.setPrimitiveInstance(this.nativeHandler.create(medium, handler));

		return objectDescription;
	}

	@SuppressWarnings("unchecked") // safe by logic
	@Override
	public void updateState(final Binary medium, final Object instance, final PersistenceLoadHandler handler)
	{
		this.nativeHandler.updateState(medium, (T)((ObjectDescription)instance).getPrimitiveInstance(), handler);
	}

}
