package one.microstream.afs.googlecloud.storage;

import static one.microstream.X.notNull;

import com.google.cloud.storage.Storage;

import one.microstream.afs.ADirectory;
import one.microstream.afs.AFile;
import one.microstream.afs.AFileSystem;
import one.microstream.afs.AItem;
import one.microstream.afs.AReadableFile;
import one.microstream.afs.AWritableFile;
import one.microstream.chars.VarString;
import one.microstream.chars.XChars;
import one.microstream.io.XIO;

public interface GcStorageFileSystem extends AFileSystem
{
	public static GcStoragePath toPath(
		final AItem item
	)
	{
		return toPath(
			item.toPath()
		);
	}

	public static GcStoragePath toPath(
		final String... pathElements
	)
	{
		return GcStoragePath.New(
			notNull(pathElements)
		);
	}


	public static GcStorageFileSystem New(
		final String  defaultProtocol,
		final Storage storage
	)
	{
		return New(
			defaultProtocol,
			GcStorageConnector.New(storage)
		);
	}


	public static GcStorageFileSystem New(
		final String             defaultProtocol,
		final GcStorageConnector connector
	)
	{
		return New(
			defaultProtocol,
			GcStorageIoHandler.New(connector)
		);
	}

	public static GcStorageFileSystem New(
		final String             defaultProtocol,
		final GcStorageIoHandler ioHandler
	)
	{
		return new GcStorageFileSystem.Default(
			notNull(defaultProtocol),
			notNull(ioHandler)
		);
	}


	public static class Default extends AFileSystem.Abstract<GcStoragePath, GcStoragePath> implements GcStorageFileSystem
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Default(
		final String             defaultProtocol,
		final GcStorageIoHandler ioHandler
		)
		{
			super(
				defaultProtocol,
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
			final GcStoragePath directory
		)
		{
			return directory.pathElements();
		}

		@Override
		public String[] resolveFileToPath(
			final GcStoragePath file
		)
		{
			return file.pathElements();
		}

		@Override
		public GcStoragePath resolve(
			final ADirectory directory
		)
		{
			return GcStorageFileSystem.toPath(directory);
		}

		@Override
		public GcStoragePath resolve(
			final AFile file
		)
		{
			return GcStorageFileSystem.toPath(file);
		}

		@Override
		protected VarString assembleItemPath(
			final AItem     item,
			final VarString vs
		)
		{
			return XChars.assembleSeparated(
				vs,
				GcStoragePath.SEPARATOR_CAHR,
				item.toPath()
			);
		}

		@Override
		public AReadableFile wrapForReading(
			final AFile  file,
			final Object user
		)
		{
			final GcStoragePath path = this.resolve(file);
			return GcStorageReadableFile.New(file, user, path);
		}

		@Override
		public AWritableFile wrapForWriting(
			final AFile  file,
			final Object user
		)
		{
			final GcStoragePath path = this.resolve(file);
			return GcStorageWritableFile.New(file, user, path);
		}

		@Override
		public AReadableFile convertToReading(
			final AWritableFile file
		)
		{
			return GcStorageReadableFile.New(
				file                          ,
				file.user()                   ,
				((GcStorageWritableFile)file).path()
			);
		}

		@Override
		public AWritableFile convertToWriting(
			final AReadableFile file
		)
		{
			return GcStorageWritableFile.New(
				file                          ,
				file.user()                   ,
				((GcStorageWritableFile)file).path()
			);
		}

	}

}
