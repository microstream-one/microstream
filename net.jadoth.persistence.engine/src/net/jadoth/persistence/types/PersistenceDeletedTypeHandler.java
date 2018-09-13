package net.jadoth.persistence.types;

import static net.jadoth.X.notNull;

import java.lang.reflect.Field;

import net.jadoth.X;
import net.jadoth.collections.types.XGettingEnum;
import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.functional._longProcedure;
import net.jadoth.swizzling.types.SwizzleBuildLinker;
import net.jadoth.swizzling.types.SwizzleFunction;

public interface PersistenceDeletedTypeHandler<M, T> extends PersistenceLegacyTypeHandler<M, T>
{
	@Override
	public default XGettingEnum<Field> getInstanceFields()
	{
		return X.empty();
	}

	@Override
	public default XGettingEnum<Field> getInstancePrimitiveFields()
	{
		return X.empty();
	}

	@Override
	public default XGettingEnum<Field> getInstanceReferenceFields()
	{
		return X.empty();
	}

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
		// no-op (or throw exception?)
	}

	@Override
	public default T create(final M medium)
	{
		/* (13.09.2018 TM)TODO: shouldn't PersistenceDeletedTypeHandler#create return null?
		 * If it throws an exception like it currently does, what's the point of having it in the first place?
		 * Getting an exception later (during loading) instead of sooner (during validation)?
		 * If a "deleted handler" has any point, then that to ignore instances of deleted types, i.e. "null out" any
		 * reference to them during loading and not creating any instance at all.
		 */
		
		// (01.06.2018 TM)EXCP: proper exception
		throw new UnsupportedOperationException(
			"Cannot create an instance of explicitely deleted type " + this.typeName() + " " + this.typeId()
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
	public default void validateFields(final XGettingSequence<Field> fieldDescriptions)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME PersistenceTypeHandler<M,T>#validateFields()
	}
	
	
	
	
	public static <M, T> PersistenceDeletedTypeHandler<M, T> New(
		final PersistenceTypeDefinition<T> typeDefinition
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

		protected Implementation(final PersistenceTypeDefinition<T> typeDefinition)
		{
			super(typeDefinition);
		}
				
	}

}
