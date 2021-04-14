package one.microstream.persistence.types;

import one.microstream.reference.Referencing;


public interface PersistenceRootReferencing extends Referencing<Object>
{
	@Override
	public Object get();
	
	public <F extends PersistenceFunction> F iterate(F iterator);
		
}
