package one.microstream.persistence.binary.types;

import static one.microstream.X.mayNull;
import static one.microstream.X.notNull;

import one.microstream.collections.types.XGettingTable;
import one.microstream.persistence.binary.internal.BinaryHandlerGenericEnum;
import one.microstream.persistence.types.PersistenceLegacyTypeHandlingListener;
import one.microstream.persistence.types.PersistenceObjectIdResolver;
import one.microstream.persistence.types.PersistenceTypeDefinition;
import one.microstream.persistence.types.PersistenceTypeHandler;
import one.microstream.reflect.XReflect;


public class BinaryLegacyTypeHandlerGenericEnum<T>
extends AbstractBinaryLegacyTypeHandlerReflective<T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static <T> BinaryLegacyTypeHandlerGenericEnum<T> New(
		final PersistenceTypeDefinition                     typeDefinition              ,
		final PersistenceTypeHandler<Binary, T>             typeHandler                 ,
		final XGettingTable<Long, BinaryValueSetter>        translatorsWithTargetOffsets,
		final PersistenceLegacyTypeHandlingListener<Binary> listener                    ,
		final boolean                                       switchByteOrder
	)
	{
		return new BinaryLegacyTypeHandlerGenericEnum<>(
			notNull(typeDefinition)                      ,
			notNull(typeHandler)                         ,
			toTranslators(translatorsWithTargetOffsets)  ,
			toTargetOffsets(translatorsWithTargetOffsets),
			mayNull(listener)                            ,
			switchByteOrder
		);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	// offsets must be determined per handler instance since different types have different persistent form offsets.
	private final long binaryOffsetOrdinal;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	BinaryLegacyTypeHandlerGenericEnum(
		final PersistenceTypeDefinition                     typeDefinition  ,
		final PersistenceTypeHandler<Binary, T>             typeHandler     ,
		final BinaryValueSetter[]                           valueTranslators,
		final long[]                                        targetOffsets   ,
		final PersistenceLegacyTypeHandlingListener<Binary> listener        ,
		final boolean                                       switchByteOrder
	)
	{
		super(typeDefinition, typeHandler, valueTranslators, targetOffsets, listener, switchByteOrder);
		this.binaryOffsetOrdinal = BinaryHandlerGenericEnum.calculateBinaryOffsetOrdinal(typeDefinition);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	// note on initializing methods: exluding the java.lang.Enum fields must already be excluded in valueTranslators
	
	public int getOrdinal(final Binary bytes)
	{
		return bytes.get_int(this.binaryOffsetOrdinal);
	}
	
	@Override
	protected T internalCreate(final Binary bytes, final PersistenceObjectIdResolver idResolver)
	{
		return XReflect.resolveEnumConstantInstanceTyped(this.type(), this.getOrdinal(bytes));
	}
	
	@Override
	public void update(final Binary rawData, final T instance, final PersistenceObjectIdResolver idResolver)
	{
		// debug hook
		super.update(rawData, instance, idResolver);
	}
	
}
