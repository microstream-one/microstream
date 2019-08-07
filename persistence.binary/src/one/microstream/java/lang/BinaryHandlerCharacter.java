package one.microstream.java.lang;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomValueFixedLength;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceObjectIdResolver;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerCharacter extends AbstractBinaryHandlerCustomValueFixedLength<Character>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerCharacter New()
	{
		return new BinaryHandlerCharacter();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerCharacter()
	{
		super(Character.class, defineValueType(char.class));
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public void store(final Binary bytes, final Character instance, final long objectId, final PersistenceStoreHandler handler)
	{
		bytes.storeCharacter(this.typeId(), objectId, instance.charValue());
	}

	@Override
	public Character create(final Binary bytes, final PersistenceObjectIdResolver idResolver)
	{
		return bytes.buildCharacter();
	}

}
