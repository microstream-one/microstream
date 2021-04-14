package one.microstream.persistence.binary.java.util;

import java.util.Locale;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomValueVariableLength;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerLocale extends AbstractBinaryHandlerCustomValueVariableLength<Locale, String>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerLocale New()
	{
		return new BinaryHandlerLocale();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerLocale()
	{
		super(
			Locale.class,
			CustomFields(
				chars("languageTag") // a little content hint down to the type dictionary level.
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	private static String instanceState(final Locale instance)
	{
		return instance.toLanguageTag();
	}
	
	private static String binaryState(final Binary data)
	{
		return data.buildString();
	}

	@Override
	public final void store(
		final Binary                          data    ,
		final Locale                          instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		// for once, they managed to do a kind of proper de/serialization logic. Amazing.
		data.storeStringSingleValue(this.typeId(), objectId, instanceState(instance));
	}

	@Override
	public Locale create(
		final Binary                 data   ,
		final PersistenceLoadHandler handler
	)
	{
		return Locale.forLanguageTag(binaryState(data));
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// validation //
	///////////////
	
	@Override
	public String getValidationStateFromInstance(final Locale instance)
	{
		return instanceState(instance);
	}
	
	@Override
	public String getValidationStateFromBinary(final Binary data)
	{
		return binaryState(data);
	}

}
