package one.microstream.viewer;

import one.microstream.persistence.binary.types.ObjectDescription;
import one.microstream.persistence.binary.types.ViewerException;
import one.microstream.storage.types.EmbeddedStorageManager;

public class StorageRestAdapter extends EmbeddedStorageRestAdapter
	implements StorageRestAdapterConverter,
	StorageRestAdapterTypeDictionary,
	StorageRestAdapterObject,
	StorageRestAdapterRoot
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final StorageViewDataConverterProvider converterProvider;
	private long defaultDataLength = Long.MAX_VALUE;

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public StorageRestAdapter(final EmbeddedStorageManager storage)
	{
		super(storage);
		this.converterProvider = new StorageViewDataConverterProvider();
	}

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public ViewerObjectDescription getObject(
		final long objectId,
		final long dataOffset,
		final long dataLength,
		final boolean resolveReferences,
		final long referenceOffset,
		final long referenceLength)
	{

		if(dataOffset < 0) throw new ViewerException("invalid parameter dataOffset");
		if(dataLength < 1) throw new ViewerException("invalid parameter dataLength");
		if(referenceOffset < 0) throw new ViewerException("invalid parameter referenceOffset");
		if(referenceLength < 1) throw new ViewerException("invalid parameter referenceLength");


		final ObjectDescription description = super.getStorageObject(objectId);
		if(resolveReferences)
		{
			description.resolveReferences(referenceOffset, referenceLength, this);
		}

		return ObjectDescriptionConverter.convert(description, dataOffset, dataLength);
	}

	@Override
	public ViewerRootDescription getUserRoot()
	{
		return super.getRoot();
	}

	@Override
	public String getTypeDictionary()
	{
		return super.getTypeDictionary();
	}

	@Override
	public StorageViewDataConverter getConverter(final String format)
	{
		return this.converterProvider.get(format);
	}

    @Override
    public long getDefaultDataLength()
    {
        return this.defaultDataLength;
    }

    @Override
    public void setDefaultDataLength(final long defaultDataLength)
    {
        this.defaultDataLength = defaultDataLength;
    }
}
