package one.microstream.java.lang;

import static one.microstream.X.notNull;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomValueFixedLength;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceRefactoringResolverProvider;
import one.microstream.persistence.types.PersistenceStoreHandler;
import one.microstream.persistence.types.PersistenceTypeDefinition;
import one.microstream.persistence.types.PersistenceTypeHandler;
import one.microstream.persistence.types.PersistenceTypeHandlerManager;
import one.microstream.reference.Referencing;

public final class BinaryHandlerClass extends AbstractBinaryHandlerCustomValueFixedLength<Class<?>>
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
		final Referencing<PersistenceTypeHandlerManager<Binary>> typeHandlerManager         ,
		final PersistenceRefactoringResolverProvider             refactoringResolverProvider
	)
	{
		return new BinaryHandlerClass(
			notNull(typeHandlerManager)         ,
			notNull(refactoringResolverProvider)
		);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Referencing<PersistenceTypeHandlerManager<Binary>> typeHandlerManager         ;
	private final PersistenceRefactoringResolverProvider             refactoringResolverProvider;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerClass(
		final Referencing<PersistenceTypeHandlerManager<Binary>> typeHandlerManager         ,
		final PersistenceRefactoringResolverProvider             refactoringResolverProvider
	)
	{
		super(handledType(), defineValueType(long.class));
		this.typeHandlerManager          = typeHandlerManager         ;
		this.refactoringResolverProvider = refactoringResolverProvider;
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final void store(
		final Binary                  bytes   ,
		final Class<?>                instance,
		final long                    objectId,
		final PersistenceStoreHandler handler
	)
	{
		final PersistenceTypeHandler<?, ?> typeTypeHandler = this.typeHandlerManager.get().ensureTypeHandler(instance);
		bytes.storeLong(
			this.typeId(),
			objectId,
			typeTypeHandler.typeId()
		);
	}

	@Override
	public final Class<?> create(final Binary bytes, final PersistenceLoadHandler handler)
	{
		final long typeId = bytes.get_long(0);
		
		final PersistenceTypeDefinition typeDefinition = this.typeHandlerManager.get()
			.typeDictionary()
			.lookupTypeById(typeId)
		;
		final Class<?> resolvedInstance = this.refactoringResolverProvider.provideResolver()
			.resolveRuntimeType(typeDefinition)
		;
		
		return resolvedInstance;
	}

}
