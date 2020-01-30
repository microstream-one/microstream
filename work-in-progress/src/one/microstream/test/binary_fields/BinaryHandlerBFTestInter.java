package one.microstream.test.binary_fields;

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;

public final class BinaryHandlerBFTestInter
extends AbstractBinaryHandlerBFTestInter<BFTestInter>
{
	public BinaryHandlerBFTestInter()
	{
		super(BFTestInter.class);
	}
	
	@Override
	public BFTestInter create(final Binary data, final PersistenceLoadHandler handler)
	{
		return new BFTestInter(
			this.ap_byte.read_byte(data),
			this.ip_short.read_short(data)
		);
	}
	
}
