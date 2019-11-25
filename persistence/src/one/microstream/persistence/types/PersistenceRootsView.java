package one.microstream.persistence.types;

import java.util.function.BiConsumer;

import one.microstream.reference.Reference;


public interface PersistenceRootsView
{
	public String defaultRootIdentifier();
	
	public Reference<Object> defaultRoot();
	                                       
	public String customRootIdentifier();
	
	public Object customRoot();
		
	public <C extends BiConsumer<String, Object>> C iterateEntries(C iterator);
}
