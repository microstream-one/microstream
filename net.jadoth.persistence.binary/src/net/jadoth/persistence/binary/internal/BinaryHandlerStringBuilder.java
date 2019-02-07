package net.jadoth.persistence.binary.internal;

import net.jadoth.X;
import net.jadoth.memory.XMemory;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.types.PersistenceLoadHandler;
import net.jadoth.persistence.types.PersistenceStoreHandler;


public final class BinaryHandlerStringBuilder extends AbstractBinaryHandlerAbstractStringBuilder<StringBuilder>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerStringBuilder()
	{
		super(StringBuilder.class);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public void store(final Binary bytes, final StringBuilder instance, final long oid, final PersistenceStoreHandler handler)
	{
		// (07.02.2019 TM)FIXME: JET-49: use Binary helper methods
		final char[] value;
		final long address;
		XMemory.set_int(
			address = bytes.storeEntityHeader(((long)instance.length() << 1) + LENGTH_LENGTH, this.typeId(), oid),
			(value = XMemory.accessChars(instance)).length
		);
		XMemory.copyArrayToAddress(value, 0, instance.length(), address);
	}

	@Override
	public StringBuilder create(final Binary bytes)
	{
		return new StringBuilder(X.checkArrayRange(bytes.get_long(0)));
	}

	@Override
	public void update(final Binary bytes, final StringBuilder instance, final PersistenceLoadHandler builder)
	{
		// (07.02.2019 TM)FIXME: JET-49: use Binary helper methods
		final long lengthChars = bytes.getBuildItemContentLength() - LENGTH_LENGTH;
		final long buildItemAddress = bytes.loadItemEntityContentAddress();
		instance.ensureCapacity(X.checkArrayRange(XMemory.get_long(buildItemAddress)));
		XMemory.setData(instance, null, buildItemAddress + LENGTH_LENGTH, lengthChars);
	}

}
