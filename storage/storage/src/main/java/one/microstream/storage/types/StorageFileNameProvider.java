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

import static one.microstream.X.coalesce;
import static one.microstream.X.notNull;

import java.util.function.Consumer;

import one.microstream.afs.types.AFile;
import one.microstream.chars.VarString;
import one.microstream.persistence.types.Persistence;
import one.microstream.storage.exceptions.StorageException;

public interface StorageFileNameProvider
{
	public String provideChannelDirectoryName(int hashIndex);
	
	public String channelDirectoryPrefix();
	
	public String dataFileType();
	
	public String transactionsFileType();

	public String rescuedFileType();
	
	public String typeDictionaryFileName();

	public String lockFileName();

	public String provideDataFileName(int channelIndex, long fileNumber);

	public String provideTransactionsFileName(int channelIndex);
	
	/* (18.06.2020 TM)TODO: remove parsing from filename provider.
	 * So far, the meta information of a file are parsed from its file name.
	 * This is dangerous since renaming a file would affect (= destroy) the storage data order and consistency.
	 * The clean solution would be to embed the meta data in a file header and then validate
	 * the file name against that information.
	 * But until then, the name provider must also serve as a parser.
	 */
	public <F extends StorageDataFile> void parseDataInventoryFile(
		StorageDataFile.Creator<F> fileCreator ,
		Consumer<? super F>        collector   ,
		int                        channelIndex,
		AFile                      file
	);
	
	
	
	
	public interface Defaults
	{
		public static String defaultChannelDirectoryPrefix()
		{
			return "channel_";
		}
		
		public static String defaultDataFilePrefix()
		{
			return "channel_";
		}
		
		public static String defaultDataFileSuffix()
		{
			return "dat";
		}

		public static String defaultTransactionsFilePrefix()
		{
			return "transactions_";
		}
		
		public static String defaultTransactionsFileSuffix()
		{
			return "sft"; // "storage file transactions"
		}
		
		public static String defaultRescuedFileSuffix()
		{
			return "bak"; // "backup" - although admittedly, that might be a bit confusing with the BackupFile concept.
		}

		public static String defaultTypeDictionaryFileName()
		{
			return Persistence.defaultFilenameTypeDictionary();
		}
		
		public static String defaultLockFileName()
		{
			return "used.lock";
		}
		
		public static StorageFileNameProvider defaultFileNameProvider()
		{
			return Default.DEFAULT;
		}
		
	}
	
	
	
	public static StorageFileNameProvider New(
		final String channelDirectoryPrefix,
		final String dataFilePrefix        ,
		final String dataFileSuffix        ,
		final String transactionsFilePrefix,
		final String transactionsFileSuffix,
		final String rescuedFileSuffix     ,
		final String typeDictionaryFileName,
		final String lockFileName
	)
	{
		return new StorageFileNameProvider.Default(
			notNull(channelDirectoryPrefix),
			notNull(dataFilePrefix)        ,
			notNull(dataFileSuffix)        ,
			notNull(transactionsFilePrefix),
			notNull(transactionsFileSuffix),
			notNull(rescuedFileSuffix)     ,
			notNull(typeDictionaryFileName),
			notNull(lockFileName)
		);
	}
	
	public final class Default implements StorageFileNameProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// constants        //
		/////////////////////
		
