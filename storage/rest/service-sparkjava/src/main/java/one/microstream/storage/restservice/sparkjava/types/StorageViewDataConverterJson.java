package one.microstream.storage.restservice.sparkjava.types;

/*-
 * #%L
 * microstream-storage-restservice-sparkjava
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

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

