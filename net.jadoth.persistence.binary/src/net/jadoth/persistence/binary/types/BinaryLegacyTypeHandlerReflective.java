package net.jadoth.persistence.binary.types;

import static net.jadoth.X.notNull;

import net.jadoth.collections.types.XGettingTable;
import net.jadoth.exceptions.TypeCastException;
import net.jadoth.persistence.types.PersistenceTypeDefinition;
import net.jadoth.persistence.types.PersistenceTypeHandlerReflective;
import net.jadoth.swizzling.types.SwizzleBuildLinker;

public final class BinaryLegacyTypeHandlerReflective<T>
extends AbstractBinaryLegacyTypeHandlerTranslating<T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static <T> BinaryLegacyTypeHandlerReflective<T> New(
		final PersistenceTypeDefinition<?>                typeDefinition              ,
		final PersistenceTypeHandlerReflective<Binary, T> typeHandler                 ,
		final XGettingTable<BinaryValueSetter, Long>      translatorsWithTargetOffsets
	)
	{
		return new BinaryLegacyTypeHandlerReflective<>(
			notNull(typeDefinition)                      ,
			notNull(typeHandler)                         ,
			toTranslators(translatorsWithTargetOffsets)  ,
			toTargetOffsets(translatorsWithTargetOffsets)
		);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryLegacyTypeHandlerReflective(
		final PersistenceTypeDefinition<?>                typeDefinition  ,
		final PersistenceTypeHandlerReflective<Binary, T> typeHandler     ,
		final BinaryValueSetter[]                         valueTranslators,
		final long[]                                      targetOffsets
	)
	{
		super(typeDefinition, typeHandler, valueTranslators, targetOffsets);
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
	public final T create(final Binary rawData)
	{
		return this.typeHandler().create(rawData);
	}

	@Override
	public final void update(final Binary rawData, final T instance, final SwizzleBuildLinker builder)
	{
		/*
		 * Explicite type check to avoid memory getting overwritten with bytes not fitting to the actual type.
		 * This can be especially critical if a custom roo resolver returns an instance that does not match
		 * the type defined by the typeId.
		 */
		if(!this.type().isInstance(instance))
		{
			throw new TypeCastException(this.type(), instance);
		}

		BinaryPersistence.updateFixedSize(
			instance                  ,
			this.valueTranslators()   ,
			this.targetOffsets()      ,
			rawData.buildItemAddress(),
			builder
		);
	}

	@Override
	public final void complete(final Binary medium, final T instance, final SwizzleBuildLinker builder)
	{
		// no-op for reflective logic
	}
	
}
