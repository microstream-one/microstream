package net.jadoth.persistence.binary.internal;

import java.io.File;

import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.swizzling.types.SwizzleStoreLinker;

public final class BinaryHandlerFile extends AbstractBinaryHandlerNativeCustomValueVariableLength<File>
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
	// methods //
	////////////

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

}
