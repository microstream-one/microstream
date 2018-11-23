package net.jadoth.persistence.test;

import static net.jadoth.X.notNull;

import java.io.File;
import java.io.PrintStream;

import net.jadoth.chars.VarString;
import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.persistence.binary.internal.BinaryFileSource;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryChunkPrinter;
import net.jadoth.persistence.exceptions.PersistenceExceptionTransfer;
import net.jadoth.persistence.types.PersistenceIdSet;
import net.jadoth.persistence.types.PersistenceSource;

public class DEBUG_BinaryFileSource implements PersistenceSource<Binary>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	private final PrintStream out;
	private final PersistenceSource<Binary> relayTarget;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public DEBUG_BinaryFileSource(final PrintStream out, final File file)
	{
		super();
		this.out         = notNull(out);
		this.relayTarget = new BinaryFileSource(file);
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
