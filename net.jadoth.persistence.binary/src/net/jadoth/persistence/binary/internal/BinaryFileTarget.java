package net.jadoth.persistence.binary.internal;

import static net.jadoth.X.notNull;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;

import net.jadoth.file.FileException;
import net.jadoth.file.JadothFiles;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.exceptions.PersistenceExceptionTransfer;
import net.jadoth.persistence.types.PersistenceTarget;

public class BinaryFileTarget implements PersistenceTarget<Binary>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final File file;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryFileTarget(final File file)
	{
		super();
		this.file = notNull(file);
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	protected FileChannel createChannel(final File file) throws FileException, IOException
	{
		return JadothFiles.createWritingFileChannel(file);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public void write(final Binary[] chunks) throws PersistenceExceptionTransfer
	{
		try(final FileChannel fch = this.createChannel(this.file))
		{
			for(final Binary chunk : chunks)
			{
				fch.write(chunk.buffers());
			}
		}
		catch(final IOException e)
		{
			throw new PersistenceExceptionTransfer(e);
		}
	}

}

