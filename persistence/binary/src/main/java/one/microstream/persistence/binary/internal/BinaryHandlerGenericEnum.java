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

import java.lang.reflect.Field;
import java.util.function.Predicate;

import one.microstream.collections.EqConstHashEnum;
import one.microstream.collections.EqHashEnum;
import one.microstream.collections.HashEnum;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.collections.types.XGettingEnum;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.collections.types.XImmutableEnum;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.BinaryValueFunctions;
import one.microstream.persistence.binary.types.BinaryValueSetter;
import one.microstream.persistence.exceptions.PersistenceException;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceEagerStoringFieldEvaluator;
import one.microstream.persistence.types.PersistenceFieldLengthResolver;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceTypeDefinition;
import one.microstream.persistence.types.PersistenceTypeDefinitionMember;
import one.microstream.persistence.types.PersistenceTypeDefinitionMemberEnumConstant;
import one.microstream.persistence.types.PersistenceTypeDefinitionMemberFieldReflective;
import one.microstream.reflect.XReflect;
import one.microstream.typing.XTypes;

public final class BinaryHandlerGenericEnum<T extends Enum<T>> extends AbstractBinaryHandlerReflective<T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static boolean isJavaLangEnumMember(final PersistenceTypeDefinitionMember member)
	{
		// changed to qualifier string comparison for compatibility with enum legacy type handlers.
		return member != null
			&& java.lang.Enum.class.getName().equals(member.runtimeQualifier())
		;
	}
	
	public static boolean isJavaLangEnumName(final PersistenceTypeDefinitionMember member)
	{
		// intentionally no reference to the name. Although final modifier and field type is not much better ...
		return isJavaLangEnumMember(member)
			&& member.type() == String.class
		;
	}
	
	public static boolean isJavaLangEnumOrdinal(final PersistenceTypeDefinitionMember member)
	{
		// intentionally no reference to the name. Although final modifier and field type is not much better ...
		return isJavaLangEnumMember(member)
			&& member.type() == int.class
		;
	}
	
	public static long calculateBinaryOffsetOrdinal(final PersistenceTypeDefinition typeDefinition)
	{
		return calculateBinaryOffset(
			typeDefinition.instanceMembers(),
			BinaryHandlerGenericEnum::isJavaLangEnumOrdinal
		);
	}
	
	public static long calculateBinaryOffsetName(final PersistenceTypeDefinition typeDefinition)
	{
		return calculateBinaryOffset(
			typeDefinition.instanceMembers(),
			BinaryHandlerGenericEnum::isJavaLangEnumName
		);
	}
	
	public static long calculateBinaryOffset(
		final XGettingCollection<? extends PersistenceTypeDefinitionMember> fields       ,
		final Predicate<? super PersistenceTypeDefinitionMember>            fieldSelector
	)
	{
		long binaryOffset = 0;
		
		for(final PersistenceTypeDefinitionMember f : fields)
		{
			if(fieldSelector.test(f))
			{
				return binaryOffset;
			}
			
			binaryOffset += equal(f.persistentMinimumLength(), f.persistentMaximumLength());
		}
		
		throw new PersistenceException("Member not found in member list.");
	}
		
	public static XImmutableEnum<PersistenceTypeDefinitionMemberEnumConstant> deriveEnumConstantMembers(
		final Class<?> enumType
	)
	{
		// can't use generics typing due to broken Object#getClass.
		XReflect.validateIsEnum(enumType);
		
		final HashEnum<PersistenceTypeDefinitionMemberEnumConstant> enumConstants = HashEnum.New();

		// crazy nested enum classes return null here
		final Object[] enumConstantsArray = enumType.getEnumConstants();
		if(enumConstantsArray != null)
		{
			// (15.11.2019 TM)NOTE: should work on any JVM and is even a little bit more elegant
			for(final Object enumInstance : enumConstantsArray)
			{
				enumConstants.add(
					PersistenceTypeDefinitionMemberEnumConstant.New(
						((Enum<?>)enumInstance).name()
					)
				);
			}
		}
		
		return enumConstants.immure();
	}
	
	
	
	public static <T extends Enum<T>> BinaryHandlerGenericEnum<T> New(
		final Class<T>                              type                      ,
		final String                                typeName                  ,
		final XGettingEnum<Field>                   persistableFields         ,
		final XGettingEnum<Field>                   persisterFields           ,
		final PersistenceFieldLengthResolver        lengthResolver            ,
		final PersistenceEagerStoringFieldEvaluator eagerStoringFieldEvaluator,
		final boolean                               switchByteOrder
	)
	{
		return new BinaryHandlerGenericEnum<>(
			type                      ,
			typeName                  ,
			persistableFields         ,
			persisterFields           ,
			lengthResolver            ,
			eagerStoringFieldEvaluator,
			switchByteOrder
		);
	}
		
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	// offsets must be determined per handler instance since different types have different persistent form offsets.
	private final long binaryOffsetName   ;
	private final long binaryOffsetOrdinal;
	
	// effectively final but must be on-demand initialized due to usage in super constructor logic. Tricky.
	private XImmutableEnum<PersistenceTypeDefinitionMemberEnumConstant> enumConstants;
	
	private final XImmutableEnum<PersistenceTypeDefinitionMember> allMembers;
	
	

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	protected BinaryHandlerGenericEnum(
		final Class<T>                              type                      ,
		final String                                typeName                  ,
		final XGettingEnum<Field>                   persistableFields         ,
		final XGettingEnum<Field>                   persisterFields           ,
		final PersistenceFieldLengthResolver        lengthResolver            ,
		final PersistenceEagerStoringFieldEvaluator eagerStoringFieldEvaluator,
		final boolean                               switchByteOrder
	)
	{
		super(type, typeName, persistableFields, persisterFields, lengthResolver, eagerStoringFieldEvaluator, switchByteOrder);
				
		// these are instance members in persistent order. Not to be mixed up with members in declared order
		this.allMembers = this.deriveAllMembers(this.instanceMembers());
		
		this.binaryOffsetName    = calculateBinaryOffsetName(this);
		this.binaryOffsetOrdinal = calculateBinaryOffsetOrdinal(this);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// initializer logic //
	//////////////////////
	
	@Override
	protected BinaryValueSetter deriveSetter(final PersistenceTypeDefinitionMemberFieldReflective member)
	{
		return this.isUnsettableField(member)
			? BinaryValueFunctions.getObjectValueSettingSkipper(member.type())
			: BinaryValueFunctions.getObjectValueSetter(member.type(), this.isSwitchedByteOrder())
		;
	}
			
	@Override
	protected EqConstHashEnum<PersistenceTypeDefinitionMember> deriveAllMembers(
		final XGettingSequence<? extends PersistenceTypeDefinitionMember> instanceMembers
	)
	{
		// whether this will be declared order or persistent order depends on the passed members
		final EqHashEnum<PersistenceTypeDefinitionMember> allMembers = MemberEnum()
			.addAll(this.enumConstants())
			.addAll(instanceMembers)
		;
		
		return allMembers.immure();
	}
	
	public XImmutableEnum<PersistenceTypeDefinitionMemberEnumConstant> enumConstants()
	{
		if(this.enumConstants == null)
		{
			this.enumConstants = deriveEnumConstantMembers(this.type());
		}
		
		return this.enumConstants;
	}


	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	protected final boolean isUnsettableField(final PersistenceTypeDefinitionMemberFieldReflective m)
	{
		/*
		 * Final primitive fields and final value type fields ("value instance constants") may not be settable, either.
		 * E.g. a final int holding some kind of business-logical uniqueId. That may not be changed.
		 * Final-ly referenced "true" instances might be a different story since they might be meant as a kind of "stub"
		 * instance for an entity (sub-)graph and are probably expected to be filled upon loading the enum instance.
		 * Any mutable field must be loaded and set, that is no question.
		 * But silently changing a final primitive field's value ... that can actually only be a bug.
		 */
		return this.isJavaLangEnumField(m) || this.isFinalPrimitiveField(m) || this.isFinalValueTypeField(m);
	}
	
	protected final boolean isJavaLangEnumField(final PersistenceTypeDefinitionMemberFieldReflective m)
	{
		// quick check for "normal" members but also essential for the checking logic below
		if(m.declaringClass() != java.lang.Enum.class)
		{
			return false;
		}
		
		// should Enum fields ever change, it is important to at least notice it and abort.
		if(isJavaLangEnumName(m) || isJavaLangEnumOrdinal(m))
		{
			return true;
		}
		
		throw new PersistenceException("Unknown " + java.lang.Enum.class.getName() + " field: " + m.name());
	}
	
	protected final boolean isFinalPrimitiveField(final PersistenceTypeDefinitionMemberFieldReflective m)
	{
		return XReflect.isFinal(m.field()) && m.field().getType().isPrimitive();
	}
	
	protected final boolean isFinalValueTypeField(final PersistenceTypeDefinitionMemberFieldReflective m)
	{
		return XReflect.isFinal(m.field()) && XTypes.isValueType(m.field().getType());
	}
	
	@Override
	public final XGettingEnum<? extends PersistenceTypeDefinitionMember> allMembers()
	{
		return this.allMembers;
	}
	
	@Override
	public Object[] collectEnumConstants()
	{
		// legacy type handlers return null here to indicate their root entry is obsolete
		return Persistence.collectEnumConstants(this);
	}
		
	@Override
	public final T create(final Binary data, final PersistenceLoadHandler handler)
	{
		/*
		 * Can't validate here since the name String instance might not have been created, yet. See #update.
		 * Nevertheless:
		 * - the enum constants storing order must be assumed to be consistent with the type dictionary constants names.
		 * - the type dictionary constants names are validated against the current runtime type.
		 * These two aspects in combination ensure that the correct enum constant instance is selected.
		 * 
		 * Mismatches between persistent form and runtime type must be handled via a LegacyTypeHandler, not here.
		 */
		
		return XReflect.resolveEnumConstantInstanceTyped(this.type(), this.getPersistedEnumOrdinal(data));
	}
	
	@Override
	public int getPersistedEnumOrdinal(final Binary data)
	{
		return data.read_int(this.binaryOffsetOrdinal);
	}
	
	public String getName(final Binary data, final PersistenceLoadHandler handler)
	{
		return (String)handler.lookupObject(data.read_long(this.binaryOffsetName));
	}
	
	private void validate(
		final Binary                 data    ,
		final T                      instance,
		final PersistenceLoadHandler handler
	)
	{
		// validate ordinal, just in case.
		final int persistentOrdinal = this.getPersistedEnumOrdinal(data);
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
		
		// super class logic already uses only setting members, i.e. not ordinal and name.
		super.updateState(data, instance, handler);
	}

}
