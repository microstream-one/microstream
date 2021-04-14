package one.microstream.persistence.binary.java.lang;

import one.microstream.collections.types.XImmutableSequence;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceReferenceLoader;
import one.microstream.persistence.types.PersistenceTypeDefinitionMemberFieldGeneric;

public abstract class AbstractBinaryHandlerNativeArrayPrimitive<A> extends AbstractBinaryHandlerNativeArray<A>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public AbstractBinaryHandlerNativeArrayPrimitive(
		final Class<A>                                                                  arrayType   ,
		final XImmutableSequence<? extends PersistenceTypeDefinitionMemberFieldGeneric> customFields
	)
	{
		super(arrayType, customFields);
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
	public final void iterateLoadableReferences(final Binary offset, final PersistenceReferenceLoader iterator)
	{
		// no references to iterate in arrays with primitive component type
	}
	
	@Override
	public final boolean hasPersistedReferences()
	{
		return false;
	}
	
}
