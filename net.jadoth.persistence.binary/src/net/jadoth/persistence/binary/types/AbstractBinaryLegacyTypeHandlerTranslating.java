package net.jadoth.persistence.binary.types;

import java.util.function.Consumer;

import net.jadoth.X;
import net.jadoth.collections.types.XGettingTable;
import net.jadoth.functional._longProcedure;
import net.jadoth.persistence.types.PersistenceFunction;
import net.jadoth.persistence.types.PersistenceLegacyTypeHandler;
import net.jadoth.persistence.types.PersistenceLegacyTypeHandlingListener;
import net.jadoth.persistence.types.PersistenceTypeDefinition;
import net.jadoth.persistence.types.PersistenceTypeHandler;

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
	
	public static final BinaryReferenceTraverser[] deriveReferenceTraversers(final PersistenceTypeDefinition typeDefinition)
	{
		final BinaryReferenceTraverser[] referenceTraversers =
			BinaryReferenceTraverser.Static.deriveReferenceTraversers(typeDefinition.members())
		;
		
		return BinaryReferenceTraverser.Static.cropToReferences(referenceTraversers);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final PersistenceTypeHandler<Binary, T>             typeHandler        ;
	private final BinaryValueSetter[]                           valueTranslators   ;
	private final long[]                                        targetOffsets      ;
	private final BinaryReferenceTraverser[]                    referenceTraversers;
	private final PersistenceLegacyTypeHandlingListener<Binary> listener           ;

	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	protected AbstractBinaryLegacyTypeHandlerTranslating(
		final PersistenceTypeDefinition                     typeDefinition  ,
		final PersistenceTypeHandler<Binary, T>             typeHandler     ,
		final BinaryValueSetter[]                           valueTranslators,
		final long[]                                        targetOffsets   ,
		final PersistenceLegacyTypeHandlingListener<Binary> listener
	)
	{
		super(typeDefinition);
		this.typeHandler      = typeHandler     ;
		this.valueTranslators = valueTranslators;
		this.targetOffsets    = targetOffsets   ;
		this.listener         = listener        ;
		
		// reference traversers mut be derived from the old type definition that fits the persisted form.
		this.referenceTraversers = deriveReferenceTraversers(typeDefinition);
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
	
	
	// runtime instance-related methods, so the current type handler must be used //
	
	@Override
	public Class<T> type()
	{
		return this.typeHandler.type();
	}
	
	@Override
	public boolean hasInstanceReferences()
	{
		return this.typeHandler.hasInstanceReferences();
	}

	@Override
	public void iterateInstanceReferences(final T instance, final PersistenceFunction iterator)
	{
		this.typeHandler.iterateInstanceReferences(instance, iterator);
	}
	
	@Override
	public <C extends Consumer<? super Class<?>>> C iterateMemberTypes(final C logic)
	{
		return this.typeHandler.iterateMemberTypes(logic);
	}
	
	// end of runtime instance-related methods //
	
	
	
	// persisted-form-related methods, so the old type definition (or derivatives of it) has be used //

	@Override
	public final void iteratePersistedReferences(final Binary rawData, final _longProcedure iterator)
	{
		BinaryReferenceTraverser.iterateReferences(
			rawData.buildItemAddress(),
			this.referenceTraversers,
			iterator
		);
	}

	// end of persisted-form-related methods //
	
	@Override
	public final T create(final Binary rawData)
	{
		// the method splitting might help jitting out the not occuring case.
		return this.listener == null
			? this.internalCreate(rawData)
			: this.internalCreateListening(rawData)
		;
	}
	
	private final T internalCreateListening(final Binary rawData)
	{
		final T instance = this.internalCreate(rawData);
		this.listener.registerLegacyTypeHandlingCreation(
			BinaryPersistence.getBuildItemObjectId(rawData),
			instance                                       ,
			this.legacyTypeDefinition()                    ,
			this.typeHandler()
		);
		
		return instance;
	}
	
	protected abstract T internalCreate(Binary rawData);
	
}
