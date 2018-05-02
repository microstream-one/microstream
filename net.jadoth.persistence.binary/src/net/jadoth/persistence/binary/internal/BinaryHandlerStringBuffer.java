package net.jadoth.persistence.binary.internal;

import net.jadoth.X;
import net.jadoth.memory.Memory;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.swizzling.types.PersistenceStoreFunction;
import net.jadoth.swizzling.types.SwizzleBuildLinker;


public final class BinaryHandlerStringBuffer extends AbstractBinaryHandlerAbstractStringBuilder<StringBuffer>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerStringBuffer(final long typeId)
	{
		super(typeId, StringBuffer.class);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public void store(final Binary bytes, final StringBuffer instance, final long oid, final PersistenceStoreFunction linker)
	{
		final char[] value;
		final long address;
		Memory.set_int(
			address = bytes.storeEntityHeader(((long)instance.length() << 1) + LENGTH_LENGTH, this.typeId(), oid),
			(value = Memory.accessChars(instance)).length
		);
		Memory.copyArray(value, address, 0, instance.length());
	}

	@Override
	public StringBuffer create(final Binary bytes)
	{
		return new StringBuffer(X.checkArrayRange(Memory.get_long(bytes.buildItemAddress())));
	}

	@Override
	public void update(final Binary bytes, final StringBuffer instance, final SwizzleBuildLinker builder)
	{
		final long lengthChars = BinaryPersistence.getBuildItemContentLength(bytes) - LENGTH_LENGTH;
		final long buildItemAddress = bytes.buildItemAddress();
		instance.ensureCapacity(X.checkArrayRange(Memory.get_long(buildItemAddress)));
		Memory.setData(instance, null, buildItemAddress + LENGTH_LENGTH, lengthChars);
	}

//	@Override
//	public void copy(final StringBuffer source, final StringBuffer target)
//	{
//		target.ensureCapacity(source.length());
//		target.setLength(0);
//		target.append(source);
//	}

}
