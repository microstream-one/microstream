package one.microstream.test.binary_fields;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustom2;
import one.microstream.persistence.binary.internal.BinaryField;


public abstract class AbstractBinaryHandlerTestBaseClass<T extends TestBaseClass>
extends AbstractBinaryHandlerCustom2<T>
{
	final BinaryField<TestBaseClass>
		baseValue_long   = Field_long(e -> e.baseValue_long, (e, v) -> e.baseValue_long = v),
		baseValue_double = Field_double(e -> e.baseValue_double, (e, v) -> e.baseValue_double = v),
		baseReference    = Field(String.class, e -> e.baseReference, (e, v) -> e.baseReference = v)
	;
		
	protected AbstractBinaryHandlerTestBaseClass(final Class<T> type)
	{
		super(type);
	}
	
}
