package net.jadoth.storage.types;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import net.jadoth.files.XFiles;
import net.jadoth.storage.exceptions.StorageExceptionIo;


/**
 * Function type that encapsulates handling of all writing accesses to persistent data, including copying,
 * truncation, deletion.
 *
 * @author TM
 */
public interface StorageFileWriter
{
	public static ByteBuffer determineLastNonEmpty(final ByteBuffer[] byteBuffers)
	{
		for(int i = byteBuffers.length - 1; i >= 0; i--)
		{
			if(byteBuffers[i].hasRemaining())
			{
				return byteBuffers[i];
			}
		}
		
		// either the array is empty or only contains empty buffers. Either way, no suitable buffer found.
		return null;
	}

	
	// (13.02.2019 TM)NOTE: single ByteBuffer variant removed to keep implementations simple.
	
	public default long writeStore(
		final StorageDataFile<?> targetFile ,
		final ByteBuffer[]       byteBuffers
	)
	{
		return this.write(targetFile, byteBuffers);
	}
	
	public default long writeTransactionEntryStore(
		final StorageLockedChannelFile transactionFile,
		final ByteBuffer[]             byteBuffers    ,
		final StorageDataFile<?>       dataFile       ,
		final long                     dataFileOffset ,
		final long                     storeLength
	)
	{
		return this.write(transactionFile, byteBuffers);
	}
	
	public default long writeTransactionEntryTransfer(
		final StorageLockedChannelFile transactionFile,
		final ByteBuffer[]             byteBuffers    ,
		final StorageDataFile<?>       dataFile       ,
		final long                     dataFileOffset ,
		final long                     storeLength
	)
	{
		return this.write(transactionFile, byteBuffers);
	}
	
	public default long writeTransactionEntryDelete(
		final StorageLockedChannelFile transactionFile,
		final ByteBuffer[]             byteBuffers    ,
		final StorageDataFile<?>       dataFile
	)
	{
		return this.write(transactionFile, byteBuffers);
	}
	
	public default long writeTransactionEntryCreate(
		final StorageLockedChannelFile transactionFile,
		final ByteBuffer[]             byteBuffers    ,
		final StorageDataFile<?>       dataFile
	)
	{
		return this.write(transactionFile, byteBuffers);
	}
	
	public default void registerChannelTruncation(final int channelIndex)
	{
		// (14.02.2019 TM)FIXME: JET-55: This is a considerable conflict. See rationale in DevLog.
		// no-op by default
	}
	
	
	
	public default long write(final StorageLockedFile file, final ByteBuffer[] byteBuffers)
	{
//		DEBUGStorage.println("storage write multiple buffers");

		// determine last non-empty buffer to be used as a write-completion check point
		final ByteBuffer lastNonEmpty = determineLastNonEmpty(byteBuffers);
		if(lastNonEmpty == null)
		{
			return 0L;
		}
		
		final FileChannel channel   = file.channel();
		final long        oldLength = file.length();
		try
		{
			while(lastNonEmpty.hasRemaining())
			{
				channel.write(byteBuffers);
			}
			
			// this is the right place for a data-safety-securing force/flush.
			channel.force(false);
			
			return file.length() - oldLength;
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
			targetfile.channel().force(false);
		}
		catch(final IOException e)
		{
			throw new RuntimeException(e); // (01.10.2014)EXCP: proper exception
		}
	}

	public default long copy(
		final StorageFile       sourceFile,
		final StorageLockedFile targetfile
	)
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
			// (13.02.2019 TM)FIXME: JET-55: What about flushing here?
			return sourceFile.channel().transferTo(sourceOffset, length, targetfile.channel());
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
			file.channel().truncate(newLength);
		}
		catch(final IOException e)
		{
			throw new RuntimeException(e); // (01.10.2014)EXCP: proper exception
		}
	}

	public default void delete(final StorageLockedChannelFile file)
	{
//		DEBUGStorage.println("storage file deletion");

		if(file.delete())
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
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public void delete(final StorageLockedChannelFile file)
		{
			// (13.10.2018 TM)NOTE: replacement to decouple concrete references to File.
			final Path source = Paths.get(file.identifier());
//			final Path source = file.file().toPath()           ;
			final Path target = this.grave.resolve(file.name());

			try
			{
				Files.move(source, target);
			}
			catch(final IOException e)
			{
				throw new StorageExceptionIo(e); // (04.03.2015 TM)EXCP: proper exception
			}
		}
		
		@Override
		public void truncate(final StorageLockedFile file, final long newLength)
		{
			/* (04.09.2017 TM)NOTE:
			 * truncation is the only possibility where data can be deleted.
			 * As a safety net, those files are backupped in full before truncated, now.
			 */
			this.createTruncationBak(file, newLength);
			StorageFileWriter.super.truncate(file, newLength);
		}
		
		public final void createTruncationBak(final StorageFile file, final long newLength)
		{
			final File dirBak = new File(this.grave.toFile(), "bak");
			
			final String bakFileName = file.name() + "_truncated_from_" + file.length() + "_to_" + newLength
				+ "_@" + System.currentTimeMillis() + ".bak"
			;
//			XDebug.debugln("Creating truncation bak file: " + bakFileName);
			XFiles.ensureDirectory(dirBak);
			try
			{
				Files.copy(
					Paths.get(file.identifier()),
					dirBak.toPath().resolve(bakFileName)
				);
//				XDebug.debugln("* bak file created: " + bakFileName);
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
				final File dir = XFiles.ensureDirectory(
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
		public final long write(final StorageLockedFile file, final ByteBuffer[] byteBuffers)
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
	
	
	// (04.09.2017 TM)NOTE: Gravedigger#createTruncationBak test
//	public static void main(final String[] args)
//	{
//		final Gravedigger gd = new Gravedigger(Paths.get("D:/test/grave"));
//		gd.createTruncationBak(new File("D:/test/sample.dat"), 10);
//	}

}
