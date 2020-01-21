package one.microstream.test.binary_fields;

import one.microstream.persistence.binary.internal.BinaryField;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;

public class BinaryHandlerTestLeafClass extends AbstractBinaryHandlerTestBaseClass<TestLeafClass>
{
	final BinaryField<TestLeafClass>
		leafValue_int1  = Field_int(e -> e.leafValue_int, (e, v) -> e.leafValue_int = v),
		leafValue_int2  = Field_int(TestLeafClass::leafValue_int, TestLeafClass::setLeafValue_int),
		leafValue_float = Field_float(e -> e.leafValue_float, (e, v) -> e.leafValue_float = v),
		leafReference   = Field(String.class, e -> e.leafReference, (e, v) -> e.leafReference = v)
	;
	
	public BinaryHandlerTestLeafClass()
	{
		super(TestLeafClass.class);
	}
	
	@Override
	public TestLeafClass create(final Binary data, final PersistenceLoadHandler handler)
	{
		return new TestLeafClass();
	}
	
}
