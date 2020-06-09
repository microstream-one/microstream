package one.microstream.afs.azure.storage;

import static one.microstream.X.notNull;

import java.nio.ByteBuffer;

import one.microstream.afs.ADirectory;
import one.microstream.afs.AFile;
import one.microstream.afs.AIoHandler;
import one.microstream.afs.AWritableFile;
import one.microstream.io.BufferProvider;

public interface AzureStorageIoHandler extends AIoHandler
{
	public AzureStorageConnector connector();



	public static AzureStorageIoHandler New(
		final AzureStorageConnector connector
	)
	{
		return new AzureStorageIoHandler.Default(
			notNull(connector)
		);
	}


	public static final class Default
	extends AIoHandler.Abstract<AzureStoragePath, AzureStoragePath, AzureStorageItemWrapper, AzureStorageFileWrapper, ADirectory, AzureStorageReadableFile, AzureStorageWritableFile>
	implements AzureStorageIoHandler
	{
		final AzureStorageConnector connector;


		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(final AzureStorageConnector connector)
		{
			super(
				AzureStorageItemWrapper .class,
				AzureStorageFileWrapper .class,
				ADirectory    .class,
				AzureStorageReadableFile.class,
				AzureStorageWritableFile.class
			);

			this.connector = connector;
		}

		@Override
		public AzureStorageConnector connector()
		{
			return this.connector;
		}

		@Override
		protected AzureStoragePath toSubjectFile(
			final AFile file
		)
		{
			return AzureStorageFileSystem.toPath(file);
		}

		@Override
		protected AzureStoragePath toSubjectDirectory(
			final ADirectory directory
		)
		{
			return AzureStorageFileSystem.toPath(directory);
		}

		@Override
		protected long subjectFileSize(
			final AzureStoragePath file
		)
		{
			return this.connector.fileSize(file);
		}

		@Override
		protected boolean subjectFileExists(
			final AzureStoragePath file
		)
		{
			return this.connector.fileExists(file);
		}

		@Override
		protected boolean subjectDirectoryExists(
			final AzureStoragePath directory
		)
		{
			return this.connector.directoryExists(directory);
		}

		@Override
		protected long specificSize(
			final AzureStorageFileWrapper file
		)
		{
			return this.subjectFileSize(file.path());
		}

		@Override
		protected boolean specificExists(
			final AzureStorageFileWrapper file
		)
		{
			return this.subjectFileExists(file.path());
		}

		@Override
		protected boolean specificExists(
			final ADirectory directory
		)
		{
			return this.subjectDirectoryExists(
				this.toSubjectDirectory(directory)
			);
		}

		@Override
		protected boolean specificOpenReading(
			final AzureStorageReadableFile file
		)
		{
			return file.openHandle();
		}

		@Override
		protected boolean specificIsOpen(
			final AzureStorageReadableFile file
		)
		{
			return file.isHandleOpen();
		}

		@Override
		protected boolean specificClose(
			final AzureStorageReadableFile file
		)
		{
			return file.closeHandle();
		}

		@Override
		protected boolean specificOpenWriting(
			final AzureStorageWritableFile file
		)
		{
			return file.openHandle();
		}

		@Override
		protected void specificCreate(
			final ADirectory directory
		)
		{
			this.connector.createDirectory(
				this.toSubjectDirectory(directory)
			);
		}

		@Override
		protected void specificCreate(
			final AzureStorageWritableFile file
		)
		{
			this.connector.createFile(
				this.toSubjectFile(file)
			);
		}

		@Override
		protected boolean specificDeleteFile(
			final AzureStorageWritableFile file
		)
		{
			return this.connector.deleteFile(
				AzureStorageFileSystem.toPath(file)
			);
		}

		@Override
		protected ByteBuffer specificReadBytes(
			final AzureStorageReadableFile sourceFile
		)
		{
			return this.connector.readData(
				AzureStorageFileSystem.toPath(sourceFile.ensureOpenHandle()),
				0,
				-1
			);
		}

		@Override
		protected ByteBuffer specificReadBytes(
			final AzureStorageReadableFile sourceFile,
			final long           position
		)
		{
			return this.connector.readData(
				AzureStorageFileSystem.toPath(sourceFile.ensureOpenHandle()),
				position,
				-1
			);
		}

		@Override
		protected ByteBuffer specificReadBytes(
			final AzureStorageReadableFile sourceFile,
			final long           position  ,
			final long           length
		)
		{
			return this.connector.readData(
				AzureStorageFileSystem.toPath(sourceFile.ensureOpenHandle()),
				position,
				length
			);
		}

		@Override
		protected long specificReadBytes(
			final AzureStorageReadableFile sourceFile  ,
			final ByteBuffer     targetBuffer
		)
		{
			return this.connector.readData(
				AzureStorageFileSystem.toPath(sourceFile.ensureOpenHandle()),
				targetBuffer,
				0,
				-1
			);
		}

		@Override
		protected long specificReadBytes(
			final AzureStorageReadableFile sourceFile  ,
			final ByteBuffer     targetBuffer,
			final long           position
		)
		{
			return this.connector.readData(
				AzureStorageFileSystem.toPath(sourceFile.ensureOpenHandle()),
				targetBuffer,
				position,
				-1
			);
		}

		@Override
		protected long specificReadBytes(
			final AzureStorageReadableFile sourceFile  ,
			final ByteBuffer     targetBuffer,
			final long           position    ,
			final long           length
		)
		{
			return this.connector.readData(
				AzureStorageFileSystem.toPath(sourceFile.ensureOpenHandle()),
				targetBuffer,
				position,
				length
			);
		}

		@Override
		protected long specificReadBytes(
			final AzureStorageReadableFile sourceFile    ,
			final BufferProvider bufferProvider
		)
		{
			bufferProvider.initializeOperation();
			try
			{
				return this.specificReadBytes(sourceFile, bufferProvider.provideNextBuffer());
			}
			finally
			{
				bufferProvider.completeOperation();
			}
		}

		@Override
		protected long specificReadBytes(
			final AzureStorageReadableFile sourceFile    ,
			final BufferProvider bufferProvider,
			final long           position
		)
		{
			bufferProvider.initializeOperation();
			try
			{
				return this.specificReadBytes(sourceFile, bufferProvider.provideNextBuffer(), position);
			}
			finally
			{
				bufferProvider.completeOperation();
			}
		}

		@Override
		protected long specificReadBytes(
			final AzureStorageReadableFile sourceFile    ,
			final BufferProvider bufferProvider,
			final long           position      ,
			final long           length
		)
		{
			bufferProvider.initializeOperation();
			try
			{
				return this.specificReadBytes(sourceFile, bufferProvider.provideNextBuffer(), position, length);
			}
			finally
			{
				bufferProvider.completeOperation();
			}
		}

		@Override
		protected long specificWriteBytes(
			final AzureStorageWritableFile                 targetFile   ,
			final Iterable<? extends ByteBuffer> sourceBuffers
		)
		{
			this.openWriting(targetFile);

			return this.connector.writeData(
				AzureStorageFileSystem.toPath(targetFile.ensureOpenHandle()),
				sourceBuffers
			);
		}

		@Override
		protected void specificMoveFile(
			final AzureStorageWritableFile  sourceFile,
			final AWritableFile   targetFile
		)
		{
			final AzureStorageWritableFile handlableTarget = this.castWritableFile(targetFile);
			this.connector.moveFile(
				AzureStorageFileSystem.toPath(sourceFile.ensureOpenHandle()),
				AzureStorageFileSystem.toPath(handlableTarget.ensureOpenHandle())
			);
		}

		@Override
		protected long specificCopyTo(
			final AzureStorageReadableFile  sourceFile,
			final AWritableFile   targetFile
		)
		{
			final AzureStorageWritableFile handlableTarget = this.castWritableFile(targetFile);
			return this.connector.copyFile(
				AzureStorageFileSystem.toPath(sourceFile.ensureOpenHandle()),
				AzureStorageFileSystem.toPath(handlableTarget.ensureOpenHandle())
			);
		}

		@Override
		protected long specificCopyTo(
			final AzureStorageReadableFile  sourceFile    ,
			final long            sourcePosition,
			final AWritableFile   targetFile
		)
		{
			final AzureStorageWritableFile handlableTarget = this.castWritableFile(targetFile);
			return this.connector.copyFile(
				AzureStorageFileSystem.toPath(sourceFile.ensureOpenHandle()),
				AzureStorageFileSystem.toPath(handlableTarget.ensureOpenHandle()),
				sourcePosition,
				-1L
			);
		}

		@Override
		protected long specificCopyTo(
			final AzureStorageReadableFile  sourceFile    ,
			final long            sourcePosition,
			final long            length        ,
			final AWritableFile   targetFile
		)
		{
			final AzureStorageWritableFile handlableTarget = this.castWritableFile(targetFile);
			return this.connector.copyFile(
				AzureStorageFileSystem.toPath(sourceFile.ensureOpenHandle()),
				AzureStorageFileSystem.toPath(handlableTarget.ensureOpenHandle()),
				sourcePosition,
				length
			);
		}

	}

}
