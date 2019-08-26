package one.microstream.persistence.binary.internal;

import java.util.function.Consumer;

import one.microstream.X;
import one.microstream.collections.types.XGettingTable;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.BinaryLegacyTypeHandler;
import one.microstream.persistence.binary.types.BinaryReferenceTraverser;
import one.microstream.persistence.binary.types.BinaryValueSetter;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLegacyTypeHandlingListener;
import one.microstream.persistence.types.PersistenceObjectIdAcceptor;
import one.microstream.persistence.types.PersistenceObjectIdResolver;
import one.microstream.persistence.types.PersistenceTypeDefinition;
import one.microstream.persistence.types.PersistenceTypeHandler;

public abstract class AbstractBinaryLegacyTypeHandlerTranslating<T>
extends BinaryLegacyTypeHandler.Abstract<T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryValueSetter[] toTranslators(
		final XGettingTable<Long, BinaryValueSetter> translatorsWithTargetOffsets
	)
	{
		final BinaryValueSetter[] translators = translatorsWithTargetOffsets.values()
			.toArray(BinaryValueSetter.class)
		;
		return translators;
	}
	
	public static long[] toTargetOffsets(
		final XGettingTable<Long, BinaryValueSetter> translatorsWithTargetOffsets
	)
	{
		final long[] targetOffsets = X.unbox(translatorsWithTargetOffsets.keys()
			.toArray(Long.class))
		;
		return targetOffsets;
	}
	
	public static final BinaryReferenceTraverser[] deriveReferenceTraversers(
		final PersistenceTypeDefinition typeDefinition ,
		final boolean                   switchByteOrder
	)
	{
		// only instance members, here. Not enum constants definitions!
		final BinaryReferenceTraverser[] referenceTraversers =
			BinaryReferenceTraverser.Static.deriveReferenceTraversers(
				typeDefinition.instanceMembers(),
				switchByteOrder
			)
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
		final PersistenceLegacyTypeHandlingListener<Binary> listener        ,
		final boolean                                       switchByteOrder
	)
	{
		super(typeDefinition);
		this.typeHandler      = typeHandler     ;
		this.valueTranslators = valueTranslators;
		this.targetOffsets    = targetOffsets   ;
		this.listener         = listener        ;
		
		// reference traversers mut be derived from the old type definition that fits the persisted form.
		this.referenceTraversers = deriveReferenceTraversers(typeDefinition, switchByteOrder);
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
	public final void iterateLoadableReferences(final Binary rawData, final PersistenceObjectIdAcceptor iterator)
	{
		BinaryReferenceTraverser.iterateReferences(
			rawData.loadItemEntityContentAddress(),
			this.referenceTraversers,
			iterator
		);
	}

	// end of persisted-form-related methods //
	
	@Override
	public final T create(final Binary rawData, final PersistenceObjectIdResolver idResolver)
	{
		// the method splitting might help jitting out the not occuring case.
		return this.listener == null
			? this.internalCreate(rawData, idResolver)
			: this.internalCreateListening(rawData, idResolver)
		;
	}
	
	private final T internalCreateListening(final Binary rawData, final PersistenceObjectIdResolver idResolver)
	{
		final T instance = this.internalCreate(rawData, idResolver);
		this.listener.registerLegacyTypeHandlingCreation(
			rawData.getBuildItemObjectId(),
			instance,
			this.legacyTypeDefinition(),
			this.typeHandler()
		);
		
		return instance;
	}
	
	protected abstract T internalCreate(Binary rawData, PersistenceObjectIdResolver idResolver);
	
}
