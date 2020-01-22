package one.microstream.viewer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.LongSerializationPolicy;

public class StorageViewDataConverterJson implements StorageViewDataConverter
{
	private static final String HTML_RESPONCE_CONTENT_TYPE = "application/json";

	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	Gson gson;


	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public StorageViewDataConverterJson()
	{
		super();
		this.gson = new GsonBuilder().setLongSerializationPolicy(LongSerializationPolicy.STRING ).serializeNulls().create();
	}


	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public String convert(final Object object)
	{
		return this.gson.toJson(object);
	}


	@Override
	public String getHtmlResponseContentType()
	{
		return HTML_RESPONCE_CONTENT_TYPE;
	}
}

