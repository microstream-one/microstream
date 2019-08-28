package one.microstream.persistence.binary.types;

import one.microstream.exceptions.TypeCastException;
import one.microstream.persistence.binary.internal.AbstractBinaryLegacyTypeHandlerTranslating;
import one.microstream.persistence.types.PersistenceLegacyTypeHandlingListener;
import one.microstream.persistence.types.PersistenceObjectIdResolver;
import one.microstream.persistence.types.PersistenceTypeDefinition;
import one.microstream.persistence.types.PersistenceTypeHandler;
import one.microstream.persistence.types.PersistenceTypeHandlerReflective;

public abstract class AbstractBinaryLegacyTypeHandlerReflective<T>
extends AbstractBinaryLegacyTypeHandlerTranslating<T>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	AbstractBinaryLegacyTypeHandlerReflective(
		final PersistenceTypeDefinition                     typeDefinition  ,
		final PersistenceTypeHandler<Binary, T>             typeHandler     ,
		final BinaryValueSetter[]                           valueTranslators,
		final long[]                                        targetOffsets   ,
		final PersistenceLegacyTypeHandlingListener<Binary> listener        ,
		final boolean                                       switchByteOrder
	)
	{
		super(typeDefinition, typeHandler, valueTranslators, targetOffsets, listener, switchByteOrder);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public PersistenceTypeHandlerReflective<Binary, T> typeHandler()
	{
		// cast safety guranteed by constructor typing
		return (PersistenceTypeHandlerReflective<Binary, T>)super.typeHandler();
	}
	
	@Override
	protected T internalCreate(final Binary rawData, final PersistenceObjectIdResolver idResolver)
	{
		// (21.03.2019 TM)XXX: just passing to the type handler (in the end to the instantiator) can be dangerous
		return this.typeHandler().create(rawData, idResolver);
	}
	
	protected void validateForUpdate(
		final Binary                      rawData   ,
		final T                           instance  ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		/*
		 * Explicit type check to avoid memory getting overwritten with bytes not fitting to the actual type.
		 * This can be especially critical if a custom root resolver returns an instance that does not match
		 * the type defined by the typeId.
		 */
		if(this.type().isInstance(instance))
		{
			return;
		}
		
		throw new TypeCastException(this.type(), instance);
	}
	
	@Override
	public final void update(
		final Binary                      rawData   ,
		final T                           instance  ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		this.validateForUpdate(rawData, instance, idResolver);
		rawData.updateFixedSize(instance, this.valueTranslators(), this.targetOffsets(), idResolver);
	}

	@Override
	public final void complete(final Binary medium, final T instance, final PersistenceObjectIdResolver idResolver)
	{
		// no-op for reflective logic
	}
	
}
