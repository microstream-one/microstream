package one.microstream.persistence.binary.internal;

import one.microstream.chars.XChars;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.exceptions.PersistenceException;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceReferenceLoader;
import one.microstream.persistence.types.PersistenceTypeDefinitionMember;


public abstract class AbstractBinaryHandlerCustomValue<T>
extends AbstractBinaryHandlerCustom<T>
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
	public final void iterateLoadableReferences(final Binary offset, final PersistenceReferenceLoader iterator)
	{
		// no references
	}
	
	@Override
	public void initialize(final Binary data, final T instance, final PersistenceLoadHandler handler)
	{
		/*
		 * No-op update logic by default. This is useful for all immutable value types (String, Integer, etc.)
		 * which normally get initialized directly at instance creation time..
		 */
	}
	
	public abstract void validateState(Binary data, T instance, PersistenceLoadHandler handler);
	
	protected static <S> void compareSimpleState(
		final Object instance,
		final S instanceState,
		final S binaryState
	)
	{
		if(instanceState.equals(binaryState))
		{
			return;
		}
		
		throwInconsistentStateException(instance, instanceState, binaryState);
	}
	
	protected static void throwInconsistentStateException(
		final Object instance     ,
		final Object instanceState,
		final Object binaryState
	)
	{
		// (30.12.2019 TM)EXCP: proper exception
		throw new PersistenceException(
			"Inconsistent state for instance " + XChars.systemString(instance) + ": \""
			+ instanceState + "\" not equal to \"" + binaryState + "\""
		);
	}
	
	@Override
	public void update(final Binary data, final T instance, final PersistenceLoadHandler handler)
	{
		this.validateState(data, instance, handler);
	}

}
