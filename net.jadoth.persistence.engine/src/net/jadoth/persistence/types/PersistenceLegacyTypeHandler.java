package net.jadoth.persistence.types;

import static net.jadoth.X.notNull;

import java.util.function.Consumer;

import net.jadoth.collections.types.XGettingEnum;
import net.jadoth.functional._longProcedure;
import net.jadoth.swizzling.types.PersistenceStoreFunction;
import net.jadoth.swizzling.types.SwizzleBuildLinker;
import net.jadoth.swizzling.types.SwizzleFunction;

public interface PersistenceLegacyTypeHandler<M, T> extends PersistenceTypeHandler<M, T>
{
	@Override
	public default PersistenceTypeHandler<M, T> initializeTypeId(final long typeId)
	{
		if(typeId == this.typeId())
		{
			return this;
		}
		
		// (01.06.2018 TM)NOTE: /!\ copied from PersistenceTypeHandler#initializeTypeId
		// (26.04.2017 TM)EXCP: proper exception
		throw new IllegalArgumentException(
			"Specified type ID " + typeId + " conflicts with already initalized type ID " + this.typeId()
		);
	}

	@Override
	public default void store(final M medium, final T instance, final long objectId, final PersistenceStoreFunction linker)
	{
		// (13.09.2018 TM)EXCP: proper exception
		throw new UnsupportedOperationException(
			PersistenceLegacyTypeHandler.class.getSimpleName()
			+ " for TypeId " + this.typeId()
			+ " (" + this.typeName()+")"
			+ " may never store anything."
		);
	}
	
	
	
	public abstract class AbstractImplementation<M, T> implements PersistenceLegacyTypeHandler<M, T>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final PersistenceTypeDefinition<T> typeDefinition;
		
		

		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected AbstractImplementation(final PersistenceTypeDefinition<T> typeDefinition)
		{
			super();
			this.typeDefinition = typeDefinition;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		// (19.09.2018 TM)FIXME: OGS-3: is it correct to just reroute everything to the old type definition? Comment.

		@Override
		public final long typeId()
		{
			return this.typeDefinition.typeId();
		}

		@Override
		public final String typeName()
		{
			return this.typeDefinition.typeName();
		}

		@Override
		public final XGettingEnum<? extends PersistenceTypeDescriptionMember> members()
		{
			return this.typeDefinition.members();
		}
		
		@Override
		public long membersPersistedLengthMinimum()
		{
			return this.typeDefinition.membersPersistedLengthMinimum();
		}
		
		@Override
		public long membersPersistedLengthMaximum()
		{
			return this.typeDefinition.membersPersistedLengthMaximum();
		}

		@Override
		public final boolean hasPersistedReferences()
		{
			return this.typeDefinition.hasPersistedReferences();
		}

		@Override
		public final boolean hasPersistedVariableLength()
		{
			return this.typeDefinition.hasPersistedVariableLength();
		}

		@Override
		public final boolean hasVaryingPersistedLengthInstances()
		{
			return this.typeDefinition.hasVaryingPersistedLengthInstances();
		}

		@Override
		public final boolean isPrimitiveType()
		{
			return this.typeDefinition.isPrimitiveType();
		}

		@Override
		public final Class<T> type()
		{
			// should always be null. Otherwise, something's fishy ...
			return this.typeDefinition.type();
		}
		
	}
	
	
	public static <M, T> PersistenceLegacyTypeHandler<M, T> Wrap(
		final PersistenceTypeDefinition<T> legacyTypeDefinition,
		final PersistenceTypeHandler<M, T> typeHandler
	)
	{
		return new DirectWrapper<>(
			notNull(legacyTypeDefinition),
			notNull(typeHandler)
		);
	}
	
	public final class DirectWrapper<M, T> extends AbstractImplementation<M, T>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final PersistenceTypeHandler<M, T> typeHandler;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public DirectWrapper(
			final PersistenceTypeDefinition<T> typeDefinition           ,
			final PersistenceTypeHandler<M, T> fittingCurrentTypeHandler
		)
		{
			super(typeDefinition);
			this.typeHandler = fittingCurrentTypeHandler;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public boolean hasInstanceReferences()
		{
			return this.typeHandler.hasInstanceReferences();
		}

		@Override
		public void iterateInstanceReferences(final T instance, final SwizzleFunction iterator)
		{
			this.typeHandler.iterateInstanceReferences(instance, iterator);
		}

		@Override
		public void iteratePersistedReferences(final M medium, final _longProcedure iterator)
		{
			this.typeHandler.iteratePersistedReferences(medium, iterator);
		}

		@Override
		public T create(final M medium)
		{
			return this.typeHandler.create(medium);
		}

		@Override
		public void update(final M medium, final T instance, final SwizzleBuildLinker builder)
		{
			this.typeHandler.update(medium, instance, builder);
		}

		@Override
		public void complete(final M medium, final T instance, final SwizzleBuildLinker builder)
		{
			this.typeHandler.complete(medium, instance, builder);
		}
		
		@Override
		public <C extends Consumer<? super Class<?>>> C iterateMemberTypes(final C logic)
		{
			return this.typeHandler.iterateMemberTypes(logic);
		}
		
	}

}

