package one.microstream.storage.restadapter.types;

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.BinaryPersistence;
import one.microstream.persistence.types.PersistenceTypeDefinitionMember;

public class ValueReaderPrimitive implements ValueReader
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final PersistenceTypeDefinitionMember typeDefinition;

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public ValueReaderPrimitive(final PersistenceTypeDefinitionMember member)
	{
		super();
		this.typeDefinition = member;
	}

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public Object readValue(final Binary binary, final long offset)
	{
		return ViewerBinaryPrimitivesReader.readPrimitive(this.typeDefinition.type(), binary, offset);
	}

	@Override
	public long getBinarySize(final Binary binary, final long offset)
	{
		return BinaryPersistence.resolvePrimitiveFieldBinaryLength(this.typeDefinition.type());
	}
}
