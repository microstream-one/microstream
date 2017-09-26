package net.jadoth.persistence.types;

import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.swizzling.types.SwizzleTypeLink;

public interface PersistenceTypeDescriptionInitializer<T> extends SwizzleTypeLink<T>
{
	/* (30.08.2017 TM)FIXME: initializer must also care about registering a Handler at the TypeHandlerManager!
	 * So a wrapper-implementation is needed, that forwards to the actual initializer for a handler but also
	 * registers the handler after it has been initialized
	 */
	
	public XGettingSequence<? extends PersistenceTypeDescriptionMember> members();
	
	public PersistenceTypeDescription<T> initialize(long typeId, PersistenceTypeLineage<T> lineage);
}