package one.microstream.storage.types;

import static one.microstream.X.notNull;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

import one.microstream.persistence.types.PersistenceTypeDictionaryIoHandler;
import one.microstream.persistence.types.PersistenceTypeDictionaryStorer;

public interface StorageIoHandler extends StorageFileProvider, StorageFileWriter, StorageFileReader
{
	public default ZStorageInventoryFile copyData(final ZStorageDataFile<?> dataFile)
	{
		final ZStorageInventoryFile targetFile = this.provideDataFile(
			dataFile.channelIndex(),
			dataFile.number()
		).inventorize();
		this.copy(dataFile, targetFile);
		
		return targetFile;
	}

	public default ZStorageInventoryFile copyTransactions(final ZStorageInventoryFile transactionsFile)
	{
		final ZStorageInventoryFile targetFile = this.provideTransactionsFile(
			transactionsFile.channelIndex()
		).inventorize();
		this.copy(transactionsFile, targetFile);
		
		return targetFile;
	}



	public final class Default implements StorageIoHandler
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final StorageFileProvider fileProvider;
		final StorageFileWriter   fileWriter  ;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Default(final StorageFileProvider fileProvider, final StorageFileWriter fileWriter)
		{
			super();
			this.fileProvider = notNull(fileProvider);
			this.fileWriter   = notNull(fileWriter)  ;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public String getStorageLocationIdentifier()
		{
			return this.fileProvider.getStorageLocationIdentifier();
		}
		
		@Override
		public PersistenceTypeDictionaryIoHandler provideTypeDictionaryIoHandler(
			final PersistenceTypeDictionaryStorer writeListener
		)
		{
			return this.fileProvider.provideTypeDictionaryIoHandler(writeListener);
		}

		@Override
		public ZStorageNumberedFile provideDataFile(final int channelIndex, final long fileNumber)
		{
			return this.fileProvider.provideDataFile(channelIndex, fileNumber);
		}

		@Override
		public ZStorageNumberedFile provideTransactionsFile(final int channelIndex)
		{
			return this.fileProvider.provideTransactionsFile(channelIndex);
		}
		
		@Override
		public ZStorageLockedFile provideLockFile()
		{
			return this.fileProvider.provideLockFile();
		}
		
		@Override
		public ZStorageNumberedFile provideDeletionTargetFile(final ZStorageNumberedFile fileToBeDeleted)
		{
			return this.fileProvider.provideDeletionTargetFile(fileToBeDeleted);
		}
		
		@Override
		public ZStorageNumberedFile provideTruncationBackupTargetFile(
			final ZStorageNumberedFile fileToBeTruncated,
			final long                newLength
		)
		{
			return this.fileProvider.provideTruncationBackupTargetFile(fileToBeTruncated, newLength);
		}

		@Override
		public <P extends Consumer<ZStorageNumberedFile>> P collectDataFiles(
			final P   collector   ,
			final int channelIndex
		)
		{
			return this.fileProvider.collectDataFiles(collector, channelIndex);
		}

		@Override
		public long write(final ZStorageLockedFile file, final ByteBuffer[] byteBuffers)
		{
			return this.fileWriter.write(file, byteBuffers);
		}

		@Override
		public long copyFilePart(
			final ZStorageLockedFile sourceFile  ,
			final long              sourceOffset,
			final long              length      ,
			final ZStorageLockedFile targetfile
		)
		{
			return this.fileWriter.copyFilePart(sourceFile, sourceOffset, length, targetfile);
		}

		@Override
		public void flush(final ZStorageLockedFile targetfile)
		{
			this.fileWriter.flush(targetfile);
		}

		@Override
		public void truncate(
			final ZStorageInventoryFile file               ,
			final long                 newLength          ,
			final StorageFileProvider  storageFileProvider
		)
		{
			this.fileWriter.truncate(file, newLength, storageFileProvider);
		}
		
		@Override
		public void delete(final ZStorageInventoryFile file, final StorageFileProvider storageFileProvider)
		{
			this.fileWriter.delete(file, storageFileProvider);
		}

	}

}
