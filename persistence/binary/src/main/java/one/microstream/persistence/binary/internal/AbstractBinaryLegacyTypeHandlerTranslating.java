package one.microstream.persistence.binary.internal;

/*-
 * #%L
 * microstream-persistence-binary
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import java.util.function.Consumer;
import java.util.function.Predicate;

import one.microstream.X;
import one.microstream.collections.types.XGettingEnum;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.BinaryLegacyTypeHandler;
import one.microstream.persistence.binary.types.BinaryReferenceTraverser;
import one.microstream.persistence.binary.types.BinaryValueSetter;
import one.microstream.persistence.exceptions.PersistenceException;
import one.microstream.persistence.exceptions.PersistenceExceptionTypeNotPersistable;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLegacyTypeHandlingListener;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceTypeDefinition;
import one.microstream.persistence.types.PersistenceTypeDefinitionMember;
import one.microstream.persistence.types.PersistenceTypeDescriptionMember;
import one.microstream.persistence.types.PersistenceTypeHandler;
import one.microstream.typing.KeyValue;
import one.microstream.util.cql.CQL;

public abstract class AbstractBinaryLegacyTypeHandlerTranslating<T>
extends BinaryLegacyTypeHandler.Abstract<T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static BinaryValueSetter[] toTranslators(
		final XGettingEnum<KeyValue<Long, BinaryValueSetter>> translatorsWithTargetOffsets
	)
	{
		validate(translatorsWithTargetOffsets);
		return CQL.from(translatorsWithTargetOffsets)
			.project(KeyValue<Long, BinaryValueSetter>::value)
			.executeInto(new BinaryValueSetter[translatorsWithTargetOffsets.intSize()])
		;
	}

	public static long[] toTargetOffsets(
		final XGettingEnum<KeyValue<Long, BinaryValueSetter>> translatorsWithTargetOffsets
	)
	{
		validate(translatorsWithTargetOffsets);
		return X.unbox(
			CQL.from(translatorsWithTargetOffsets)
				.project(kv -> {
					final Long offset= kv.key();
					return offset == null ? -1L : offset;
				})
				.executeInto(new Long[translatorsWithTargetOffsets.intSize()])
		);
	}

	private static void validate(final XGettingEnum<KeyValue<Long, BinaryValueSetter>> translatorsWithTargetOffsets)
	{
		final Predicate<KeyValue<Long, BinaryValueSetter>> isNullEntry = e ->
			e.value() == null
		;

		if(translatorsWithTargetOffsets.containsSearched(isNullEntry))
		{
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

	private final PersistenceTypeHandler<Binary, T>             typeHandler     ;
	private final BinaryValueSetter[]                           valueTranslators;
	private final long[]                                        targetOffsets   ;
	private final PersistenceLegacyTypeHandlingListener<Binary> listener        ;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	protected AbstractBinaryLegacyTypeHandlerTranslating(
		final PersistenceTypeDefinition                     typeDefinition     ,
		final PersistenceTypeHandler<Binary, T>             typeHandler        ,
		final BinaryValueSetter[]                           valueTranslators   ,
		final long[]                                        targetOffsets      ,
		final PersistenceLegacyTypeHandlingListener<Binary> listener           ,
		final boolean                                       switchByteOrder
	)
	{
		super(typeDefinition);
		this.typeHandler         = typeHandler        ;
		this.valueTranslators    = valueTranslators   ;
		this.targetOffsets       = targetOffsets      ;
		this.listener            = listener           ;
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
	public int getPersistedEnumOrdinal(final Binary data)
	{
		// Must pass through all default methods to be a correct wrapper.
		return this.typeHandler.getPersistedEnumOrdinal(data);
	}


	// runtime instance-related methods, so the current type handler must be used //

	@Override
	public Class<T> type()
	{
		return this.typeHandler.type();
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



	@Override
	public final T create(final Binary rawData, final PersistenceLoadHandler handler)
	{
		// the method splitting might help jitting out the not occurring case.
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
