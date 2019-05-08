package one.microstream.persistence.binary.internal;

import java.util.function.Consumer;

import one.microstream.X;
import one.microstream.collections.types.XGettingEnum;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.BinaryTypeHandler;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceObjectIdAcceptor;
import one.microstream.persistence.types.PersistenceTypeDefinitionMember;

public abstract class AbstractBinaryHandlerTrivial<T> extends BinaryTypeHandler.Abstract<T>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public AbstractBinaryHandlerTrivial(final Class<T> type)
	{
		super(type);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public void update(final Binary medium, final T instance, final PersistenceLoadHandler handler)
	{
		// no-op, no state to update
	}
	
	@Override
	public final void complete(final Binary medium, final T instance, final PersistenceLoadHandler handler)
	{
		/* any "trival" implementation cannot have the need for a completion step
		 * (see non-reference-hashing collections for other examples)
		 */
	}

	@Override
	public final void iterateInstanceReferences(final T instance, final PersistenceFunction iterator)
	{
		// no-op, no references
	}

	@Override
	public final void iteratePersistedReferences(final Binary offset, final PersistenceObjectIdAcceptor iterator)
	{
		// no-op, no references
	}
	
	@Override
	public XGettingEnum<? extends PersistenceTypeDefinitionMember> members()
	{
		return X.empty();
	}
	
	@Override
	public long membersPersistedLengthMinimum()
	{
		return 0;
	}
	
	@Override
	public long membersPersistedLengthMaximum()
	{
		return 0;
	}
	
	@Override
	public boolean isPrimitiveType()
	{
		return false;
	}
	
	@Override
	public final boolean hasPersistedReferences()
	{
		return false;
	}
	
	@Override
	public final boolean hasInstanceReferences()
	{
		return false;
	}
	
	@Override
	public final boolean hasVaryingPersistedLengthInstances()
	{
		return false;
	}
	
	@Override
	public final <C extends Consumer<? super Class<?>>> C iterateMemberTypes(final C logic)
	{
		// no member types to iterate in a trivial handler implementation
		return logic;
	}
	
}
