package one.microstream.persistence.binary.types;

import one.microstream.persistence.types.PersistenceStoreHandler;

public interface BinaryValueStorer
{
	public long storeValueFromMemory(
		Object                          source       ,
		long                            sourceOffset ,
		long                            targetAddress,
		PersistenceStoreHandler<Binary> persister
	);
}
