package one.microstream.storage.restservice.sparkjava.types;

import java.lang.reflect.Type;
import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.LongSerializationPolicy;

import one.microstream.storage.restadapter.types.StorageViewDataConverter;

public class StorageViewDataConverterJson implements StorageViewDataConverter
{
	private static final String   HTML_RESPONCE_CONTENT_TYPE = "application/json";
	private static final String[] FORMAT_STRINGS              = {HTML_RESPONCE_CONTENT_TYPE, "json"};

	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Gson gson;


	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public StorageViewDataConverterJson()
	{
		super();


        final JsonSerializer<Date> serializerDate = new JsonSerializer<Date>()
        {
            @Override
            public JsonElement serialize(final Date src, final Type typeOfSrc, final JsonSerializationContext context)
            {
                return new JsonPrimitive(src.toInstant().toString());
            }
        };

        this.gson = new GsonBuilder()
            .setLongSerializationPolicy(LongSerializationPolicy.STRING )
            .serializeNulls()
            .registerTypeAdapter(Date.class, serializerDate)
            .create();
	}


	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	public Gson getGson()
	{
		return this.gson;
	}

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


	@Override
	public String[] getFormatStrings()
	{
		return FORMAT_STRINGS;
	}
	
}

