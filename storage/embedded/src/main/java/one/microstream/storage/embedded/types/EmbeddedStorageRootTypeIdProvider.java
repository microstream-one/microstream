package one.microstream.storage.embedded.types;

/*-
 * #%L
 * microstream-storage-embedded
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

import static one.microstream.math.XMath.positive;

import one.microstream.persistence.types.PersistenceTypeManager;
import one.microstream.storage.exceptions.StorageExceptionInitialization;
import one.microstream.storage.types.StorageRootTypeIdProvider;

public interface EmbeddedStorageRootTypeIdProvider extends StorageRootTypeIdProvider
{
	public void initialize(PersistenceTypeManager typeIdResolver);



	public static EmbeddedStorageRootTypeIdProvider New(final Class<?> rootType)
	{
		return new EmbeddedStorageRootTypeIdProvider.Default(rootType);
	}

	public final class Default implements EmbeddedStorageRootTypeIdProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final Class<?> rootType;

		private transient Long cachedRootTypeId;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(final Class<?> rootType)
		{
			super();
			this.rootType = rootType;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final long provideRootTypeId()
		{
			if(this.cachedRootTypeId == null)
			{
				throw new StorageExceptionInitialization("not initialized");
			}
			return this.cachedRootTypeId;
		}

		@Override
		public final void initialize(final PersistenceTypeManager typeIdResolver)
		{
			final long typeId = typeIdResolver.ensureTypeId(this.rootType);
			this.cachedRootTypeId = positive(typeId);
		}

	}

}
