package one.microstream.persistence.binary.types;

import static one.microstream.X.mayNull;
import static one.microstream.X.notNull;

import one.microstream.collections.types.XGettingTable;
import one.microstream.exceptions.TypeCastException;
import one.microstream.persistence.binary.internal.AbstractBinaryLegacyTypeHandlerTranslating;
import one.microstream.persistence.types.PersistenceLegacyTypeHandlingListener;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceTypeDefinition;
import one.microstream.persistence.types.PersistenceTypeHandlerReflective;

public final class BinaryLegacyTypeHandlerReflective<T>
extends AbstractBinaryLegacyTypeHandlerTranslating<T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static <T> BinaryLegacyTypeHandlerReflective<T> New(
		final PersistenceTypeDefinition                     typeDefinition              ,
		final PersistenceTypeHandlerReflective<Binary, T>   typeHandler                 ,
		final XGettingTable<Long, BinaryValueSetter>        translatorsWithTargetOffsets,
		final PersistenceLegacyTypeHandlingListener<Binary> listener                    ,
		final boolean                                       switchByteOrder
	)
	{
		return new BinaryLegacyTypeHandlerReflective<>(
			notNull(typeDefinition)                      ,
			notNull(typeHandler)                         ,
			toTranslators(translatorsWithTargetOffsets)  ,
			toTargetOffsets(translatorsWithTargetOffsets),
			mayNull(listener)                            ,
			switchByteOrder
		);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryLegacyTypeHandlerReflective(
		final PersistenceTypeDefinition                     typeDefinition  ,
		final PersistenceTypeHandlerReflective<Binary, T>   typeHandler     ,
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
	protected T internalCreate(final Binary rawData, final PersistenceLoadHandler handler)
	{
		return this.typeHandler().create(rawData, handler);
	}
	
	@Override
	public final void update(final Binary rawData, final T instance, final PersistenceLoadHandler handler)
	{
		/*
		 * Explicit type check to avoid memory getting overwritten with bytes not fitting to the actual type.
		 * This can be especially critical if a custom root resolver returns an instance that does not match
		 * the type defined by the typeId.
		 */
		if(!this.type().isInstance(instance))
		{
			throw new TypeCastException(this.type(), instance);
		}

		rawData.updateFixedSize(instance, this.valueTranslators(), this.targetOffsets(), handler);
	}

	@Override
	public final void complete(final Binary medium, final T instance, final PersistenceLoadHandler handler)
	{
		// no-op for reflective logic
	}
	
}