		static final StorageFileNameProvider.Default DEFAULT = new StorageFileNameProvider.Default(
			Defaults.defaultChannelDirectoryPrefix(),
			Defaults.defaultDataFilePrefix        (),
			Defaults.defaultDataFileSuffix        (),
			Defaults.defaultTransactionsFilePrefix(),
			Defaults.defaultTransactionsFileSuffix(),
			Defaults.defaultRescuedFileSuffix()     ,
			Defaults.defaultTypeDictionaryFileName(),
			Defaults.defaultLockFileName          ()
		);
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final String
			channelDirectoryPrefix,
			dataFilePrefix        ,
			dataFileType          ,
			transactionsFilePrefix,
			transactionsFileType  ,
			rescuedFileType       ,
			typeDictionaryFileName,
			lockFileName
		;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
			final String channelDirectoryPrefix,
			final String dataFilePrefix        ,
			final String dataFileType          ,
			final String transactionsFilePrefix,
			final String transactionsFileType  ,
			final String rescuedFileType       ,
			final String typeDictionaryFileName,
			final String lockFileName
		)
		{
			super();
			this.channelDirectoryPrefix = channelDirectoryPrefix;
			this.dataFilePrefix         = dataFilePrefix        ;
			this.dataFileType           = dataFileType          ;
			this.transactionsFilePrefix = transactionsFilePrefix;
			this.transactionsFileType   = transactionsFileType  ;
			this.rescuedFileType        = rescuedFileType       ;
			this.typeDictionaryFileName = typeDictionaryFileName;
			this.lockFileName           = lockFileName          ;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		public String dataFilePrefix()
		{
			return this.dataFilePrefix;
		}
		
		public String transactionsFilePrefix()
		{
			return this.transactionsFilePrefix;
		}

		@Override
		public String channelDirectoryPrefix()
		{
			return this.channelDirectoryPrefix;
		}
		
		@Override
		public String provideChannelDirectoryName(final int hashIndex)
		{
			return this.channelDirectoryPrefix() + hashIndex;
		}
		
		@Override
		public String dataFileType()
		{
			return this.dataFileType;
		}
		
		@Override
		public String transactionsFileType()
		{
			return this.transactionsFileType;
		}
		
		@Override
		public String rescuedFileType()
		{
			return this.rescuedFileType;
		}
		
		@Override
		public String typeDictionaryFileName()
		{
			return this.typeDictionaryFileName;
		}

		@Override
		public String lockFileName()
		{
			return this.lockFileName;
		}

		@Override
		public final String provideDataFileName(final int channelIndex, final long fileNumber)
		{
			return this.dataFilePrefix + channelIndex + '_' + fileNumber;
		}

		@Override
		public final String provideTransactionsFileName(final int channelIndex)
		{
			return this.transactionsFilePrefix + channelIndex;
		}

		@Override
		public <F extends StorageDataFile> void parseDataInventoryFile(
			final StorageDataFile.Creator<F> fileCreator ,
			final Consumer<? super F>        collector   ,
			final int                        channelIndex,
			final AFile                      file
		)
		{
			final String filename = file.name();
			if(!filename.startsWith(this.dataFilePrefix))
			{
				return;
			}
			if(!this.dataFileType.equals(file.type()))
			{
				return;
			}
			
			/*
			 * From here on, the file must have a valid data file name.
			 * Anything else is an error, since it is most probably a data file with a ruined file name.
			 * If someone wants to create a backup file of a data file in the same directory,
			 * they can/must give it another file type or name prefix.
			 */

			final String middlePart = filename.substring(this.dataFilePrefix.length());
			final int separatorIndex = middlePart.indexOf('_');
			if(separatorIndex < 0)
			{
				throw new StorageException("Invalid data file name: " + file);
			}
			
			final String hashIndexString = middlePart.substring(0, separatorIndex);
			try
			{
				if(Integer.parseInt(hashIndexString) != channelIndex)
				{
					throw new StorageException("Invalid channel for data file: " + file);
				}
			}
			catch(final NumberFormatException e)
			{
				throw new StorageException("Invalid data file name: " + file);
			}

			final String fileNumberString = middlePart.substring(separatorIndex + 1);
			final long fileNumber;
			try
			{
				fileNumber = Long.parseLong(fileNumberString);
			}
			catch(final NumberFormatException e)
			{
				throw new StorageException("Invalid data file name: " + file);
			}
			
			// strictly validly named file, collect.
			collector.accept(fileCreator.createDataFile(file, channelIndex, fileNumber));
		}
		
		@Override
		public String toString()
		{
			return VarString.New()
				.add(this.getClass().getName()).add(':').lf()
				.blank().add("channel directory prefix").tab().add('=').blank().add(this.channelDirectoryPrefix).lf()
				.blank().add("storage file prefix"     ).tab().add('=').blank().add(this.dataFilePrefix     ).lf()
				.blank().add("file suffix"             ).tab().add('=').blank().add(this.dataFileType     ).lf()
				.blank().add("lockFileName"            ).tab().add('=').blank().add(this.lockFileName       )
				.toString()
			;
		}
		
	}
	
	
	public static StorageFileNameProvider.Builder<?> Builder()
	{
		return new StorageFileNameProvider.Builder.Default<>();
	}
	
	public interface Builder<B extends Builder<?>>
	{
		public String channelDirectoryPrefix();

		public B setChannelDirectoryPrefix(String channelDirectoryPrefix);

		public String dataFilePrefix();

		public B setDataFilePrefix(String storageFilePrefix);

		public String dataFileSuffix();

		public B setDataFileSuffix(String storageFileSuffix);

		public String transactionsFilePrefix();

		public B setTransactionsFilePrefix(String transactionsFilePrefix);

		public String transactionsFileSuffix();

		public B setTransactionsFileSuffix(String transactionsFileSuffix);

		public String rescuedFileSuffix();

		public B setRescuedFileSuffix(String rescuedFileSuffix);

		public String typeDictionaryFileName();

		public B setTypeDictionaryFileName(String typeDictionaryFileName);

		public String lockFileName();

		public B setLockFileName(String lockFileName);
				
		public StorageFileNameProvider createFileNameProvider();
		
		
		
		/**
		 * 
		 * @param channelDirectoryPrefix may <b>not</b> be null.
		 * @param storageFilePrefix may <b>not</b> be null.
		 * @param storageFileSuffix may <b>not</b> be null.
		 * @param transactionsFilePrefix may <b>not</b> be null.
		 * @param transactionsFileSuffix may <b>not</b> be null.
		 * @param typeDictionaryFileName may <b>not</b> be null.
		 * @param lockFileName may <b>not</b> be null.
		 * @param rescuedFileSuffix may <b>not</b> be null.
		 * 
		 * @return a new {@link StorageFileNameProvider} instance
		 */
		public static StorageFileNameProvider.Default New(
			final String channelDirectoryPrefix,
			final String storageFilePrefix     ,
			final String storageFileSuffix     ,
			final String transactionsFilePrefix,
			final String transactionsFileSuffix,
			final String rescuedFileSuffix     ,
			final String typeDictionaryFileName,
			final String lockFileName
		)
		{
			return new StorageFileNameProvider.Default(
				notNull(channelDirectoryPrefix),
				notNull(storageFilePrefix)     ,
				notNull(storageFileSuffix)     ,
				notNull(transactionsFilePrefix),
				notNull(transactionsFileSuffix),
				notNull(rescuedFileSuffix)     ,
				notNull(typeDictionaryFileName),
				notNull(lockFileName)
			);
		}
		
		
		public class Default<B extends Builder.Default<?>> implements StorageFileNameProvider.Builder<B>
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			private String
				channelDirectoryPrefix,
				dataFilePrefix        ,
				dataFileSuffix        ,
				transactionsFilePrefix,
				transactionsFileSuffix,
				rescuedFileSuffix     ,
				typeDictionaryFileName,
				lockFileName
			;
			
			

			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			Default()
			{
				super();
			}
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// methods //
			////////////
						
			@SuppressWarnings("unchecked")
			protected final B $()
			{
				return (B)this;
			}

			@Override
			public String channelDirectoryPrefix()
			{
				return this.channelDirectoryPrefix;
			}

			@Override
			public B setChannelDirectoryPrefix(final String channelDirectoryPrefix)
			{
				this.channelDirectoryPrefix = channelDirectoryPrefix;
				return this.$();
			}

			@Override
			public String dataFilePrefix()
			{
				return this.dataFilePrefix;
			}

			@Override
			public B setDataFilePrefix(final String dataFilePrefix)
			{
				this.dataFilePrefix = dataFilePrefix;
				return this.$();
			}

			@Override
			public String dataFileSuffix()
			{
				return this.dataFileSuffix;
			}

			@Override
			public B setDataFileSuffix(final String dataFileSuffix)
			{
				this.dataFileSuffix = dataFileSuffix;
				return this.$();
			}

			@Override
			public String transactionsFilePrefix()
			{
				return this.transactionsFilePrefix;
			}

			@Override
			public B setTransactionsFilePrefix(final String transactionsFilePrefix)
			{
				this.transactionsFilePrefix = transactionsFilePrefix;
				return this.$();
			}

			@Override
			public String transactionsFileSuffix()
			{
				return this.transactionsFileSuffix;
			}

			@Override
			public B setTransactionsFileSuffix(final String transactionsFileSuffix)
			{
				this.transactionsFileSuffix = transactionsFileSuffix;
				return this.$();
			}

			@Override
			public String rescuedFileSuffix()
			{
				return this.rescuedFileSuffix;
			}

			@Override
			public B setRescuedFileSuffix(final String rescuedFileSuffix)
			{
				this.rescuedFileSuffix = rescuedFileSuffix;
				return this.$();
			}

			@Override
			public String typeDictionaryFileName()
			{
				return this.typeDictionaryFileName;
			}

			@Override
			public B setTypeDictionaryFileName(final String typeDictionaryFileName)
			{
				this.typeDictionaryFileName = typeDictionaryFileName;
				return this.$();
			}

			@Override
			public String lockFileName()
			{
				return this.lockFileName;
			}

			@Override
			public B setLockFileName(final String lockFileName)
			{
				this.lockFileName = lockFileName;
				return this.$();
			}
			

			@Override
			public StorageFileNameProvider createFileNameProvider()
			{
				return StorageFileNameProvider.New(
					coalesce(this.channelDirectoryPrefix, Defaults.defaultChannelDirectoryPrefix()),
					coalesce(this.dataFilePrefix        , Defaults.defaultDataFilePrefix()        ),
					coalesce(this.dataFileSuffix        , Defaults.defaultDataFileSuffix()        ),
					coalesce(this.transactionsFilePrefix, Defaults.defaultTransactionsFilePrefix()),
					coalesce(this.transactionsFileSuffix, Defaults.defaultTransactionsFileSuffix()),
					coalesce(this.rescuedFileSuffix     , Defaults.defaultRescuedFileSuffix()     ),
					coalesce(this.typeDictionaryFileName, Defaults.defaultTypeDictionaryFileName()),
					coalesce(this.lockFileName          , Defaults.defaultLockFileName()          )
				);
			}
			
		}
		
	}
	
}
