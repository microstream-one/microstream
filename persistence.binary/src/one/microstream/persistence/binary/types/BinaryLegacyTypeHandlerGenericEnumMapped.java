package one.microstream.persistence.binary.types;

import static one.microstream.X.mayNull;
import static one.microstream.X.notNull;

import one.microstream.collections.types.XGettingTable;
import one.microstream.persistence.types.PersistenceLegacyTypeHandler;
import one.microstream.persistence.types.PersistenceLegacyTypeHandlingListener;
import one.microstream.persistence.types.PersistenceObjectIdResolver;
import one.microstream.persistence.types.PersistenceTypeDefinition;
import one.microstream.persistence.types.PersistenceTypeHandler;


public class BinaryLegacyTypeHandlerGenericEnumMapped<T>
extends BinaryLegacyTypeHandlerGenericEnum<T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static <T> BinaryLegacyTypeHandlerGenericEnumMapped<T> New(
		final PersistenceTypeDefinition                     typeDefinition              ,
		final PersistenceTypeHandler<Binary, T>             typeHandler                 ,
		final XGettingTable<Long, BinaryValueSetter>        translatorsWithTargetOffsets,
		final Integer[]                                     ordinalMapping              ,
		final PersistenceLegacyTypeHandlingListener<Binary> listener                    ,
		final boolean                                       switchByteOrder
	)
	{
		return new BinaryLegacyTypeHandlerGenericEnumMapped<>(
			notNull(typeDefinition)                      ,
			notNull(typeHandler)                         ,
			toTranslators(translatorsWithTargetOffsets)  ,
			toTargetOffsets(translatorsWithTargetOffsets),
			notNull(ordinalMapping)                      ,
			mayNull(listener)                            ,
			switchByteOrder
		);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final Integer[] ordinalMapping;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	BinaryLegacyTypeHandlerGenericEnumMapped(
		final PersistenceTypeDefinition                     typeDefinition  ,
		final PersistenceTypeHandler<Binary, T>             typeHandler     ,
		final BinaryValueSetter[]                           valueTranslators,
		final long[]                                        targetOffsets   ,
		final Integer[]                                     ordinalMapping  ,
		final PersistenceLegacyTypeHandlingListener<Binary> listener        ,
		final boolean                                       switchByteOrder
	)
	{
		super(typeDefinition, typeHandler, valueTranslators, targetOffsets, listener, switchByteOrder);
		this.ordinalMapping = ordinalMapping;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	
	@Override
	protected T internalCreate(final Binary bytes, final PersistenceObjectIdResolver idResolver)
	{
		return PersistenceLegacyTypeHandler.resolveEnumConstant(this, bytes, this.ordinalMapping);
	}
	
}
