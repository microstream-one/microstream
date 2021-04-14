package one.microstream.persistence.internal;

import java.util.function.Consumer;

import one.microstream.persistence.exceptions.PersistenceExceptionConsistency;
import one.microstream.persistence.exceptions.PersistenceExceptionTypeHandlerConsistencyUnhandledTypeId;
import one.microstream.persistence.exceptions.PersistenceExceptionTypeNotPersistable;
import one.microstream.persistence.types.PersistenceDataTypeHolder;
import one.microstream.persistence.types.PersistenceLegacyTypeHandler;
import one.microstream.persistence.types.PersistenceTypeHandler;
import one.microstream.persistence.types.PersistenceTypeHandlerProvider;
import one.microstream.persistence.types.PersistenceTypeLink;

/**
 * Trivial implementation that throws a {@link PersistenceExceptionTypeNotPersistable} for every type.
 * <p>
 * Useful if only pre-registered types shall be handleable and every unhandled type shall be indicated to be
 * unpersistable.
 *
 * 
 *
 * @param <D>
 */
public class PersistenceTypeHandlerProviderFailing<D>
extends PersistenceDataTypeHolder.Default<D>
implements PersistenceTypeHandlerProvider<D>
{
	@Override
	public <T> PersistenceTypeHandler<D, T> provideTypeHandler(final Class<T> type)
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
	public final <T> PersistenceTypeHandler<D, T> ensureTypeHandler(final Class<T> type)
		throws PersistenceExceptionTypeNotPersistable
	{
		/*
		 * This is not an API misdesign abuse of this exception (like in the JDK), but
		 * rather this implementation actually does not support that operation.
		 */
		throw new UnsupportedOperationException();
	}
	
	@Override
	public final <C extends Consumer<? super PersistenceTypeHandler<D, ?>>> C iterateTypeHandlers(final C iterator)
	{
		/*
		 * This is not an API OOP misdesign abuse of this exception (like in the JDK), but
		 * rather this implementation actually does not support that operation.
		 */
		throw new UnsupportedOperationException();
	}
	
	@Override
	public <C extends Consumer<? super PersistenceLegacyTypeHandler<D, ?>>> C iterateLegacyTypeHandlers(final C iterator)
	{
		/*
		 * This is not an API OOP misdesign abuse of this exception (like in the JDK), but
		 * rather this implementation actually does not support that operation.
		 */
		throw new UnsupportedOperationException();
	}
	
	@Override
	public <C extends Consumer<? super PersistenceTypeHandler<D, ?>>> C iterateAllTypeHandlers(final C iterator)
	{
		/*
		 * This is not an API OOP misdesign abuse of this exception (like in the JDK), but
		 * rather this implementation actually does not support that operation.
		 */
		throw new UnsupportedOperationException();
	}
	

	PersistenceTypeHandlerProviderFailing(final Class<D> dataType)
	{
		super(dataType);
	}

}
