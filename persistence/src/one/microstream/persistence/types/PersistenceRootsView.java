package one.microstream.persistence.types;

import java.util.function.BiConsumer;


public interface PersistenceRootsView
{
	
	public static String rootIdentifier()
	{
		// just a convenience relaying method.
		return Persistence.rootIdentifier();
	}
	
	// (16.12.2019 TM)NOTE: no deprecated methods in non-convenience API (while still in early versions ...)
	
//	@Deprecated
//	public default String defaultRootIdentifier()
//	{
//		return Persistence.defaultRootIdentifier();
//	}
//
//	@Deprecated
//	public default Referencing<Object> defaultRoot()
//	{
//		return this.root();
//	}
//
//	@Deprecated
//	public default String customRootIdentifier()
//	{
//		return Persistence.customRootIdentifier();
//	}
//
//	@Deprecated
//	public default Object customRoot()
//	{
//		final PersistenceRootReferencing rootReference = this.root();
//
//		return rootReference != null
//			? rootReference.get()
//			: null
//		;
//	}
	
	public PersistenceRootReferencing rootReference();
		
	public <C extends BiConsumer<String, Object>> C iterateEntries(C iterator);
}
