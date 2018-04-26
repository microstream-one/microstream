package net.jadoth.persistence.binary.internal;

import net.jadoth.collections.X;
import net.jadoth.memory.objectstate.ObjectStateHandlerLookup;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.persistence.types.PersistenceTypeDescriptionMemberPseudoField;
import net.jadoth.swizzling.types.SwizzleBuildLinker;
import net.jadoth.swizzling.types.PersistenceStoreFunction;

public abstract class AbstractBinaryHandlerStateless<T> extends AbstractBinaryHandlerNativeCustom<T>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public AbstractBinaryHandlerStateless(final long tid, final Class<T> type)
	{
		super(tid, type, X.<PersistenceTypeDescriptionMemberPseudoField>empty());
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final void store(final Binary bytes, final T instance, final long oid, final PersistenceStoreFunction linker)
	{
		BinaryPersistence.storeStateless(bytes, this.typeId(), oid);
	}

	@Override
	public final void update(final Binary bytes, final T instance, final SwizzleBuildLinker builder)
	{
		// no-op
	}

	@Override
	public final boolean isEqual(final T source, final T target, final ObjectStateHandlerLookup stateHandlerLookup)
	{
		// the only reasonable equality for stateless instances
		return source == target;
	}

	@Override
	public final boolean hasInstanceReferences()
	{
		return false;
	}
	
	@Override
	public final boolean hasPersistedReferences()
	{
		return false;
	}
	
	@Override
	public final boolean hasPersistedVariableLength()
	{
		return false;
	}

	@Override
	public final boolean hasVaryingPersistedLengthInstances()
	{
		return false;
	}

//	@Override
//	public final void copy(final T source, final T target)
//	{
//		// well it can be called, no problem, but it won't (can't) do anything.
//	}

}
