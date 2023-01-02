package one.microstream.storage.embedded.types;

/*-
 * #%L
 * MicroStream Embedded Storage
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

import one.microstream.persistence.types.ObjectIdsProcessor;
import one.microstream.persistence.types.ObjectIdsSelector;
import one.microstream.persistence.types.PersistenceObjectRegistry;

public interface EmbeddedStorageObjectRegistryCallback extends ObjectIdsSelector
{
	public void initializeObjectRegistry(PersistenceObjectRegistry objectRegistry);



	public static EmbeddedStorageObjectRegistryCallback New()
	{
		return new EmbeddedStorageObjectRegistryCallback.Default();
	}

	public final class Default implements EmbeddedStorageObjectRegistryCallback
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private PersistenceObjectRegistry objectRegistry;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default()
		{
			super();
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public synchronized void initializeObjectRegistry(final PersistenceObjectRegistry objectRegistry)
		{
			if(this.objectRegistry != null)
			{
				if(this.objectRegistry == objectRegistry)
				{
					return;
				}

				// (29.07.2022 TM)EXCP: proper exception
				throw new RuntimeException("ObjectRegistry already initialized.");
			}

			this.objectRegistry = objectRegistry;
		}

		@Override
		public synchronized boolean processSelected(final ObjectIdsProcessor processor)
		{
			if(this.objectRegistry == null)
			{
				// object registry not yet initialized (i.e. no application-side storage connection yet)
				processor.processObjectIdsByFilter(objectId -> false);
				return true;
			}

			// efficient for embedded mode, but server mode should use #selectLiveObjectIds instead.
			return this.objectRegistry.processLiveObjectIds(processor);
		}

	}

}
