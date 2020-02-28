package one.microstream.java.util;

import java.util.Currency;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomValueVariableLength;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerCurrency extends AbstractBinaryHandlerCustomValueVariableLength<Currency>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerCurrency New()
	{
		return new BinaryHandlerCurrency();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerCurrency()
	{
		super(
			Currency.class,
			CustomFields(
				chars("currencyCode")
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	private static String instanceState(final Currency instance)
	{
		return instance.getCurrencyCode();
	}
	
	private static String binaryState(final Binary data)
	{
		return data.buildString();
	}

	@Override
	public final void store(
		final Binary                  data    ,
		final Currency                instance,
		final long                    objectId,
		final PersistenceStoreHandler handler
	)
	{
		data.storeStringSingleValue(this.typeId(), objectId, instanceState(instance));
	}

	@Override
	public Currency create(
		final Binary                 data   ,
		final PersistenceLoadHandler handler
	)
	{
		return Currency.getInstance(binaryState(data));
	}
	
	@Override
	public void validateState(
		final Binary                 data    ,
		final Currency                 instance,
		final PersistenceLoadHandler handler
	)
	{
		compareSimpleState(instance, instanceState(instance), binaryState(data));
	}

}
