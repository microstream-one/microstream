package one.microstream.persistence.types;

import static one.microstream.X.notNull;

import java.util.function.Consumer;


/**
 * This is, of course, not a handler that is unreachable, but a handler for a type whose instances are
 * (decided by the developer) no longer reachable in the entity graph, despite potentially still being present
 * in a live database, but not having been cleaned up, yet. However, this effectively means that the handler
 * itself should be kind of "unreachable", too, in the sense that it may never be necessary to access and use it.
 * Without such a handler, a class could never be removed at the design level without replacement
 * until the last instance of it has been cleaned up the the storage housekeeping. That is an unpleasent dependency.
 * The preferable way is to explicitly tell the typing system to "ignore" those types. That is done by registering
 * a kind of dummy type handler. That dummy handler is this type here.
 * 
 * @author TM
 *
 * @param <M>
 * @param <T>
 */
public interface PersistenceUnreachableTypeHandler<M, T> extends PersistenceLegacyTypeHandler<M, T>
{
	@Override
	public default boolean hasInstanceReferences()
	{
		return false;
	}

	@Override
	public default void iterateInstanceReferences(final T instance, final PersistenceFunction iterator)
	{
		// no-op: for all intents and purposes, an unreachable instance's references need not and must not be iterated.
	}

	@Override
	public default void iterateLoadableReferences(final M medium, final PersistenceReferenceLoader iterator)
	{
		// no-op: for all intents and purposes, an unreachable instance's references need not and must not be iterated.
	}
	
	@Override
	public default Class<T> type()
	{
		return null;
	}

	@Override
	public default T create(final M medium, final PersistenceLoadHandler handler)
	{
		// (01.06.2018 TM)EXCP: proper exception
		throw new UnsupportedOperationException(
			"Cannot create an instance of a type explicitly marked as unreachable: " + this.toTypeIdentifier()
		);
	}

	@Override
	public default void update(final M medium, final T instance, final PersistenceLoadHandler handler)
	{
		// nothing to do here in either case (null or exception)
	}

	@Override
	public default void complete(final M medium, final T instance, final PersistenceLoadHandler handler)
	{
		// nothing to do here in either case (null or exception)
	}
	
	@Override
	default <C extends Consumer<? super Class<?>>> C iterateMemberTypes(final C logic)
	{
		// nothing to iterate. No need to throw an exception, either.
		return logic;
	}
	
	
	
	public static <M, T> PersistenceUnreachableTypeHandler<M, T> New(
		final PersistenceTypeDefinition typeDefinition
	)
	{
		return new PersistenceUnreachableTypeHandler.Default<>(
			notNull(typeDefinition)
		);
	}
	
	public class Default<M, T>
	extends PersistenceLegacyTypeHandler.Abstract<M, T>
	implements PersistenceUnreachableTypeHandler<M, T>
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Default(final PersistenceTypeDefinition typeDefinition)
		{
			super(typeDefinition);
		}
				
	}

}
