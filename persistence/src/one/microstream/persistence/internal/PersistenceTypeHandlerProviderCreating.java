package one.microstream.persistence.internal;

import static one.microstream.X.notNull;

import java.util.function.Consumer;

import one.microstream.persistence.exceptions.PersistenceExceptionConsistency;
import one.microstream.persistence.exceptions.PersistenceExceptionTypeNotPersistable;
import one.microstream.persistence.types.PersistenceDataTypeHolder;
import one.microstream.persistence.types.PersistenceLegacyTypeHandler;
import one.microstream.persistence.types.PersistenceTypeHandler;
import one.microstream.persistence.types.PersistenceTypeHandlerEnsurer;
import one.microstream.persistence.types.PersistenceTypeHandlerProvider;
import one.microstream.persistence.types.PersistenceTypeLink;
import one.microstream.persistence.types.PersistenceTypeManager;

public final class PersistenceTypeHandlerProviderCreating<D>
extends PersistenceDataTypeHolder.Default<D>
implements PersistenceTypeHandlerProvider<D>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static <D> PersistenceTypeHandlerProviderCreating<D> New(
		final Class<D>                         dataType          ,
		final PersistenceTypeManager           typeManager       ,
		final PersistenceTypeHandlerEnsurer<D> typeHandlerEnsurer
	)
	{
		return new PersistenceTypeHandlerProviderCreating<>(
			notNull(dataType)          ,
			notNull(typeManager)       ,
			notNull(typeHandlerEnsurer)
		);
	}

	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final PersistenceTypeManager           typeManager       ;
	private final PersistenceTypeHandlerEnsurer<D> typeHandlerEnsurer;

		

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	PersistenceTypeHandlerProviderCreating(
		final Class<D>                         dataType          ,
		final PersistenceTypeManager           typeManager       ,
		final PersistenceTypeHandlerEnsurer<D> typeHandlerEnsurer
	)
	{
		super(dataType);
		this.typeManager        = typeManager       ;
		this.typeHandlerEnsurer = typeHandlerEnsurer;
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final <T> PersistenceTypeHandler<D, ? super T> provideTypeHandler(final Class<T> type)
	{
		/*
		 * The recursive member field type handler creation comes AFTER this method,
		 * so no need to call typeId ensuring (allocation&assignment) in advance as it was before
		 * (see old code below)
		 */
		final PersistenceTypeHandler<D, ? super T> lookedUpTypeHandler = this.ensureTypeHandler(type);
		
		final PersistenceTypeHandler<D, ? super T> typeHandler;
		if(type == lookedUpTypeHandler.type())
		{
			final long typeId = this.typeManager.ensureTypeId(type);
			typeHandler = lookedUpTypeHandler.initialize(typeId);
		}
		else
		{
			// "abstract type" type handlers are just mapped. No need for an assigned typeId.
			typeHandler = lookedUpTypeHandler;
		}
		
		return typeHandler;
		
		// (06.04.2020 TM)NOTE: old logic before "abstract type" TypeHandlers (e.g. java.nio.file.Path)
//		// type<->tid mapping is created in advance.
//		final long typeId = this.typeManager.ensureTypeId(type);
//
//		final PersistenceTypeHandler<D, ? super T> protoTypeHandler = this.ensureTypeHandler(type);
//		final PersistenceTypeHandler<D, ? super T> typeHandler      = protoTypeHandler.initialize(typeId);
//
//		return typeHandler;
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
	public final <T> PersistenceTypeHandler<D, ? super T> ensureTypeHandler(final Class<T> type)
		throws PersistenceExceptionTypeNotPersistable
	{
		return this.typeHandlerEnsurer.ensureTypeHandler(type);
	}
	
	@Override
	public final <C extends Consumer<? super PersistenceTypeHandler<D, ?>>> C iterateTypeHandlers(final C iterator)
	{
		return this.typeHandlerEnsurer.iterateTypeHandlers(iterator);
	}
	
	@Override
	public <C extends Consumer<? super PersistenceLegacyTypeHandler<D, ?>>> C iterateLegacyTypeHandlers(final C iterator)
	{
		return this.typeHandlerEnsurer.iterateLegacyTypeHandlers(iterator);
	}
	
	@Override
	public <C extends Consumer<? super PersistenceTypeHandler<D, ?>>> C iterateAllTypeHandlers(final C iterator)
	{
		return this.typeHandlerEnsurer.iterateAllTypeHandlers(iterator);
	}

}
