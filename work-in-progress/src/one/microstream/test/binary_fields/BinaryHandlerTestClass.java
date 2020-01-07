package one.microstream.test.binary_fields;

import one.microstream.persistence.binary.internal.BinaryField;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;

public class BinaryHandlerTestClass extends AbstractBinaryHandlerTestBaseClass<TestLeafClass>
{
	private final BinaryField<TestLeafClass>
		leafValue_int1   = Field_int(e -> e.leafValue_int, (e, v) -> e.leafValue_int = v),
		leafValue_int2   = Field_int(TestLeafClass::leafValue_int, TestLeafClass::setLeafValue_int)
//		leafValue_float = Field(float.class),
//		leafReference   = Field(String.class)
	;
	
	
	protected BinaryHandlerTestClass()
	{
		super(TestLeafClass.class);
	}
	
	@Override
	public TestLeafClass create(final Binary data, final PersistenceLoadHandler handler)
	{
		// (07.01.2020 TM)FIXME: priv#88: make generic, but PersistenceTypeInstantiator<Binary, T> is overkill ...
		return new TestLeafClass();
	}
}
