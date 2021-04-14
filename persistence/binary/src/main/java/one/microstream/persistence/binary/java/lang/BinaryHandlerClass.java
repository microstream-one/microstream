package one.microstream.persistence.binary.java.lang;

import static one.microstream.X.notNull;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomValueFixedLength;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;
import one.microstream.persistence.types.PersistenceTypeDefinition;
import one.microstream.persistence.types.PersistenceTypeHandlerManager;
import one.microstream.reference.Referencing;

public final class BinaryHandlerClass extends AbstractBinaryHandlerCustomValueFixedLength<Class<?>, Long>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	@SuppressWarnings({"unchecked",  "rawtypes"})
	private static Class<Class<?>> handledType()
	{
		// no idea how to get ".class" to work otherwise
		return (Class)Class.class;
	}
	
	public static BinaryHandlerClass New(
		final Referencing<PersistenceTypeHandlerManager<Binary>> typeHandlerManager
	)
	{
		return new BinaryHandlerClass(
			notNull(typeHandlerManager)
		);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Referencing<PersistenceTypeHandlerManager<Binary>> typeHandlerManager;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerClass(
		final Referencing<PersistenceTypeHandlerManager<Binary>> typeHandlerManager
	)
	{
		super(handledType(), defineValueType(long.class));
		this.typeHandlerManager = typeHandlerManager;
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	private long instanceState(final Class<?> instance)
	{
		return this.typeHandlerManager.get().ensureTypeHandler(instance).typeId();
	}
	
	private static long binaryState(final Binary data)
	{
		return data.read_long(0);
	}

	@Override
	public final void store(
		final Binary                          data    ,
		final Class<?>                        instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		final long classTypeId = this.instanceState(instance);
		data.storeLong(
			this.typeId(),
			objectId,
			classTypeId
		);
	}

	@Override
	public final Class<?> create(final Binary data, final PersistenceLoadHandler handler)
	{
		final long typeId = binaryState(data);
		
		final PersistenceTypeDefinition typeDefinition = this.typeHandlerManager.get()
			.typeDictionary()
			.lookupTypeById(typeId)
		;
		
		// can be null for unmapped legacy types. Nothing to do about that, here. Let application logic decide/react.
		final Class<?> resolvedInstance = typeDefinition.type();
		
		return resolvedInstance;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// validation //
	///////////////
	
	// actually never called, just to satisfy the interface
	@Override
	public Long getValidationStateFromInstance(final Class<?> instance)
	{
		return this.instanceState(instance);
	}

	// actually never called, just to satisfy the interface
	@Override
	public Long getValidationStateFromBinary(final Binary data)
	{
		return binaryState(data);
	}
	
	@Override
	public void validateState(
		final Binary                 data    ,
		final Class<?>               instance,
		final PersistenceLoadHandler handler
	)
	{
		final long instanceState = this.instanceState(instance);
		final long binaryState   = binaryState(data);
		
		if(instanceState == binaryState)
		{
			return;
		}
		
		this.throwInconsistentStateException(instance, instanceState, binaryState);
	}

}
