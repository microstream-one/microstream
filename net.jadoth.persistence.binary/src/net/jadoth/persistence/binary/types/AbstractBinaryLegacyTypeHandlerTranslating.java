package net.jadoth.persistence.binary.types;

import java.util.function.Consumer;

import net.jadoth.X;
import net.jadoth.collections.types.XGettingTable;
import net.jadoth.functional._longProcedure;
import net.jadoth.persistence.types.PersistenceLegacyTypeHandler;
import net.jadoth.persistence.types.PersistenceTypeDefinition;
import net.jadoth.persistence.types.PersistenceTypeHandler;
import net.jadoth.swizzling.types.SwizzleFunction;

public abstract class AbstractBinaryLegacyTypeHandlerTranslating<T>
extends PersistenceLegacyTypeHandler.AbstractImplementation<Binary, T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryValueTranslator[] toTranslators(
		final XGettingTable<BinaryValueTranslator, Long> translatorsWithTargetOffsets
	)
	{
		final BinaryValueTranslator[] translators = translatorsWithTargetOffsets.keys()
			.toArray(BinaryValueTranslator.class)
		;
		return translators;
	}
	
	public static long[] toTargetOffsets(
		final XGettingTable<BinaryValueTranslator, Long> translatorsWithTargetOffsets
	)
	{
		final long[] targetOffsets = X.unbox(translatorsWithTargetOffsets.values()
			.toArray(Long.class))
		;
		return targetOffsets;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final PersistenceTypeHandler<Binary, T> typeHandler     ;
	private final BinaryValueTranslator[]           valueTranslators;
	private final long[]                            targetOffsets   ;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	protected AbstractBinaryLegacyTypeHandlerTranslating(
		final PersistenceTypeDefinition<T>      typeDefinition  ,
		final PersistenceTypeHandler<Binary, T> typeHandler     ,
		final BinaryValueTranslator[]           valueTranslators,
		final long[]                            targetOffsets
	)
	{
		super(typeDefinition);
		this.typeHandler      = typeHandler     ;
		this.valueTranslators = valueTranslators;
		this.targetOffsets    = targetOffsets   ;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	public PersistenceTypeHandler<Binary, T> typeHandler()
	{
		return this.typeHandler;
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
	public void iteratePersistedReferences(final Binary rawData, final _longProcedure iterator)
	{
		this.typeHandler.iteratePersistedReferences(rawData, iterator);
	}
	
	@Override
	public <C extends Consumer<? super Class<?>>> C iterateMemberTypes(final C logic)
	{
		return this.typeHandler.iterateMemberTypes(logic);
	}
		
	
	
	
}
