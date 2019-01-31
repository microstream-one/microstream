package net.jadoth.persistence.binary.internal;

import net.jadoth.X;
import net.jadoth.memory.XMemory;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.types.PersistenceLoadHandler;
import net.jadoth.persistence.types.PersistenceStoreHandler;


public final class BinaryHandlerStringBuffer extends AbstractBinaryHandlerAbstractStringBuilder<StringBuffer>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerStringBuffer()
	{
		super(StringBuffer.class);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public void store(final Binary bytes, final StringBuffer instance, final long oid, final PersistenceStoreHandler handler)
	{
		final char[] value;
		final long address;
		XMemory.set_int(
			address = bytes.storeEntityHeader(((long)instance.length() << 1) + LENGTH_LENGTH, this.typeId(), oid),
			(value = XMemory.accessChars(instance)).length
		);
		XMemory.copyArrayToAddress(value, 0, instance.length(), address);
	}

	@Override
	public StringBuffer create(final Binary bytes)
	{
		return new StringBuffer(X.checkArrayRange(XMemory.get_long(bytes.loadItemEntityContentAddress())));
	}

	@Override
	public void update(final Binary bytes, final StringBuffer instance, final PersistenceLoadHandler builder)
	{
		final long lengthChars = bytes.getBuildItemContentLength() - LENGTH_LENGTH;
		final long buildItemAddress = bytes.loadItemEntityContentAddress();
		instance.ensureCapacity(X.checkArrayRange(XMemory.get_long(buildItemAddress)));
		XMemory.setData(instance, null, buildItemAddress + LENGTH_LENGTH, lengthChars);
	}

//	@Override
//	public void copy(final StringBuffer source, final StringBuffer target)
//	{
//		target.ensureCapacity(source.length());
//		target.setLength(0);
//		target.append(source);
//	}

}
