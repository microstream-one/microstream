package net.jadoth.persistence.types;

import static net.jadoth.X.notNull;

import java.lang.reflect.Field;

import net.jadoth.collections.types.XGettingEnum;
import net.jadoth.collections.types.XGettingSequence;
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
		public final boolean isPrimitiveType()
		{
			return this.typeDefinition.isPrimitiveType();
		}

		@Override
		public final boolean hasVaryingPersistedLengthInstances()
		{
			return this.typeDefinition.hasVaryingPersistedLengthInstances();
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
		return new Wrapper<>(
			notNull(legacyTypeDefinition),
			notNull(typeHandler)
		);
	}
	
	public final class Wrapper<M, T> extends AbstractImplementation<M, T>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final PersistenceTypeHandler<M, T> typeHandler;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Wrapper(
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
		public final XGettingEnum<Field> getInstanceFields()
		{
			return this.typeHandler.getInstanceFields();
		}

		@Override
		public XGettingEnum<Field> getInstancePrimitiveFields()
		{
			return this.typeHandler.getInstancePrimitiveFields();
		}

		@Override
		public XGettingEnum<Field> getInstanceReferenceFields()
		{
			return this.typeHandler.getInstanceReferenceFields();
		}

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
		public void validateFields(final XGettingSequence<Field> fieldDescriptions)
		{
			this.typeHandler.validateFields(fieldDescriptions);
		}
		
	}

}
