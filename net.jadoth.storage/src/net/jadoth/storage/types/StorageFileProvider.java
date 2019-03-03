package net.jadoth.storage.types;

import static net.jadoth.X.mayNull;
import static net.jadoth.X.notNull;

import java.io.File;
import java.util.function.Consumer;

import net.jadoth.chars.VarString;
import net.jadoth.files.XFiles;
import net.jadoth.persistence.types.PersistenceTypeDictionaryIoHandler;

public interface StorageFileProvider extends PersistenceTypeDictionaryIoHandler.Provider
{
	/* (03.03.2019 TM)TODO: proper file abstraction
	 * An abstraction on the persistence layer is required with types like
	 * - PersistenceDataItem (Folder or File)
	 * - PersistenceDataLocation extends PersistenceDataItem (Folder, has n PersistenceDataItems, each with unique name)
	 * - PersistenceDataFile extends PersistenceDataItem (File, must always be Folder + String name)
	 * 
	 * Then this type here will no longer extend PersistenceTypeDictionaryIoHandler.Provider,
	 * but just a PersistenceTypeDictionaryDataFileProvider
	 */
	
	@Override
	public PersistenceTypeDictionaryIoHandler provideTypeDictionaryIoHandler();
	
	public StorageNumberedFile provideDataFile(int channelIndex, long fileNumber);

	public StorageNumberedFile provideTransactionsFile(int channelIndex);
	
	public StorageNumberedFile provideDeletionTargetFile(StorageNumberedFile fileToBeDeleted);
	
	public StorageNumberedFile provideTruncationBackupTargetFile(StorageNumberedFile fileToBeTruncated, long newLength);

	public <P extends Consumer<StorageNumberedFile>> P collectDataFiles(P collector, int channelIndex);
	
	
	public interface Defaults
	{

		public static String defaultStorageDirectory()
		{
			return "storage";
		}
		
		public static String defaultDeletionDirectory()
		{
			return null;
		}
		
		public static String defaultTruncationBackupDirectory()
		{
			return null;
		}
		
		public static String defaultChannelDirectoryPrefix()
		{
			return "channel_";
		}
		
		public static String defaultStorageFilePrefix()
		{
			return "channel_";
		}
		
		public static String defaultStorageFileSuffix()
		{
			return ".dat";
		}

		public static String defaultTransactionFilePrefix()
		{
			return "transactions_";
		}
		
		public static String defaultTransactionFileSuffix()
		{
			return "sft"; // "torage file transactions"
		}
		
	}


	public final class Static
	{
		public static final <C extends Consumer<? super StorageNumberedFile>>
		C collectFile(
			final C      collector       ,
			final int    channelIndex    ,
			final File   storageDirectory,
			final String fileBaseName    ,
			final String suffix
		)
		{
			final File[] files = storageDirectory.listFiles();
			if(files != null)
			{
				for(final File file : files)
				{
					internalCollectFile(collector, channelIndex, file, fileBaseName, suffix);
				}
			}

			return collector;
		}

		private static final void internalCollectFile(
			final Consumer<? super StorageNumberedFile> collector   ,
			final int                                   hashIndex   ,
			final File                                  file        ,
			final String                                fileBaseName,
			final String                                suffix
		)
		{
			if(file.isDirectory())
			{
				return;
			}

			final String filename = file.getName();
			if(!filename.startsWith(fileBaseName))
			{
				return;
			}
			if(!filename.endsWith(suffix))
			{
				return;
			}

			final String middlePart = filename.substring(fileBaseName.length(), filename.length() - suffix.length());
			final int separatorIndex = middlePart.indexOf('_');
			if(separatorIndex < 0)
			{
				return;
			}
			
			final String hashIndexString = middlePart.substring(0, separatorIndex);
			try
			{
				if(Integer.parseInt(hashIndexString) != hashIndex)
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
			collector.accept(StorageNumberedFile.New(hashIndex, fileNumber, file));
		}



		private Static()
		{
			// static only
			throw new UnsupportedOperationException();
		}
	}
	
	
	
	public static Builder<?> Builder()
	{
		return new StorageFileProvider.Builder.Implementation<>();
	}
	
	public interface Builder<B extends Builder<?>>
	{
		public String storageDirectory();

		public B setStorageDirectory(String mainDirectory);

		public String deletionDirectory();

		public B setDeletionDirectory(String deletionDirectory);

		public String truncationDirectory();

		public B setTruncationDirectory(String truncationDirectory);

		public String channelDirectoryPrefix();

		public B setChannelDirectoryPrefix(String channelDirectoryPrefix);

		public String storageFilePrefix();

		public B setStorageFilePrefix(String storageFilePrefix);

		public String storageFileSuffix();

		public B setStorageFileSuffix(String storageFileSuffix);

		public String transactionsFilePrefix();

		public B setTransactionsFilePrefix(String transactionsFilePrefix);

		public String transactionsFileSuffix();

