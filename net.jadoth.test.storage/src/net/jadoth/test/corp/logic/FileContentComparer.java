package net.jadoth.test.corp.logic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import net.jadoth.X;
import net.jadoth.collections.BulkList;
import net.jadoth.collections.EqHashTable;
import net.jadoth.memory.XMemory;
import net.jadoth.typing.KeyValue;

public class FileContentComparer
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	private static ByteBuffer readFile(final File file, final ByteBuffer buffer) throws IOException
	{
		try(RandomAccessFile raf = new RandomAccessFile(file, "r"))
		{
			final FileChannel sourceChannel = raf.getChannel();
			final long        length        = sourceChannel.size();
			
			final ByteBuffer effectiveBuffer = ensureByteBuffer(length, buffer);
			
			while(buffer.hasRemaining())
			{
				sourceChannel.read(effectiveBuffer);
			}
			
			return effectiveBuffer;
		}
	}
	
	private static ByteBuffer ensureByteBuffer(
		final long       requiredLength,
		final ByteBuffer buffer
	)
	{
		if(buffer == null || buffer.capacity() < requiredLength)
		{
			XMemory.deallocateDirectByteBuffer(buffer);
			final ByteBuffer newBuffer = ByteBuffer.allocateDirect(X.checkArrayRange(requiredLength));
			return newBuffer;
		}
		
		buffer.clear();
		return buffer;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private ByteBuffer sourceBuffer;
	private ByteBuffer targetBuffer;
	
	private long sourceBufferAddress;
	private long targetBufferAddress;
	
	private BulkList<Mismatch> mismatches;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	private void setSourceBuffer(final ByteBuffer sourceBuffer)
	{
		this.sourceBuffer = sourceBuffer;
		this.sourceBufferAddress = XMemory.getDirectByteBufferAddress(sourceBuffer);
	}
	private void setTargetBuffer(final ByteBuffer targetBuffer)
	{
		this.targetBuffer = targetBuffer;
		this.targetBufferAddress = XMemory.getDirectByteBufferAddress(targetBuffer);
	}
	
	public void compareFiles(final EqHashTable<File, File> files) throws FileNotFoundException
	{
		for(final KeyValue<File, File> kv : files)
		{
			this.compareFiles(kv.key(), kv.value());
		}
	}
	
	public void compareFiles(final File sourceFile, final File targetFile) throws FileNotFoundException
	{
		try
		{
			this.setSourceBuffer(readFile(sourceFile, this.sourceBuffer));
			this.setTargetBuffer(readFile(sourceFile, this.targetBuffer));
			this.compareBufferedContents();
		}
		catch(final Exception e)
		{
			this.mismatches.add(new Mismatch(sourceFile, -1, targetFile, -1, -1, e));
		}
	}
	
	private void compareBufferedContents()
	{
		// (04.03.2019 TM)FIXME: JET-55: compareBufferedContents
	}
	
	
	static final class Mismatch
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final File      sourceFile   ;
		final long      sourceLength ;
		final File      targetFile   ;
		final long      targetLength ;
		final long      mismatchIndex;
		final Exception exception    ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Mismatch(
			final File      sourceFile   ,
			final long      sourceLength ,
			final File      targetFile   ,
			final long      targetLength ,
			final long      mismatchIndex,
			final Exception exception
		)
		{
			super();
			this.sourceFile    = sourceFile   ;
			this.sourceLength  = sourceLength ;
			this.targetFile    = targetFile   ;
			this.targetLength  = targetLength ;
			this.mismatchIndex = mismatchIndex;
			this.exception     = exception    ;
		}
		
	}
	
}
