package one.microstream.afs.blobstore.types;

/*-
 * #%L
 * microstream-afs-blobstore
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

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

public interface BlobStoreIoHandler extends AIoHandler
{
	public BlobStoreConnector connector();


	public static BlobStoreIoHandler New(
		final BlobStoreConnector connector
	)
	{
		return New(
			WriteController.Enabled(),
			connector
		);
	}


	public static BlobStoreIoHandler New(
		final WriteController    writeController,
		final BlobStoreConnector connector
	)
	{
		return new BlobStoreIoHandler.Default(
			notNull(writeController),
			notNull(connector)
		);
	}


	public static final class Default
	extends AIoHandler.Abstract<
		BlobStorePath,
		BlobStorePath,
		BlobStoreItemWrapper,
		BlobStoreFileWrapper,
		ADirectory,
		BlobStoreReadableFile,
		BlobStoreWritableFile
	>
	implements BlobStoreIoHandler
	{
		private final BlobStoreConnector connector;


		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final WriteController    writeController,
			final BlobStoreConnector connector
		)
		{
			super(
				writeController            ,
				BlobStoreItemWrapper .class,
				BlobStoreFileWrapper .class,
				ADirectory           .class,
				BlobStoreReadableFile.class,
				BlobStoreWritableFile.class
			);

			this.connector = connector;
		}

		@Override
		public BlobStoreConnector connector()
		{
			return this.connector;
		}

		@Override
		protected BlobStorePath toSubjectFile(
			final AFile file
		)
		{
			return BlobStoreFileSystem.toPath(file);
		}

		@Override
		protected BlobStorePath toSubjectDirectory(
			final ADirectory directory
		)
		{
			return BlobStoreFileSystem.toPath(directory);
		}

		@Override
		protected long subjectFileSize(
			final BlobStorePath file
		)
		{
			return this.connector.fileSize(file);
		}

		@Override
		protected boolean subjectFileExists(
			final BlobStorePath file
		)
		{
			return this.connector.fileExists(file);
		}

		@Override
		protected boolean subjectDirectoryExists(
			final BlobStorePath directory
		)
		{
			return this.connector.directoryExists(directory);
		}

		@Override
		protected long specificSize(
			final BlobStoreFileWrapper file
		)
		{
			return this.subjectFileSize(file.path());
		}

		@Override
		protected boolean specificExists(
			final BlobStoreFileWrapper file
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
			final BlobStorePath      directoryPath = this.toSubjectDirectory(parent);

			this.connector.visitChildren(directoryPath, new BlobStorePathVisitor()
			{
				@Override
				public void visitDirectory(
					final BlobStorePath parent       ,
					final String        directoryName
				)
				{
					items.add(directoryName);
				}

				@Override
				public void visitFile(
					final BlobStorePath parent  ,
					final String        fileName
				)
				{
					items.add(fileName);
				}
			});

			return items;
		}

		@Override
		protected XGettingEnum<String> specificListDirectories(
			final ADirectory parent
		)
		{
			final EqHashEnum<String> items         = EqHashEnum.New();
			final BlobStorePath      directoryPath = this.toSubjectDirectory(parent);

			this.connector.visitChildren(directoryPath, new BlobStorePathVisitor()
			{
				@Override
				public void visitDirectory(
					final BlobStorePath parent       ,
					final String        directoryName
				)
				{
					items.add(directoryName);
				}

				@Override
				public void visitFile(
					final BlobStorePath parent  ,
					final String        fileName
				)
				{
					// no-op
				}
			});

			return items;
		}

		@Override
		protected XGettingEnum<String> specificListFiles(
			final ADirectory parent
		)
		{
			final EqHashEnum<String> items         = EqHashEnum.New();
			final BlobStorePath      directoryPath = this.toSubjectDirectory(parent);

			this.connector.visitChildren(directoryPath, new BlobStorePathVisitor()
			{
				@Override
				public void visitDirectory(
					final BlobStorePath parent       ,
					final String        directoryName
				)
				{
					// no-op
				}

				@Override
				public void visitFile(
					final BlobStorePath parent  ,
					final String        fileName
				)
				{
					items.add(fileName);
				}
			});

			return items;
		}

		@Override
		protected void specificInventorize(
			final ADirectory directory
		)
		{
			final BlobStorePath directoryPath = this.toSubjectDirectory(directory);
			if(!this.subjectDirectoryExists(directoryPath))
			{
				// nothing to do
				return;
			}

			this.connector.visitChildren(directoryPath, new BlobStorePathVisitor()
			{
				@Override
				public void visitDirectory(
					final BlobStorePath parent       ,
					final String        directoryName
				)
				{
					directory.ensureDirectory(directoryName);
				}

				@Override
				public void visitFile(
					final BlobStorePath parent  ,
					final String        fileName
				)
				{
					directory.ensureFile(fileName);
				}
			});
		}

		@Override
		protected boolean specificOpenReading(
			final BlobStoreReadableFile file
		)
		{
			return file.openHandle();
		}

		@Override
		protected boolean specificIsOpen(
			final BlobStoreReadableFile file
		)
		{
			return file.isHandleOpen();
		}

		@Override
		protected boolean specificClose(
			final BlobStoreReadableFile file
		)
		{
			return file.closeHandle();
		}

		@Override
		protected boolean specificOpenWriting(
			final BlobStoreWritableFile file
		)
		{
			return file.openHandle();
		}
		
		@Override
		protected boolean specificIsEmpty(
			final ADirectory directory
		)
		{
			return this.connector.isEmpty(
				this.toSubjectDirectory(directory)
			);
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
			final BlobStoreWritableFile file
		)
		{
			this.connector.createFile(
				this.toSubjectFile(file)
			);
		}

		@Override
		protected boolean specificDeleteFile(
			final BlobStoreWritableFile file
		)
		{
			return this.connector.deleteFile(
				BlobStoreFileSystem.toPath(file)
			);
		}

		@Override
		protected ByteBuffer specificReadBytes(
			final BlobStoreReadableFile sourceFile
		)
		{
			return this.connector.readData(
				BlobStoreFileSystem.toPath(sourceFile.ensureOpenHandle()),
				0,
				-1
			);
		}

		@Override
		protected ByteBuffer specificReadBytes(
			final BlobStoreReadableFile sourceFile,
			final long                  position
		)
		{
			return this.connector.readData(
				BlobStoreFileSystem.toPath(sourceFile.ensureOpenHandle()),
				position,
				-1
			);
		}

		@Override
		protected ByteBuffer specificReadBytes(
			final BlobStoreReadableFile sourceFile,
			final long                  position  ,
			final long                  length
		)
		{
			return this.connector.readData(
				BlobStoreFileSystem.toPath(sourceFile.ensureOpenHandle()),
				position,
				length
			);
		}

		@Override
		protected long specificReadBytes(
			final BlobStoreReadableFile sourceFile  ,
			final ByteBuffer            targetBuffer
		)
		{
			return this.connector.readData(
				BlobStoreFileSystem.toPath(sourceFile.ensureOpenHandle()),
				targetBuffer,
				0,
				-1
			);
		}

		@Override
		protected long specificReadBytes(
			final BlobStoreReadableFile sourceFile  ,
			final ByteBuffer            targetBuffer,
			final long                  position
		)
		{
			return this.connector.readData(
				BlobStoreFileSystem.toPath(sourceFile.ensureOpenHandle()),
				targetBuffer,
				position,
				targetBuffer.remaining()
			);
		}

		@Override
		protected long specificReadBytes(
			final BlobStoreReadableFile sourceFile  ,
			final ByteBuffer            targetBuffer,
			final long                  position    ,
			final long                  length
		)
		{
			return this.connector.readData(
				BlobStoreFileSystem.toPath(sourceFile.ensureOpenHandle()),
				targetBuffer,
				position,
				length
			);
		}

		@Override
		protected long specificReadBytes(
			final BlobStoreReadableFile sourceFile    ,
			final BufferProvider        bufferProvider
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
			final BlobStoreReadableFile sourceFile    ,
			final BufferProvider        bufferProvider,
			final long                  position
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
			final BlobStoreReadableFile sourceFile    ,
			final BufferProvider        bufferProvider,
			final long                  position      ,
			final long                  length
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
			final BlobStoreWritableFile          targetFile   ,
			final Iterable<? extends ByteBuffer> sourceBuffers
		)
		{
			this.openWriting(targetFile);

			return this.connector.writeData(
				BlobStoreFileSystem.toPath(targetFile.ensureOpenHandle()),
				sourceBuffers
			);
		}

		@Override
		protected void specificMoveFile(
			final BlobStoreWritableFile sourceFile,
			final AWritableFile         targetFile
		)
		{
			final BlobStoreWritableFile handlableTarget = this.castWritableFile(targetFile);
			this.connector.moveFile(
				BlobStoreFileSystem.toPath(sourceFile.ensureOpenHandle()),
				BlobStoreFileSystem.toPath(handlableTarget.ensureOpenHandle())
			);
		}

		@Override
		protected long specificCopyTo(
			final BlobStoreReadableFile sourceFile,
			final AWritableFile         targetFile
		)
		{
			final BlobStoreWritableFile handlableTarget = this.castWritableFile(targetFile);
			return this.connector.copyFile(
				BlobStoreFileSystem.toPath(sourceFile.ensureOpenHandle()),
				BlobStoreFileSystem.toPath(handlableTarget.ensureOpenHandle()),
				0,
				-1
			);
		}

		@Override
		protected long specificCopyTo(
			final BlobStoreReadableFile sourceFile    ,
			final long                  sourcePosition,
			final AWritableFile         targetFile
		)
		{
			final BlobStoreWritableFile handlableTarget = this.castWritableFile(targetFile);
			return this.connector.copyFile(
				BlobStoreFileSystem.toPath(sourceFile.ensureOpenHandle()),
				BlobStoreFileSystem.toPath(handlableTarget.ensureOpenHandle()),
				sourcePosition,
				-1L
			);
		}

		@Override
		protected long specificCopyTo(
			final BlobStoreReadableFile sourceFile    ,
			final long                  sourcePosition,
			final long                  length        ,
			final AWritableFile         targetFile
		)
		{
			final BlobStoreWritableFile handlableTarget = this.castWritableFile(targetFile);
			return this.connector.copyFile(
				BlobStoreFileSystem.toPath(sourceFile.ensureOpenHandle()),
				BlobStoreFileSystem.toPath(handlableTarget.ensureOpenHandle()),
				sourcePosition,
				length
			);
		}

		@Override
		protected long specificCopyFrom(
			final AReadableFile         source       ,
			final BlobStoreWritableFile targetSubject
		)
		{
			final BlobStoreReadableFile handlableSource = this.castReadableFile(source);
			return this.connector.copyFile(
				BlobStoreFileSystem.toPath(handlableSource.ensureOpenHandle()),
				BlobStoreFileSystem.toPath(targetSubject.ensureOpenHandle()),
				0,
				-1
			);
		}

		@Override
		protected long specificCopyFrom(
			final AReadableFile         source        ,
			final long                  sourcePosition,
			final BlobStoreWritableFile targetSubject
		)
		{
			final BlobStoreReadableFile handlableSource = this.castReadableFile(source);
			return this.connector.copyFile(
				BlobStoreFileSystem.toPath(handlableSource.ensureOpenHandle()),
				BlobStoreFileSystem.toPath(targetSubject.ensureOpenHandle()),
				sourcePosition,
				-1L
			);
		}

		@Override
		protected long specificCopyFrom(
			final AReadableFile         source        ,
			final long                  sourcePosition,
			final long                  length        ,
			final BlobStoreWritableFile targetSubject
		)
		{
			final BlobStoreReadableFile handlableSource = this.castReadableFile(source);
			return this.connector.copyFile(
				BlobStoreFileSystem.toPath(handlableSource.ensureOpenHandle()),
				BlobStoreFileSystem.toPath(targetSubject.ensureOpenHandle()),
				sourcePosition,
				length
			);
		}

		@Override
		protected void specificTruncateFile(
			final BlobStoreWritableFile file   ,
			final long                  newSize
		)
		{
			this.connector.truncateFile(file.path(), newSize);
		}

	}

}
