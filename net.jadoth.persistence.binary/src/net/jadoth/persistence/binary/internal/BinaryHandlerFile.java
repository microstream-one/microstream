package net.jadoth.persistence.binary.internal;

import java.io.File;

import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.swizzling.types.SwizzleStoreLinker;

public final class BinaryHandlerFile extends AbstractBinaryHandlerNativeCustom<File>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerFile(final long typeId)
	{
		super(typeId, File.class, pseudoFields(
			chars("path")
		));
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

//	@Override
//	public long getFixedBinaryContentLength()
//	{
//		return 0L;
//	}

	@Override
	public boolean isVariableBinaryLengthType()
	{
		return true;
	}

	@Override
	public void store(final Binary bytes, final File instance, final long oid, final SwizzleStoreLinker linker)
	{
		BinaryPersistence.storeStringValue(bytes, this.typeId(), oid, instance.getPath());
	}

	@Override
	public File create(final Binary bytes)
	{
		return new File(BinaryPersistence.buildString(bytes));
	}

	@Override
	public final boolean hasInstanceReferences()
	{
		return false;
	}

	@Override
	public boolean hasVariableBinaryLengthInstances()
	{
		return false;
	}

}
