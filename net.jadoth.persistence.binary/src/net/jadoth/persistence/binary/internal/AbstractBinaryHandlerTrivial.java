package net.jadoth.persistence.binary.internal;

import java.util.function.Consumer;

import net.jadoth.X;
import net.jadoth.collections.types.XGettingEnum;
import net.jadoth.functional._longProcedure;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryTypeHandler;
import net.jadoth.persistence.binary.types.BinaryValueAccessor;
import net.jadoth.persistence.types.PersistenceFunction;
import net.jadoth.persistence.types.PersistenceLoadHandler;
import net.jadoth.persistence.types.PersistenceTypeDefinitionMember;

public abstract class AbstractBinaryHandlerTrivial<T> extends BinaryTypeHandler.AbstractImplementation<T>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public AbstractBinaryHandlerTrivial(
		final Class<T>            type               ,
		final BinaryValueAccessor binaryValueAccessor
	)
	{
		super(type, binaryValueAccessor);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public void update(final Binary medium, final T instance, final PersistenceLoadHandler builder)
	{
		// no-op, no state to update
	}
	
	@Override
	public final void complete(final Binary medium, final T instance, final PersistenceLoadHandler builder)
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
	public final void iteratePersistedReferences(final Binary offset, final _longProcedure iterator)
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
