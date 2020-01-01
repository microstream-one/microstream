package one.microstream.persistence.binary.types;

import one.microstream.exceptions.TypeCastException;
import one.microstream.persistence.binary.internal.AbstractBinaryLegacyTypeHandlerTranslating;
import one.microstream.persistence.types.PersistenceLegacyTypeHandlingListener;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceReferenceLoader;
import one.microstream.persistence.types.PersistenceTypeDefinition;
import one.microstream.persistence.types.PersistenceTypeHandler;
import one.microstream.persistence.types.PersistenceTypeHandlerReflective;

public abstract class AbstractBinaryLegacyTypeHandlerReflective<T>
extends AbstractBinaryLegacyTypeHandlerTranslating<T>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final BinaryReferenceTraverser[] oldBinaryLayoutReferenceTraversers;
	
	
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

		/* (01.01.2020 TM)NOTE: Bugfix:
		 * Moved from AbstractBinaryLegacyTypeHandlerTranslating here as a recent fix was only correct for ~Rerouting
		 * but incorrect for ~Reflective LegacyHandler. The latter needs the old version before the fix.
		 */
		// reference traversers mut be derived from the old type definition that fits the persisted layout.
		this.oldBinaryLayoutReferenceTraversers = deriveReferenceTraversers(typeDefinition, switchByteOrder);
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
	public final void iterateLoadableReferences(final Binary rawData, final PersistenceReferenceLoader iterator)
	{
		rawData.iterateReferences(this.oldBinaryLayoutReferenceTraversers, iterator);
	}
	
	@Override
	protected T internalCreate(final Binary rawData, final PersistenceLoadHandler handler)
	{
		// (21.03.2019 TM)XXX: just passing to the type handler (in the end to the instantiator) can be dangerous
		return this.typeHandler().create(rawData, handler);
	}
	
	protected void validateForUpdate(
		final Binary                 data    ,
		final T                      instance,
		final PersistenceLoadHandler handler
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
	public void updateState(
		final Binary                 data    ,
		final T                      instance,
		final PersistenceLoadHandler handler
	)
	{
		this.validateForUpdate(data, instance, handler);
		data.updateFixedSize(instance, this.valueTranslators(), this.targetOffsets(), handler);
	}

	@Override
	public final void complete(final Binary data, final T instance, final PersistenceLoadHandler handler)
	{
		// no-op for reflective logic
	}
	
}
