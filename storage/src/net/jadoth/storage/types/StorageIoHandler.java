package net.jadoth.storage.types;

import static net.jadoth.X.notNull;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

import net.jadoth.persistence.types.PersistenceTypeDictionaryIoHandler;
import net.jadoth.persistence.types.PersistenceTypeDictionaryStorer;

public interface StorageIoHandler extends StorageFileProvider, StorageFileWriter, StorageFileReader
{
	public default StorageInventoryFile copyData(final StorageDataFile<?> dataFile)
	{
		final StorageInventoryFile targetFile = this.provideDataFile(
			dataFile.channelIndex(),
			dataFile.number()
		).inventorize();
		this.copy(dataFile, targetFile);
		
		return targetFile;
	}

	public default StorageInventoryFile copyTransactions(final StorageInventoryFile transactionsFile)
	{
		final StorageInventoryFile targetFile = this.provideTransactionsFile(
			transactionsFile.channelIndex()
		).inventorize();
		this.copy(transactionsFile, targetFile);
		
		return targetFile;
	}



	public final class Implementation implements StorageIoHandler
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final StorageFileProvider fileProvider;
		final StorageFileWriter   fileWriter  ;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Implementation(final StorageFileProvider fileProvider, final StorageFileWriter fileWriter)
		{
			super();
			this.fileProvider = notNull(fileProvider);
			this.fileWriter   = notNull(fileWriter)  ;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public PersistenceTypeDictionaryIoHandler provideTypeDictionaryIoHandler(
			final PersistenceTypeDictionaryStorer writeListener
		)
		{
			return this.fileProvider.provideTypeDictionaryIoHandler(writeListener);
		}

		@Override
		public StorageNumberedFile provideDataFile(final int channelIndex, final long fileNumber)
		{
			return this.fileProvider.provideDataFile(channelIndex, fileNumber);
		}

		@Override
		public StorageNumberedFile provideTransactionsFile(final int channelIndex)
		{
			return this.fileProvider.provideTransactionsFile(channelIndex);
		}
		
		@Override
		public StorageNumberedFile provideDeletionTargetFile(final StorageNumberedFile fileToBeDeleted)
		{
			return this.fileProvider.provideDeletionTargetFile(fileToBeDeleted);
		}
		
		@Override
		public StorageNumberedFile provideTruncationBackupTargetFile(
			final StorageNumberedFile fileToBeTruncated,
			final long                newLength
		)
		{
			return this.fileProvider.provideTruncationBackupTargetFile(fileToBeTruncated, newLength);
		}

		@Override
		public <P extends Consumer<StorageNumberedFile>> P collectDataFiles(
			final P   collector   ,
			final int channelIndex
		)
		{
			return this.fileProvider.collectDataFiles(collector, channelIndex);
		}

		@Override
		public long write(final StorageLockedFile file, final ByteBuffer[] byteBuffers)
		{
			return this.fileWriter.write(file, byteBuffers);
		}

		@Override
		public long copyFilePart(
			final StorageLockedFile sourceFile  ,
			final long              sourceOffset,
			final long              length      ,
			final StorageLockedFile targetfile
		)
		{
			return this.fileWriter.copyFilePart(sourceFile, sourceOffset, length, targetfile);
		}

		@Override
		public void flush(final StorageLockedFile targetfile)
		{
			this.fileWriter.flush(targetfile);
		}

		@Override
		public void truncate(
			final StorageInventoryFile file               ,
			final long                 newLength          ,
			final StorageFileProvider  storageFileProvider
		)
		{
			this.fileWriter.truncate(file, newLength, storageFileProvider);
		}
		
		@Override
		public void delete(final StorageInventoryFile file, final StorageFileProvider storageFileProvider)
		{
			this.fileWriter.delete(file, storageFileProvider);
		}

	}

}