		public B setTransactionsFileSuffix(String transactionsFileSuffix);
		
		public StorageFileProvider createFileProvider();
		
		
		
		public class Implementation<B extends Builder.Implementation<?>> implements StorageFileProvider.Builder<B>
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			private String
				storageDirectory       = StorageFileProvider.Defaults.defaultStorageDirectory()         ,
				deletionDirectory      = StorageFileProvider.Defaults.defaultDeletionDirectory()        ,
				truncationDirectory    = StorageFileProvider.Defaults.defaultTruncationBackupDirectory(),
				channelDirectoryPrefix = StorageFileProvider.Defaults.defaultChannelDirectoryPrefix()   ,
				storageFilePrefix      = StorageFileProvider.Defaults.defaultStorageFilePrefix()        ,
				storageFileSuffix      = StorageFileProvider.Defaults.defaultStorageFileSuffix()        ,
				transactionsFilePrefix = StorageFileProvider.Defaults.defaultTransactionFilePrefix()    ,
				transactionsFileSuffix = StorageFileProvider.Defaults.defaultTransactionFileSuffix()
			;
			
			

			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			Implementation()
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
			public String storageDirectory()
			{
				return this.storageDirectory;
			}

			@Override
			public B setStorageDirectory(final String storageDirectory)
			{
				this.storageDirectory = storageDirectory;
				return this.$();
			}

			@Override
			public String deletionDirectory()
			{
				return this.deletionDirectory;
			}

			@Override
			public B setDeletionDirectory(final String deletionDirectory)
			{
				this.deletionDirectory = deletionDirectory;
				return this.$();
			}

			@Override
			public String truncationDirectory()
			{
				return this.truncationDirectory;
			}

			@Override
			public B setTruncationDirectory(final String truncationDirectory)
			{
				this.truncationDirectory = truncationDirectory;
				return this.$();
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
			public String storageFilePrefix()
			{
				return this.storageFilePrefix;
			}

			@Override
			public B setStorageFilePrefix(final String storageFilePrefix)
			{
				this.storageFilePrefix = storageFilePrefix;
				return this.$();
			}

			@Override
			public String storageFileSuffix()
			{
				return this.storageFileSuffix;
			}

			@Override
			public B setStorageFileSuffix(final String storageFileSuffix)
			{
				this.storageFileSuffix = storageFileSuffix;
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
			public StorageFileProvider createFileProvider()
			{
				return StorageFileProvider.New(
					this.storageDirectory      ,
					this.deletionDirectory     ,
					this.truncationDirectory   ,
					this.channelDirectoryPrefix,
					this.storageFilePrefix     ,
					this.storageFileSuffix     ,
					this.transactionsFilePrefix,
					this.transactionsFileSuffix
				);
			}
			
		}
		
	}
	
	
	public static StorageFileProvider.Implementation New(
		final String baseDirectory           ,
		final String deletionDirectory       ,
		final String truncationDirectory     ,
		final String channelDirectoryBaseName,
		final String storageFileBaseName     ,
		final String storageFileSuffix       ,
		final String transactionsFileBaseName,
		final String transactionsFileSuffix
	)
	{
		return new StorageFileProvider.Implementation(
			mayNull(baseDirectory)           , // null means working directory
			mayNull(deletionDirectory)       , // null means actually delete files
			mayNull(truncationDirectory)     , // null means actually delete files
			notNull(channelDirectoryBaseName),
			notNull(storageFileBaseName)     ,
			notNull(storageFileSuffix)       ,
			notNull(transactionsFileBaseName),
			notNull(transactionsFileSuffix)
		);
	}

	

	public abstract class AbstractImplementation implements StorageFileProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final String baseDirectory         ;
		private final String deletionDirectory     ;
		private final String truncationDirectory   ;
		private final String channelDirectoryPrefix;
		private final String storageFilePrefix     ;
		private final String storageFileSuffix     ;
		private final String transactionsFilePrefix;
		private final String transactionsFileSuffix;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		public AbstractImplementation(
			final String baseDirectory         ,
			final String deletionDirectory     ,
			final String truncationDirectory   ,
			final String channelDirectoryPrefix,
			final String storageFilePrefix     ,
			final String storageFileSuffix     ,
			final String transactionsFilePrefix,
			final String transactionsFileSuffix
		)
		{
			super();
			this.baseDirectory          = baseDirectory         ;
			this.deletionDirectory      = deletionDirectory     ;
			this.truncationDirectory    = truncationDirectory   ;
			this.channelDirectoryPrefix = channelDirectoryPrefix;
			this.storageFilePrefix      = storageFilePrefix     ;
			this.storageFileSuffix      = storageFileSuffix     ;
			this.transactionsFilePrefix = transactionsFilePrefix;
			this.transactionsFileSuffix = transactionsFileSuffix;
		}

		public String baseDirectory()
		{
			return this.baseDirectory;
		}

		public String deletionDirectory()
		{
			return this.deletionDirectory;
		}

