package one.microstream.java.util;

import java.util.Locale;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomValueVariableLength;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerLocale extends AbstractBinaryHandlerCustomValueVariableLength<Locale>
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
		final Binary                  bytes   ,
		final Locale                  instance,
		final long                    objectId,
		final PersistenceStoreHandler handler
	)
	{
		// for once, they managed to do a kind of proper de/serialization logic. Amazing.
		bytes.storeStringValue(this.typeId(), objectId, instanceState(instance));
	}

	@Override
	public Locale create(
		final Binary                 bytes  ,
		final PersistenceLoadHandler handler
	)
	{
		return Locale.forLanguageTag(binaryState(bytes));
	}
	
	@Override
	public void validateState(
		final Binary                 data    ,
		final Locale                 instance,
		final PersistenceLoadHandler handler
	)
	{
		compareSimpleState(instance, instanceState(instance), binaryState(data));
	}

}
