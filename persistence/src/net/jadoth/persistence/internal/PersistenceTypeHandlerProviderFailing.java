package net.jadoth.persistence.internal;

import java.util.function.Consumer;

import net.jadoth.persistence.exceptions.PersistenceExceptionConsistency;
import net.jadoth.persistence.exceptions.PersistenceExceptionTypeHandlerConsistencyUnhandledTypeId;
import net.jadoth.persistence.exceptions.PersistenceExceptionTypeNotPersistable;
import net.jadoth.persistence.types.PersistenceTypeHandler;
import net.jadoth.persistence.types.PersistenceTypeHandlerProvider;
import net.jadoth.persistence.types.PersistenceTypeLink;

/**
 * Trivial implementation that throws a {@link PersistenceExceptionTypeNotPersistable} for every type.
 * <p>
 * Useful if only pre-registered types shall be handleable and every unhandled type shall be indicated to be
 * unpersistable.
 *
 * @author Thomas Muenz
 *
 * @param <M>
 */
public class PersistenceTypeHandlerProviderFailing<M> implements PersistenceTypeHandlerProvider<M>
{
	@Override
	public <T> PersistenceTypeHandler<M, T> provideTypeHandler(final Class<T> type)
		throws PersistenceExceptionTypeNotPersistable
	{
		throw new PersistenceExceptionTypeNotPersistable(type);
	}

	@Override
	public long ensureTypeId(final Class<?> type)
	{
		throw new PersistenceExceptionTypeNotPersistable(type);
	}

	@Override
	public Class<?> ensureType(final long typeId)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public long currentTypeId()
	{
		/*
		 * This is not an API misdesign abuse of this exception (like in the JDK), but
		 * rather this implementation actually does not support that operation.
		 */
		throw new UnsupportedOperationException();
	}

	@Override
	public long lookupTypeId(final Class<?> type)
	{
		throw new PersistenceExceptionTypeNotPersistable(type);
	}

	@Override
	public <T> Class<T> lookupType(final long typeId)
	{
		throw new PersistenceExceptionTypeHandlerConsistencyUnhandledTypeId(typeId);
	}
	
	@Override
	public boolean validateTypeMapping(final long typeId, final Class<?> type)
		throws PersistenceExceptionConsistency
	{
		/*
		 * This is not an API misdesign abuse of this exception (like in the JDK), but
		 * rather this implementation actually does not support that operation.
		 */
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean validateTypeMappings(final Iterable<? extends PersistenceTypeLink> mappings)
		throws PersistenceExceptionConsistency
	{
		/*
		 * This is not an API misdesign abuse of this exception (like in the JDK), but
		 * rather this implementation actually does not support that operation.
		 */
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean registerType(final long tid, final Class<?> type) throws PersistenceExceptionConsistency
	{
		/*
		 * This is not an API misdesign abuse of this exception (like in the JDK), but
		 * rather this implementation actually does not support that operation.
		 */
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean registerTypes(final Iterable<? extends PersistenceTypeLink> types)
		throws PersistenceExceptionConsistency
	{
		/*
		 * This is not an API misdesign abuse of this exception (like in the JDK), but
		 * rather this implementation actually does not support that operation.
		 */
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateCurrentHighestTypeId(final long highestTypeId)
	{
		/*
		 * This is not an API misdesign abuse of this exception (like in the JDK), but
		 * rather this implementation actually does not support that operation.
		 */
		throw new UnsupportedOperationException();
	}
	
	@Override
	public final <T> PersistenceTypeHandler<M, T> ensureTypeHandler(final Class<T> type)
		throws PersistenceExceptionTypeNotPersistable
	{
		/*
		 * This is not an API misdesign abuse of this exception (like in the JDK), but
		 * rather this implementation actually does not support that operation.
		 */
		throw new UnsupportedOperationException();
	}
	
	@Override
	public final <C extends Consumer<? super PersistenceTypeHandler<M, ?>>> C iterateTypeHandlers(final C iterator)
	{
		/*
		 * This is not an API misdesign abuse of this exception (like in the JDK), but
		 * rather this implementation actually does not support that operation.
		 */
		throw new UnsupportedOperationException();
	}

}
