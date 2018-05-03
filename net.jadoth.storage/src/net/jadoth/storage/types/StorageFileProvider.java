package net.jadoth.storage.types;

import static net.jadoth.X.notNull;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.function.Consumer;

import net.jadoth.util.chars.VarString;
import net.jadoth.util.file.JadothFiles;

public interface StorageFileProvider
{
	public StorageInventoryFile     provideStorageFile(int channelIndex, long fileNumber);

	public StorageLockedChannelFile provideTransactionsFile(int channelIndex);

	public <P extends Consumer<StorageInventoryFile>> P collectStorageFiles(P collector, int channelIndex);



	public final class Static
	{
		public static final void collectFile(
			final Consumer<StorageInventoryFile> collector       ,
			final int                             channelIndex    ,
			final File                            storageDirectory,
			final String                          fileBaseName    ,
			final String                          dotSuffix
		)
		{
			final File[] files = storageDirectory.listFiles();
			if(files == null)
			{
				return; // x_x @FindBugs
			}

			for(final File file : files)
			{
				internalCollectFile(collector, channelIndex, file, fileBaseName, dotSuffix);
			}
		}

		private static final void internalCollectFile(
			final Consumer<StorageInventoryFile> collector   ,
			final int                             hashIndex   ,
			final File                            file        ,
			final String                          fileBaseName,
			final String                          dotSuffix
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
			if(!filename.endsWith(dotSuffix))
			{
				return;
			}

			final String middlePart = filename.substring(fileBaseName.length(), filename.length() - dotSuffix.length());
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

			final FileLock lock = StorageLockedFile.openFileChannel(file);

			// strictly validly named file, collect.
			collector.accept(new StorageInventoryFile.Implementation(hashIndex, file, lock, fileNumber));
		}



		private Static()
		{
			// static only
			throw new UnsupportedOperationException();
		}
	}


	public final class Implementation implements StorageFileProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////

		static final FileLock openFileChannel(final File file)
		{
//			DEBUGStorage.println("Thread " + Thread.currentThread().getName() + " opening channel for " + file);
			FileChannel channel = null;
			try
			{
				final FileLock fileLock = StorageLockedFile.openFileChannel(file);
				channel = fileLock.channel();
				channel.position(channel.size());
				return fileLock;
			}
			catch(final IOException e)
			{
				JadothFiles.closeSilent(channel);
				throw new RuntimeException(e); // (04.05.2013)EXCP: proper exception
			}
		}


		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final File   baseDirectory           ;
		private final String channelDirectoryBaseName;
		private final String storageFileBaseName     ;
		private final String storageFileSuffix       ;
		private final String transactionsFileBaseName;
		private final String transactionsFileSuffix  ;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		public Implementation(
			final File   baseDirectory           ,
			final String channelDirectoryBaseName,
			final String storageFileBaseName     ,
			final String storageFileSuffix       ,
			final String transactionsFileBaseName,
			final String transactionsFileSuffix
		)
		{
			super();
			this.baseDirectory            =         baseDirectory            ; // may be null (working directory)
			this.channelDirectoryBaseName = notNull(channelDirectoryBaseName);
			this.storageFileBaseName      = notNull(storageFileBaseName)     ;
			this.storageFileSuffix        = notNull(storageFileSuffix)       ;
			this.transactionsFileBaseName = notNull(transactionsFileBaseName);
			this.transactionsFileSuffix   = notNull(transactionsFileSuffix)  ;
		}


		private String dotFileSuffix()
		{
			// theoretical runtime inefficient, but hardly relevant regarding the file IO performance overhead
			return '.' + this.provideStorageFileSuffix();
		}

		public final File provideBaseDirectory()
		{
			return this.baseDirectory;
		}

		public final File provideChannelDirectory(final File parentDirectory, final int hashIndex)
		{
			return JadothFiles.ensureDirectory(
				new File(parentDirectory, this.channelDirectoryBaseName + hashIndex)
			);
		}

		public File provideChannelDirectory(final int channelIndex)
		{
			return this.provideChannelDirectory(this.provideBaseDirectory(), channelIndex);
		}

		public final String provideStorageFileName(final int channelIndex, final long fileNumber)
		{
			return this.storageFileBaseName + channelIndex + '_' + fileNumber;
		}

		public final String provideStorageFileSuffix()
		{
			return this.storageFileSuffix;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final StorageInventoryFile provideStorageFile(final int channelIndex, final long fileNumber)
		{
			final File file = new File(
				this.provideChannelDirectory(channelIndex),
				this.provideStorageFileName(channelIndex, fileNumber) + this.dotFileSuffix()
			);
			return new StorageInventoryFile.Implementation(channelIndex, file, openFileChannel(file), fileNumber);
		}

		@Override
		public StorageLockedChannelFile provideTransactionsFile(final int channelIndex)
		{
			final File file = new File(
				this.provideChannelDirectory(channelIndex),
				this.transactionsFileBaseName + channelIndex + '.' + this.transactionsFileSuffix
			);
			return StorageLockedChannelFile.New(channelIndex, file, openFileChannel(file));
		}

		@Override
		public <P extends Consumer<StorageInventoryFile>> P collectStorageFiles(
			final P   collector   ,
			final int channelIndex
		)
		{
			Static.collectFile(
				collector,
				channelIndex,
				this.provideChannelDirectory(channelIndex),
				this.channelDirectoryBaseName,
				this.dotFileSuffix()
			);
			return collector;
		}

		@Override
		public String toString()
		{
			return VarString.New()
				.add(this.getClass().getName()).add(':').lf()
				.blank().add("base directory"             ).tab().add('=').blank().add(this.baseDirectory           ).lf()
				.blank().add("channel directory base name").tab().add('=').blank().add(this.channelDirectoryBaseName).lf()
				.blank().add("storage file base name"     ).tab().add('=').blank().add(this.storageFileBaseName     ).lf()
				.blank().add("file suffix"                ).tab().add('=').blank().add(this.storageFileSuffix       )
				.toString()
			;
		}

	}

}
