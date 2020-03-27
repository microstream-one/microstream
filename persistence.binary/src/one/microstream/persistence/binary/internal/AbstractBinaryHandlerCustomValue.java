package one.microstream.persistence.binary.internal;

import one.microstream.collections.types.XGettingSequence;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.ValidatingBinaryHandler;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceReferenceLoader;
import one.microstream.persistence.types.PersistenceTypeDefinitionMember;


public abstract class AbstractBinaryHandlerCustomValue<T, S>
extends AbstractBinaryHandlerCustom<T>
implements ValidatingBinaryHandler<T, S>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public AbstractBinaryHandlerCustomValue(
		final Class<T>                                                    type  ,
		final XGettingSequence<? extends PersistenceTypeDefinitionMember> fields
	)
	{
		super(type, fields);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public final boolean hasPersistedReferences()
	{
		return false;
	}

	@Override
	public final void iterateLoadableReferences(final Binary offset, final PersistenceReferenceLoader iterator)
	{
		// no references
	}
	
	@Override
	public void initializeState(final Binary data, final T instance, final PersistenceLoadHandler handler)
	{
		/*
		 * No-op update logic by default. This is useful for all immutable value types (String, Integer, etc.)
		 * which normally get initialized directly at instance creation time..
		 */
	}
		
	@Override
	public void updateState(final Binary data, final T instance, final PersistenceLoadHandler handler)
	{
		this.validateState(data, instance, handler);
	}

}
