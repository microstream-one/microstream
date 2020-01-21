package one.microstream.viewer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.LongSerializationPolicy;

public class StorageViewDataConverterJson implements StorageViewDataConverter
{
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
	public String convert(final ViewerRootDescription root)
	{
		return this.gson.toJson(root);
	}

	@Override
	public String convert(final ViewerObjectDescription preprocessed)
	{
		return this.gson.toJson(preprocessed);
	}
}

