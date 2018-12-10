package net.jadoth.persistence.binary.internal;

import net.jadoth.X;
import net.jadoth.low.XVM;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryPersistence;
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
		XVM.set_int(
			address = bytes.storeEntityHeader(((long)instance.length() << 1) + LENGTH_LENGTH, this.typeId(), oid),
			(value = XVM.accessChars(instance)).length
		);
		XVM.copyArray(value, address, 0, instance.length());
	}

	@Override
	public StringBuffer create(final Binary bytes)
	{
		return new StringBuffer(X.checkArrayRange(XVM.get_long(bytes.buildItemAddress())));
	}

	@Override
	public void update(final Binary bytes, final StringBuffer instance, final PersistenceLoadHandler builder)
	{
		final long lengthChars = BinaryPersistence.getBuildItemContentLength(bytes) - LENGTH_LENGTH;
		final long buildItemAddress = bytes.buildItemAddress();
		instance.ensureCapacity(X.checkArrayRange(XVM.get_long(buildItemAddress)));
		XVM.setData(instance, null, buildItemAddress + LENGTH_LENGTH, lengthChars);
	}

//	@Override
//	public void copy(final StringBuffer source, final StringBuffer target)
//	{
//		target.ensureCapacity(source.length());
//		target.setLength(0);
//		target.append(source);
//	}

}
