package one.microstream.afs.aws.s3;

import static one.microstream.X.notNull;

import java.nio.ByteBuffer;

import one.microstream.afs.ADirectory;
import one.microstream.afs.AFile;
import one.microstream.afs.AIoHandler;
import one.microstream.afs.AWritableFile;
import one.microstream.io.BufferProvider;

public interface S3IoHandler extends AIoHandler
{
	public S3Connector connector();



	public static S3IoHandler New(
		final S3Connector connector
	)
	{
		return new S3IoHandler.Default(
			notNull(connector)
		);
	}


	public static final class Default
	extends AIoHandler.Abstract<
		S3Path,
		S3Path,
		S3ItemWrapper,
		S3FileWrapper,
		ADirectory,
		S3ReadableFile,
		S3WritableFile
	>
	implements S3IoHandler
	{
		private final S3Connector connector;


		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(final S3Connector connector)
		{
			super(
				S3ItemWrapper .class,
				S3FileWrapper .class,
				ADirectory    .class,
				S3ReadableFile.class,
				S3WritableFile.class
			);

			this.connector = connector;
		}

		@Override
		public S3Connector connector()
		{
			return this.connector;
		}

		@Override
		protected S3Path toSubjectFile(
			final AFile file
		)
		{
			return S3FileSystem.toPath(file);
		}

		@Override
		protected S3Path toSubjectDirectory(
			final ADirectory directory
		)
		{
			return S3FileSystem.toPath(directory);
		}

		@Override
		protected long subjectFileSize(
			final S3Path file
		)
		{
			return this.connector.fileSize(file);
		}

		@Override
		protected boolean subjectFileExists(
			final S3Path file
		)
		{
			return this.connector.fileExists(file);
		}

		@Override
		protected boolean subjectDirectoryExists(
			final S3Path directory
		)
		{
			return this.connector.directoryExists(directory);
		}

		@Override
		protected long specificSize(
			final S3FileWrapper file
		)
		{
			return this.subjectFileSize(file.path());
		}

		@Override
		protected boolean specificExists(
			final S3FileWrapper file
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
			final S3ReadableFile file
		)
		{
			return file.openHandle();
		}

		@Override
		protected boolean specificIsOpen(
			final S3ReadableFile file
		)
		{
			return file.isHandleOpen();
		}

		@Override
		protected boolean specificClose(
			final S3ReadableFile file
		)
		{
			return file.closeHandle();
		}

		@Override
		protected boolean specificOpenWriting(
			final S3WritableFile file
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
			final S3WritableFile file
		)
		{
			this.connector.createFile(
				this.toSubjectFile(file)
			);
		}

		@Override
		protected boolean specificDeleteFile(
			final S3WritableFile file
		)
		{
			return this.connector.deleteFile(
				S3FileSystem.toPath(file)
			);
		}

		@Override
		protected ByteBuffer specificReadBytes(
			final S3ReadableFile sourceFile
		)
		{
			return this.connector.readData(
				S3FileSystem.toPath(sourceFile.ensureOpenHandle()),
				0,
				-1
			);
		}

		@Override
		protected ByteBuffer specificReadBytes(
			final S3ReadableFile sourceFile,
			final long           position
		)
		{
			return this.connector.readData(
				S3FileSystem.toPath(sourceFile.ensureOpenHandle()),
				position,
				-1
			);
		}

		@Override
		protected ByteBuffer specificReadBytes(
			final S3ReadableFile sourceFile,
			final long           position  ,
			final long           length
		)
		{
			return this.connector.readData(
				S3FileSystem.toPath(sourceFile.ensureOpenHandle()),
				position,
				length
			);
		}

		@Override
		protected long specificReadBytes(
			final S3ReadableFile sourceFile  ,
			final ByteBuffer     targetBuffer
		)
		{
			return this.connector.readData(
				S3FileSystem.toPath(sourceFile.ensureOpenHandle()),
				targetBuffer,
				0,
				-1
			);
		}

		@Override
		protected long specificReadBytes(
			final S3ReadableFile sourceFile  ,
			final ByteBuffer     targetBuffer,
			final long           position
		)
		{
			return this.connector.readData(
				S3FileSystem.toPath(sourceFile.ensureOpenHandle()),
				targetBuffer,
				position,
				-1
			);
		}

		@Override
		protected long specificReadBytes(
			final S3ReadableFile sourceFile  ,
			final ByteBuffer     targetBuffer,
			final long           position    ,
			final long           length
		)
		{
			return this.connector.readData(
				S3FileSystem.toPath(sourceFile.ensureOpenHandle()),
				targetBuffer,
				position,
				length
			);
		}

		@Override
		protected long specificReadBytes(
			final S3ReadableFile sourceFile    ,
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
			final S3ReadableFile sourceFile    ,
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
			final S3ReadableFile sourceFile    ,
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
			final S3WritableFile                 targetFile   ,
			final Iterable<? extends ByteBuffer> sourceBuffers
		)
		{
			this.openWriting(targetFile);

			return this.connector.writeData(
				S3FileSystem.toPath(targetFile.ensureOpenHandle()),
				sourceBuffers
			);
		}

		@Override
		protected void specificMoveFile(
			final S3WritableFile  sourceFile,
			final AWritableFile   targetFile
		)
		{
			final S3WritableFile handlableTarget = this.castWritableFile(targetFile);
			this.connector.moveFile(
				S3FileSystem.toPath(sourceFile.ensureOpenHandle()),
				S3FileSystem.toPath(handlableTarget.ensureOpenHandle())
			);
		}

		@Override
		protected long specificCopyTo(
			final S3ReadableFile  sourceFile,
			final AWritableFile   targetFile
		)
		{
			final S3WritableFile handlableTarget = this.castWritableFile(targetFile);
			return this.connector.copyFile(
				S3FileSystem.toPath(sourceFile.ensureOpenHandle()),
				S3FileSystem.toPath(handlableTarget.ensureOpenHandle())
			);
		}

		@Override
		protected long specificCopyTo(
			final S3ReadableFile  sourceFile    ,
			final long            sourcePosition,
			final AWritableFile   targetFile
		)
		{
			final S3WritableFile handlableTarget = this.castWritableFile(targetFile);
			return this.connector.copyFile(
				S3FileSystem.toPath(sourceFile.ensureOpenHandle()),
				S3FileSystem.toPath(handlableTarget.ensureOpenHandle()),
				sourcePosition,
				-1L
			);
		}

		@Override
		protected long specificCopyTo(
			final S3ReadableFile  sourceFile    ,
			final long            sourcePosition,
			final long            length        ,
			final AWritableFile   targetFile
		)
		{
			final S3WritableFile handlableTarget = this.castWritableFile(targetFile);
			return this.connector.copyFile(
				S3FileSystem.toPath(sourceFile.ensureOpenHandle()),
				S3FileSystem.toPath(handlableTarget.ensureOpenHandle()),
				sourcePosition,
				length
			);
		}

	}

}
