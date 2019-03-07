package net.jadoth.persistence.binary.internal;

import net.jadoth.collections.types.XImmutableSequence;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.types.PersistenceFunction;
import net.jadoth.persistence.types.PersistenceObjectIdAcceptor;
import net.jadoth.persistence.types.PersistenceTypeDefinitionMemberPseudoField;

public abstract class AbstractBinaryHandlerNativeArrayPrimitive<A> extends AbstractBinaryHandlerNativeArray<A>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public AbstractBinaryHandlerNativeArrayPrimitive(
		final Class<A>                                                                 arrayType   ,
		final XImmutableSequence<? extends PersistenceTypeDefinitionMemberPseudoField> pseudoFields
	)
	{
		super(arrayType, pseudoFields);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final void iterateInstanceReferences(final A instance, final PersistenceFunction iterator)
	{
		// no references to iterate in arrays with primitive component type
	}

	@Override
	public final void iteratePersistedReferences(final Binary offset, final PersistenceObjectIdAcceptor iterator)
	{
		// no references to iterate in arrays with primitive component type
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
	
}
