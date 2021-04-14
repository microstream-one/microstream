package one.microstream.storage.restservice.sparkjava.types;

import one.microstream.storage.restadapter.types.StorageViewDataConverter;
import one.microstream.storage.restadapter.types.StorageViewDataConverterProvider;
import one.microstream.storage.restservice.sparkjava.exceptions.InvalidRouteParametersException;
import spark.Response;

public abstract class RouteBaseConvertable<T extends StorageViewDataConverterProvider> extends RouteBase<T>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public RouteBaseConvertable(final T apiAdapter)
	{
		super(apiAdapter);
	}


	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	public String toRequestedFormat(final Object object, final String requestedFormat, final Response response)
	{
		if(requestedFormat != null)
		{
			final StorageViewDataConverter converter = this.apiAdapter.getConverter(requestedFormat);
			if(converter != null)
			{
				final String responseContentType = converter.getHtmlResponseContentType();

				if(responseContentType != null)
				{
					response.type(responseContentType);
				}

				return converter.convert(object);
			}
			throw new InvalidRouteParametersException("format");
		}

		response.type("application/json");
		return this.apiAdapter.getConverter("application/json").convert(object);
	}
}