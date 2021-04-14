package one.microstream.persistence.binary.types;

import one.microstream.chars.XChars;
import one.microstream.persistence.binary.exceptions.BinaryPersistenceException;
import one.microstream.persistence.types.PersistenceLoadHandler;

public interface ValidatingBinaryHandler<T, S>
{
	public default void validateState(final Binary data, final T instance, final PersistenceLoadHandler handler)
	{
		this.validateStates(
			instance,
			this.getValidationStateFromInstance(instance),
			this.getValidationStateFromBinary(data)
		);
	}
	
	public S getValidationStateFromInstance(T instance);
	
	public S getValidationStateFromBinary(Binary data);
	
	public default void validateStates(
		final T instance     ,
		final S instanceState,
		final S binaryState
	)
	{
		if(instanceState.equals(binaryState))
		{
			return;
		}
		
		this.throwInconsistentStateException(instance, instanceState, binaryState);
	}
	
	public default void throwInconsistentStateException(
		final T      instance                   ,
		final Object instanceStateRepresentation,
		final Object binaryStateRepresentation
	)
	{
		throw new BinaryPersistenceException(
			"Inconsistent state for instance " + XChars.systemString(instance) + ": \""
			+ instanceStateRepresentation + "\" not equal to \"" + binaryStateRepresentation + "\""
		);
	}
	
}
