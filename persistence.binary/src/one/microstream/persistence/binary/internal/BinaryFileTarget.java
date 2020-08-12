package one.microstream.persistence.binary.internal;

import static one.microstream.X.notNull;

import java.nio.ByteBuffer;

import one.microstream.X;
import one.microstream.afs.AFS;
import one.microstream.afs.AFile;
import one.microstream.afs.AFileSystem;
import one.microstream.collections.ArrayView;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.exceptions.PersistenceException;
import one.microstream.persistence.exceptions.PersistenceExceptionTransfer;
import one.microstream.persistence.types.PersistenceTarget;

public class BinaryFileTarget implements PersistenceTarget<Binary>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final AFile       file;
	private final AFileSystem fs  ;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryFileTarget(final AFile file)
	{
		super();
		this.file = notNull(file);
		this.fs   = file.fileSystem();
	}
	
	

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public void write(final Binary chunk) throws PersistenceExceptionTransfer
	{
		try
		{
			this.validateIsWritable();
			
			final ArrayView<ByteBuffer> buffers = X.ArrayView(chunk.buffers());
			AFS.applyWriting(this.file, wf ->
				wf.writeBytes(buffers)
			);
		}
		catch(final Exception e)
		{
			// (01.10.2014 TM)EXCP: proper exception
			throw new PersistenceException(e);
		}
	}
	
	@Override
	public final void validateIsWritable()
	{
		this.fs.validateIsWritable();
	}
	
	@Override
	public final boolean isWritable()
	{
		return this.fs.isWritable();
	}
	
}

