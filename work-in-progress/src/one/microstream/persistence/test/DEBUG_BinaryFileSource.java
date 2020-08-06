package one.microstream.persistence.test;

import static one.microstream.X.notNull;

import java.io.PrintStream;

import one.microstream.afs.AFile;
import one.microstream.chars.VarString;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.persistence.binary.internal.BinaryFileSource;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.exceptions.PersistenceExceptionTransfer;
import one.microstream.persistence.types.PersistenceIdSet;
import one.microstream.persistence.types.PersistenceSource;

public class DEBUG_BinaryFileSource implements PersistenceSource<Binary>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final PrintStream out;
	private final PersistenceSource<Binary> relayTarget;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public DEBUG_BinaryFileSource(final PrintStream out, final AFile file)
	{
		super();
		this.out         = notNull(out);
		this.relayTarget = BinaryFileSource.New(file, false);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public XGettingCollection<? extends Binary> read() throws PersistenceExceptionTransfer
	{
		final XGettingCollection<? extends Binary> chunks = this.relayTarget.read();
		final VarString vc = VarString.New();
		chunks.iterate(new BinaryChunkPrinter(vc));
		this.out.println("read:"+vc);
		return chunks;
	}

	@Override
	public XGettingCollection<? extends Binary> readByObjectIds(final PersistenceIdSet[] oids) throws PersistenceExceptionTransfer
	{
		// simple input stream reading implementation can't do complex queries, so just read "everything" provided
		return this.read();
	}

//	@Override
//	public XGettingCollection<? extends Binary> readByTypeId(final long typeId) throws PersistenceExceptionTransfer
//	{
//		// simple input stream reading implementation can't do complex queries, so just read "everything" provided
//		return this.read();
//	}

}
