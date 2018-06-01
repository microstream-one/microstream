package net.jadoth.persistence.internal;

import static net.jadoth.X.notNull;

import java.lang.reflect.Field;

import net.jadoth.X;
import net.jadoth.collections.types.XGettingEnum;
import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.functional._longProcedure;
import net.jadoth.persistence.types.PersistenceTypeDefinition;
import net.jadoth.persistence.types.PersistenceTypeDescriptionMember;
import net.jadoth.persistence.types.PersistenceTypeHandler;
import net.jadoth.swizzling.types.PersistenceStoreFunction;
import net.jadoth.swizzling.types.SwizzleBuildLinker;
import net.jadoth.swizzling.types.SwizzleFunction;

public final class PersistenceDeletedTypeHandler<M, T> implements PersistenceTypeHandler<M, T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static final <M, T> PersistenceDeletedTypeHandler<M, T> New(
		final PersistenceTypeDefinition<T> typeDefinition
	)
	{
		return new PersistenceDeletedTypeHandler<>(
			notNull(typeDefinition)
		);
	}
		
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final PersistenceTypeDefinition<T> typeDefinition;
	
	

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	PersistenceDeletedTypeHandler(final PersistenceTypeDefinition<T> typeDefinition)
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
	public final XGettingSequence<? extends PersistenceTypeDescriptionMember> members()
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

	@Override
	public final XGettingEnum<Field> getInstanceFields()
	{
		return X.empty();
	}

	@Override
	public final XGettingEnum<Field> getInstancePrimitiveFields()
	{
		return X.empty();
	}

	@Override
	public final XGettingEnum<Field> getInstanceReferenceFields()
	{
		return X.empty();
	}

	@Override
	public final boolean hasInstanceReferences()
	{
		return false;
	}

	@Override
	public final void iterateInstanceReferences(final T instance, final SwizzleFunction iterator)
	{
		// no-op (or throw exception?)
	}

	@Override
	public final void iteratePersistedReferences(final M medium, final _longProcedure iterator)
	{
		// no-op (or throw exception?)
	}

	@Override
	public final void store(final M medium, final T instance, final long objectId, final PersistenceStoreFunction linker)
	{
		// (01.06.2018 TM)EXCP: proper exception
		throw new UnsupportedOperationException("A type handler for a deleted type can never store anything.");
	}

	@Override
	public final T create(final M medium)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME PersistenceTypeHandler<M,T>#create()
	}

	@Override
	public final void update(final M medium, final T instance, final SwizzleBuildLinker builder)
	{
		// nothing to do here in either case (null or exception)
	}

	@Override
	public final void complete(final M medium, final T instance, final SwizzleBuildLinker builder)
	{
		// nothing to do here in either case (null or exception)
	}

	@Override
	public final void validateFields(final XGettingSequence<Field> fieldDescriptions)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME PersistenceTypeHandler<M,T>#validateFields()
	}

	@Override
	public final PersistenceTypeHandler<M, T> initializeTypeId(final long typeId)
	{
		if(typeId == this.typeId())
		{
			return this;
		}
		
		// (01.06.2018 TM)NOTE: /!\ copied from PersistenceTypeHandler#initializeTypeId
		// (26.04.2017 TM)EXCP: proper exception
		throw new RuntimeException(
			"Specified type ID " + typeId + " conflicts with already initalized type ID " + this.typeId()
		);
	}
	
}
