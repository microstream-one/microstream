package one.microstream.java.io;

import java.io.File;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomValueVariableLength;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceObjectIdResolver;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerFile extends AbstractBinaryHandlerCustomValueVariableLength<File>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerFile New()
	{
		return new BinaryHandlerFile();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerFile()
	{
		super(
			File.class,
			CustomFields(
				chars("path")
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public void store(final Binary bytes, final File instance, final long objectId, final PersistenceStoreHandler handler)
	{
		bytes.storeStringValue(this.typeId(), objectId, instance.getPath());
	}

	@Override
	public File create(final Binary bytes, final PersistenceObjectIdResolver idResolver)
	{
		return new File(bytes.buildString());
	}

}
