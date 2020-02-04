package one.microstream.storage.restadapter;

import one.microstream.storage.types.EmbeddedStorageManager;

public interface StorageRestAdapter
	extends StorageRestAdapterTypeDictionary,
		StorageRestAdapterObject,
		StorageRestAdapterRoot,
		StorageRestAdapterStorageInfo,
		StorageViewDataConverterProvider
{

	public class Default implements StorageRestAdapter
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final StorageViewDataConverterProvider converterProvider;
		private final EmbeddedStorageRestAdapter embeddedStorageRestAdapter;
		private long defaultDataLength = Long.MAX_VALUE;

		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Default(final EmbeddedStorageManager storage)
		{
			//super(storage);
			super();
			this.embeddedStorageRestAdapter = new EmbeddedStorageRestAdapter(storage);
			this.converterProvider = new StorageViewDataConverterProvider.Default();
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


			final ObjectDescription description = this.embeddedStorageRestAdapter.getStorageObject(objectId);
			if(resolveReferences)
			{
				description.resolveReferences(referenceOffset, referenceLength, this.embeddedStorageRestAdapter);
			}

			return ViewerObjectDescriptionCreator.create(description, dataOffset, dataLength);
		}

		@Override
		public ViewerRootDescription getUserRoot()
		{
			return this.embeddedStorageRestAdapter.getRoot();
		}

		@Override
		public String getTypeDictionary()
		{
			return this.embeddedStorageRestAdapter.getTypeDictionary();
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

		@Override
		public ViewerStorageFileStatistics getStorageFilesStatistics()
		{
			return ViewerStorageFileStatistics.New(this.embeddedStorageRestAdapter.getFileStatistics());
		}

		@Override
		public StorageViewDataConverter getConverter(final String format)
		{
			return this.converterProvider.getConverter(format);
		}

		@Override
		public boolean register(final StorageViewDataConverter converter, final String format)
		{
			return this.converterProvider.register(converter, format);
		}
	}
}
