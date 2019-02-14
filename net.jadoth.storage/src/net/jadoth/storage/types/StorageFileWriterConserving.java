package net.jadoth.storage.types;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import net.jadoth.files.XFiles;
import net.jadoth.storage.exceptions.StorageExceptionIo;

/**
 * Implementation that does not delete file but moves them into a "grave" directory instead.
 * This strategy is viable mostly for debugging purposes only, as it rather squanders the hard disc space.
 * Productive use of this strategy would require regular manual or scripted cleanup
 *
 * @author TM
 */
public final class StorageFileWriterConserving implements StorageFileWriter
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	final Path grave;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public StorageFileWriterConserving(final Path grave)
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
//		final Path source = file.file().toPath()           ;
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
//		XDebug.debugln("Creating truncation bak file: " + bakFileName);
		XFiles.ensureDirectory(dirBak);
		try
		{
			Files.copy(
				Paths.get(file.identifier()),
				dirBak.toPath().resolve(bakFileName)
			);
//			XDebug.debugln("* bak file created: " + bakFileName);
		}
		catch(final IOException e)
		{
			throw new StorageExceptionIo(e); // (04.03.2015 TM)EXCP: proper exception
		}
	}
	
	

	public static final class Provider implements StorageFileWriter.Provider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final File   parentDirectory;
		final String directoryPrefix;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Provider(final File parentDirectory, final String directoryPrefix)
		{
			super();
			this.parentDirectory = parentDirectory;
			this.directoryPrefix = directoryPrefix;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public StorageFileWriter provideWriter(final int channelIndex)
		{
			final File dir = XFiles.ensureDirectory(
				new File(this.parentDirectory, this.directoryPrefix + channelIndex)
			);
			return new StorageFileWriterConserving(dir.toPath());
		}

	}

}
