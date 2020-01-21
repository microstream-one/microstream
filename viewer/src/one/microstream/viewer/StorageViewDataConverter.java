package one.microstream.viewer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.LongSerializationPolicy;

public class StorageViewDataConverter
{
	Gson gson;

	public StorageViewDataConverter()
	{
		super();
		this.gson = new GsonBuilder().setLongSerializationPolicy(LongSerializationPolicy.STRING ).serializeNulls().create();
	}

	public String convert(final ViewerRootDescription root)
	{
		return this.gson.toJson(root);
	}

	public String convert(final SimpleObjectDescription preprocessed)
	{
		return this.gson.toJson(preprocessed);
	}
}

