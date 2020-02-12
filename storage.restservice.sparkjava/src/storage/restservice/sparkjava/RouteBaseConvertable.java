package storage.restservice.sparkjava;

import one.microstream.storage.restadapter.StorageRestAdapterConverter;
import one.microstream.storage.restadapter.StorageViewDataConverter;
import spark.Response;

public abstract class RouteBaseConvertable<T extends StorageRestAdapterConverter> extends RouteBase<T>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public RouteBaseConvertable(final T storageRestAdapter)
	{
		super(storageRestAdapter);
	}


	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	public String toRequestedFormat(final Object object, final String requestedFormat, final Response response)
	{
		if(requestedFormat != null)
		{
			final StorageViewDataConverter converter = this.storageRestAdapter.getConverter(requestedFormat);
			if(converter != null)
			{
				final String responseContentType = converter.getHtmlResponseContentType();

				if(responseContentType != null)
				{
					response.type(responseContentType);
				}

				return converter.convert(object);
			}
			throw new InvalidRouteParametersException("format invalid");
		}

		response.type("application/json");
		return this.storageRestAdapter.getConverter("application/json").convert(object);
	}
}