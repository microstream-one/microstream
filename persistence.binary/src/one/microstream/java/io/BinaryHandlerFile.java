package one.microstream.java.io;

import java.io.File;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomValueVariableLength;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerFile extends AbstractBinaryHandlerCustomValueVariableLength<File>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryHandlerFile()
	{
		super(
			File.class,
			pseudoFields(
				chars("path")
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public void store(final Binary bytes, final File instance, final long oid, final PersistenceStoreHandler handler)
	{
		bytes.storeStringValue(this.typeId(), oid, instance.getPath());
	}

	@Override
	public File create(final Binary bytes, final PersistenceLoadHandler handler)
	{
		return new File(bytes.buildString());
	}

}
