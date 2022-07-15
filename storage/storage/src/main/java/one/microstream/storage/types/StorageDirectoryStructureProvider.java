package one.microstream.storage.types;

/*-
 * #%L
 * microstream-storage
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

import one.microstream.afs.types.ADirectory;


public interface StorageDirectoryStructureProvider
{
	public ADirectory provideChannelDirectory(
		ADirectory              storageRootDirectory,
		int                     channelIndex        ,
		StorageFileNameProvider fileNameProvider
	);
	
	
	public interface Defaults
	{
		public static StorageDirectoryStructureProvider defaultDirectoryStructureProvider()
		{
			return Default.DEFAULT;
		}
	}
	
		
	public static StorageDirectoryStructureProvider New()
	{
		return new StorageDirectoryStructureProvider.Default();
	}
	
	public final class Default implements StorageDirectoryStructureProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// constants //
		//////////////
		
		static final StorageDirectoryStructureProvider.Default DEFAULT = new StorageDirectoryStructureProvider.Default();
		
		
		
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
		public final ADirectory provideChannelDirectory(
			final ADirectory              storageRootDirectory,
			final int                     channelIndex        ,
			final StorageFileNameProvider fileNameProvider
		)
		{
			final String channelDirectoryName = fileNameProvider.provideChannelDirectoryName(channelIndex);
			final ADirectory channelDirectory = storageRootDirectory.ensureDirectory(channelDirectoryName);
			
			channelDirectory.ensureExists();
			
			return channelDirectory;
		}
		
	}
	
}
