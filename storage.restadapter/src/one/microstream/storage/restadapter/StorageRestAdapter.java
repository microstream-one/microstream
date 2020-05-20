package one.microstream.storage.restadapter;

import static one.microstream.X.notNull;

import one.microstream.storage.types.StorageManager;

public interface StorageRestAdapter
	extends StorageRestAdapterTypeDictionary,
		StorageRestAdapterObject,
		StorageRestAdapterRoot,
		StorageRestAdapterStorageInfo,
		StorageViewDataConverterRegistry
{
	public static StorageRestAdapter New(
		final StorageManager storage
	)
	{
		return new Default(
			StorageViewDataConverterRegistry.New() ,
			EmbeddedStorageRestAdapter.New(storage)
		);
	}

	public static StorageRestAdapter New(
		final StorageViewDataConverterRegistry converterRegistry         ,
		final EmbeddedStorageRestAdapter       embeddedStorageRestAdapter
	)
	{
		return new Default(
			notNull(converterRegistry)         ,
			notNull(embeddedStorageRestAdapter)
		);
	}


	public class Default implements StorageRestAdapter
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final StorageViewDataConverterRegistry converterRegistry;
		private final EmbeddedStorageRestAdapter       embeddedStorageRestAdapter;
		private long                                   defaultValueLength = Long.MAX_VALUE;

		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final StorageViewDataConverterRegistry converterRegistry         ,
			final EmbeddedStorageRestAdapter       embeddedStorageRestAdapter
		)
		{
			this.converterRegistry          = converterRegistry;
			this.embeddedStorageRestAdapter = embeddedStorageRestAdapter;
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
				throw new StorageRestAdapterException("invalid parameter fixedOffset");
			}
			if(fixedLength < 0)
			{
				throw new StorageRestAdapterException("invalid parameter fixedLength");
			}
			if(variableOffset < 0)
			{
				throw new StorageRestAdapterException("invalid parameter variableOffset");
			}
			if(variableLength < 0)
			{
				throw new StorageRestAdapterException("invalid parameter variableLength");
			}
			if(valueLength < 0)
			{
				throw new StorageRestAdapterException("invalid parameter valueLength");
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
			return this.converterRegistry.getConverter(format);
		}

		@Override
		public boolean addConverter(final StorageViewDataConverter converter, final String format)
		{
			return this.converterRegistry.addConverter(converter, format);
		}

	}

}
