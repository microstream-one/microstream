package net.jadoth.persistence.binary.internal;

import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.swizzling.types.SwizzleHandler;

public final class BinaryHandlerNativeCharacter extends AbstractBinaryHandlerNativeCustomValueFixedLength<Character>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerNativeCharacter()
	{
		super(Character.class, defineValueType(char.class));
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public void store(final Binary bytes, final Character instance, final long oid, final SwizzleHandler handler)
	{
		BinaryPersistence.storeCharacter(bytes, this.typeId(), oid, instance.charValue());
	}

	@Override
	public Character create(final Binary bytes)
	{
		return BinaryPersistence.buildCharacter(bytes);
	}

}
