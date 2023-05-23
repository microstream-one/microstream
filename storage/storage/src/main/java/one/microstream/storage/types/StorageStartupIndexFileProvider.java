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

import static one.microstream.X.notNull;

import java.util.ArrayList;
import java.util.List;

import one.microstream.afs.types.ADirectory;
import one.microstream.afs.types.AFile;
import one.microstream.afs.types.AFileSystem;

public interface StorageStartupIndexFileProvider
{
	/**
	 * Provides the AFileSystem for the startup index files.
	 * 
	 * @return the used AFileSystem
	 */
	public AFileSystem fileSystem();

	/**
	 * Provides the base directory that for the startup index files.
	 * 
	 * @return ADirectory the startup index base directory.
	 */
	public ADirectory baseDirectory();
	
	/**
	 * Provide the directory that is used to store the StorageStartupIndexFiles.
	 * 
	 * @return ADirectory the directory in the {@link #baseDirectory()} that is used to store the StorageStartupIndexFiles.
	 */
	public ADirectory provideIndexDirectory();
	
	/**
	 * Provides the StorageStartupIndexFile for the supplied StorageDataFile
	 * 
	 * @param dataFile a StorageDataFile
	 * @return a StorageStartupIndexFile
	 */
	public StorageStartupIndexFile provideStartupIndexFile(StorageDataFile dataFile);
	
	/**
	 * Collect all index files for a storage channel.
	 * 
	 * @param channelIndex the storage channel.
	 * @return index files associated to the channel.
	 */
	public List<AFile> getFiles(int channelIndex);
	
	public interface StorageStartupIndexFileNameProvider
	{
		public String startupIndexDirectoryName();
		
		public String startupIndexFilePrefix();
		
		public String startupIndexFileSuffix();
		
		public String provideStartupIndexFileName(int channelIndex, long fileNumber);
		
		public interface Defaults {
			
			public static String defaultStartupIndexDirectoryName()
			{
				return "startupindex";
			}
			
			public static String defaultIndexFilePrefix()
			{
				return "index_";
			}
			
			public static String defaultIndexFileSuffix()
			{
				return "idx";
			}
		}
		
		public static StorageStartupIndexFileNameProvider New()
		{
			return Default.DEFAULT;
		}
		
		public static StorageStartupIndexFileNameProvider New(
			final String indexDirectory ,
			final String indexFilePrefix,
			final String indexFileSuffix
		)
		{
			return new StorageStartupIndexFileNameProvider.Default(
				notNull(indexDirectory ),
				notNull(indexFilePrefix),
				notNull(indexFileSuffix)
			);
		}
		
		public final class Default implements StorageStartupIndexFileNameProvider
		{
			///////////////////////////////////////////////////////////////////////////
			// constants        //
			/////////////////////
			
			static final StorageStartupIndexFileNameProvider.Default DEFAULT = new StorageStartupIndexFileNameProvider.Default(
				Defaults.defaultStartupIndexDirectoryName(),
				Defaults.defaultIndexFilePrefix(),
				Defaults.defaultIndexFileSuffix()
			);
			
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			private final String indexDirectory;
			private final String indexFilePrefix;
			private final String indexFileSuffix;
			
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////
			
			public Default(
				final String indexDirectory ,
				final String indexFilePrefix,
				final String indexFileSuffix
			)
			{
				this.indexDirectory  = indexDirectory;
				this.indexFilePrefix = indexFilePrefix;
				this.indexFileSuffix = indexFileSuffix;
			}
			
			///////////////////////////////////////////////////////////////////////////
			// methods //
			////////////
			
			@Override
			public String startupIndexDirectoryName()
			{
				return this.indexDirectory;
			}

			@Override
			public String startupIndexFilePrefix()
			{
				return this.indexFilePrefix;
			}

			@Override
			public String startupIndexFileSuffix()
			{
				return this.indexFileSuffix;
			}

			@Override
			public String provideStartupIndexFileName(final int channelIndex, final long fileNumber)
			{
				return this.startupIndexFilePrefix() + channelIndex + "_" + fileNumber;
			}
		}
	}
	
	
	public final class Default implements StorageStartupIndexFileProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final AFileSystem                         fileSystem;
		private final ADirectory                          baseDirectory;
		private final StorageStartupIndexFileNameProvider fileNameProvider;
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		public Default(
			final ADirectory                          baseDirectory,
			final StorageStartupIndexFileNameProvider fileNameProvider
		)
		{
			super();
			this.baseDirectory    = baseDirectory;
			this.fileSystem       = baseDirectory.fileSystem();
			this.fileNameProvider = fileNameProvider;
		}

		@Override
		public StorageStartupIndexFile provideStartupIndexFile(final StorageDataFile dataFile)
		{
			final ADirectory indexDirectory = this.provideIndexDirectory();
			final String     indexFileName  = this.fileNameProvider.provideStartupIndexFileName(dataFile.channelIndex(), dataFile.number());
			final String     indexFileType  = this.fileNameProvider.startupIndexFileSuffix();
			final AFile      file           = indexDirectory.ensureFile(indexFileName, indexFileType);
			
			return new StorageStartupIndexFile.Default(file, dataFile.channelIndex());
		}
 
		@Override
		public ADirectory provideIndexDirectory()
		{
			final ADirectory indexDirectory = this.baseDirectory().ensureDirectory(this.fileNameProvider.startupIndexDirectoryName());
			indexDirectory.ensureExists();
			return indexDirectory;
		}

		@Override
		public AFileSystem fileSystem()
		{
			return this.fileSystem;
		}

		@Override
		public ADirectory baseDirectory()
		{
			return this.baseDirectory;
		}

		@Override
		public List<AFile> getFiles(final int channelIndex)
		{
			final List<AFile> indexFiles = new ArrayList<>();
			
			this.provideIndexDirectory().iterateFiles( f -> {
				
				if(f.name().startsWith(this.fileNameProvider.startupIndexFilePrefix()))
				{
					if(f.type().endsWith(this.fileNameProvider.startupIndexFileSuffix()))
					{
						final String middlePart = f.name().substring(this.fileNameProvider.startupIndexFilePrefix().length());
						final int separatorIndex = middlePart.indexOf('_');
						if(separatorIndex >= 0)
						{
							final String hashIndexString = middlePart.substring(0, separatorIndex);
							try
							{
								if(Integer.parseInt(hashIndexString) == channelIndex)
								{
									@SuppressWarnings("unused")
									final String fileNumberString = middlePart.substring(separatorIndex + 1);
									//if parsed successfully file name is verified.
									indexFiles.add(f);
								}
							}
							catch(final NumberFormatException e)
							{
								//ignore NumberFormatException, ignore file
							}
						}

					}
				}
					
			});
			return indexFiles;
		}
	}

}
