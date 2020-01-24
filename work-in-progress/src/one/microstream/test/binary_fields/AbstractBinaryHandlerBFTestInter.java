package one.microstream.test.binary_fields;

import one.microstream.persistence.binary.types.BinaryField;


public abstract class AbstractBinaryHandlerBFTestInter<T extends BFTestInter>
extends AbstractBinaryHandlerBFTestAbstract<T>
{
	final BinaryField<T>
		ip_short = Field_short(e -> e.ip_short),
		ip_char = Field_char(e -> e.ip_char, (e, v) -> e.ip_char = v)
	;
	
	public AbstractBinaryHandlerBFTestInter(final Class<T> type)
	{
		super(type);
	}
	
}
