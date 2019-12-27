package one.microstream.persistence.binary.internal;

import java.util.function.Consumer;
import java.util.function.Predicate;

import one.microstream.X;
import one.microstream.collections.types.XGettingEnum;
import one.microstream.collections.types.XGettingTable;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.BinaryLegacyTypeHandler;
import one.microstream.persistence.binary.types.BinaryReferenceTraverser;
import one.microstream.persistence.binary.types.BinaryValueSetter;
import one.microstream.persistence.exceptions.PersistenceException;
import one.microstream.persistence.exceptions.PersistenceExceptionTypeNotPersistable;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLegacyTypeHandlingListener;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceReferenceLoader;
import one.microstream.persistence.types.PersistenceTypeDefinition;
import one.microstream.persistence.types.PersistenceTypeDefinitionMember;
import one.microstream.persistence.types.PersistenceTypeDescriptionMember;
import one.microstream.persistence.types.PersistenceTypeHandler;
import one.microstream.typing.KeyValue;

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
		validate(translatorsWithTargetOffsets);
		final BinaryValueSetter[] translators = translatorsWithTargetOffsets.values()
			.toArray(BinaryValueSetter.class)
		;
		return translators;
	}
	
	public static long[] toTargetOffsets(
		final XGettingTable<Long, BinaryValueSetter> translatorsWithTargetOffsets
	)
	{
		validate(translatorsWithTargetOffsets);
		final long[] targetOffsets = X.unbox(translatorsWithTargetOffsets.keys()
			.toArray(Long.class))
		;
		return targetOffsets;
	}
	
	private static void validate(final XGettingTable<Long, BinaryValueSetter> translatorsWithTargetOffsets)
	{
		final Predicate<KeyValue<Long, BinaryValueSetter>> isNullEntry = e ->
			e.key() == null || e.value() == null
		;
		
		if(translatorsWithTargetOffsets.containsSearched(isNullEntry))
		{
			// (02.09.2019 TM)EXCP: proper exception
			throw new PersistenceException("Value translator mapping contains an invalid null-entry.");
		}
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
		

		// (12.11.2019 TM)NOTE: must be derived from the NEW type definition since create relayouts the load data.
		this.referenceTraversers = deriveReferenceTraversers(typeHandler, switchByteOrder);
		
		// reference traversers mut be derived from the old type definition that fits the persisted form.
//		this.referenceTraversers = deriveReferenceTraversers(typeDefinition, switchByteOrder);
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
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// default method implementations //
	///////////////////////////////////
	
	/*
	 * Tricky:
	 * Must pass through all default methods to be a correct wrapper.
	 * Otherwise, the wrapper changes the behavior in an unwanted fashion.
	 */
	
	@Override
	public XGettingEnum<? extends PersistenceTypeDefinitionMember> membersInDeclaredOrder()
	{
		// Must pass through all default methods to be a correct wrapper.
		return this.typeHandler.membersInDeclaredOrder();
	}
	
	@Override
	public XGettingEnum<? extends PersistenceTypeDescriptionMember> storingMembers()
	{
		return this.typeHandler.storingMembers();
	}
	
	@Override
	public XGettingEnum<? extends PersistenceTypeDescriptionMember> settingMembers()
	{
		return this.typeHandler.settingMembers();
	}
	
	@Override
	public void guaranteeSpecificInstanceViablity() throws PersistenceExceptionTypeNotPersistable
	{
		// Must pass through all default methods to be a correct wrapper.
		this.typeHandler.guaranteeSpecificInstanceViablity();
	}
	
	@Override
	public boolean isSpecificInstanceViable()
	{
		// Must pass through all default methods to be a correct wrapper.
		return this.typeHandler.isSpecificInstanceViable();
	}
	
	@Override
	public void guaranteeSubTypeInstanceViablity() throws PersistenceExceptionTypeNotPersistable
	{
		// Must pass through all default methods to be a correct wrapper.
		this.typeHandler.guaranteeSubTypeInstanceViablity();
	}
	
	@Override
	public boolean isSubTypeInstanceViable()
	{
		// Must pass through all default methods to be a correct wrapper.
		return this.typeHandler.isSubTypeInstanceViable();
	}
	
	@Override
	public Object[] collectEnumConstants()
	{
		// indicate discarding of constants root entry during root resolving
		return null;
	}
	
	@Override
	public int getPersistedEnumOrdinal(final Binary medium)
	{
		// Must pass through all default methods to be a correct wrapper.
		return this.typeHandler.getPersistedEnumOrdinal(medium);
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
	public final void iterateLoadableReferences(final Binary rawData, final PersistenceReferenceLoader iterator)
	{
		rawData.iterateReferences(this.referenceTraversers, iterator);
	}

	// end of persisted-form-related methods //
	
	@Override
	public final T create(final Binary rawData, final PersistenceLoadHandler handler)
	{
		// the method splitting might help jitting out the not occuring case.
		return this.listener == null
			? this.internalCreate(rawData, handler)
			: this.internalCreateListening(rawData, handler)
		;
	}
	
	private final T internalCreateListening(final Binary rawData, final PersistenceLoadHandler handler)
	{
		final T instance = this.internalCreate(rawData, handler);
		this.listener.registerLegacyTypeHandlingCreation(
			rawData.getBuildItemObjectId(),
			instance,
			this.legacyTypeDefinition(),
			this.typeHandler()
		);
		
		return instance;
	}
	
	protected abstract T internalCreate(Binary rawData, PersistenceLoadHandler handler);
	
}
