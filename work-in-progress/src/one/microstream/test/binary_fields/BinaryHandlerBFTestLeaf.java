package one.microstream.test.binary_fields;

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.BinaryField;
import one.microstream.persistence.types.PersistenceLoadHandler;

public class BinaryHandlerBFTestLeaf extends AbstractBinaryHandlerBFTestInter<BFTestLeaf>
{
	final BinaryField<BFTestLeaf>
		lp_int    = Field_int(e -> e.lp_int),
		lp_float  = Field_float(e -> e.lp_float, (e, v) -> e.lp_float = v),
		lp_long   = Field_long(BFTestLeaf::get_longValue, BFTestLeaf::set_longValue),
		lp_double = Field_double(BFTestLeaf::get_doubleValue, BFTestLeaf::set_doubleValue)
	;
	
	public BinaryHandlerBFTestLeaf()
	{
		super(BFTestLeaf.class);
	}
	
	@Override
	public BFTestLeaf create(final Binary data, final PersistenceLoadHandler handler)
	{
		return new BFTestLeaf(
			this.ap_byte.read_byte(data),
			this.ip_short.read_short(data),
			this.lp_int.read_int(data)
		);
	}
	
}
