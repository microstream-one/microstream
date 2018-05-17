package net.jadoth.persistence.internal;

import static net.jadoth.X.notNull;

import net.jadoth.persistence.exceptions.PersistenceExceptionTypeNotPersistable;
import net.jadoth.persistence.types.PersistenceTypeHandler;
import net.jadoth.persistence.types.PersistenceTypeHandlerEnsurer;
import net.jadoth.persistence.types.PersistenceTypeHandlerManager;
import net.jadoth.persistence.types.PersistenceTypeHandlerProvider;
import net.jadoth.swizzling.exceptions.SwizzleExceptionConsistency;
import net.jadoth.swizzling.types.SwizzleTypeLink;
import net.jadoth.swizzling.types.SwizzleTypeManager;

public final class PersistenceTypeHandlerProviderCreating<M> implements PersistenceTypeHandlerProvider<M>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static <M> PersistenceTypeHandlerProviderCreating<M> New(
		final SwizzleTypeManager               typeManager       ,
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

	private final SwizzleTypeManager               typeManager       ;
	private final PersistenceTypeHandlerEnsurer<M> typeHandlerEnsurer;

		

	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	PersistenceTypeHandlerProviderCreating(
		final SwizzleTypeManager               typeManager       ,
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
		final PersistenceTypeHandlerManager<M> typeHandlerManager,
		final Class<T>                         type              ,
		final long                             typeId
	)
		throws PersistenceExceptionTypeNotPersistable
	{
		final PersistenceTypeHandler<M, T> protoTypeHandler = this.ensureTypeHandler(type);
		final PersistenceTypeHandler<M, T> typeHandler      = protoTypeHandler.initializeTypeId(typeId);
		
		// (14.05.2018 TM)FIXME: why is the register() call done here instead of in the ensureHandler logic inside the manager?
		typeHandlerManager.registerTypeHandler(typeHandler);

		/* must ensure type handlers for all field types as well to keep type definitions consistent
		 * if some field's type is "too abstract" to be persisted, is has to be registered to an
		 * apropriate type handler (No-op, etc.) manually beforehand.
		 *
		 * creating new type handlers in the process will eventually end up here again for the new types
		 */
		typeHandler.getInstanceReferenceFields().iterate(e ->
		{
			try
			{
				typeHandlerManager.ensureTypeHandler(e.getType());
			}
			catch(final Throwable t)
			{
				throw t; // debug hook
			}
		});

		return typeHandler;
	}

	@Override
	public final <T> PersistenceTypeHandler<M, T> provideTypeHandler(
		final PersistenceTypeHandlerManager<M> typeHandlerManager,
		final Class<T>                         type
	)
	{
		// type<->tid mapping is created in advance.
		final long typeId = this.typeManager.ensureTypeId(type);
		
		return this.provideTypeHandler(typeHandlerManager, type, typeId);
	}

	@Override
	public final <T> PersistenceTypeHandler<M, T> provideTypeHandler(
		final PersistenceTypeHandlerManager<M> typeHandlerManager,
		final long                             typeId
	)
	{
		// either the type can be found or an exception is thrown
		final Class<T> type = this.typeManager.ensureType(typeId);
		
		return this.provideTypeHandler(typeHandlerManager, type, typeId);
	}

	@Override
	public final long ensureTypeId(final Class<?> type)
	{
		return this.typeManager.ensureTypeId(type);
	}

	@Override
	public final <T> Class<T> ensureType(final long typeId)
	{
		return this.typeManager.ensureType(typeId);
	}

	@Override
	public final long currentTypeId()
	{
		return this.typeManager.currentTypeId();
	}

	@Override
	public final boolean registerType(final long tid, final Class<?> type) throws SwizzleExceptionConsistency
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
	public final void validateExistingTypeMapping(final long typeId, final Class<?> type)
	{
		this.typeManager.validateExistingTypeMapping(typeId, type);
	}

	@Override
	public final void validateExistingTypeMappings(final Iterable<? extends SwizzleTypeLink<?>> mappings)
		throws SwizzleExceptionConsistency
	{
		this.typeManager.validateExistingTypeMappings(mappings);
	}

	@Override
	public final void validatePossibleTypeMappings(final Iterable<? extends SwizzleTypeLink<?>> mappings)
		throws SwizzleExceptionConsistency
	{
		this.typeManager.validatePossibleTypeMappings(mappings);
	}

	@Override
	public final void updateCurrentHighestTypeId(final long highestTypeId)
	{
		this.typeManager.updateCurrentHighestTypeId(highestTypeId);
	}
	
	final <T> PersistenceTypeHandler<M, T> ensureTypeHandler(final Class<T> type)
		throws PersistenceExceptionTypeNotPersistable
	{
		return this.typeHandlerEnsurer.ensureTypeHandler(type);
	}
	
//	@Override
//	public final <C extends Consumer<? super PersistenceTypeHandler<M, ?>>> C iterateTypeHandlers(final C iterator)
//	{
//		return this.typeHandlerEnsurer.iterateTypeHandlers(iterator);
//	}

}
