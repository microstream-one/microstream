package one.microstream.test.binary_fields;

import one.microstream.persistence.binary.internal.CustomBinaryHandler;
import one.microstream.persistence.binary.types.BinaryField;


public abstract class AbstractBinaryHandlerBFTestAbstract<T extends BFTestAbstract>
extends CustomBinaryHandler<T>
{
	final BinaryField<BFTestAbstract>
		ap_byte = Field_byte(e -> e.ap_byte),
	
		ap_boolean = Field_boolean(e -> e.ap_boolean, (e, v) -> e.ap_boolean = v),
		arString1 = Field(String.class, e -> e.arString1, (e, v) -> e.arString1 = v),
	
		aDerivedString = Field_long(BFTestAbstract::getDerivedStringValue, BFTestAbstract::setDerivedStringValue),
		aDerivedDate   = Field_long(BFTestAbstract::getDerivedDateTimestamp, BFTestAbstract::setDerivedDateTimestamp)
	;
		
	protected AbstractBinaryHandlerBFTestAbstract(final Class<T> type)
	{
		super(type);
	}
	
}
