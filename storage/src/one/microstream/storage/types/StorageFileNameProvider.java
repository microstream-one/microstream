package one.microstream.storage.types;

import static one.microstream.X.coalesce;
import static one.microstream.X.notNull;

import java.util.function.Consumer;

import one.microstream.afs.AFile;
import one.microstream.chars.VarString;
import one.microstream.persistence.types.Persistence;

public interface StorageFileNameProvider
{
	
	public String provideChannelDirectoryName(int hashIndex);
	
	public String dataFileSuffix();
	
	public String transactionsFileSuffix();
	
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
			Defaults.defaultTypeDictionaryFileName(),
			Defaults.defaultLockFileName          ()
		);
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final String
			channelDirectoryPrefix,
			dataFilePrefix        ,
			dataFileSuffix        ,
			transactionsFilePrefix,
			transactionsFileSuffix,
			typeDictionaryFileName,
			lockFileName
		;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
			final String channelDirectoryPrefix,
			final String dataFilePrefix        ,
			final String dataFileSuffix        ,
			final String transactionsFilePrefix,
			final String transactionsFileSuffix,
			final String typeDictionaryFileName,
			final String lockFileName
		)
		{
			super();
			this.channelDirectoryPrefix = channelDirectoryPrefix;
			this.dataFilePrefix         = dataFilePrefix        ;
			this.dataFileSuffix         = dataFileSuffix        ;
			this.transactionsFilePrefix = transactionsFilePrefix;
			this.transactionsFileSuffix = transactionsFileSuffix;
			this.typeDictionaryFileName = typeDictionaryFileName;
			this.lockFileName           = lockFileName          ;
		}
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

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
		public String dataFileSuffix()
		{
			return this.dataFileSuffix;
		}
		
		@Override
		public String transactionsFileSuffix()
		{
			return this.transactionsFileSuffix;
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
			if(!this.dataFileSuffix.equals(file.type()))
			{
				return;
			}

			final String middlePart = filename.substring(this.dataFilePrefix.length(), filename.length() - this.dataFileSuffix.length());
			final int separatorIndex = middlePart.indexOf('_');
			if(separatorIndex < 0)
			{
				return;
			}
			
			final String hashIndexString = middlePart.substring(0, separatorIndex);
			try
			{
				if(Integer.parseInt(hashIndexString) != channelIndex)
				{
					return;
				}
			}
			catch(final NumberFormatException e)
			{
				return;
			}

			final String fileNumberString = middlePart.substring(separatorIndex + 1);
			final long fileNumber;
			try
			{
				fileNumber = Long.parseLong(fileNumberString);
			}
			catch(final NumberFormatException e)
			{
				return; // not a strictly validly named file, ignore intentionally despite all previous matches.
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
				.blank().add("file suffix"             ).tab().add('=').blank().add(this.dataFileSuffix     ).lf()
				.blank().add("lockFileName"            ).tab().add('=').blank().add(this.lockFileName       )
				.toString()
			;
		}
		
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
		 */
		public static StorageFileNameProvider.Default New(
			final String channelDirectoryPrefix,
			final String storageFilePrefix     ,
			final String storageFileSuffix     ,
			final String transactionsFilePrefix,
			final String transactionsFileSuffix,
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
				dataFilePrefix     ,
				dataFileSuffix     ,
				transactionsFilePrefix,
				transactionsFileSuffix,
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
					coalesce(this.typeDictionaryFileName, Defaults.defaultTypeDictionaryFileName()),
					coalesce(this.lockFileName          , Defaults.defaultLockFileName()          )
				);
			}
			
		}
		
	}
	
}
