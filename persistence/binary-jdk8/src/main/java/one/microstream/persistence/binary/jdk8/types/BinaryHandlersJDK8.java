package one.microstream.persistence.binary.jdk8.types;

import one.microstream.X;
import one.microstream.persistence.binary.jdk8.java.util.BinaryHandlerArrayList;
import one.microstream.persistence.binary.jdk8.java.util.BinaryHandlerHashMap;
import one.microstream.persistence.binary.jdk8.java.util.BinaryHandlerHashSet;
import one.microstream.persistence.binary.jdk8.java.util.BinaryHandlerHashtable;
import one.microstream.persistence.binary.jdk8.java.util.BinaryHandlerLinkedHashMap;
import one.microstream.persistence.binary.jdk8.java.util.BinaryHandlerLinkedHashSet;
import one.microstream.persistence.binary.jdk8.java.util.BinaryHandlerProperties;
import one.microstream.persistence.binary.jdk8.java.util.BinaryHandlerStack;
import one.microstream.persistence.binary.jdk8.java.util.BinaryHandlerVector;
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
