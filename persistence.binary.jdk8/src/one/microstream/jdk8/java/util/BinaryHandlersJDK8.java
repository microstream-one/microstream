package one.microstream.jdk8.java.util;

import one.microstream.X;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceFoundation;

public final class BinaryHandlersJDK8
{
	public static <F extends PersistenceFoundation<Binary, ?>> F registerJDK8TypeHandlers(final F foundation)
	{
		foundation.executeTypeHandlerRegistration((r, c) ->
			r.registerTypeHandlers(X.List(
				// JDK 1.0 collections
				new BinaryHandlerVector(c)      ,
				new BinaryHandlerHashtable()    ,
				new BinaryHandlerStack(c)       ,
				new BinaryHandlerProperties()   ,
				
				// JDK 1.2 collections
				new BinaryHandlerArrayList(c)   ,
				new BinaryHandlerHashSet()      ,
				new BinaryHandlerHashMap()      ,
				
				// JDK 1.4 collections
				new BinaryHandlerLinkedHashMap(),
				new BinaryHandlerLinkedHashSet()
			))
		);
		
		return foundation;
	}
}
