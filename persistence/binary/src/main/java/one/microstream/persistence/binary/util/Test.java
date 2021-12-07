
package one.microstream.persistence.binary.util;

import one.microstream.collections.EqHashTable;
import one.microstream.reference.Lazy;


public class Test
{
	public static void main(final String[] args)
	{
		final SerializerFoundation<?> foundation = SerializerFoundation.New()
			.registerEntityTypes(Lazy.class);
		final Serializer<byte[]>      serializer = Serializer.Bytes(foundation);
		
		final Lazy                    lazy       = Lazy.Reference("ahoj");
		
		final EqHashTable<Integer, Object> table = EqHashTable.New();
		table.put(100, lazy);
		
		final byte[]                  bytes      = serializer.serialize(table);
	}
}
