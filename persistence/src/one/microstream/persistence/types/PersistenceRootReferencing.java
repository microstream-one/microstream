package one.microstream.persistence.types;

import java.util.function.Supplier;

import one.microstream.reference.Referencing;


public interface PersistenceRootReferencing extends Supplier<Object>, Referencing<Object>
{
	@Override
	public Object get();
	
	public <F extends PersistenceFunction> F iterate(F iterator);
		
}
