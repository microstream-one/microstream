package one.microstream.test.binary_fields;

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;

public final class BinaryHandlerTestBaseClass extends AbstractBinaryHandlerTestBaseClass<TestBaseClass>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	protected BinaryHandlerTestBaseClass()
	{
		super(TestBaseClass.class);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final TestBaseClass create(final Binary data, final PersistenceLoadHandler handler)
	{
		return new TestBaseClass();
	}
	
}
