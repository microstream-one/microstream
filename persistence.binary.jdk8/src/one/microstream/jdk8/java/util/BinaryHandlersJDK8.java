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
				BinaryHandlerVector.New(c)      ,
				BinaryHandlerHashtable.New()    ,
				BinaryHandlerStack.New(c)       ,
				BinaryHandlerProperties.New()   ,
				
				// JDK 1.2 collections
				BinaryHandlerArrayList.New(c)   ,
				BinaryHandlerHashSet.New()      ,
				BinaryHandlerHashMap.New()      ,
				
				// JDK 1.4 collections
				BinaryHandlerLinkedHashMap.New(),
				BinaryHandlerLinkedHashSet.New()
			))
		);
		
		return foundation;
	}
}
