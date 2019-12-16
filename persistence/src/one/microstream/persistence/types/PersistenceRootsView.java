package one.microstream.persistence.types;

import java.util.function.BiConsumer;


public interface PersistenceRootsView
{
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
	
	public PersistenceRootReferencing root();
	
		
	public <C extends BiConsumer<String, Object>> C iterateEntries(C iterator);
}
