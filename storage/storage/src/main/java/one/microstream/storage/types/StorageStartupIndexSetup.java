package one.microstream.storage.types;

/*-
 * #%L
 * MicroStream Storage
 * %%
 * Copyright (C) 2019 - 2023 MicroStream Software
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

public interface StorageStartupIndexSetup {

	public StorageStartupIndexFileProvider startupIndexFileProvider();
	
	public StorageStartupFileIndexer startupFileIndexer();

	public StorageStartupIndexManager createStartupIndexManager();
	
	public class Default implements StorageStartupIndexSetup
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final StorageStartupIndexFileProvider startupIndexFileProvider;
		private final StorageStartupFileIndexer       storageStartupFileIndexer;
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		public Default(
			final StorageStartupIndexFileProvider startupIndexFileProvider,
			final StorageStartupFileIndexer       storageStartupFileIndexer
		)
		{
			super();
			this.startupIndexFileProvider = startupIndexFileProvider;
			this.storageStartupFileIndexer = storageStartupFileIndexer;
		}

		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public StorageStartupIndexFileProvider startupIndexFileProvider()
		{
			return this.startupIndexFileProvider;
		}
		
		@Override
		public StorageStartupFileIndexer startupFileIndexer()
		{
			return this.storageStartupFileIndexer;
		}

		@Override
		public StorageStartupIndexManager createStartupIndexManager()
		{
			return new StorageStartupIndexManager.Default(
				this.startupIndexFileProvider,
				this.storageStartupFileIndexer);
		}
		
	}
}
