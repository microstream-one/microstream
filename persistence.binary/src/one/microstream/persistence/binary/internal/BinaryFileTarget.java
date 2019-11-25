package one.microstream.persistence.binary.internal;

import static one.microstream.X.notNull;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;

import one.microstream.io.FileException;
import one.microstream.io.XIO;
import one.microstream.io.XPaths;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.exceptions.PersistenceExceptionTransfer;
import one.microstream.persistence.types.PersistenceTarget;

public class BinaryFileTarget implements PersistenceTarget<Binary>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Path file;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryFileTarget(final Path file)
	{
		super();
		this.file = notNull(file);
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	protected FileChannel createChannel(final Path file) throws FileException, IOException
	{
		return XPaths.openFileChannelWriting(file);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public void write(final Binary chunk) throws PersistenceExceptionTransfer
	{
		try(final FileChannel fileChannel = this.createChannel(this.file))
		{
			XIO.appendAllGuaranteed(fileChannel, chunk.buffers());
		}
		catch(final IOException e)
		{
			// (01.10.2014)EXCP: proper exception
			throw new RuntimeException(e);
		}
		
		// (12.07.2019 TM)NOTE: naive old code from 2012 or so. Replaced by centralized newer and reliable code above.
//		try(final FileChannel fch = this.createChannel(this.file))
//		{
//			fch.write(chunk.buffers());
//		}
//		catch(final IOException e)
//		{
//			throw new PersistenceExceptionTransfer(e);
//		}
	}

}

