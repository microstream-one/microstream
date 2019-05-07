package one.microstream.persistence.test;

import java.util.function.Consumer;

import one.microstream.X;
import one.microstream.chars.VarString;
import one.microstream.memory.XMemory;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.BinaryEntityDataReader;


public final class BinaryChunkPrinter implements Consumer<Binary>, BinaryEntityDataReader
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////
	
	private static final int MAX_LITERAL_LENGTH_LONG = 20;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

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
	// instance fields //
	////////////////////

	private final VarString vc;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryChunkPrinter(final VarString vc)
	{
		super();
		this.vc = vc;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final void accept(final Binary e)
	{
		this.appendBinary(e);
	}

	public final VarString appendBinary(final Binary bytes)
	{
		this.printHeader();
		this.printEntities(bytes);
		this.printFooter();
		
		return this.vc;
	}

	private void printHeader()
	{
		this.vc.lf()
		.add("       Offset        |      Length         |      Type-Id        |      Object-Id      | Content" ).lf()
		;
		this.printSeperator();
	}
	
	private void printEntities(final Binary bytes)
	{
		bytes.iterateEntityData(this);
	}
	
	@Override
	public final void readBinaryEntityData(final long entityAddress)
	{
		this.printEntity(entityAddress);
	}
	
	private void printEntity(final long entityAddress)
	{
		final long totalLength = Binary.getEntityLengthRawValue(entityAddress);
		final long typeId      = Binary.getEntityTypeIdRawValue(entityAddress);
		final long objectId    = Binary.getEntityObjectIdRawValue(entityAddress);
		final long dataLength  = Binary.entityContentLength(totalLength);

		final byte[] content = new byte[X.checkArrayRange(dataLength)];
		XMemory.copyRangeToArray(Binary.entityContentAddress(entityAddress), content);
		printEntity(this.vc, entityAddress, totalLength, typeId, objectId, content).lf();
	}

	private void printFooter()
	{
		this.printSeperator();
	}

	private void printSeperator()
	{
		this.vc.add("-------------------------------------------------------------------------------------------").lf();
	}

}
