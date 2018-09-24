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
	
	public static BinaryValueSetter[] toTranslators(
		final XGettingTable<BinaryValueSetter, Long> translatorsWithTargetOffsets
	)
	{
		final BinaryValueSetter[] translators = translatorsWithTargetOffsets.keys()
			.toArray(BinaryValueSetter.class)
		;
		return translators;
	}
	
	public static long[] toTargetOffsets(
		final XGettingTable<BinaryValueSetter, Long> translatorsWithTargetOffsets
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
	private final BinaryValueSetter[]               valueTranslators;
	private final long[]                            targetOffsets   ;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	protected AbstractBinaryLegacyTypeHandlerTranslating(
		final PersistenceTypeDefinition<T>      typeDefinition  ,
		final PersistenceTypeHandler<Binary, T> typeHandler     ,
		final BinaryValueSetter[]               valueTranslators,
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
	
	protected BinaryValueSetter[] valueTranslators()
	{
		return this.valueTranslators;
	}
	
	protected long[] targetOffsets()
	{
		return this.targetOffsets;
	}
	
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
		// (24.09.2018 TM)FIXME: OGS-3: must be old order, not new!
		this.typeHandler.iteratePersistedReferences(rawData, iterator);
	}
	
	@Override
	public <C extends Consumer<? super Class<?>>> C iterateMemberTypes(final C logic)
	{
		return this.typeHandler.iterateMemberTypes(logic);
	}
	
}
