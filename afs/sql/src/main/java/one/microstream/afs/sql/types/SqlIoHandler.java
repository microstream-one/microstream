package one.microstream.afs.sql.types;

import static one.microstream.X.notNull;

import java.nio.ByteBuffer;

import one.microstream.afs.types.ADirectory;
import one.microstream.afs.types.AFile;
import one.microstream.afs.types.AIoHandler;
import one.microstream.afs.types.AReadableFile;
import one.microstream.afs.types.AWritableFile;
import one.microstream.afs.types.WriteController;
import one.microstream.collections.EqHashEnum;
import one.microstream.collections.types.XGettingEnum;
import one.microstream.io.BufferProvider;

public interface SqlIoHandler extends AIoHandler
{
	public SqlConnector connector();


	public static SqlIoHandler New(
		final SqlConnector connector
	)
	{
		return New(
			WriteController.Enabled(),
			connector
		);
	}

	public static SqlIoHandler New(
		final WriteController writeController,
		final SqlConnector    connector
	)
	{
		return new SqlIoHandler.Default(
			notNull(writeController),
			notNull(connector)
		);
	}

	public static final class Default
	extends AIoHandler.Abstract<
		SqlPath,
		SqlPath,
		SqlItemWrapper,
		SqlFileWrapper,
		ADirectory,
		SqlReadableFile,
		SqlWritableFile
	>
	implements SqlIoHandler
	{
		private final SqlConnector connector;


		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final WriteController writeController,
			final SqlConnector    sqlConnector
		)
		{
			super(
				writeController      ,
				SqlItemWrapper .class,
				SqlFileWrapper .class,
				ADirectory     .class,
				SqlReadableFile.class,
				SqlWritableFile.class
			);

			this.connector = sqlConnector;
		}

		@Override
		public SqlConnector connector()
		{
			return this.connector;
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
			return this.connector.fileSize(file);
		}

		@Override
		protected boolean subjectFileExists(
			final SqlPath file
		)
		{
			return this.connector.fileExists(file);
		}

		@Override
		protected boolean subjectDirectoryExists(
			final SqlPath directory
		)
		{
			return this.connector.directoryExists(directory);
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
		protected XGettingEnum<String> specificListItems(
			final ADirectory parent
		)
		{
			final EqHashEnum<String> items         = EqHashEnum.New();
			final SqlPath            directoryPath = this.toSubjectDirectory(parent);
			this.connector.visitDirectories(
				directoryPath,
				items::add
			);
			this.connector.visitFiles(
				directoryPath,
				items::add
			);
			return items;
		}

		@Override
		protected XGettingEnum<String> specificListDirectories(
			final ADirectory parent
		)
		{
			final EqHashEnum<String> directories = EqHashEnum.New();
			this.connector.visitDirectories(
				this.toSubjectDirectory(parent),
				directories::add
			);
			return directories;
		}

		@Override
		protected XGettingEnum<String> specificListFiles(
			final ADirectory parent
		)
		{
			final EqHashEnum<String> files = EqHashEnum.New();
			this.connector.visitFiles(
				this.toSubjectDirectory(parent),
				files::add
			);
			return files;
		}

		@Override
		protected void specificInventorize(
			final ADirectory directory
		)
		{
			final SqlPath dirPath = this.toSubjectDirectory(directory);
			if(!this.subjectDirectoryExists(dirPath))
			{
				// nothing to do
				return;
			}

			this.connector.visitDirectories(dirPath, directory::ensureDirectory);
			this.connector.visitFiles      (dirPath, directory::ensureFile     );
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
			return file.openHandle();
		}

		/*
		 * Per default directories are created recursively.
		 * But since we create tables named with the full path of the directory,
		 * a single create is sufficient.
		 */
		@Override
		public void create(final ADirectory directory)
		{
			this.validateHandledDirectory(directory);

			synchronized(this)
			{
				directory.iterateObservers(o ->
					o.onBeforeDirectoryCreate(directory)
				);

				this.specificCreate(directory);

				directory.iterateObservers(o ->
					o.onAfterDirectoryCreate(directory)
				);
			}
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
			return this.connector.deleteFile(
				SqlFileSystem.toPath(file)
			);
		}

		@Override
		protected ByteBuffer specificReadBytes(
			final SqlReadableFile sourceFile
		)
		{
			return this.connector.readData(
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
			return this.connector.readData(
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
			return this.connector.readData(
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
			return this.connector.readData(
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
			return this.connector.readData(
				SqlFileSystem.toPath(sourceFile.ensureOpenHandle()),
				targetBuffer,
				position,
				targetBuffer.remaining()
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
			return this.connector.readData(
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
				return this.specificReadBytes(sourceFile, bufferProvider.provideBuffer());
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
				return this.specificReadBytes(sourceFile, bufferProvider.provideBuffer(), position);
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
				return this.specificReadBytes(sourceFile, bufferProvider.provideBuffer(length), position, length);
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
			return this.connector.writeData(
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
			this.connector.moveFile(
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
			return this.connector.copyFile(
				SqlFileSystem.toPath(sourceFile.ensureOpenHandle()),
				SqlFileSystem.toPath(handlableTarget.ensureOpenHandle()),
				0,
				-1
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
			return this.connector.copyFile(
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
			return this.connector.copyFile(
				SqlFileSystem.toPath(sourceFile.ensureOpenHandle()),
				SqlFileSystem.toPath(handlableTarget.ensureOpenHandle()),
				sourcePosition,
				length
			);
		}

		@Override
		protected long specificCopyFrom(
			final AReadableFile   source       ,
			final SqlWritableFile targetSubject
		)
		{
			final SqlReadableFile handlableSource = this.castReadableFile(source);
			return this.connector.copyFile(
				SqlFileSystem.toPath(handlableSource.ensureOpenHandle()),
				SqlFileSystem.toPath(targetSubject.ensureOpenHandle()),
				0,
				-1
			);
		}

		@Override
		protected long specificCopyFrom(
			final AReadableFile   source        ,
			final long            sourcePosition,
			final SqlWritableFile targetSubject
		)
		{
			final SqlReadableFile handlableSource = this.castReadableFile(source);
			return this.connector.copyFile(
				SqlFileSystem.toPath(handlableSource.ensureOpenHandle()),
				SqlFileSystem.toPath(targetSubject.ensureOpenHandle()),
				sourcePosition,
				-1L
			);
		}

		@Override
		protected long specificCopyFrom(
			final AReadableFile   source        ,
			final long            sourcePosition,
			final long            length        ,
			final SqlWritableFile targetSubject
		)
		{
			final SqlReadableFile handlableSource = this.castReadableFile(source);
			return this.connector.copyFile(
				SqlFileSystem.toPath(handlableSource.ensureOpenHandle()),
				SqlFileSystem.toPath(targetSubject.ensureOpenHandle()),
				sourcePosition,
				length
			);
		}

		@Override
		protected void specificTruncateFile(
			final SqlWritableFile file   ,
			final long            newSize
		)
		{
			this.connector.truncateFile(file.path(), newSize);
		}

	}

}
