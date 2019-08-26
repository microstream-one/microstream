package one.microstream.persistence.binary.types;

import one.microstream.persistence.binary.internal.BinaryHandlerGenericEnum;
import one.microstream.persistence.types.PersistenceLegacyTypeHandlingListener;
import one.microstream.persistence.types.PersistenceObjectIdResolver;
import one.microstream.persistence.types.PersistenceTypeDefinition;
import one.microstream.persistence.types.PersistenceTypeHandlerReflective;
import one.microstream.reflect.XReflect;


public abstract class AbstractBinaryLegacyTypeHandlerGenericEnum<T>
extends AbstractBinaryLegacyTypeHandlerReflective<T>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	// offsets must be determined per handler instance since different types have different persistent form offsets.
	private final long binaryOffsetName   ;
	private final long binaryOffsetOrdinal;
	private final Integer[] ordinalMapping;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	AbstractBinaryLegacyTypeHandlerGenericEnum(
		final PersistenceTypeDefinition                     typeDefinition  ,
		final PersistenceTypeHandlerReflective<Binary, T>   typeHandler     ,
		final BinaryValueSetter[]                           valueTranslators,
		final long[]                                        targetOffsets   ,
		final Integer[]                                     ordinalMapping  ,
		final PersistenceLegacyTypeHandlingListener<Binary> listener        ,
		final boolean                                       switchByteOrder
	)
	{
		super(typeDefinition, typeHandler, valueTranslators, targetOffsets, listener, switchByteOrder);
		this.ordinalMapping      = ordinalMapping;
		this.binaryOffsetName    = BinaryHandlerGenericEnum.calculateBinaryOffsetName(typeDefinition);
		this.binaryOffsetOrdinal = BinaryHandlerGenericEnum.calculateBinaryOffsetOrdinal(typeDefinition);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	public int getOrdinal(final Binary bytes)
	{
		return bytes.get_int(this.binaryOffsetOrdinal);
	}
	
	public String getName(final Binary bytes, final PersistenceObjectIdResolver idResolver)
	{
		return (String)idResolver.lookupObject(bytes.get_long(this.binaryOffsetName));
	}
	
	@Override
	protected T internalCreate(final Binary bytes, final PersistenceObjectIdResolver idResolver)
	{
		final Integer mappedOrdinal = this.ordinalMapping[this.getOrdinal(bytes)];
		if(mappedOrdinal == null)
		{
			// enum constant intentionally deleted, return null as instance (effectively "deleting" it on load)
			return null;
		}
		
		final Object enumConstantInstance = XReflect.getEnumConstantInstance(this.type(), mappedOrdinal.intValue());
		
		/*
		 * Can't validate here since the name String instance might not have been created, yet. See #update.
		 * Nevertheless:
		 * - the enum constants storing order must be assumed to be consistent with the type dictionary constants names.
		 * - the type dictionary constants names are validated against the current runtime type.
		 * These two aspects in combination ensure that the correct enum constant instance is selected.
		 * 
		 * Mismatches between persistent form and runtime type must be handled via a LegacyTypeHandler, not here.
		 */
		
		/*
		 * Required for AIC-like special subclass enums constants:
		 * The instance is actually of type T, but it is stored in a "? super T" array of its parent enum type.
		 */
		@SuppressWarnings("unchecked")
		final T enumConstantinstance = (T)enumConstantInstance;
		
		return enumConstantinstance;
	}
	
	// (26.08.2019 TM)FIXME: priv#23: what about update here? nothing? already handled?
	
}
