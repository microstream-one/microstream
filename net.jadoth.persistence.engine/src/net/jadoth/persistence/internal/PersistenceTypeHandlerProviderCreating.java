package net.jadoth.persistence.internal;

import static net.jadoth.Jadoth.notNull;

import java.lang.reflect.Field;

import net.jadoth.collections.types.XGettingSequence;
import java.util.function.Consumer;
import net.jadoth.persistence.exceptions.PersistenceExceptionTypeNotPersistable;
import net.jadoth.persistence.types.PersistenceTypeHandler;
import net.jadoth.persistence.types.PersistenceTypeHandlerCreator;
import net.jadoth.persistence.types.PersistenceTypeHandlerCreatorLookup;
import net.jadoth.persistence.types.PersistenceTypeHandlerManager;
import net.jadoth.persistence.types.PersistenceTypeHandlerProvider;
import net.jadoth.persistence.types.PersistenceTypeSovereignty;
import net.jadoth.swizzling.exceptions.SwizzleExceptionConsistency;
import net.jadoth.swizzling.types.SwizzleTypeLink;
import net.jadoth.swizzling.types.SwizzleTypeManager;

public final class PersistenceTypeHandlerProviderCreating<M> implements PersistenceTypeHandlerProvider<M>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	private final SwizzleTypeManager                     typeManager             ;
	private final PersistenceTypeHandlerCreatorLookup<M> typeHandlerCreatorLookup;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public PersistenceTypeHandlerProviderCreating(
		final SwizzleTypeManager                     typeManager             ,
		final PersistenceTypeHandlerCreatorLookup<M> typeHandlerCreatorLookup
	)
	{
		super();
		this.typeManager              = notNull(typeManager)             ;
		this.typeHandlerCreatorLookup = notNull(typeHandlerCreatorLookup);
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	protected final <T> PersistenceTypeHandler<M, T> provideTypeHandler(
		final PersistenceTypeHandlerManager<M> typeHandlerManager,
		final Class<T>                         type              ,
		final long                             typeId
	)
		throws PersistenceExceptionTypeNotPersistable
	{
		final PersistenceTypeHandlerCreator<M> creator     = this.typeHandlerCreatorLookup.lookupCreator(type);
		final PersistenceTypeHandler<M, T>     typeHandler = creator.createTypeHandler(
			type,
			typeId,
			this.typeManager
		);

		typeHandlerManager.register(typeHandler);

		/* must ensure type handlers for all field types as well to keep type definitions consistent
		 * if some field's type is "too abstract" to be persisted, is has to be registered to an
		 * apropriate type handler (No-op, etc.) manually beforehand.
		 *
		 * creating new type handlers in the process will eventually end up here again for the new types
		 */
		typeHandler.getInstanceReferenceFields().iterate(new Consumer<Field>()
		{
			@Override
			public void accept(final Field e)
			{
				try
				{
					typeHandlerManager.ensureTypeHandler(e.getType());
				}
				catch(final Throwable t)
				{
					throw t; // debug hook
				}
			}
		});

		return typeHandler;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final PersistenceTypeSovereignty typeSovereignty()
	{
		return PersistenceTypeSovereignty.MASTER;
	}

	@Override
	public final <T> PersistenceTypeHandler<M, T> provideTypeHandler(
		final PersistenceTypeHandlerManager<M> typeHandlerManager,
		final Class<T>                         type
	)
	{
		// create type<->tid mapping in advance.
		final long typeId = this.typeManager.ensureTypeId(type);
		return this.provideTypeHandler(typeHandlerManager, type, typeId);
	}

	@Override
	public final <T> PersistenceTypeHandler<M, T> provideTypeHandler(
		final PersistenceTypeHandlerManager<M> typeHandlerManager,
		final long typeId
	)
	{
		// lookup type or throw exception.
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
	public final void validateTypeMapping(final long typeId, final Class<?> type)
	{
		this.typeManager.validateTypeMapping(typeId, type);
	}

	@Override
	public final void validateExistingTypeMappings(final XGettingSequence<? extends SwizzleTypeLink<?>> mappings)
		throws SwizzleExceptionConsistency
	{
		this.typeManager.validateExistingTypeMappings(mappings);
	}

	@Override
	public final void validatePossibleTypeMappings(final XGettingSequence<? extends SwizzleTypeLink<?>> mappings)
		throws SwizzleExceptionConsistency
	{
		this.typeManager.validatePossibleTypeMappings(mappings);
	}

	@Override
	public final void updateCurrentHighestTypeId(final long highestTypeId)
	{
		this.typeManager.updateCurrentHighestTypeId(highestTypeId);
	}

}
