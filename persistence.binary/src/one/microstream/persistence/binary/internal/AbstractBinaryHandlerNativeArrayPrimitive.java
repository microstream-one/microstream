package one.microstream.persistence.binary.internal;

import one.microstream.collections.types.XImmutableSequence;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceObjectIdAcceptor;
import one.microstream.persistence.types.PersistenceTypeDefinitionMemberPseudoField;

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
