package one.microstream.storage.restadapter.types;

import one.microstream.persistence.binary.types.Binary;

public class ValueReaderReference implements ValueReader
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public ValueReaderReference()
	{
		super();
	}

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public Object readValue(final Binary binary, final long address)
	{
		return new ObjectReferenceWrapper(ViewerBinaryPrimitivesReader.readReference(binary, address));
	}

	@Override
	public long getBinarySize(final Binary binary, final long address)
	{
		return Binary.objectIdByteLength();
	}
}
