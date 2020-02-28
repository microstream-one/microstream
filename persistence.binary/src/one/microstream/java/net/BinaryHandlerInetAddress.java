package one.microstream.java.net;

import java.net.InetAddress;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomValueVariableLength;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerInetAddress extends AbstractBinaryHandlerCustomValueVariableLength<InetAddress, String>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerInetAddress New()
	{
		return new BinaryHandlerInetAddress();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerInetAddress()
	{
		super(
			InetAddress.class,
			CustomFields(
				chars("address")
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	private static String instanceState(final InetAddress instance)
	{
		// (28.02.2020 TM)FIXME: priv#117: add flaming comment
		return instance.toString();
	}
	
	private static String binaryState(final Binary data)
	{
		return data.buildString();
	}

	@Override
	public final void store(
		final Binary                  data    ,
		final InetAddress             instance,
		final long                    objectId,
		final PersistenceStoreHandler handler
	)
	{
		// for once, they managed to do a kind of proper de/serialization logic. Amazing.
		data.storeStringSingleValue(this.typeId(), objectId, instanceState(instance));
	}

	@Override
	public InetAddress create(
		final Binary                 data   ,
		final PersistenceLoadHandler handler
	)
	{
		// (28.02.2020 TM)FIXME: priv#117: BinaryHandlerInetAddress#create()
		throw new one.microstream.meta.NotImplementedYetError();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// validation //
	///////////////
	
	@Override
	public String getValidationStateFromInstance(final InetAddress instance)
	{
		return instanceState(instance);
	}
	
	@Override
	public String getValidationStateFromBinary(final Binary data)
	{
		return binaryState(data);
	}

}
