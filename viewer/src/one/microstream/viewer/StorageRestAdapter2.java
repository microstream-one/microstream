package one.microstream.viewer;

import one.microstream.persistence.binary.types.ViewerObjectDescription;
import one.microstream.storage.types.EmbeddedStorageManager;

public class StorageRestAdapter2 extends EmbeddedStorageRestAdapter
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final StorageViewDataConverter converter;

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	public StorageRestAdapter2(final EmbeddedStorageManager storage)
	{
		super(storage);
		this.converter = new StorageViewDataConverter();
	}

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	public String getObject(final long objectId)
	{
		final ViewerObjectDescription description = super.getStorageObject(objectId);
		return this.converter.convert(description);
	}

	public String getObject(final long objectId, final long dataOffset, final long dataLength)
	{
		final ViewerObjectDescription description = super.getStorageObject(objectId);
		return this.converter.convert(description, dataOffset, dataLength);
	}

	public String getObject(
		final long objectId,
		final long dataOffset,
		final long dataLength,
		final boolean resolveReferences,
		final long referenceOffset,
		final long referenceLength)
	{
		final ViewerObjectDescription description = super.getStorageObject(objectId);
		if(resolveReferences)
		{
			description.resolveReferences(referenceOffset, referenceLength, this);
		}
		return this.converter.convert(description, dataOffset, dataLength);
	}

	public String getUserRoot()
	{
		final ViewerRootDescription root = super.getRoot();
		return this.converter.convert(root);
	}
}
