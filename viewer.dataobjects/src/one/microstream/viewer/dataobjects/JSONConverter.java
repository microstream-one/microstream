package one.microstream.viewer.dataobjects;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;


/**
 *
 * Convert viewer data to JSON using gson
 *
 */
public class JSONConverter implements ObjectDescriptionConverter<String>
{
	private final Gson gson;

	public JSONConverter()
	{
		super();

		final RuntimeTypeAdapterFactory<MemberValue> runtimeTypeAdapterFactory
		= RuntimeTypeAdapterFactory
		.of(MemberValue.class)
		.registerSubtype(MemberValue.class)
		.registerSubtype(ReferenceValue.class);

		this.gson = new GsonBuilder().registerTypeAdapterFactory(runtimeTypeAdapterFactory).create();
	}

	@Override
	public String convert(final ObjectDescription objectDescription)
	{
		return this.gson.toJson(objectDescription);
	}

	@Override
	public ObjectDescription toObjectDescription(final String json)
	{
		return this.gson.fromJson(json, ObjectDescription.class);
	}

	@Override
	public String convert(final RootObjectDescription rootObjectDescription)
	{
		return this.gson.toJson(rootObjectDescription);
	}

	@Override
	public RootObjectDescription toRootObjectDescription(final String json)
	{
		return this.gson.fromJson(json, RootObjectDescription.class);
	}

	@Override
	public String convert(final MemberDescription memberDescription)
	{
		return this.gson.toJson(memberDescription);
	}

	@Override
	public MemberDescription toMemberDescription(final String from)
	{
		return this.gson.fromJson(from, MemberDescription.class);
	}

	@Override
	public String convert(final List<MemberDescription> memberDescriptions)
	{
		return this.gson.toJson(memberDescriptions);
	}

	@Override
	public List<MemberDescription> toMemberList(final String json)
	{
		return this.gson.fromJson(json, new TypeToken<List<MemberDescription>>(){}.getType());
	}

}