		public String truncationDirectory()
		{
			return this.truncationDirectory;
		}
		
		public String channelDirectoryPrefix()
		{
			return this.channelDirectoryPrefix;
		}
		
		public String storageFileSuffix()
		{
			return this.storageFileSuffix;
		}

		public final String provideStorageFileName(final int channelIndex, final long fileNumber)
		{
			return this.storageFilePrefix + channelIndex + '_' + fileNumber + this.storageFileSuffix;
		}

		public final String provideTransactionFileName(final int channelIndex)
		{
			return this.transactionsFilePrefix + channelIndex + '.' + this.transactionsFileSuffix;
		}
		


		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////



		@Override
		public String toString()
		{
			return VarString.New()
				.add(this.getClass().getName()).add(':').lf()
				.blank().add("base directory"          ).tab().add('=').blank().add(this.baseDirectory         ).lf()
				.blank().add("deletion directory"      ).tab().add('=').blank().add(this.deletionDirectory     ).lf()
				.blank().add("channel directory prefix").tab().add('=').blank().add(this.channelDirectoryPrefix).lf()
				.blank().add("storage file prefix"     ).tab().add('=').blank().add(this.storageFilePrefix     ).lf()
				.blank().add("file suffix"             ).tab().add('=').blank().add(this.storageFileSuffix     )
				.toString()
			;
		}
		
	}
	
	public final class Implementation extends StorageFileProvider.AbstractImplementation
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Implementation(
			final String baseDirectory         ,
			final String deletionDirectory     ,
			final String truncationDirectory   ,
			final String channelDirectoryPrefix,
			final String storageFilePrefix     ,
			final String storageFileSuffix     ,
			final String transactionsFilePrefix,
			final String transactionsFileSuffix
		)
		{
			super(
				baseDirectory         ,
				deletionDirectory     ,
				truncationDirectory   ,
				channelDirectoryPrefix,
				storageFilePrefix     ,
				storageFileSuffix     ,
				transactionsFilePrefix,
				transactionsFileSuffix
			);
		}
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		public final File provideChannelDirectory(final String parentDirectory, final int hashIndex)
		{
			return XFiles.ensureDirectory(
				new File(parentDirectory, this.channelDirectoryPrefix() + hashIndex)
			);
		}

		public File provideChannelDirectory(final int channelIndex)
		{
			return this.provideChannelDirectory(this.baseDirectory(), channelIndex);
		}
		
		@Override
		public final StorageNumberedFile provideDataFile(final int channelIndex, final long fileNumber)
		{
			final File file = new File(
				this.provideChannelDirectory(channelIndex),
				this.provideStorageFileName(channelIndex, fileNumber)
			);
			
			return StorageNumberedFile.New(channelIndex, fileNumber, file);
		}

		@Override
		public StorageNumberedFile provideTransactionsFile(final int channelIndex)
		{
			final File file = new File(
				this.provideChannelDirectory(channelIndex),
				this.provideTransactionFileName(channelIndex)
			);

			return StorageNumberedFile.New(channelIndex, Storage.transactionsFileNumber(), file);
		}

		@Override
		public StorageNumberedFile provideDeletionTargetFile(final StorageNumberedFile fileToBeDeleted)
		{
			final String deletionDirectory = this.deletionDirectory();
			if(deletionDirectory == null)
			{
				return null;
			}
			
			final int  channelIndex = fileToBeDeleted.channelIndex();
			final long fileNumber   = fileToBeDeleted.number();
			
			final File file = new File(
				this.provideChannelDirectory(deletionDirectory, channelIndex),
				this.provideStorageFileName(channelIndex, fileNumber)
			);
			
			return StorageNumberedFile.New(channelIndex, fileNumber, file);
		}
		
		@Override
		public StorageNumberedFile provideTruncationBackupTargetFile(
			final StorageNumberedFile fileToBeTruncated,
			final long                newLength
		)
		{
			final String truncationDirectory = this.truncationDirectory();
			if(truncationDirectory == null)
			{
				return null;
			}
			
			final int  channelIndex = fileToBeTruncated.channelIndex();
			final long fileNumber   = fileToBeTruncated.number();
			
			final File file = new File(
				this.provideChannelDirectory(truncationDirectory, channelIndex),
				this.provideStorageFileName(channelIndex, fileNumber)
				+ "_truncated_from_" + fileToBeTruncated.length() + "_to_" + newLength
				+ "_@" + System.currentTimeMillis() + ".bak"
			);
			
			return StorageNumberedFile.New(channelIndex, fileNumber, file);
		}

		@Override
		public <P extends Consumer<StorageNumberedFile>> P collectDataFiles(
			final P   collector   ,
			final int channelIndex
		)
		{
			return Static.collectFile(
				collector,
				channelIndex,
				this.provideChannelDirectory(channelIndex),
				this.channelDirectoryPrefix(),
				this.storageFileSuffix()
			);
		}
		
	}

}
