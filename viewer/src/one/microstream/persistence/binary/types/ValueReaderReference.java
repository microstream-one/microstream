package one.microstream.persistence.binary.types;

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
		return new ViewerObjectReferenceWrapper(ViewerBinaryPrimitivesReader.readReference(binary, address));
	}

	@Override
	public long getBinarySize(final Binary binary, final long address)
	{
		return Binary.objectIdByteLength();
	}
}
