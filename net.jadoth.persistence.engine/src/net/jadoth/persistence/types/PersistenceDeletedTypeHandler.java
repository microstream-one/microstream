package net.jadoth.persistence.types;

import static net.jadoth.X.notNull;

import java.util.function.Consumer;

import net.jadoth.functional._longProcedure;
import net.jadoth.swizzling.types.SwizzleBuildLinker;
import net.jadoth.swizzling.types.SwizzleFunction;

/**
 * "Deleted" handler to mark explicitely deleted types. Note that this is not a "unwanted instances suppressing handler".
 * Explicitely marking a type as deleted only makes sense to handle type dictionary entries that are known by the
 * developer to no longer be relevant, because the database no longer contains any instances of that type. At least
 * none that would be reachable by a loading cascade. Should any persisted entity record of a deleted type be loaded
 * nonetheless, it is an error that must be propagated (in {@link #create(Object)}). Hence, that method may NOT
 * just return null. Maybe an "instance suppressing handler" might be a viable feature, as well (which is doubtful),
 * but that would somethind different from a deleted type handler.
 * 
 * @author TM
 *
 * @param <M>
 * @param <T>
 */
public interface PersistenceDeletedTypeHandler<M, T> extends PersistenceLegacyTypeHandler<M, T>
{
	@Override
	public default boolean hasInstanceReferences()
	{
		return false;
	}

	@Override
	public default void iterateInstanceReferences(final T instance, final SwizzleFunction iterator)
	{
		// no-op (or throw exception?)
	}

	@Override
	public default void iteratePersistedReferences(final M medium, final _longProcedure iterator)
	{
		// no-op: whatever bytes representing references have been loaded for the instance, they shall be ignored.
	}
	
	@Override
	public default Class<T> type()
	{
		// (25.09.2018 TM)EXCP: proper exception
		throw new UnsupportedOperationException("Type deleted: " + this.toTypeIdentifier() + ".");
	}

	@Override
	public default T create(final M medium)
	{
		// (01.06.2018 TM)EXCP: proper exception
		throw new UnsupportedOperationException(
			"Cannot create an instance of explicitely deleted type " + this.toTypeIdentifier()
		);
	}

	@Override
	public default void update(final M medium, final T instance, final SwizzleBuildLinker builder)
	{
		// nothing to do here in either case (null or exception)
	}

	@Override
	public default void complete(final M medium, final T instance, final SwizzleBuildLinker builder)
	{
		// nothing to do here in either case (null or exception)
	}
	
	@Override
	default <C extends Consumer<? super Class<?>>> C iterateMemberTypes(final C logic)
	{
		// nothing to iterate. No need to throw an exception, either.
		return logic;
	}
	
	
	
	public static <M, T> PersistenceDeletedTypeHandler<M, T> New(
		final PersistenceTypeDefinition typeDefinition
	)
	{
		return new PersistenceDeletedTypeHandler.Implementation<>(
			notNull(typeDefinition)
		);
	}
	
	public class Implementation<M, T>
	extends PersistenceLegacyTypeHandler.AbstractImplementation<M, T>
	implements PersistenceDeletedTypeHandler<M, T>
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Implementation(final PersistenceTypeDefinition typeDefinition)
		{
			super(typeDefinition);
		}
				
	}

}
