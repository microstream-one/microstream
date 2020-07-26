package one.microstream.persistence.test;

import java.io.PrintStream;

import one.microstream.afs.AFile;
import one.microstream.persistence.binary.internal.BinaryFileTarget;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.exceptions.PersistenceExceptionTransfer;

public class DEBUG_BinaryFileTarget extends BinaryFileTarget
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	final PrintStream out;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public DEBUG_BinaryFileTarget(final PrintStream out, final AFile file)
	{
		super(file);
		this.out = out;
	}

	@Override
	public void write(final Binary chunks) throws PersistenceExceptionTransfer
	{
//		final VarString vc = VarString.LargeVarString();
//		final BinaryChunkPrinter printer = new BinaryChunkPrinter(vc);
//		for(int i = 0; i < chunks.length; i++)
//		{
//			if(chunks[i] == null)
//			{
//				continue;
//			}
//			printer.appendByteBuffer(chunks[i].buffers(), i + 1);
//		}
//		this.out.println("writing:"+vc);
		super.write(chunks);
	}

//	@Override
//	protected FileChannel createChannel(final File file) throws FileException, IOException
//	{
//		return new DebugHexPrinter(this.out, super.createChannel(file));
//	}

}
