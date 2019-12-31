package one.microstream.persistence.binary.internal;

import static one.microstream.X.notNull;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;

import one.microstream.io.FileException;
import one.microstream.io.XIO;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.exceptions.PersistenceException;
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
		return XIO.openFileChannelWriting(file);
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
			// (01.10.2014 TM)EXCP: proper exception
			throw new PersistenceException(e);
		}
	}

}

