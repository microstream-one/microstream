package one.microstream.afs.azure.storage;

import static one.microstream.X.notNull;

import com.azure.storage.blob.BlobServiceClient;

import one.microstream.afs.ADirectory;
import one.microstream.afs.AFile;
import one.microstream.afs.AFileSystem;
import one.microstream.afs.AItem;
import one.microstream.afs.AReadableFile;
import one.microstream.afs.AWritableFile;
import one.microstream.chars.VarString;
import one.microstream.chars.XChars;
import one.microstream.io.XIO;

public interface AzureStorageFileSystem extends AFileSystem
{
	public static AzureStoragePath toPath(
		final AItem item
	)
	{
		return toPath(
			item.toPath()
		);
	}

	public static AzureStoragePath toPath(
		final String... pathElements
	)
	{
		return AzureStoragePath.New(
			notNull(pathElements)
		);
	}


	public static AzureStorageFileSystem New(
		final String            defaultProtocol,
		final BlobServiceClient serviceClient
	)
	{
		return New(
			defaultProtocol,
			AzureStorageConnector.New(serviceClient)
		);
	}


	public static AzureStorageFileSystem New(
		final String                defaultProtocol,
		final AzureStorageConnector connector
	)
	{
		return New(
			defaultProtocol,
			AzureStorageIoHandler.New(connector)
		);
	}

	public static AzureStorageFileSystem New(
		final String                defaultProtocol,
		final AzureStorageIoHandler ioHandler
	)
	{
		return new AzureStorageFileSystem.Default(
			notNull(defaultProtocol),
			notNull(ioHandler)
		);
	}


	public static class Default
	extends AFileSystem.Abstract<AzureStoragePath, AzureStoragePath>
	implements AzureStorageFileSystem
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Default(
		final String                defaultProtocol,
		final AzureStorageIoHandler ioHandler
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
			final AzureStoragePath directory
		)
		{
			return directory.pathElements();
		}

		@Override
		public String[] resolveFileToPath(
			final AzureStoragePath file
		)
		{
			return file.pathElements();
		}

		@Override
		public AzureStoragePath resolve(
			final ADirectory directory
		)
		{
			return AzureStorageFileSystem.toPath(directory);
		}

		@Override
		public AzureStoragePath resolve(
			final AFile file
		)
		{
			return AzureStorageFileSystem.toPath(file);
		}

		@Override
		protected VarString assembleItemPath(
			final AItem     item,
			final VarString vs
		)
		{
			return XChars.assembleSeparated(
				vs,
				AzureStoragePath.SEPARATOR_CAHR,
				item.toPath()
			);
		}

		@Override
		public AReadableFile wrapForReading(
			final AFile  file,
			final Object user
		)
		{
			final AzureStoragePath path = this.resolve(file);
			return AzureStorageReadableFile.New(file, user, path);
		}

		@Override
		public AWritableFile wrapForWriting(
			final AFile  file,
			final Object user
		)
		{
			final AzureStoragePath path = this.resolve(file);
			return AzureStorageWritableFile.New(file, user, path);
		}

		@Override
		public AReadableFile convertToReading(
			final AWritableFile file
		)
		{
			return AzureStorageReadableFile.New(
				file,
				file.user(),
				((AzureStorageWritableFile)file).path()
			);
		}

		@Override
		public AWritableFile convertToWriting(
			final AReadableFile file
		)
		{
			return AzureStorageWritableFile.New(
				file,
				file.user(),
				((AzureStorageWritableFile)file).path()
			);
		}

	}

}
