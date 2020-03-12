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
		private final EmbeddedStorageRestAdapter       embeddedStorageRestAdapter;
		private long                                   defaultValueLength = Long.MAX_VALUE;

		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Default(final EmbeddedStorageManager storage)
		{
			super();
			this.embeddedStorageRestAdapter = new EmbeddedStorageRestAdapter(storage);
			this.converterProvider          = new StorageViewDataConverterProvider.Default();
		}

		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public ViewerObjectDescription getObject(
			final long objectId,
			final long fixedOffset,
			final long fixedLength,
			final long variableOffset,
			final long variableLength,
			final long valueLength,
			final boolean resolveReferences)
		{
			if(fixedOffset < 0)
			{
				throw new ViewerException("invalid parameter fixedOffset");
			}
			if(fixedLength < 0)
			{
				throw new ViewerException("invalid parameter fixedLength");
			}
			if(variableOffset < 0)
			{
				throw new ViewerException("invalid parameter variableOffset");
			}
			if(variableLength < 0)
			{
				throw new ViewerException("invalid parameter variableLength");
			}
			if(valueLength < 0)
			{
				throw new ViewerException("invalid parameter valueLength");
			}

			final ObjectDescription description = this.embeddedStorageRestAdapter.getStorageObject(objectId);
			if(resolveReferences)
			{
				description.resolveReferences(
					fixedOffset,
					fixedLength,
					variableOffset,
					variableLength,
					this.embeddedStorageRestAdapter
				);
			}

			return new ViewerObjectDescriptionCreator(
				description,
				fixedOffset,
				fixedLength,
				variableOffset,
				variableLength,
				valueLength
			).create();
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
	    public long getDefaultValueLength()
	    {
	        return this.defaultValueLength;
	    }

	    @Override
	    public void setDefaultValueLength(final long defaultValueLength)
	    {
	        this.defaultValueLength = defaultValueLength;
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
