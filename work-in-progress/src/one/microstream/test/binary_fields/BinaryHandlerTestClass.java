package one.microstream.test.binary_fields;

import one.microstream.persistence.binary.internal.BinaryField;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

public class BinaryHandlerTestClass extends AbstractBinaryHandlerTestBaseClass<TestLeafClass>
{
	private final BinaryField<TestLeafClass>
		leafValue_int1   = Field(e -> e.leafValue_int, (e, v) -> e.leafValue_int = v),
		leafValue_int2   = Field(TestLeafClass::leafValue_int, TestLeafClass::setLeafValue_int)
//		leafValue_float = Field(float.class),
//		leafReference   = Field(String.class)
	;
	
	protected BinaryHandlerTestClass()
	{
		super(TestLeafClass.class);
	}
	
	@Override
	public void store(final Binary data, final TestLeafClass instance, final long objectId, final PersistenceStoreHandler handler)
	{
		super.store(data, instance, objectId, handler);
		this.leafValue_int  .store_long    (data, instance.leafValue_int  );
		this.leafValue_float.store_double  (data, instance.leafValue_float);
		this.leafReference  .storeReference(data, instance.leafReference, handler);
	}
	
	@Override
	public TestLeafClass create(final Binary data, final PersistenceLoadHandler handler)
	{
		return new TestLeafClass();
	}
}
