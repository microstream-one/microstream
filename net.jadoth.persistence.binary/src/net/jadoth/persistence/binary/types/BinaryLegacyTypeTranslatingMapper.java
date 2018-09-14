package net.jadoth.persistence.binary.types;

import static net.jadoth.X.notNull;

import java.lang.reflect.Field;

import net.jadoth.collections.types.XGettingEnum;
import net.jadoth.functional._longProcedure;
import net.jadoth.low.XVM;
import net.jadoth.persistence.types.PersistenceLegacyTypeHandler;
import net.jadoth.persistence.types.PersistenceTypeDefinition;
import net.jadoth.persistence.types.PersistenceTypeDescriptionMember;
import net.jadoth.persistence.types.PersistenceTypeHandler;
import net.jadoth.swizzling.types.SwizzleBuildLinker;
import net.jadoth.swizzling.types.SwizzleFunction;

public final class BinaryLegacyTypeTranslatingMapper<T>
extends PersistenceLegacyTypeHandler.AbstractImplementation<Binary, T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	private static long calculateBinaryContentLength(final PersistenceTypeHandler<Binary, ?> typeHandler)
	{
		long binaryContentLength = 0;
		for(final PersistenceTypeDescriptionMember e : typeHandler.members())
		{
			// minimum length is assumed to never be max long
			binaryContentLength += e.persistentMaximumLength();
		}
		
		return binaryContentLength;
	}
	
	public static <T> BinaryLegacyTypeTranslatingMapper<T> New(
		final PersistenceTypeDefinition<T>      typeDefinition,
		final PersistenceTypeHandler<Binary, T> typeHandler
	)
	{
		if(typeHandler.hasVaryingPersistedLengthInstances())
		{
			// (14.09.2018 TM)TODO: support VaryingPersistedLengthInstances
			throw new UnsupportedOperationException(
				"Types with instances of varying persisted length are not supported, yet."
			);
		}
		
		final long binaryContentLength = calculateBinaryContentLength(typeHandler);
		
		return new BinaryLegacyTypeTranslatingMapper<>(
			notNull(typeDefinition),
			notNull(typeHandler)   ,
			binaryContentLength
		);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final PersistenceTypeHandler<Binary, T> typeHandler        ;
	private final long                              binaryContentLength;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryLegacyTypeTranslatingMapper(
		final PersistenceTypeDefinition<T>      typeDefinition     ,
		final PersistenceTypeHandler<Binary, T> typeHandler        ,
		final long                              binaryContentLength
	)
	{
		super(typeDefinition);
		this.typeHandler         = typeHandler        ;
		this.binaryContentLength = binaryContentLength;
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
	public void iteratePersistedReferences(final Binary medium, final _longProcedure iterator)
	{
		this.typeHandler.iteratePersistedReferences(medium, iterator);
	}

	@Override
	public T create(final Binary medium)
	{
		// redirect the Binary building instance address to a newly allocated memory range with the new structure
		// (14.09.2018 TM)FIXME: OGS-3: construct a suitable entity header with preceding the content bytes
		medium.entityContentAddress = XVM.allocate(this.binaryContentLength);
		
		// (14.09.2018 TM)FIXME: OGS-3: copy and translate values to helper.

		final T instance = this.typeHandler.create(medium);
		
		
		throw new net.jadoth.meta.NotImplementedYetError();
//		return instance;
	}

	@Override
	public void update(final Binary medium, final T instance, final SwizzleBuildLinker builder)
	{
		this.typeHandler.update(medium, instance, builder);
	}

	@Override
	public void complete(final Binary medium, final T instance, final SwizzleBuildLinker builder)
	{
		this.typeHandler.complete(medium, instance, builder);
	}
	
}
