package one.microstream.afs.sql;

import static one.microstream.X.notNull;

import java.nio.ByteBuffer;

import one.microstream.afs.ADirectory;
import one.microstream.afs.AFile;
import one.microstream.afs.AIoHandler;
import one.microstream.afs.AWritableFile;
import one.microstream.io.BufferProvider;

public interface SqlIoHandler extends AIoHandler
{
	public SqlConnector sqlConnector();


	public static SqlIoHandler New(
		final SqlConnector sqlConnector
	)
	{
		return new SqlIoHandler.Default(
			notNull(sqlConnector)
		);
	}


	public static final class Default
	extends AIoHandler.Abstract<SqlPath, SqlPath, SqlItemWrapper, SqlFileWrapper, ADirectory, SqlReadableFile, SqlWritableFile>
	implements SqlIoHandler
	{
		final SqlConnector sqlConnector;


		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(final SqlConnector sqlConnector)
		{
			super(
				SqlItemWrapper .class,
				SqlFileWrapper .class,
				ADirectory     .class,
				SqlReadableFile.class,
				SqlWritableFile.class
			);

			this.sqlConnector = sqlConnector;
		}

		@Override
		public SqlConnector sqlConnector()
		{
			return this.sqlConnector;
		}

		@Override
		protected SqlPath toSubjectFile(
			final AFile file
		)
		{
			return SqlFileSystem.toPath(file);
		}

		@Override
		protected SqlPath toSubjectDirectory(
			final ADirectory directory
		)
		{
			return SqlFileSystem.toPath(directory);
		}

		@Override
		protected long subjectFileSize(
			final SqlPath file
		)
		{
			return this.sqlConnector.fileSize(file);
		}

		@Override
		protected boolean subjectFileExists(
			final SqlPath file
		)
		{
			return this.sqlConnector.fileExists(file);
		}

		@Override
		protected boolean subjectDirectoryExists(
			final SqlPath directory
		)
		{
			/*
			 * Directories (tables) are created on demand when a file is opened for writing.
			 * @see #specificOpenWriting
			 */

			return true;
		}

		@Override
		protected long specificSize(
			final SqlFileWrapper file
		)
		{
			return this.subjectFileSize(file.path());
		}

		@Override
		protected boolean specificExists(
			final SqlFileWrapper file
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
			final SqlReadableFile file
		)
		{
			return file.openHandle();
		}

		@Override
		protected boolean specificIsOpen(
			final SqlReadableFile file
		)
		{
			return file.isHandleOpen();
		}

		@Override
		protected boolean specificClose(
			final SqlReadableFile file
		)
		{
			return file.closeHandle();
		}

		@Override
		protected boolean specificOpenWriting(
			final SqlWritableFile file
		)
		{
			this.sqlConnector.ensureDirectory(file.path().parentPath());

			return file.openHandle();
		}

		@Override
		protected void specificCreate(
			final ADirectory directory
		)
		{
			/*
			 * Directories (tables) are created on demand when a file is opened for writing.
			 * @see #specificOpenWriting
			 */
		}

		@Override
		protected void specificCreate(
			final SqlWritableFile file
		)
		{
			// existence of parent directory has already been ensured before calling this method
			// file records are created on first write
		}

		@Override
		protected boolean specificDeleteFile(
			final SqlWritableFile file
		)
		{
			return this.sqlConnector.deleteFile(
				SqlFileSystem.toPath(file)
			);
		}

		@Override
		protected ByteBuffer specificReadBytes(
			final SqlReadableFile sourceFile
		)
		{
			return this.sqlConnector.readData(
				SqlFileSystem.toPath(sourceFile.ensureOpenHandle()),
				0,
				-1
			);
		}

		@Override
		protected ByteBuffer specificReadBytes(
			final SqlReadableFile sourceFile,
			final long            position
		)
		{
			return this.sqlConnector.readData(
				SqlFileSystem.toPath(sourceFile.ensureOpenHandle()),
				position,
				-1
			);
		}

		@Override
		protected ByteBuffer specificReadBytes(
			final SqlReadableFile sourceFile,
			final long            position  ,
			final long            length
		)
		{
			return this.sqlConnector.readData(
				SqlFileSystem.toPath(sourceFile.ensureOpenHandle()),
				position,
				length
			);
		}

		@Override
		protected long specificReadBytes(
			final SqlReadableFile sourceFile  ,
			final ByteBuffer      targetBuffer
		)
		{
			return this.sqlConnector.readData(
				SqlFileSystem.toPath(sourceFile.ensureOpenHandle()),
				targetBuffer,
				0,
				-1
			);
		}

		@Override
		protected long specificReadBytes(
			final SqlReadableFile sourceFile  ,
			final ByteBuffer      targetBuffer,
			final long            position
		)
		{
			return this.sqlConnector.readData(
				SqlFileSystem.toPath(sourceFile.ensureOpenHandle()),
				targetBuffer,
				position,
				-1
			);
		}

		@Override
		protected long specificReadBytes(
			final SqlReadableFile sourceFile  ,
			final ByteBuffer      targetBuffer,
			final long            position    ,
			final long            length
		)
		{
			return this.sqlConnector.readData(
				SqlFileSystem.toPath(sourceFile.ensureOpenHandle()),
				targetBuffer,
				position,
				length
			);
		}

		@Override
		protected long specificReadBytes(
			final SqlReadableFile sourceFile    ,
			final BufferProvider  bufferProvider
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
			final SqlReadableFile sourceFile    ,
			final BufferProvider  bufferProvider,
			final long            position
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
			final SqlReadableFile sourceFile    ,
			final BufferProvider  bufferProvider,
			final long            position      ,
			final long            length
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
			final SqlWritableFile                targetFile   ,
			final Iterable<? extends ByteBuffer> sourceBuffers
		)
		{
			this.openWriting(targetFile);

			return this.sqlConnector.writeData(
				SqlFileSystem.toPath(targetFile.ensureOpenHandle()),
				sourceBuffers
			);
		}

		@Override
		protected void specificMoveFile(
			final SqlWritableFile sourceFile,
			final AWritableFile   targetFile
		)
		{
			final SqlWritableFile handlableTarget = this.castWritableFile(targetFile);
			this.sqlConnector.moveFile(
				SqlFileSystem.toPath(sourceFile.ensureOpenHandle()),
				SqlFileSystem.toPath(handlableTarget.ensureOpenHandle())
			);
		}

		@Override
		protected long specificCopyTo(
			final SqlReadableFile sourceFile,
			final AWritableFile   targetFile
		)
		{
			final SqlWritableFile handlableTarget = this.castWritableFile(targetFile);
			return this.sqlConnector.copyFile(
				SqlFileSystem.toPath(sourceFile.ensureOpenHandle()),
				SqlFileSystem.toPath(handlableTarget.ensureOpenHandle())
			);
		}

		@Override
		protected long specificCopyTo(
			final SqlReadableFile sourceFile    ,
			final long            sourcePosition,
			final AWritableFile   targetFile
		)
		{
			final SqlWritableFile handlableTarget = this.castWritableFile(targetFile);
			return this.sqlConnector.copyFile(
				SqlFileSystem.toPath(sourceFile.ensureOpenHandle()),
				SqlFileSystem.toPath(handlableTarget.ensureOpenHandle()),
				sourcePosition,
				-1L
			);
		}

		@Override
		protected long specificCopyTo(
			final SqlReadableFile sourceFile    ,
			final long            sourcePosition,
			final long            length        ,
			final AWritableFile   targetFile
		)
		{
			final SqlWritableFile handlableTarget = this.castWritableFile(targetFile);
			return this.sqlConnector.copyFile(
				SqlFileSystem.toPath(sourceFile.ensureOpenHandle()),
				SqlFileSystem.toPath(handlableTarget.ensureOpenHandle()),
				sourcePosition,
				length
			);
		}

	}

}
