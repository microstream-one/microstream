package one.microstream.java;

import java.lang.reflect.Field;

import one.microstream.collections.BulkList;
import one.microstream.collections.EqHashTable;
import one.microstream.collections.HashEnum;
import one.microstream.collections.types.XGettingEnum;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.collections.types.XImmutableEnum;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerReflective;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceEagerStoringFieldEvaluator;
import one.microstream.persistence.types.PersistenceFieldLengthResolver;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceTypeDefinitionMember;
import one.microstream.persistence.types.PersistenceTypeDefinitionMemberEnumConstant;
import one.microstream.persistence.types.PersistenceTypeDefinitionMemberFieldReflective;
import one.microstream.reflect.XReflect;

public final class BinaryHandlerEnum<T extends Enum<T>> extends AbstractBinaryHandlerReflective<T>
{
	public static <T extends Enum<T>> BinaryHandlerEnum<T> New(
		final Class<T>                              type                      ,
		final XGettingEnum<Field>                   allFields                 ,
		final PersistenceFieldLengthResolver        lengthResolver            ,
		final PersistenceEagerStoringFieldEvaluator eagerStoringFieldEvaluator,
		final boolean                               switchByteOrder
	)
	{
		return new BinaryHandlerEnum<>(
			type                      ,
			allFields                 ,
			lengthResolver            ,
			eagerStoringFieldEvaluator,
			switchByteOrder
		);
	}
		
	// (30.07.2019 TM)FIXME: priv#23: enum BinaryHandler special case implementation

	public static void main(final String[] args)
	{
		XReflect.iterateDeclaredFieldsUpwards(MyEnum.class, f ->
		{
			// non-instance fields are always discarded
			if(!XReflect.isInstanceField(f))
			{
				return;
			}
			
			System.out.println(f.getType() + " " + f.getName());
		});
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	private static <E extends Enum<E>> EqHashTable<String, E> initializeEnumReferencesCache(final Class<E> type)
	{
		final EqHashTable<String, E> cachedEnumReferences = EqHashTable.New();
		
		final E[] enumConstants = type.getEnumConstants();
		for(final E e : enumConstants)
		{
			cachedEnumReferences.add(e.name(), e);
		}
		
		return cachedEnumReferences;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	/*
	 * Cached, because the weird tinkering in the JDK's enumConstantDirectory code is creepy.
	 */
	private final EqHashTable<String, T> cachedEnumReferences;
	
	private final PersistenceTypeDefinitionMemberFieldReflective java_lang_Enum_name   ;
	private final PersistenceTypeDefinitionMemberFieldReflective java_lang_Enum_ordinal;
	
	private final XImmutableEnum<PersistenceTypeDefinitionMember> allMembers;
	
	// (09.06.2017 TM)NOTE: would have to hold fields to orinal and name here as special cases
	



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	protected BinaryHandlerEnum(
		final Class<T>                              type                      ,
		final XGettingEnum<Field>                   persistableFields         ,
		final PersistenceFieldLengthResolver        lengthResolver            ,
		final PersistenceEagerStoringFieldEvaluator eagerStoringFieldEvaluator,
		final boolean                               switchByteOrder
	)
	{
		super(type, persistableFields, lengthResolver, eagerStoringFieldEvaluator, switchByteOrder);
		this.cachedEnumReferences   = initializeEnumReferencesCache(type);
		this.allMembers             = deriveAllMembers(type, this.instanceMembers());
		this.java_lang_Enum_name    = this.searchMemberField("name");
		this.java_lang_Enum_ordinal = this.searchMemberField("ordinal");
	}
	
	private static <E extends Enum<E>> XImmutableEnum<PersistenceTypeDefinitionMember> deriveAllMembers(
		final Class<E>                                                    type           ,
		final XGettingSequence<? extends PersistenceTypeDefinitionMember> instanceMembers
	)
	{
		final XGettingSequence<PersistenceTypeDefinitionMemberEnumConstant> enumConstants = deriveEnumMembers(type);
		
		final HashEnum<PersistenceTypeDefinitionMember> allMembers = HashEnum.<PersistenceTypeDefinitionMember>New()
			.addAll(enumConstants)
			.addAll(instanceMembers)
		;
		
		return allMembers.immure();
	}
	
	private PersistenceTypeDefinitionMemberFieldReflective searchMemberField(final String name)
	{
		for(final PersistenceTypeDefinitionMemberFieldReflective memberField : this.instanceMembers())
		{
			if(memberField.declaringClass() == java.lang.Enum.class && memberField.field().getName().equals(name))
			{
				return memberField;
			}
		}
		
		// (30.07.2019 TM)EXCP: proper exception
		throw new RuntimeException("Enum-intrinsic field \"" + name + "\" not found in type " + this.type());
	}
	
	public static <T extends Enum<T>> XGettingSequence<PersistenceTypeDefinitionMemberEnumConstant> deriveEnumMembers(
		final Class<T> enumType
	)
	{
		final BulkList<PersistenceTypeDefinitionMemberEnumConstant> enumConstants = BulkList.New();
		for(final Field field : enumType.getDeclaredFields())
		{
			if(field.isEnumConstant())
			{
				enumConstants.add(
					PersistenceTypeDefinitionMemberEnumConstant.New(
						field.getName()
					)
				);
			}
		}
		
		return enumConstants;
	}


	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public final XGettingEnum<? extends PersistenceTypeDefinitionMember> allMembers()
	{
		return this.allMembers;
	}
	
	private String getEnumName(final Binary bytes)
	{
		/* (09.06.2017 TM)FIXME: priv#23: BinaryHandlerEnum#getEnumName()
		 * Must use bytes.buildItemAddress() plus offset to the Enum#name field
		 * Hm. But that is a String reference and not present, yet.
		 * Maybe use the ordinal after all? In the end, that has to be consistent, anyway.
		 */
//		return this.instantiator.newInstance();
		
		throw new one.microstream.meta.NotImplementedYetError();
	}
	
	@Override
	public final T create(final Binary bytes, final PersistenceLoadHandler handler)
	{
		// (23.07.2019 TM)EXCP: proper exception
		throw new UnsupportedOperationException(
			"Instances of an enum type NEVER get created by the library. Only the JVM does that."
		);
	}
	
	@Override
	public void update(final Binary bytes, final T instance, final PersistenceLoadHandler handler)
	{
		/* (09.06.2017 TM)FIXME: priv#23: BinaryHandlerEnum#update()
		 * must not set Enum#ordinal and Enum#name, but rather validate the loaded data's consistency in regard
		 * to them.
		 * Only the other fields may get updated.
		 * 
		 */
		throw new one.microstream.meta.NotImplementedYetError();
	}

}
