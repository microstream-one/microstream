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

import one.microstream.collections.BulkList;
import one.microstream.collections.ConstHashEnum;
import one.microstream.collections.types.XGettingEnum;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.exceptions.PersistenceException;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceTypeDefinitionMember;
import one.microstream.persistence.types.PersistenceTypeDefinitionMemberEnumConstant;
import one.microstream.reflect.XReflect;

public abstract class AbstractBinaryHandlerCustomEnum<T extends Enum<T>> extends AbstractBinaryHandlerCustom<T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static <T> Class<T> validateIsEnum(final Class<T> type)
	{
		if(XReflect.isEnum(type))
		{
			return type;
		}

		throw new IllegalArgumentException("Not an Enum type: " + type.getName());
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	final ConstHashEnum<PersistenceTypeDefinitionMemberEnumConstant> constantMembers;
	final ConstHashEnum<PersistenceTypeDefinitionMember>             allMembers     ;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	protected AbstractBinaryHandlerCustomEnum(
		final Class<T>                                                                type           ,
		final XGettingSequence<? extends PersistenceTypeDefinitionMemberEnumConstant> constantMembers,
		final XGettingSequence<? extends PersistenceTypeDefinitionMember>             instanceMembers
	)
	{
		this(type, deriveTypeName(type), constantMembers, instanceMembers);
	}
	
	protected AbstractBinaryHandlerCustomEnum(
		final Class<T>                                                                type           ,
		final String                                                                  typeName       ,
		final XGettingSequence<? extends PersistenceTypeDefinitionMemberEnumConstant> constantMembers,
		final XGettingSequence<? extends PersistenceTypeDefinitionMember>             instanceMembers
	)
	{
		super(validateIsEnum(type), typeName, instanceMembers);
		this.constantMembers = ConstHashEnum.New(constantMembers);
		this.allMembers      = ConstHashEnum.New(
			BulkList.<PersistenceTypeDefinitionMember>New(constantMembers).addAll(instanceMembers)
		);
	}

	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public Object[] collectEnumConstants()
	{
		// legacy type handlers return null here to indicate their root entry is obsolete
		return Persistence.collectEnumConstants(this);
	}
	
	@Override
	public final XGettingEnum<? extends PersistenceTypeDefinitionMember> allMembers()
	{
		return this.allMembers;
	}
	
	protected abstract int getOrdinal(Binary data);
	
	protected abstract String getName(Binary data, PersistenceLoadHandler handler);
	
	@Override
	public T create(final Binary data, final PersistenceLoadHandler handler)
	{
		// copied from BinaryHandlerEnum#create
		
		// Class detour required for AIC-like special subclass enums constants.
		final Object[] jvmEnumConstants  = XReflect.getDeclaredEnumClass(this.type()).getEnumConstants();
		final int      persistentOrdinal = this.getOrdinal(data);
		
		/*
		 * Can't validate here since the name String instance might not have been created, yet. See #update.
		 * Nevertheless:
		 * - the enum constants storing order must be assumed to be consistent with the type dictionary constants names.
		 * - the type dictionary constants names are validated against the current runtime type.
		 * These two aspects in combination ensure that the correct enum constant instance is selected.
		 * 
		 * Mismatches between persistent form and runtime type must be handled via a LegacyTypeHandler, not here.
		 */
		
		/*
		 * Required for AIC-like special subclass enums constants:
		 * The instance is actually of type T, but it is stored in a "? super T" array of it parent enum type.
		 */
		@SuppressWarnings("unchecked")
		final T enumConstantinstance = (T)jvmEnumConstants[persistentOrdinal];
		
		return enumConstantinstance;
	}
	
	protected void validate(
		final Binary                 data    ,
		final T                      instance,
		final PersistenceLoadHandler handler
	)
	{
		// validate ordinal, just in case.
		final int persistentOrdinal = this.getOrdinal(data);
		if(persistentOrdinal != instance.ordinal())
		{
			throw new PersistenceException(
				"Inconcistency for " + instance.getDeclaringClass().getName() + "." + instance.name()
			);
		}
		
		final String persistentName = this.getName(data, handler);
		if(!instance.name().equals(persistentName))
		{
			throw new PersistenceException(
				"Enum constant inconsistency:"
				+ " in type " + this.type().getName()
				+ " persisted instance with ordinal " + persistentOrdinal + ", name \"" + persistentName + "\""
				+ " does not match"
				+ " JVM-created instance with ordinal " + instance.ordinal() + ", name \"" + instance.name() + "\""
			);
		}
	}
		
	@Override
	public void updateState(final Binary data, final T instance, final PersistenceLoadHandler handler)
	{
		// must thoroughly validate the linked jvm-generated(!) instance before modifying its state!
		this.validate(data, instance, handler);
	}
	
}
