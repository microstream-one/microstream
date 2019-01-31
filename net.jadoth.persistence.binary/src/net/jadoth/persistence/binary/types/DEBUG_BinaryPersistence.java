package net.jadoth.persistence.binary.types;

import static net.jadoth.chars.VarString.New;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

import net.jadoth.X;
import net.jadoth.chars.VarString;
import net.jadoth.memory.XMemory;
import net.jadoth.persistence.types.PersistenceTypeLink;

// CHECKSTYLE.OFF: MagicNumber: just a deprecated debugging class

@Deprecated
public final class DEBUG_BinaryPersistence
{
	public static final String oidToString(final long oid)
	{
		final byte[] bytes = new byte[8];
		XMemory.put_long(bytes, 0, oid);
		return "OID = " + oid + " (" + New().addHexDec(bytes) + ")";
	}

	public static final String tidToString(final long tid)
	{
		final byte[] bytes = new byte[8];
		XMemory.put_long(bytes, 0, tid);
		return "TID = " + tid + " (" + New().addHexDec(bytes) + ")";
	}


	public static final long[] getEntityHeaderLongsFromDataAddress(final Binary bytes)
	{
		if(bytes.loadItemEntityContentAddress() == 0)
		{
			throw new NullPointerException();
		}
		return new long[]
		{
			bytes.getBuildItemContentLength(),
			bytes.getBuildItemTypeId(),
			bytes.getBuildItemObjectId()
		};
	}

	public static final String getEntityHeaderFromDataAddress(final Binary bytes)
	{
		final long[] header = getEntityHeaderLongsFromDataAddress(bytes);
		return "Entity @" + bytes.loadItemEntityContentAddress()
			+ "\nLEN=" + header[0] + " (" + Long.toHexString(header[0]).toUpperCase() + ")"
			+ "\nTID=" + header[1] + " (" + Long.toHexString(header[1]).toUpperCase() + ")"
			+ "\nOID=" + header[2] + " (" + Long.toHexString(header[2]).toUpperCase() + ")"
		;
	}

	public static final String binaryToString(final Binary bytes)
	{
		final VarString vs = VarString.New(4096);
		
		bytes.iterateEntityData(entityAddress ->
		{
			final byte[] array = new byte[X.checkArrayRange(XMemory.get_long(entityAddress))];
			XMemory.copyRangeToArray(entityAddress, array);
			final String s = format8ByteWise(0,
				VarString.New().addHexDec(array).toString()
			);
			vs.add(s).lf();
		});
		
		return vs.toString();
	}

	public static final String chunkToString(final ByteBuffer buffer)
	{
		final byte[] bytes = new byte[buffer.limit()];
		buffer.flip();
		buffer.get(bytes);

		return format8ByteWise(0,
			New()
			.addHexDec(bytes)
			.toString()
		);
	}

	public static final void iterateByteBuffers(final ByteBuffer[] byteBuffers, final Consumer<byte[]> iterator)
	{
		for(final ByteBuffer buffer : byteBuffers)
		{
			if(buffer == null || buffer.position() == 0)
			{
				continue; // no data yet, only a reserved entry in the internal array
			}

			// defensive copy for debug purposes is very reasonable. Performance downside is irrelevant.
			final byte[] bytes = new byte[buffer.limit()];
			buffer.flip();
			buffer.get(bytes);

			// pass only a copy of the data, neither an actual bytebuffer nor the actual memory address
			iterator.accept(bytes);
		}
	}

	public static final String format8ByteWise(final int headerLength, final String binaryRepresentation)
	{
		final int length = binaryRepresentation.length();
		final VarString vc = VarString.New(length * 2);
		if(headerLength > 0)
		{
			vc.add(binaryRepresentation.substring(0, headerLength)).lf();
		}
		for(int i = headerLength; i < length; i += 16)
		{
			vc.add(binaryRepresentation.substring(i, Math.min(i + 16, length))).lf();
		}
		return vc.toString();
	}

	public static final String typeToString(final PersistenceTypeLink typeLink)
	{
		return "Type = " + typeLink.type() + " " + tidToString(typeLink.typeId());
	}

	public static final String offsetToString(final long offset)
	{
		return "Offset = " + (offset - 16L) + " (without base offset)";
	}


	private DEBUG_BinaryPersistence()
	{
		// static only
		throw new UnsupportedOperationException();
	}

}

// CHECKSTYLE.ON: MagicNumbers
