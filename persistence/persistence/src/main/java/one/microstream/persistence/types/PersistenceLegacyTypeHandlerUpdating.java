package one.microstream.persistence.types;

import java.util.function.Consumer;

import org.slf4j.Logger;

import one.microstream.collections.types.XGettingEnum;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLegacyTypeHandler;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceReferenceLoader;
import one.microstream.persistence.types.PersistenceTypeDefinitionMember;
import one.microstream.persistence.types.Storer;
import one.microstream.util.logging.Logging;

/**
 * A PersistenceLegacyTypeHandler that updates the persisted state of a legacy type after
 * loading. This implementation wraps the original type handler.
 * To apply that handler, the related Creator {@link PersistenceLegacyTypeHandlerUpdatingCreator} has to be setup in the
 * StorageConnectionFoundation.
 * 
 *
 * @param <D> The storage data type.
 * @param <T> The type processed by the PersistenceLegacyTypeHandler.
 */
public class PersistenceLegacyTypeHandlerUpdating<D, T> implements PersistenceLegacyTypeHandler<D, T>
{

	private final static Logger logger = Logging.getLogger(PersistenceLegacyTypeHandlerUpdating.class);
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final PersistenceLegacyTypeHandler<D, T> typeHandler;

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceLegacyTypeHandlerUpdating(final PersistenceLegacyTypeHandler<D, T> typeHandler)
	{
		super();
		this.typeHandler = typeHandler;
	}
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	
	@Override
	public Class<D> dataType()
	{
		return this.typeHandler.dataType();
	}

	@Override
	public Class<T> type()
	{
		return this.typeHandler.type();
	}

	@Override
	public XGettingEnum<? extends PersistenceTypeDefinitionMember> allMembers()
	{
		return this.typeHandler.allMembers();
	}

	@Override
	public XGettingEnum<? extends PersistenceTypeDefinitionMember> instanceMembers()
	{
		return this.typeHandler.instanceMembers();
	}

	@Override
	public void iterateInstanceReferences(final T instance, final PersistenceFunction iterator)
	{
		this.typeHandler.iterateInstanceReferences(instance, iterator);
	}

	@Override
	public void iterateLoadableReferences(final D data, final PersistenceReferenceLoader iterator)
	{
		this.typeHandler.iterateLoadableReferences(data, iterator);
	}

	@Override
	public T create(final D data, final PersistenceLoadHandler handler) {
		return this.typeHandler.create(data, handler);
	}

	@Override
	public void updateState(final D data, final T instance, final PersistenceLoadHandler handler)
	{
		this.typeHandler.updateState(data, instance, handler);
	}

	@Override
	public void complete(final D data, final T instance, final PersistenceLoadHandler handler)
	{
		this.typeHandler.complete(data, instance, handler);
		
		logger.debug("persisting new state of legacy mapped type", instance.getClass());
		//ensure lazy storer, don't update other instances...
		final Storer s = handler.getPersister().createLazyStorer();
		s.store(instance);
		s.commit();
	}

	@Override
	public <C extends Consumer<? super Class<?>>> C iterateMemberTypes(final C logic)
	{
		return this.typeHandler.iterateMemberTypes(logic);
	}

	@Override
	public long typeId()
	{
		return this.typeHandler.typeId();
	}

	@Override
	public String typeName()
	{
		return this.typeHandler.typeName();
	}

	@Override
	public boolean hasPersistedReferences()
	{
		return this.typeHandler.hasPersistedReferences();
	}

	@Override
	public long membersPersistedLengthMinimum()
	{
		return this.typeHandler.membersPersistedLengthMinimum();
	}

	@Override
	public long membersPersistedLengthMaximum() {
		return this.typeHandler.membersPersistedLengthMaximum();
	}

	@Override
	public boolean isPrimitiveType() {
		return this.typeHandler.isPrimitiveType();
	}

	@Override
	public boolean hasVaryingPersistedLengthInstances()
	{
		return this.typeHandler.hasVaryingPersistedLengthInstances();
	}
			
}
