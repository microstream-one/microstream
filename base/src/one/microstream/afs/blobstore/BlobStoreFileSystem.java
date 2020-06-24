package one.microstream.afs.blobstore;

import static one.microstream.X.notNull;

import one.microstream.afs.ADirectory;
import one.microstream.afs.AFile;
import one.microstream.afs.AFileSystem;
import one.microstream.afs.AItem;
import one.microstream.afs.AReadableFile;
import one.microstream.afs.AWritableFile;
import one.microstream.chars.VarString;
import one.microstream.chars.XChars;
import one.microstream.io.XIO;

public interface BlobStoreFileSystem extends AFileSystem
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


	public static class Default
	extends    AFileSystem.Abstract<BlobStorePath, BlobStorePath>
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
				((BlobStoreWritableFile)file).path()
			);
		}

	}

}
