package one.microstream.persistence.binary.internal;

import java.lang.reflect.Field;
import java.util.function.Predicate;

import one.microstream.collections.BulkList;
import one.microstream.collections.EqConstHashEnum;
import one.microstream.collections.EqHashEnum;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.collections.types.XGettingEnum;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.collections.types.XImmutableEnum;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceEagerStoringFieldEvaluator;
import one.microstream.persistence.types.PersistenceFieldLengthResolver;
import one.microstream.persistence.types.PersistenceObjectIdResolver;
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
//		return member != null
//			&& member.declaringClass() == java.lang.Enum.class
//			&& XReflect.isFinal(member.field())
//		;
	}
	
	public static boolean isJavaLangEnumName(final PersistenceTypeDefinitionMember member)
	{
		// intentionally no reference to the name since. Although final modifier and field type is not much better ...
		return isJavaLangEnumMember(member)
			&& member.type() == String.class
		;
	}
	
	public static boolean isJavaLangEnumOrdinal(final PersistenceTypeDefinitionMember member)
	{
		// intentionally no reference to the name since. Although final modifier and field type is not much better ...
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
		
		// (01.08.2019 TM)EXCP: proper exception
		throw new RuntimeException("Member not found in member list.");
	}
	
	private static <E extends Enum<E>> XImmutableEnum<PersistenceTypeDefinitionMember> deriveAllMembers(
		final Class<E>                                                    type           ,
		final XGettingSequence<? extends PersistenceTypeDefinitionMember> instanceMembers
	)
	{
		final XGettingSequence<PersistenceTypeDefinitionMemberEnumConstant> enumConstants =
			deriveEnumConstantMembers(type)
		;
		
		final EqHashEnum<PersistenceTypeDefinitionMember> allMembers = MemberEnum()
			.addAll(enumConstants)
			.addAll(instanceMembers)
		;
		
		return allMembers.immure();
	}
	
	public static XGettingSequence<PersistenceTypeDefinitionMemberEnumConstant> deriveEnumConstantMembers(
		final Class<?> enumType
	)
	{
		// can't use generics typing due to broken Object#getClass.
		XReflect.validateIsEnum(enumType);
		
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
	
	
	
	public static <T extends Enum<T>> BinaryHandlerGenericEnum<T> New(
		final Class<T>                              type                      ,
		final String                                typeName                  ,
		final XGettingEnum<Field>                   allFields                 ,
		final PersistenceFieldLengthResolver        lengthResolver            ,
		final PersistenceEagerStoringFieldEvaluator eagerStoringFieldEvaluator,
		final boolean                               switchByteOrder
	)
	{
		return new BinaryHandlerGenericEnum<>(
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
	
	// offsets must be determined per handler instance since different types have different persistent form offsets.
	private final long binaryOffsetName   ;
	private final long binaryOffsetOrdinal;
	
	private final XImmutableEnum<PersistenceTypeDefinitionMember> allMembers;
	


	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	protected BinaryHandlerGenericEnum(
		final Class<T>                              type                      ,
		final String                                typeName                  ,
		final XGettingEnum<Field>                   persistableFields         ,
		final PersistenceFieldLengthResolver        lengthResolver            ,
		final PersistenceEagerStoringFieldEvaluator eagerStoringFieldEvaluator,
		final boolean                               switchByteOrder
	)
	{
		super(type, typeName, persistableFields, lengthResolver, eagerStoringFieldEvaluator, switchByteOrder);
		
		final XGettingEnum<? extends PersistenceTypeDefinitionMember> fields = this.instanceMembers();
		this.allMembers          = deriveAllMembers(type, fields);
		this.binaryOffsetName    = calculateBinaryOffsetName(this);
		this.binaryOffsetOrdinal = calculateBinaryOffsetOrdinal(this);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// initializer logic //
	//////////////////////
	
	@Override
	protected EqConstHashEnum<PersistenceTypeDefinitionMemberFieldReflective> filterSettingMembers(
		final EqConstHashEnum<PersistenceTypeDefinitionMemberFieldReflective> members
	)
	{
		return members.filterTo(MemberEnum(), this::settableField).immure();
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
	
	protected final boolean settableField(final PersistenceTypeDefinitionMemberFieldReflective m)
	{
		/*
		 * Final primitive fields and final value type fields ("value instance constants") may not be settable, either.
		 * E.g. a final int holding some kind of business-logical uniqueId. That may not be changed.
		 * Final-ly referenced "true" instances might be a different story since they might be meant as a kind of "stub"
		 * instance for an entity (sub-)graph and are probably expected to be filled upon loading the enum instance.
		 * Any mutable field must be loaded and set, that is no question.
		 * But silently changing a final primitive field's value ... that can actually only be a bug.
		 */
		return !this.isJavaLangEnumField(m) && !this.isFinalPrimitiveField(m) && !this.isFinalValueTypeField(m);
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
		
		// (01.08.2019 TM)EXCP: proper exception
		throw new RuntimeException("Unknown " + java.lang.Enum.class.getName() + " field: " + m.name());
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
	public final T create(final Binary bytes, final PersistenceObjectIdResolver idResolver)
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
		
		return XReflect.resolveEnumConstantInstanceTyped(this.type(), this.getPersistedEnumOrdinal(bytes));
	}
	
	@Override
	public int getPersistedEnumOrdinal(final Binary bytes)
	{
		return bytes.get_int(this.binaryOffsetOrdinal);
	}
	
	public String getName(final Binary bytes, final PersistenceObjectIdResolver idResolver)
	{
		return (String)idResolver.lookupObject(bytes.get_long(this.binaryOffsetName));
	}
	
	private void validate(
		final Binary                      bytes     ,
		final T                           instance  ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		// validate ordinal, just in case.
		final int persistentOrdinal = this.getPersistedEnumOrdinal(bytes);
		if(persistentOrdinal != instance.ordinal())
		{
			// (01.08.2019 TM)EXCP: proper exception
			throw new RuntimeException(
				"Inconcistency for " + instance.getDeclaringClass().getName() + "." + instance.name()
			);
		}
		
		final String persistentName = this.getName(bytes, idResolver);
		if(!instance.name().equals(persistentName))
		{
			// (09.08.2019 TM)EXCP: proper exception
			throw new RuntimeException(
				"Enum constant inconsistency:"
				+ " in type " + this.type().getName()
				+ " persisted instance with ordinal " + persistentOrdinal + ", name \"" + persistentName + "\""
				+ " does not match"
				+ " JVM-created instance with ordinal " + instance.ordinal() + ", name \"" + instance.name() + "\""
			);
		}
	}
		
	@Override
	public void update(final Binary bytes, final T instance, final PersistenceObjectIdResolver idResolver)
	{
		// must thoroughly validate the linked jvm-generated(!) instance before modifying its state!
		this.validate(bytes, instance, idResolver);
		
		// super class logic already uses only setting members, i.e. not ordinal and name.
		super.update(bytes, instance, idResolver);
	}

}
