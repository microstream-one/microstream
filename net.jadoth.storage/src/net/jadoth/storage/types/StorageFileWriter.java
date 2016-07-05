package net.jadoth.storage.types;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;

import net.jadoth.storage.exceptions.StorageExceptionIo;
import net.jadoth.util.file.JadothFiles;


/**
 * Function type that encapsulates handling of all writing accesses to persistent data, including copying,
 * truncation, deletion.
 *
 * @author TM
 */
public interface StorageFileWriter
{
	public default void write(final StorageLockedFile file, final ByteBuffer byteBuffer)
	{
//		DEBUGStorage.println("storage write single buffer");

		try
		{
			file.fileChannel().write(byteBuffer);
//			file.fileChannel().force(false); // (12.02.2015 TM)NOTE: replaced by explicit flush() calls on all usesites
		}
		catch(final IOException e)
		{
			throw new RuntimeException(e); // (01.10.2014)EXCP: proper exception
		}
	}

	public default void flush(final StorageLockedFile targetfile)
	{
		try
		{
			targetfile.fileChannel().force(false);
		}
		catch(final IOException e)
		{
			throw new RuntimeException(e); // (01.10.2014)EXCP: proper exception
		}
	}

	public default void write(final StorageLockedFile file, final ByteBuffer... byteBuffers)
	{
//		DEBUGStorage.println("storage write multiple buffers");

		final ByteBuffer  last    = byteBuffers[byteBuffers.length - 1];
		final FileChannel channel = file.fileChannel();

		try
		{
			while(last.hasRemaining())
			{
				channel.write(byteBuffers);
			}
//			channel.force(false); // (12.02.2015 TM)NOTE: replaced by explicit flush() calls on all usesites
		}
		catch(final IOException e)
		{
			throw new RuntimeException(e); // (01.10.2014)EXCP: proper exception
		}
	}

	public default long copy(final StorageFile sourceFile, final StorageLockedFile targetfile)
	{
		return this.copy(sourceFile, targetfile, 0, sourceFile.length());
	}

	public default long copy(
		final StorageFile       sourceFile  ,
		final StorageLockedFile targetfile  ,
		final long              sourceOffset,
		final long              length
	)
	{
//		DEBUGStorage.println("storage copy file range");

		try
		{
			return sourceFile.fileChannel().transferTo(sourceOffset, length, targetfile.fileChannel());
		}
		catch(final IOException e)
		{
			throw new RuntimeException(e); // (01.10.2014)EXCP: proper exception
		}
	}

	public default void truncate(final StorageLockedFile file, final long newLength)
	{
//		DEBUGStorage.println("storage file truncation");

		try
		{
			file.fileChannel().truncate(newLength);
		}
		catch(final IOException e)
		{
			throw new RuntimeException(e); // (01.10.2014)EXCP: proper exception
		}
	}

	public default void delete(final StorageLockedChannelFile file)
	{
//		DEBUGStorage.println("storage file deletion");

		if(file.file().delete())
		{
			return;
		}
		throw new RuntimeException("Could not delete file " + file); // (02.10.2014 TM)EXCP: proper exception
	}

	public final class Implementation implements StorageFileWriter
	{
		// since default methods, interfaces should be directly instantiable :(
	}

	/**
	 * Implementation that does not delete file but moves them into a "grave" directory instead.
	 * This strategy is viable mostly for debugging purposes only, as it rather squanders the hard disc space.
	 * Productive use of this strategy would require regular manual or scripted cleanup
	 *
	 * @author TM
	 */
	public final class Gravedigger implements StorageFileWriter
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final Path grave;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Gravedigger(final Path grave)
		{
			super();
			this.grave = grave;
		}

		@Override
		public void delete(final StorageLockedChannelFile file)
		{
			final Path source = file.file().toPath()                     ;
			final Path target = this.grave.resolve(file.file().getName());

			try
			{
				Files.move(source, target);
			}
			catch(final IOException e)
			{
				throw new StorageExceptionIo(e); // (04.03.2015 TM)EXCP: proper exception
			}
		}

		public static final class Provider implements StorageFileWriter.Provider
		{
			final File   parentDirectory;
			final String directoryPrefix;

			public Provider(final File parentDirectory, final String directoryPrefix)
			{
				super();
				this.parentDirectory = parentDirectory;
				this.directoryPrefix = directoryPrefix;
			}

			@Override
			public StorageFileWriter provideWriter(final int channelIndex)
			{
				final File dir = JadothFiles.ensureDirectory(
					new File(this.parentDirectory, this.directoryPrefix + channelIndex)
				);
				return new Gravedigger(dir.toPath());
			}

		}

	}


	/**
	 * Trivial (naive) ready only implementation of a {@link StorageFileWriter}.
	 *
	 * @author TM
	 */
	public final class ReadOnlyImplementation implements StorageFileWriter
	{

		@Override
		public final void write(final StorageLockedFile file, final ByteBuffer byteBuffer)
		{
			throw new UnsupportedOperationException(); // naive exception
		}

		@Override
		public final void write(final StorageLockedFile file, final ByteBuffer... byteBuffers)
		{
			throw new UnsupportedOperationException(); // naive exception
		}

		@Override
		public final long copy(
			final StorageFile       sourceFile  ,
			final StorageLockedFile targetfile  ,
			final long              sourceOffset,
			final long              length
		)
		{
			throw new UnsupportedOperationException(); // naive exception
		}

//		@Override
//		public void copyFile(final StorageFile sourceFile, final File targetFile)
//		{
//			throw new UnsupportedOperationException(); // naive exception
//		}

		@Override
		public final void truncate(final StorageLockedFile file, final long newLength)
		{
			throw new UnsupportedOperationException(); // naive exception
		}

		@Override
		public final void delete(final StorageLockedChannelFile file)
		{
			throw new UnsupportedOperationException(); // naive exception
		}

	}



	@FunctionalInterface
	public interface Provider
	{
		public StorageFileWriter provideWriter(final int channelIndex);

		public final class Implementation implements StorageFileWriter.Provider
		{
			@Override
			public StorageFileWriter provideWriter(final int channelIndex)
			{
				return new StorageFileWriter.Implementation();
			}
		}

	}

}
