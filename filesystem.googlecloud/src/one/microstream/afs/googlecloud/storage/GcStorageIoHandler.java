package one.microstream.afs.googlecloud.storage;

import static one.microstream.X.notNull;

import java.nio.ByteBuffer;

import one.microstream.afs.ADirectory;
import one.microstream.afs.AFile;
import one.microstream.afs.AIoHandler;
import one.microstream.afs.AWritableFile;
import one.microstream.io.BufferProvider;

public interface GcStorageIoHandler extends AIoHandler
{
	public GcStorageConnector connector();



	public static GcStorageIoHandler New(
		final GcStorageConnector connector
	)
	{
		return new GcStorageIoHandler.Default(
			notNull(connector)
		);
	}


	public static final class Default
	extends AIoHandler.Abstract<
		GcStoragePath,
		GcStoragePath,
		GcStorageItemWrapper,
		GcStorageFileWrapper,
		ADirectory,
		GcStorageReadableFile,
		GcStorageWritableFile
	>
	implements GcStorageIoHandler
	{
		private final GcStorageConnector connector;


		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(final GcStorageConnector connector)
		{
			super(
				GcStorageItemWrapper .class,
				GcStorageFileWrapper .class,
				ADirectory           .class,
				GcStorageReadableFile.class,
				GcStorageWritableFile.class
			);

			this.connector = connector;
		}

		@Override
		public GcStorageConnector connector()
		{
			return this.connector;
		}

		@Override
		protected GcStoragePath toSubjectFile(
			final AFile file
		)
		{
			return GcStorageFileSystem.toPath(file);
		}

		@Override
		protected GcStoragePath toSubjectDirectory(
			final ADirectory directory
		)
		{
			return GcStorageFileSystem.toPath(directory);
		}

		@Override
		protected long subjectFileSize(
			final GcStoragePath file
		)
		{
			return this.connector.fileSize(file);
		}

		@Override
		protected boolean subjectFileExists(
			final GcStoragePath file
		)
		{
			return this.connector.fileExists(file);
		}

		@Override
		protected boolean subjectDirectoryExists(
			final GcStoragePath directory
		)
		{
			return this.connector.directoryExists(directory);
		}

		@Override
		protected long specificSize(
			final GcStorageFileWrapper file
		)
		{
			return this.subjectFileSize(file.path());
		}

		@Override
		protected boolean specificExists(
			final GcStorageFileWrapper file
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
			final GcStorageReadableFile file
		)
		{
			return file.openHandle();
		}

		@Override
		protected boolean specificIsOpen(
			final GcStorageReadableFile file
		)
		{
			return file.isHandleOpen();
		}

		@Override
		protected boolean specificClose(
			final GcStorageReadableFile file
		)
		{
			return file.closeHandle();
		}

		@Override
		protected boolean specificOpenWriting(
			final GcStorageWritableFile file
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
			final GcStorageWritableFile file
		)
		{
			this.connector.createFile(
				this.toSubjectFile(file)
			);
		}

		@Override
		protected boolean specificDeleteFile(
			final GcStorageWritableFile file
		)
		{
			return this.connector.deleteFile(
				GcStorageFileSystem.toPath(file)
			);
		}

		@Override
		protected ByteBuffer specificReadBytes(
			final GcStorageReadableFile sourceFile
		)
		{
			return this.connector.readData(
				GcStorageFileSystem.toPath(sourceFile.ensureOpenHandle()),
				0,
				-1
			);
		}

		@Override
		protected ByteBuffer specificReadBytes(
			final GcStorageReadableFile sourceFile,
			final long                  position
		)
		{
			return this.connector.readData(
				GcStorageFileSystem.toPath(sourceFile.ensureOpenHandle()),
				position,
				-1
			);
		}

		@Override
		protected ByteBuffer specificReadBytes(
			final GcStorageReadableFile sourceFile,
			final long                  position  ,
			final long                  length
		)
		{
			return this.connector.readData(
				GcStorageFileSystem.toPath(sourceFile.ensureOpenHandle()),
				position,
				length
			);
		}

		@Override
		protected long specificReadBytes(
			final GcStorageReadableFile sourceFile  ,
			final ByteBuffer            targetBuffer
		)
		{
			return this.connector.readData(
				GcStorageFileSystem.toPath(sourceFile.ensureOpenHandle()),
				targetBuffer,
				0,
				-1
			);
		}

		@Override
		protected long specificReadBytes(
			final GcStorageReadableFile sourceFile  ,
			final ByteBuffer            targetBuffer,
			final long                  position
		)
		{
			return this.connector.readData(
				GcStorageFileSystem.toPath(sourceFile.ensureOpenHandle()),
				targetBuffer,
				position,
				-1
			);
		}

		@Override
		protected long specificReadBytes(
			final GcStorageReadableFile sourceFile  ,
			final ByteBuffer            targetBuffer,
			final long                  position    ,
			final long                  length
		)
		{
			return this.connector.readData(
				GcStorageFileSystem.toPath(sourceFile.ensureOpenHandle()),
				targetBuffer,
				position,
				length
			);
		}

		@Override
		protected long specificReadBytes(
			final GcStorageReadableFile sourceFile    ,
			final BufferProvider        bufferProvider
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
			final GcStorageReadableFile sourceFile    ,
			final BufferProvider        bufferProvider,
			final long                  position
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
			final GcStorageReadableFile sourceFile    ,
			final BufferProvider        bufferProvider,
			final long                  position      ,
			final long                  length
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
			final GcStorageWritableFile          targetFile   ,
			final Iterable<? extends ByteBuffer> sourceBuffers
		)
		{
			this.openWriting(targetFile);

			return this.connector.writeData(
				GcStorageFileSystem.toPath(targetFile.ensureOpenHandle()),
				sourceBuffers
			);
		}

		@Override
		protected void specificMoveFile(
			final GcStorageWritableFile sourceFile,
			final AWritableFile         targetFile
		)
		{
			final GcStorageWritableFile handlableTarget = this.castWritableFile(targetFile);
			this.connector.moveFile(
				GcStorageFileSystem.toPath(sourceFile.ensureOpenHandle()),
				GcStorageFileSystem.toPath(handlableTarget.ensureOpenHandle())
			);
		}

		@Override
		protected long specificCopyTo(
			final GcStorageReadableFile sourceFile,
			final AWritableFile         targetFile
		)
		{
			final GcStorageWritableFile handlableTarget = this.castWritableFile(targetFile);
			return this.connector.copyFile(
				GcStorageFileSystem.toPath(sourceFile.ensureOpenHandle()),
				GcStorageFileSystem.toPath(handlableTarget.ensureOpenHandle())
			);
		}

		@Override
		protected long specificCopyTo(
			final GcStorageReadableFile sourceFile    ,
			final long                  sourcePosition,
			final AWritableFile         targetFile
		)
		{
			final GcStorageWritableFile handlableTarget = this.castWritableFile(targetFile);
			return this.connector.copyFile(
				GcStorageFileSystem.toPath(sourceFile.ensureOpenHandle()),
				GcStorageFileSystem.toPath(handlableTarget.ensureOpenHandle()),
				sourcePosition,
				-1L
			);
		}

		@Override
		protected long specificCopyTo(
			final GcStorageReadableFile sourceFile    ,
			final long                  sourcePosition,
			final long                  length        ,
			final AWritableFile         targetFile
		)
		{
			final GcStorageWritableFile handlableTarget = this.castWritableFile(targetFile);
			return this.connector.copyFile(
				GcStorageFileSystem.toPath(sourceFile.ensureOpenHandle()),
				GcStorageFileSystem.toPath(handlableTarget.ensureOpenHandle()),
				sourcePosition,
				length
			);
		}

	}

}
