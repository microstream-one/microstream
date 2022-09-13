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

import one.microstream.afs.types.ADirectory;
import one.microstream.afs.types.AFile;
import one.microstream.afs.types.AFileSystem;
import one.microstream.afs.types.AItem;
import one.microstream.afs.types.AReadableFile;
import one.microstream.afs.types.AResolver;
import one.microstream.afs.types.AWritableFile;
import one.microstream.chars.VarString;
import one.microstream.chars.XChars;
import one.microstream.io.XIO;

public interface BlobStoreFileSystem extends AFileSystem, AResolver<BlobStorePath, BlobStorePath>
{
	public static BlobStorePath toPath(
		final AItem item
	)
	{
		return toPath(
			item.toPath()
		);
	}

	public static BlobStorePath toPath(
		final String... pathElements
	)
	{
		return BlobStorePath.New(
			notNull(pathElements)
		);
	}


	public static BlobStoreFileSystem New(
		final BlobStoreConnector connector
	)
	{
		return New(
			BlobStoreIoHandler.New(connector)
		);
	}

	public static BlobStoreFileSystem New(
		final BlobStoreIoHandler ioHandler
	)
	{
		return new BlobStoreFileSystem.Default(
			notNull(ioHandler)
		);
	}

	@Override
	public BlobStoreIoHandler ioHandler();


	public static class Default
	extends    AFileSystem.Abstract<BlobStoreIoHandler, BlobStorePath, BlobStorePath>
	implements BlobStoreFileSystem
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Default(
			final BlobStoreIoHandler ioHandler
		)
		{
			super(
				"http://",
				ioHandler
			);
		}


		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public String deriveFileIdentifier(
			final String fileName,
			final String fileType
		)
		{
			return XIO.addFileSuffix(fileName, fileType);
		}

		@Override
		public String deriveFileName(
			final String fileIdentifier
		)
		{
			return XIO.getFilePrefix(fileIdentifier);
		}

		@Override
		public String deriveFileType(
			final String fileIdentifier
		)
		{
			return XIO.getFileSuffix(fileIdentifier);
		}

		@Override
		public String getFileName(
			final AFile file
		)
		{
			return XIO.getFilePrefix(file.identifier());
		}

		@Override
		public String getFileType(
			final AFile file
		)
		{
			return XIO.getFileSuffix(file.identifier());
		}

		@Override
		public String[] resolveDirectoryToPath(
			final BlobStorePath directory
		)
		{
			return directory.pathElements();
		}

		@Override
		public String[] resolveFileToPath(
			final BlobStorePath file
		)
		{
			return file.pathElements();
		}

		@Override
		public BlobStorePath resolve(
			final ADirectory directory
		)
		{
			return BlobStoreFileSystem.toPath(directory);
		}

		@Override
		public BlobStorePath resolve(
			final AFile file
		)
		{
			return BlobStoreFileSystem.toPath(file);
		}

		@Override
		protected VarString assembleItemPath(
			final AItem     item,
			final VarString vs
		)
		{
			return XChars.assembleSeparated(
				vs,
				BlobStorePath.SEPARATOR_CHAR,
				item.toPath()
			);
		}

		@Override
		public AReadableFile wrapForReading(
			final AFile  file,
			final Object user
		)
		{
			final BlobStorePath path = this.resolve(file);
			return BlobStoreReadableFile.New(file, user, path);
		}

		@Override
		public AWritableFile wrapForWriting(
			final AFile  file,
			final Object user
		)
		{
			final BlobStorePath path = this.resolve(file);
			return BlobStoreWritableFile.New(file, user, path);
		}

		@Override
		public AReadableFile convertToReading(
			final AWritableFile file
		)
		{
			return BlobStoreReadableFile.New(
				file,
				file.user(),
				((BlobStoreWritableFile)file).path()
			);
		}

		@Override
		public AWritableFile convertToWriting(
			final AReadableFile file
		)
		{
			return BlobStoreWritableFile.New(
				file,
				file.user(),
				((BlobStoreReadableFile)file).path()
			);
		}

	}

}
