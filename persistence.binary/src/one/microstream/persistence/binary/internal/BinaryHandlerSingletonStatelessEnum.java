package one.microstream.persistence.binary.internal;

import static one.microstream.X.notNull;

import one.microstream.X;
import one.microstream.collections.Singleton;
import one.microstream.collections.types.XGettingEnum;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;
import one.microstream.persistence.types.PersistenceTypeDefinitionMember;
import one.microstream.persistence.types.PersistenceTypeDefinitionMemberEnumConstant;
import one.microstream.persistence.types.PersistenceTypeHandler;
import one.microstream.reflect.XReflect;

public final class BinaryHandlerSingletonStatelessEnum<T> extends AbstractBinaryHandlerTrivial<T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static boolean isSingletonEnumType(final Class<?> type)
	{
		return XReflect.isEnum(type) && type.getEnumConstants().length == 1;
	}
	
	public static <T> Class<T> validateIsSingletonEnumType(final Class<T> type)
	{
		if(isSingletonEnumType(type))
		{
			return type;
		}
		
		throw new IllegalArgumentException("Not a singleton Enum type: " + type);
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Enum<T>> BinaryHandlerSingletonStatelessEnum<T> New(final Class<?> type)
	{
		return new BinaryHandlerSingletonStatelessEnum<>(
			(Class<T>)XReflect.validateIsEnum(type)
		);
	}
	
	private final Singleton<PersistenceTypeDefinitionMemberEnumConstant> enumConstantMember;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	protected BinaryHandlerSingletonStatelessEnum(final Class<T> type)
	{
		super(validateIsSingletonEnumType(type));
		
		// the notNull is very important to detect incompatibility issues with other JVMs.
		this.enumConstantMember = X.Singleton(
			notNull(BinaryHandlerGenericEnum.deriveEnumConstantMembers(type).get())
		);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public final Object[] collectEnumConstants()
	{
		// single enum constant has already been validated by constructor logic
		return Persistence.collectEnumConstants(this);
	}
	
	@Override
	public final XGettingEnum<? extends PersistenceTypeDefinitionMember> allMembers()
	{
		return this.enumConstantMember;
	}

	@Override
	public final void store(
		final Binary                          data    ,
		final T                               instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		data.storeEntityHeader(0, this.typeId(), objectId);
	}

	@SuppressWarnings("unchecked")
	@Override
	public final T create(final Binary data, final PersistenceLoadHandler handler)
	{
		return (T)XReflect.getDeclaredEnumClass(this.type()).getEnumConstants()[0];
	}
	
	@Override
	public final synchronized PersistenceTypeHandler<Binary, T> initialize(final long typeId)
	{
		// debug hook
		return super.initialize(typeId);
	}
	
}
