package one.microstream.test.corp.main;

import one.microstream.X;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomValueFixedLength;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

public class MainTestCustomHandlerAmbiguousFieldNames
{
	
	public static void main(final String[] args)
	{
		final MyTypeHandler th = new MyTypeHandler();
		System.out.println(th.instanceMembers());
	}
}


final class MyTypeHandler extends AbstractBinaryHandlerCustomValueFixedLength<String, String>
{

	protected MyTypeHandler()
	{
		super(String.class, X.List(
			CustomField(String.class, "value"),
			CustomField(String.class, "value")
		));
	}

	@Override
	public void store(final Binary data, final String instance, final long objectId, final PersistenceStoreHandler handler)
	{
		// FIXME AbstractBinaryHandlerCustom<String>#store()
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public String create(final Binary data, final PersistenceLoadHandler handler)
	{
		// FIXME AbstractBinaryHandlerCustom<String>#create()
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public void validateState(final Binary data, final String instance, final PersistenceLoadHandler handler)
	{
		// FIXME AbstractBinaryHandlerCustomValue<String>#validateState()
		throw new one.microstream.meta.NotImplementedYetError();
	}
	
	@Override
	public String getValidationStateFromInstance(final String instance)
	{
		// FIXME MyTypeHandler#getValidationStateFromInstance()
		throw new one.microstream.meta.NotImplementedYetError();
	}
	
	@Override
	public String getValidationStateFromBinary(final Binary data)
	{
		// FIXME MyTypeHandler#getValidationStateFromBinary()
		throw new one.microstream.meta.NotImplementedYetError();
	}
	
}