package one.microstream.persistence.binary.internal;

import one.microstream.collections.types.XGettingSequence;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceTypeDefinitionMemberPseudoField;


public abstract class AbstractBinaryHandlerCustomIterable<T extends Iterable<?>>
extends AbstractBinaryHandlerCustomCollection<T>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public AbstractBinaryHandlerCustomIterable(
		final Class<T>                                                               type        ,
		final XGettingSequence<? extends PersistenceTypeDefinitionMemberPseudoField> pseudoFields
	)
	{
		super(type, pseudoFields);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public void iterateInstanceReferences(final T instance, final PersistenceFunction iterator)
	{
		Persistence.iterateReferencesIterable(iterator, instance);
	}
	
}
