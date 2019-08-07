package one.microstream.java;

import static one.microstream.X.notNull;

import java.lang.reflect.Field;

import one.microstream.collections.BulkList;
import one.microstream.collections.EqConstHashEnum;
import one.microstream.collections.EqHashEnum;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.collections.types.XGettingEnum;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.collections.types.XImmutableEnum;
import one.microstream.meta.XDebug;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerReflective;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceEagerStoringFieldEvaluator;
import one.microstream.persistence.types.PersistenceFieldLengthResolver;
import one.microstream.persistence.types.PersistenceObjectIdResolver;
import one.microstream.persistence.types.PersistenceTypeDefinitionMember;
import one.microstream.persistence.types.PersistenceTypeDefinitionMemberEnumConstant;
import one.microstream.persistence.types.PersistenceTypeDefinitionMemberFieldReflective;

public final class BinaryHandlerEnum<T extends Enum<T>> extends AbstractBinaryHandlerReflective<T>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////
	
	private static final String JAVA_LANG_ENUM_FIELD_NAME_NAME    = "name"   ;
	private static final String JAVA_LANG_ENUM_FIELD_NAME_ORDINAL = "ordinal";
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static boolean isJavaLangEnumMember(final PersistenceTypeDefinitionMemberFieldReflective member)
	{
		return member != null
			&& member.declaringClass() == java.lang.Enum.class
		;
	}
	
	public static boolean isJavaLangEnumName(final PersistenceTypeDefinitionMemberFieldReflective member)
	{
		return isJavaLangEnumMember(member)
			&& JAVA_LANG_ENUM_FIELD_NAME_NAME.equals(member.name())
		;
	}
	
	public static boolean isJavaLangEnumOrdinal(final PersistenceTypeDefinitionMemberFieldReflective member)
	{
		return isJavaLangEnumMember(member)
			&& JAVA_LANG_ENUM_FIELD_NAME_ORDINAL.equals(member.name())
		;
	}
	
	public static long calculateBinaryOffset(
		final PersistenceTypeDefinitionMemberFieldReflective                               member ,
		final XGettingCollection<? extends PersistenceTypeDefinitionMemberFieldReflective> members
	)
	{
		long binaryOffset = 0;
		
		for(final PersistenceTypeDefinitionMemberFieldReflective m : members)
		{
			if(m == member)
			{
				return binaryOffset;
			}
			
			binaryOffset += equal(m.persistentMinimumLength(), m.persistentMaximumLength());
		}
		
		// (01.08.2019 TM)EXCP: proper exception
		throw new RuntimeException("Member not found in member list: " + member.identifier());
	}
	
	private static <E extends Enum<E>> XImmutableEnum<PersistenceTypeDefinitionMember> deriveAllMembers(
		final Class<E>                                                    type           ,
		final XGettingSequence<? extends PersistenceTypeDefinitionMember> instanceMembers
	)
	{
		final XGettingSequence<PersistenceTypeDefinitionMemberEnumConstant> enumConstants = deriveEnumMembers(type);
		
		final EqHashEnum<PersistenceTypeDefinitionMember> allMembers = MemberEnum()
			.addAll(enumConstants)
			.addAll(instanceMembers)
		;
		
		return allMembers.immure();
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
	
	
	
	public static <T extends Enum<T>> BinaryHandlerEnum<T> New(
		final Class<T>                              type                      ,
		final String                                typeName                  ,
		final XGettingEnum<Field>                   allFields                 ,
		final PersistenceFieldLengthResolver        lengthResolver            ,
		final PersistenceEagerStoringFieldEvaluator eagerStoringFieldEvaluator,
		final boolean                               switchByteOrder
	)
	{
		return new BinaryHandlerEnum<>(
			type                      ,
			typeName                  ,
			allFields                 ,
			lengthResolver            ,
			eagerStoringFieldEvaluator,
			switchByteOrder
		);
	}
		
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
		
	private final PersistenceTypeDefinitionMemberFieldReflective java_lang_Enum_name   ;
	private final PersistenceTypeDefinitionMemberFieldReflective java_lang_Enum_ordinal;
	
	// offsets must be determined per handler instance since different types have different persistent form offsets.
	// (01.08.2019 TM)FIXME: priv#23: what about binaryOffsetName?
	private final long binaryOffsetName   ;
	private final long binaryOffsetOrdinal;
	
	private final XImmutableEnum<PersistenceTypeDefinitionMember> allMembers;
	


	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	protected BinaryHandlerEnum(
		final Class<T>                              type                      ,
		final String                                typeName                  ,
		final XGettingEnum<Field>                   persistableFields         ,
		final PersistenceFieldLengthResolver        lengthResolver            ,
		final PersistenceEagerStoringFieldEvaluator eagerStoringFieldEvaluator,
		final boolean                               switchByteOrder
	)
	{
		super(type, typeName, persistableFields, lengthResolver, eagerStoringFieldEvaluator, switchByteOrder);
		this.allMembers             = deriveAllMembers(type, this.instanceMembers());
		this.java_lang_Enum_name    = notNull(this.instanceMembers().search(BinaryHandlerEnum::isJavaLangEnumName));
		this.java_lang_Enum_ordinal = notNull(this.instanceMembers().search(BinaryHandlerEnum::isJavaLangEnumOrdinal));
		this.binaryOffsetName       = calculateBinaryOffset(this.java_lang_Enum_name   , this.instanceMembers());
		this.binaryOffsetOrdinal    = calculateBinaryOffset(this.java_lang_Enum_ordinal, this.instanceMembers());
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// initializer logic //
	//////////////////////
	
	@Override
	protected EqConstHashEnum<PersistenceTypeDefinitionMemberFieldReflective> filterSettingMembers(
		final EqConstHashEnum<PersistenceTypeDefinitionMemberFieldReflective> members
	)
	{
		return members.filterTo(MemberEnum(), this::notJavaLangEnumField).immure();
	}
	
	@Override
	protected long[] initializeSettingMemoryOffsets()
	{
		// additional long[] must be created instead of referencing that for storing offsets
		return objectFieldOffsets(this.instanceSettingMembers());
	}


	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	protected final boolean notJavaLangEnumField(final PersistenceTypeDefinitionMemberFieldReflective m)
	{
		// (01.08.2019 TM)FIXME: priv#23: DEBUG
		XDebug.println(m.identifier() + " declaring class = " + m.declaringClass());
		
		// quick check for "normal" members but also essential for the checking logic below
		if(m.declaringClass() != java.lang.Enum.class)
		{
			return true;
		}
		
		// should Enum fields ever change, it is important to at least notice it and abort.
		if(isJavaLangEnumName(m) || isJavaLangEnumOrdinal(m))
		{
			return false;
		}
		
		// (01.08.2019 TM)EXCP: proper exception
		throw new RuntimeException("Unknown " + java.lang.Enum.class.getName() + " field: " + m.name());
	}
	
	@Override
	public final XGettingEnum<? extends PersistenceTypeDefinitionMember> allMembers()
	{
		return this.allMembers;
	}
	
	@Override
	public final T create(final Binary bytes, final PersistenceObjectIdResolver idResolver)
	{
		// (23.07.2019 TM)EXCP: proper exception
		throw new UnsupportedOperationException(
			"Instances of an enum type NEVER get created by the library. Only the JVM does that."
		);
	}
	
	private void validateOrdinal(final Binary bytes, final T instance)
	{
		final int ordinal = bytes.get_int(this.binaryOffsetOrdinal);
		if(ordinal == instance.ordinal())
		{
			return;
		}
		
		// (01.08.2019 TM)EXCP: proper exception
		throw new RuntimeException(
			"Inconcistency for " + instance.getDeclaringClass().getName() + "." + instance.name()
		);
	}
	
	@Override
	public void update(final Binary bytes, final T instance, final PersistenceObjectIdResolver idResolver)
	{
		this.validateOrdinal(bytes, instance);
		
		// super class logic already uses only setting members, i.e. not ordinal and name.
		super.update(bytes, instance, idResolver);
	}

}
