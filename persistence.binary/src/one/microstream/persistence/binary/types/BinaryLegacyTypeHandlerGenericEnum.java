package one.microstream.persistence.binary.types;

import static one.microstream.X.mayNull;
import static one.microstream.X.notNull;

import one.microstream.collections.types.XGettingEnum;
import one.microstream.persistence.binary.internal.BinaryHandlerGenericEnum;
import one.microstream.persistence.types.PersistenceLegacyTypeHandlingListener;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceTypeDefinition;
import one.microstream.persistence.types.PersistenceTypeHandler;
import one.microstream.reflect.XReflect;
import one.microstream.typing.KeyValue;


public class BinaryLegacyTypeHandlerGenericEnum<T>
extends AbstractBinaryLegacyTypeHandlerReflective<T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static <T> BinaryLegacyTypeHandlerGenericEnum<T> New(
		final PersistenceTypeDefinition                       typeDefinition              ,
		final PersistenceTypeHandler<Binary, T>               typeHandler                 ,
		final XGettingEnum<KeyValue<Long, BinaryValueSetter>> translatorsWithTargetOffsets,
		final PersistenceLegacyTypeHandlingListener<Binary>   listener                    ,
		final boolean                                         switchByteOrder
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

	public int getOrdinal(final Binary data)
	{
		return data.read_int(this.binaryOffsetOrdinal);
	}

	@Override
	protected T internalCreate(final Binary data, final PersistenceLoadHandler handler)
	{
		return XReflect.resolveEnumConstantInstanceTyped(this.type(), this.getOrdinal(data));
	}

	@Override
	public void updateState(final Binary rawData, final T instance, final PersistenceLoadHandler handler)
	{
		// debug hook
		super.updateState(rawData, instance, handler);
	}

}
