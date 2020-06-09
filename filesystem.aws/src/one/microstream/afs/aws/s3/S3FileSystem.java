package one.microstream.afs.aws.s3;

import static one.microstream.X.notNull;

import com.amazonaws.services.s3.AmazonS3;

import one.microstream.afs.ADirectory;
import one.microstream.afs.AFile;
import one.microstream.afs.AFileSystem;
import one.microstream.afs.AItem;
import one.microstream.afs.AReadableFile;
import one.microstream.afs.AWritableFile;
import one.microstream.chars.VarString;
import one.microstream.chars.XChars;
import one.microstream.io.XIO;

public interface S3FileSystem extends AFileSystem
{
	public static S3Path toPath(
		final AItem item
	)
	{
		return toPath(
			item.toPath()
		);
	}

	public static S3Path toPath(
		final String... pathElements
	)
	{
		return S3Path.New(
			notNull(pathElements)
		);
	}


	public static S3FileSystem New(
		final String   defaultProtocol,
		final AmazonS3 s3
	)
	{
		return New(
			defaultProtocol    ,
			S3Connector.New(s3)
		);
	}


	public static S3FileSystem New(
		final String      defaultProtocol,
		final S3Connector connector
	)
	{
		return New(
			defaultProtocol               ,
			S3IoHandler.New(connector)
		);
	}

	public static S3FileSystem New(
		final String      defaultProtocol,
		final S3IoHandler ioHandler
	)
	{
		return new S3FileSystem.Default(
			notNull(defaultProtocol),
			notNull(ioHandler)
		);
	}


	public static class Default extends AFileSystem.Abstract<S3Path, S3Path> implements S3FileSystem
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Default(
		final String      defaultProtocol,
		final S3IoHandler ioHandler
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
			final S3Path directory
		)
		{
			return directory.pathElements();
		}

		@Override
		public String[] resolveFileToPath(
			final S3Path file
		)
		{
			return file.pathElements();
		}

		@Override
		public S3Path resolve(
			final ADirectory directory
		)
		{
			return S3FileSystem.toPath(directory);
		}

		@Override
		public S3Path resolve(
			final AFile file
		)
		{
			return S3FileSystem.toPath(file);
		}

		@Override
		protected VarString assembleItemPath(
			final AItem     item,
			final VarString vs
		)
		{
			return XChars.assembleSeparated(
				vs,
				S3Path.SEPARATOR_CAHR,
				item.toPath()
			);
		}

		@Override
		public AReadableFile wrapForReading(
			final AFile  file,
			final Object user
		)
		{
			final S3Path path = this.resolve(file);
			return S3ReadableFile.New(file, user, path);
		}

		@Override
		public AWritableFile wrapForWriting(
			final AFile  file,
			final Object user
		)
		{
			final S3Path path = this.resolve(file);
			return S3WritableFile.New(file, user, path);
		}

		@Override
		public AReadableFile convertToReading(
			final AWritableFile file
		)
		{
			return S3ReadableFile.New(
				file                          ,
				file.user()                   ,
				((S3WritableFile)file).path()
			);
		}

		@Override
		public AWritableFile convertToWriting(
			final AReadableFile file
		)
		{
			return S3WritableFile.New(
				file                          ,
				file.user()                   ,
				((S3WritableFile)file).path()
			);
		}

	}

}
