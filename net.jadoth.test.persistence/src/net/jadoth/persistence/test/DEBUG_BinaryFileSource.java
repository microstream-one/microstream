package net.jadoth.persistence.test;

import static net.jadoth.X.notNull;

import java.io.File;
import java.io.PrintStream;

import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.persistence.binary.internal.BinaryFileSource;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryChunkPrinter;
import net.jadoth.persistence.exceptions.PersistenceExceptionTransfer;
import net.jadoth.persistence.types.PersistenceSource;
import net.jadoth.swizzling.types.SwizzleIdSet;
import net.jadoth.util.chars.VarString;

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
	public XGettingCollection<? extends Binary> readInitial() throws PersistenceExceptionTransfer
	{
		final XGettingCollection<? extends Binary> chunks = this.relayTarget.readInitial();
		final VarString vc = VarString.New();
		chunks.iterate(new BinaryChunkPrinter(vc));
		this.out.println("read:"+vc);
		return chunks;
	}

	@Override
	public XGettingCollection<? extends Binary> readByObjectIds(final SwizzleIdSet[] oids) throws PersistenceExceptionTransfer
	{
		// simple input stream reading implementation can't do complex queries, so just read "everything" provided
		return this.readInitial();
	}

//	@Override
//	public XGettingCollection<? extends Binary> readByTypeId(final long typeId) throws PersistenceExceptionTransfer
//	{
//		// simple input stream reading implementation can't do complex queries, so just read "everything" provided
//		return this.read();
//	}

}
