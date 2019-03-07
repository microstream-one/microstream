package one.microstream.persistence.internal;

import static one.microstream.X.notNull;

import java.util.function.Consumer;

import one.microstream.persistence.exceptions.PersistenceExceptionConsistency;
import one.microstream.persistence.exceptions.PersistenceExceptionTypeNotPersistable;
import one.microstream.persistence.types.PersistenceTypeHandler;
import one.microstream.persistence.types.PersistenceTypeHandlerEnsurer;
import one.microstream.persistence.types.PersistenceTypeHandlerProvider;
import one.microstream.persistence.types.PersistenceTypeLink;
import one.microstream.persistence.types.PersistenceTypeManager;

public final class PersistenceTypeHandlerProviderCreating<M> implements PersistenceTypeHandlerProvider<M>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static <M> PersistenceTypeHandlerProviderCreating<M> New(
		final PersistenceTypeManager               typeManager       ,
		final PersistenceTypeHandlerEnsurer<M> typeHandlerEnsurer
	)
	{
		return new PersistenceTypeHandlerProviderCreating<>(
			notNull(typeManager)       ,
			notNull(typeHandlerEnsurer)
		);
	}

	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	private final PersistenceTypeManager               typeManager       ;
	private final PersistenceTypeHandlerEnsurer<M> typeHandlerEnsurer;

		

	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	PersistenceTypeHandlerProviderCreating(
		final PersistenceTypeManager               typeManager       ,
		final PersistenceTypeHandlerEnsurer<M> typeHandlerEnsurer
	)
	{
		super();
		this.typeManager        = typeManager       ;
		this.typeHandlerEnsurer = typeHandlerEnsurer;
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	protected final <T> PersistenceTypeHandler<M, T> provideTypeHandler(
		final Class<T> type  ,
		final long     typeId
	)
		throws PersistenceExceptionTypeNotPersistable
	{
		final PersistenceTypeHandler<M, T> protoTypeHandler = this.ensureTypeHandler(type);
		final PersistenceTypeHandler<M, T> typeHandler      = protoTypeHandler.initializeTypeId(typeId);

		return typeHandler;
	}

	@Override
	public final <T> PersistenceTypeHandler<M, T> provideTypeHandler(final Class<T> type)
	{
		// type<->tid mapping is created in advance.
		final long typeId = this.typeManager.ensureTypeId(type);
		
		return this.provideTypeHandler(type, typeId);
	}

	@Override
	public final long ensureTypeId(final Class<?> type)
	{
		return this.typeManager.ensureTypeId(type);
	}

	@Override
	public final Class<?> ensureType(final long typeId)
	{
		return this.typeManager.ensureType(typeId);
	}

	@Override
	public final long currentTypeId()
	{
		return this.typeManager.currentTypeId();
	}

	@Override
	public final boolean registerType(final long tid, final Class<?> type) throws PersistenceExceptionConsistency
	{
		return this.typeManager.registerType(tid, type);
	}

	@Override
	public final long lookupTypeId(final Class<?> type)
	{
		return this.typeManager.lookupTypeId(type);
	}

	@Override
	public final <T> Class<T> lookupType(final long typeId)
	{
		return this.typeManager.lookupType(typeId);
	}
	
	@Override
	public boolean validateTypeMapping(final long typeId, final Class<?> type) throws PersistenceExceptionConsistency
	{
		return this.typeManager.validateTypeMapping(typeId, type);
	}
	
	@Override
	public boolean validateTypeMappings(final Iterable<? extends PersistenceTypeLink> mappings)
		throws PersistenceExceptionConsistency
	{
		return this.typeManager.validateTypeMappings(mappings);
	}
		
	@Override
	public boolean registerTypes(final Iterable<? extends PersistenceTypeLink> types)
		throws PersistenceExceptionConsistency
	{
		return this.typeManager.registerTypes(types);
	}

	@Override
	public final void updateCurrentHighestTypeId(final long highestTypeId)
	{
		this.typeManager.updateCurrentHighestTypeId(highestTypeId);
	}
	
	@Override
	public final <T> PersistenceTypeHandler<M, T> ensureTypeHandler(final Class<T> type)
		throws PersistenceExceptionTypeNotPersistable
	{
		return this.typeHandlerEnsurer.ensureTypeHandler(type);
	}
	
	@Override
	public final <C extends Consumer<? super PersistenceTypeHandler<M, ?>>> C iterateTypeHandlers(final C iterator)
	{
		return this.typeHandlerEnsurer.iterateTypeHandlers(iterator);
	}

}
