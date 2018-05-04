package net.jadoth.persistence.binary.types;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

import net.jadoth.X;
import net.jadoth.chars.VarString;
import net.jadoth.memory.Memory;


public final class BinaryChunkPrinter implements Consumer<Binary>
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////
	
	private static final int MAX_LITERAL_LENGTH_LONG = 20;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// static methods   //
	/////////////////////

	private static void padLong(final VarString vc, final long value)
	{
		vc.padLeft(Long.toString(value), MAX_LITERAL_LENGTH_LONG, ' ');
	}

	private static VarString printEntity(
		final VarString vc,
		final long address,
		final long length,
		final long typeId,
		final long objtId,
		final byte[] content
	)
	{
//		vc.add("@");
		padLong(vc, address);
		vc.add(" |");
		padLong(vc, length);
		vc.add(" |");
		padLong(vc, typeId);
		vc.add(" |");
		padLong(vc, objtId);
		vc.add(" | ");
		vc.addHexDec(content);
		return vc;
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	private final VarString vc;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryChunkPrinter(final VarString vc)
	{
		super();
		this.vc = vc;
	}

	private void printChunk(final long startAddress, final long bound)
	{
		final long baseOffset = startAddress - 8;
		for(long address = startAddress; address < bound;)
		{
			address += this.appendEntity(baseOffset, address);
		}
	}

	private long appendEntity(final long baseOffset, final long address)
	{
		final long totalLength = BinaryPersistence.getEntityLength(address);
		final long typeId      = BinaryPersistence.getEntityTypeId(address);
		final long objectId    = BinaryPersistence.getEntityObjectId(address);
		final long dataLength  = BinaryPersistence.entityDataLength(totalLength);

		final byte[] content = new byte[X.checkArrayRange(dataLength)];
		Memory.copyRangeToArray(BinaryPersistence.entityDataAddress(address), content);
		printEntity(this.vc, address - baseOffset, totalLength, typeId, objectId, content).lf();
		return totalLength;
	}



	private void printChunkHeader(final int chunkNumber, final long startOffset, final long chunkLength)
	{
		// length of the length in bytes (8)
		final long ll = BinaryPersistence.lengthLength();
		
		this.vc.lf().add("Chunk #" + chunkNumber + " (length = 8 + " + chunkLength + ") @ " + (startOffset - ll)).lf()
		.add("       Offset        |      Length         |      Type-Id        |      Object-Id      | Content" ).lf()
		;
		this.printSeperator();
	}

	private void printChunkFooter()
	{
		this.printSeperator();
	}

	private void printSeperator()
	{
		this.vc.add("-------------------------------------------------------------------------------------------").lf();
	}

	public final VarString appendByteBuffer(final ByteBuffer[] byteBuffer, final int number)
	{
		for(final ByteBuffer buffer : byteBuffer)
		{
			this.appendByteBuffer(buffer, number);
		}
		return this.vc;
	}

	private void appendByteBuffer(final ByteBuffer byteBuffer, final int number)
	{
		final long baseAddress = Memory.directByteBufferAddress(byteBuffer);
//		this.appendChunk(number, BinaryPersistence.chunkDataAddress(baseAddress), baseAddress + byteBuffer.limit());
		this.appendChunk(number, baseAddress, baseAddress + byteBuffer.limit());

	}

	public final VarString appendBinary(final Binary bytes)
	{
		// use sneaky debugging hook methods
		final long[] startOffsets = bytes.internalGetStartOffsets();
		final long[] boundOffsets = bytes.internalGetBoundOffsets();

		for(int i = 0; i < startOffsets.length; i++)
		{
			this.appendChunk(i + 1, startOffsets[i], boundOffsets[i]);
		}
		return this.vc;
	}

	private void appendChunk(final int number, final long startOffset, final long boundOffset)
	{
		this.printChunkHeader(number, startOffset, boundOffset - startOffset);
		this.printChunk(startOffset, boundOffset);
		this.printChunkFooter();
	}

	@Override
	public final void accept(final Binary e)
	{
		this.appendBinary(e);
	}


}
