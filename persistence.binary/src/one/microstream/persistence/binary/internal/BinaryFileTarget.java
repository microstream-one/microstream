package one.microstream.persistence.binary.internal;

import static one.microstream.X.notNull;

import one.microstream.X;
import one.microstream.afs.AFS;
import one.microstream.afs.AFile;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.exceptions.PersistenceException;
import one.microstream.persistence.exceptions.PersistenceExceptionTransfer;
import one.microstream.persistence.types.PersistenceTarget;

public class BinaryFileTarget implements PersistenceTarget<Binary>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final AFile file;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryFileTarget(final AFile file)
	{
		super();
		this.file = notNull(file);
	}
	
	

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public void write(final Binary chunk) throws PersistenceExceptionTransfer
	{
		try
		{
			AFS.applyWriting(this.file, wf -> wf.writeBytes(X.ArrayView(chunk.buffers())));
		}
		catch(final Exception e)
		{
			// (01.10.2014 TM)EXCP: proper exception
			throw new PersistenceException(e);
		}
	}

}

