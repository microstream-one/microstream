package net.jadoth.storage.types;

import static net.jadoth.Jadoth.notNull;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

public interface StorageIoHandler extends StorageFileProvider, StorageFileWriter, StorageFileReader
{
	public default StorageInventoryFile copyData(final StorageDataFile<?> dataFile)
	{
		final StorageInventoryFile targetFile = this.provideStorageFile(dataFile.channelIndex(), dataFile.number());
		this.copy(dataFile, targetFile);
		this.flush(targetFile); // for one whole file, always flushing is appropriate
		return targetFile;
	}

	public default StorageLockedChannelFile copyTransactions(final StorageLockedChannelFile transactionsFile)
	{
		final StorageLockedChannelFile targetFile = this.provideTransactionsFile(transactionsFile.channelIndex());
		this.copy(transactionsFile, targetFile);
		this.flush(targetFile); // for one whole file, always flushing is appropriate
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
		public StorageInventoryFile provideStorageFile(final int channelIndex, final long fileNumber)
		{
			return this.fileProvider.provideStorageFile(channelIndex, fileNumber);
		}

		@Override
		public StorageLockedChannelFile provideTransactionsFile(final int channelIndex)
		{
			return this.fileProvider.provideTransactionsFile(channelIndex);
		}

		@Override
		public <P extends Consumer<StorageInventoryFile>> P collectStorageFiles(
			final P   collector   ,
			final int channelIndex
		)
		{
			return this.fileProvider.collectStorageFiles( collector, channelIndex);
		}

		@Override
		public void write(final StorageLockedFile file, final ByteBuffer byteBuffer)
		{
			this.fileWriter.write(file, byteBuffer);
		}

		@Override
		public void write(final StorageLockedFile file, final ByteBuffer... byteBuffers)
		{
			this.fileWriter.write(file, byteBuffers);
		}

		@Override
		public long copy(
			final StorageFile       sourceFile  ,
			final StorageLockedFile targetfile  ,
			final long              sourceOffset,
			final long              length
		)
		{
			return this.fileWriter.copy(sourceFile, targetfile, sourceOffset, length);
		}

		@Override
		public void flush(final StorageLockedFile targetfile)
		{
			this.fileWriter.flush(targetfile);
		}

//		@Override
//		public void copyFile(final StorageFile sourceFile, final File targetFile)
//		{
//			this.fileWriter.copyFile(sourceFile, targetFile);
//		}

		@Override
		public void truncate(final StorageLockedFile file, final long newLength)
		{
			this.fileWriter.truncate(file, newLength);
		}

		@Override
		public void delete(final StorageLockedChannelFile file)
		{
			this.fileWriter.delete(file);
		}

	}

}
