package net.jadoth.persistence.binary.types;

import static net.jadoth.X.notNull;

import net.jadoth.collections.types.XGettingTable;
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
		final PersistenceTypeDefinition<T>                typeDefinition              ,
		final PersistenceTypeHandlerReflective<Binary, T> typeHandler                 ,
		final XGettingTable<BinaryValueTranslator, Long>  translatorsWithTargetOffsets
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
		final PersistenceTypeDefinition<T>                typeDefinition  ,
		final PersistenceTypeHandlerReflective<Binary, T> typeHandler     ,
		final BinaryValueTranslator[]                     valueTranslators,
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
		// (20.09.2018 TM)FIXME: OGS-3: copy other logic from Generic handler
		
		final BinaryValueTranslator[] translators   = this.valueTranslators;
		final int                     length        = translators.length   ;
		final long[]                  targetOffsets = this.targetOffsets   ;
				
		long srcAddress = rawData.entityContentAddress;
		for(int i = 0; i < length; i++)
		{
			srcAddress = translators[i].translateValue(srcAddress, instance, targetOffsets[i], builder);
		}
	}

	@Override
	public final void complete(final Binary medium, final T instance, final SwizzleBuildLinker builder)
	{
		// no-op for reflective logic
	}
	
}
