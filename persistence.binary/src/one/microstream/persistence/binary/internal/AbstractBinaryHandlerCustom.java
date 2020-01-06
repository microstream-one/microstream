package one.microstream.persistence.binary.internal;

import static one.microstream.X.notNull;

import java.lang.reflect.Field;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import one.microstream.X;
import one.microstream.chars.XChars;
import one.microstream.collections.BulkList;
import one.microstream.collections.EqHashTable;
import one.microstream.collections.HashTable;
import one.microstream.collections.XArrays;
import one.microstream.collections.types.XAddingCollection;
import one.microstream.collections.types.XGettingEnum;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.collections.types.XGettingTable;
import one.microstream.collections.types.XImmutableEnum;
import one.microstream.collections.types.XImmutableSequence;
import one.microstream.exceptions.NoSuchFieldRuntimeException;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.BinaryPersistence;
import one.microstream.persistence.binary.types.BinaryTypeHandler;
import one.microstream.persistence.exceptions.PersistenceException;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;
import one.microstream.persistence.types.PersistenceTypeDefinitionMember;
import one.microstream.persistence.types.PersistenceTypeDefinitionMemberFieldGeneric;
import one.microstream.persistence.types.PersistenceTypeDefinitionMemberFieldGenericComplex;
import one.microstream.persistence.types.PersistenceTypeDefinitionMemberFieldGenericSimple;
import one.microstream.persistence.types.PersistenceTypeDefinitionMemberFieldGenericVariableLength;
import one.microstream.persistence.types.PersistenceTypeDescriptionMember;
import one.microstream.persistence.types.PersistenceTypeDescriptionMemberFieldGeneric;
import one.microstream.reflect.Getter;
import one.microstream.reflect.Getter_boolean;
import one.microstream.reflect.Getter_byte;
import one.microstream.reflect.Getter_char;
import one.microstream.reflect.Getter_double;
import one.microstream.reflect.Getter_float;
import one.microstream.reflect.Getter_int;
import one.microstream.reflect.Getter_long;
import one.microstream.reflect.Getter_short;
import one.microstream.reflect.Setter;
import one.microstream.reflect.Setter_boolean;
import one.microstream.reflect.Setter_byte;
import one.microstream.reflect.Setter_char;
import one.microstream.reflect.Setter_double;
import one.microstream.reflect.Setter_float;
import one.microstream.reflect.Setter_int;
import one.microstream.reflect.Setter_long;
import one.microstream.reflect.Setter_short;
import one.microstream.reflect.XReflect;


public abstract class AbstractBinaryHandlerCustom<T>
extends BinaryTypeHandler.Abstract<T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static final XImmutableSequence<PersistenceTypeDefinitionMemberFieldGeneric>
	defineValueType(final Class<?> valueType)
	{
		return X.Constant(CustomField(valueType, "value"));
	}
	
	public static final PersistenceTypeDefinitionMemberFieldGenericSimple CustomField(
		final Class<?> type,
		final String   name
	)
	{
		return CustomField(type, null, name);
	}
	
	public static final PersistenceTypeDefinitionMemberFieldGenericSimple CustomField(
		final Class<?> type     ,
		final String   qualifier,
		final String   name
	)
	{
		return PersistenceTypeDefinitionMemberFieldGenericSimple.New(
			type.getName(),
			qualifier,
			name,
			type,
			!type.isPrimitive(),
			BinaryPersistence.resolveFieldBinaryLength(type),
			BinaryPersistence.resolveFieldBinaryLength(type)
		);
	}
	
	public static final PersistenceTypeDefinitionMemberFieldGeneric chars(final String name)
	{
		return PersistenceTypeDefinitionMemberFieldGenericVariableLength.Chars(
			name,
			Binary.binaryListMinimumLength(),
			Binary.binaryListMaximumLength()
		);
	}

	public static final PersistenceTypeDefinitionMemberFieldGeneric bytes(final String name)
	{
		return PersistenceTypeDefinitionMemberFieldGenericVariableLength.Bytes(
			name,
			Binary.binaryListMinimumLength(),
			Binary.binaryListMaximumLength()
		);
	}

	public static final XImmutableSequence<PersistenceTypeDefinitionMemberFieldGeneric>
	CustomFields(final PersistenceTypeDefinitionMemberFieldGeneric... customFields)
	{
		return X.ConstList(customFields);
	}

	public static final PersistenceTypeDefinitionMemberFieldGenericComplex
	Complex(
		final String                                          name        ,
		final PersistenceTypeDescriptionMemberFieldGeneric... customFields
	)
	{
		return PersistenceTypeDefinitionMemberFieldGenericComplex.New(
			name,
			X.ConstList(customFields),
			Binary.binaryListMinimumLength(),
			Binary.binaryListMaximumLength()
		);
	}
	
	public static final XGettingSequence<? extends PersistenceTypeDefinitionMemberFieldGeneric> SizedArrayFields(
		final PersistenceTypeDefinitionMemberFieldGeneric... preHeaderFields
	)
	{
		return SimpleArrayFields(
			XArrays.add(
				preHeaderFields,
				AbstractBinaryHandlerCustom.CustomField(long.class, "capacity")
			)
		);
	}

	public static final XGettingSequence<? extends PersistenceTypeDefinitionMemberFieldGeneric> SimpleArrayFields(
		final PersistenceTypeDefinitionMemberFieldGeneric... preHeaderFields
	)
	{
		return AbstractBinaryHandlerCustom.CustomFields(
			XArrays.add(
				preHeaderFields,
				AbstractBinaryHandlerCustom.Complex("elements",
					AbstractBinaryHandlerCustom.CustomField(Object.class, "element")
				)
			)
		);
	}
	
	public static final XGettingSequence<? extends PersistenceTypeDefinitionMemberFieldGeneric> keyValuesFields(
		final PersistenceTypeDefinitionMemberFieldGeneric... preHeaderFields
	)
	{
		return AbstractBinaryHandlerCustom.CustomFields(
			XArrays.add(
				preHeaderFields,
				AbstractBinaryHandlerCustom.Complex("elements",
					AbstractBinaryHandlerCustom.CustomField(Object.class, "key"),
					AbstractBinaryHandlerCustom.CustomField(Object.class, "value")
				)
			)
		);
	}
		

	
	protected static final <T> BinaryField<T> Field(
		final Getter_byte<T> getter,
		final Setter_byte<T> setter
	)
	{
		return new BinaryField.Default_byte<>(notNull(getter), notNull(setter));
	}
		
	protected static final <T> BinaryField<T> Field(
		final Getter_boolean<T> getter,
		final Setter_boolean<T> setter
	)
	{
		return new BinaryField.Default_boolean<>(notNull(getter), notNull(setter));
	}
		
	protected static final <T> BinaryField<T> Field(
		final Getter_short<T> getter,
		final Setter_short<T> setter
	)
	{
		return new BinaryField.Default_short<>(notNull(getter), notNull(setter));
	}
		
	protected static final <T> BinaryField<T> Field(
		final Getter_char<T> getter,
		final Setter_char<T> setter
	)
	{
		return new BinaryField.Default_char<>(notNull(getter), notNull(setter));
	}
		
	protected static final <T> BinaryField<T> Field(
		final Getter_int<T> getter,
		final Setter_int<T> setter
	)
	{
		return new BinaryField.Default_int<>(notNull(getter), notNull(setter));
	}
		
	protected static final <T> BinaryField<T> Field(
		final Getter_float<T> getter,
		final Setter_float<T> setter
	)
	{
		return new BinaryField.Default_float<>(notNull(getter), notNull(setter));
	}
		
	protected static final <T> BinaryField<T> Field(
		final Getter_long<T> getter,
		final Setter_long<T> setter
	)
	{
		return new BinaryField.Default_long<>(notNull(getter), notNull(setter));
	}
		
	protected static final <T> BinaryField<T> Field(
		final Getter_double<T> getter,
		final Setter_double<T> setter
	)
	{
		return new BinaryField.Default_double<>(notNull(getter), notNull(setter));
	}
		
	protected static final <T, R> BinaryField<T> Field(
		final Getter<T, R> getter,
		final Setter<T, R> setter
	)
	{
		return new BinaryField.DefaultReference(notNull(getter), notNull(setter));
	}
	
//	protected static final BinaryField Field(
//		final Class<?> type,
//		final String   name
//	)
//	{
//		return BinaryField.New(type, name);
//	}
	
//	protected static final BinaryField FieldComplex(
//		final PersistenceTypeDefinitionMemberFieldGeneric... nestedFields
//	)
//	{
//		return BinaryField.Complex(nestedFields);
//	}
//
//	protected static final BinaryField FieldComplex(
//		final String                                         name        ,
//		final PersistenceTypeDefinitionMemberFieldGeneric... nestedFields
//	)
//	{
//		return BinaryField.Complex(name, nestedFields);
//	}
//
//	protected static final BinaryField FieldBytes()
//	{
//		return BinaryField.Bytes();
//	}
//
//	protected static final BinaryField FieldBytes(final String name)
//	{
//		return BinaryField.Bytes(name);
//	}
//
//	protected static final BinaryField FieldChars()
//	{
//		return BinaryField.Chars();
//	}
//
//	protected static final BinaryField FieldChars(final String name)
//	{
//		return BinaryField.Chars(name);
//	}
	
	protected static final Field getInstanceFieldOfType(
		final Class<?> declaringType,
		final Class<?> fieldType
	)
		throws NoSuchFieldRuntimeException
	{
		return XReflect.setAccessible(
			XReflect.getInstanceFieldOfType(declaringType, fieldType)
		);
	}

	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private XImmutableEnum<? extends PersistenceTypeDefinitionMember> members;
	
	private final long binaryLengthMinimum;
	private final long binaryLengthMaximum;
	
	// (06.01.2020 TM)FIXME: priv#88: why is this needed?
	private Class<?> initializationInvokingClass;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	protected AbstractBinaryHandlerCustom(final Class<T> type)
	{
		this(type, null);
	}

	protected AbstractBinaryHandlerCustom(
		final Class<T>                                                    type   ,
		final XGettingSequence<? extends PersistenceTypeDefinitionMember> members
	)
	{
		this(type, deriveTypeName(type), members);
	}
	
	protected AbstractBinaryHandlerCustom(
		final Class<T>                                                    type    ,
		final String                                                      typeName,
		final XGettingSequence<? extends PersistenceTypeDefinitionMember> members
	)
	{
		super(type, typeName);
		this.members = validateAndImmure(members);
		this.binaryLengthMinimum = PersistenceTypeDescriptionMember.calculatePersistentMinimumLength(0, members);
		this.binaryLengthMaximum = PersistenceTypeDescriptionMember.calculatePersistentMaximumLength(0, members);
	}
	
	

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public boolean isPrimitiveType()
	{
		return false;
	}
	
	@Override
	public XGettingEnum<? extends PersistenceTypeDefinitionMember> allMembers()
	{
		// with the exception of some special types (primitive definition and enums), there are only instance members.
		return this.instanceMembers();
	}
	
	@Override
	public synchronized XGettingEnum<? extends PersistenceTypeDefinitionMember> instanceMembers()
	{
		if(this.members == null)
		{
			this.initializeInstanceMembers();
		}
		
		return this.members;
	}
	
	protected void initializeInstanceMembers()
	{
		if(this.members != null)
		{
			// already initialized, e.g. via constructor
			return;
		}
		
		// (06.01.2020 TM)FIXME: priv#88: search for BinaryFields in class hiararchy
		this.initializeBinaryFieldsExplicitely(this.getClass());
		throw new one.microstream.meta.NotImplementedYetError();
	}
	
	@Override
	public long membersPersistedLengthMinimum()
	{
		return this.binaryLengthMinimum;
	}
	
	@Override
	public long membersPersistedLengthMaximum()
	{
		return this.binaryLengthMaximum;
	}

	@Override
	public abstract void store(Binary data, T instance, long objectId, PersistenceStoreHandler handler);

	@Override
	public void iterateInstanceReferences(final T instance, final PersistenceFunction iterator)
	{
		// no-op, no references
	}

	@Override
	public abstract T create(Binary data, PersistenceLoadHandler handler);

	@Override
	public void complete(final Binary data, final T instance, final PersistenceLoadHandler handler)
	{
		// no-op for normal implementation (see non-reference-hashing collections for other examples)
	}
	
	@Override
	public <C extends Consumer<? super Class<?>>> C iterateMemberTypes(final C logic)
	{
		// native handling logic should normally not have any member types that have to be iterated here
		return logic;
	}

	protected final synchronized void initializeBinaryFieldsExplicitely(final Class<?> invokingClass)
	{
		if(this.initializationInvokingClass != null)
		{
			if(this.initializationInvokingClass == invokingClass)
			{
				// consistent no-op, abort.
				return;
			}
			
			// (04.04.2019 TM)EXCP: proper exception
			throw new PersistenceException(
				XChars.systemString(this)
				+ " already initialized by an invokation from class "
				+ this.initializationInvokingClass.getName()
			);
		}
		
		this.initializeBinaryFields();
		this.initializationInvokingClass = invokingClass;
	}
	
	protected final synchronized void initializeBinaryFields()
	{
		final HashTable<Class<?>, XGettingSequence<BinaryField.Initializable>> binaryFieldsPerClass = HashTable.New();
		
		this.collectBinaryFields(binaryFieldsPerClass);

		final EqHashTable<String, BinaryField.Initializable> binaryFieldsInOrder = EqHashTable.New();
		this.defineBinaryFieldOrder(binaryFieldsPerClass, (name, field) ->
		{
			/* (17.04.2019 TM)FIXME: priv#88: name must be unique.
			 * Also see about PersistenceTypeDefinitionMember in BinaryField.
			 */
			if(!binaryFieldsInOrder.add(name, field))
			{
				// (04.04.2019 TM)EXCP: proper exception
				throw new PersistenceException(
					BinaryField.class.getSimpleName() + " with the name \"" + name + "\" is already registered."
				);
			}
		});
		
		this.initializeBinaryFieldOffsets(binaryFieldsInOrder);
		// (18.04.2019 TM)FIXME: priv#88: assign to members field here or somewhere appropriate.
	}
	
	private void collectBinaryFields(
		final HashTable<Class<?>, XGettingSequence<BinaryField.Initializable>> binaryFieldsPerClass
	)
	{
		for(Class<?> c = this.getClass(); c != AbstractBinaryHandlerCustom.class; c = c.getSuperclass())
		{
			/*
			 * This construction is necessary to maintain the collection order even if a class
			 * overrides the collecting logic
			 */
			final BulkList<BinaryField.Initializable> binaryFieldsOfClass = BulkList.New();
			this.collectDeclaredBinaryFields(c, binaryFieldsOfClass);

			// already existing entries (added by an extending class in an override of this method) are allowed
			binaryFieldsPerClass.add(c, binaryFieldsOfClass);
		}
	}
	
	protected void collectDeclaredBinaryFields(
		final Class<?>                                     clazz ,
		final XAddingCollection<BinaryField.Initializable> target
	)
	{
		for(final Field field : clazz.getDeclaredFields())
		{
			if(!BinaryField.class.isAssignableFrom(field.getType()))
			{
				continue;
			}
			try
			{
				field.setAccessible(true);
				final BinaryField binaryField = (BinaryField)field.get(this);
				if(!(binaryField instanceof BinaryField.Initializable))
				{
					continue;
				}
				
				final BinaryField.Initializable initializable = (BinaryField.Initializable)binaryField;
				initializable.initializeName(field.getName());
				target.add(initializable);
			}
			catch(final Exception e)
			{
				// (17.04.2019 TM)EXCP: proper exception
				throw new PersistenceException(e);
			}
		}
	}
	
	protected void defineBinaryFieldOrder(
		final XGettingTable<Class<?>, XGettingSequence<BinaryField.Initializable>> binaryFieldsPerClass,
		final BiConsumer<String, BinaryField.Initializable>                        collector
	)
	{
		/*
		 * With the class hiararchy collection order guaranteed above, this loop does:
		 * - order binaryFields from most to least specific class ("upwards")
		 * - order binaryFields per class in declaration order
		 */
		for(final XGettingSequence<BinaryField.Initializable> binaryFieldsOfClass : binaryFieldsPerClass.values())
		{
			for(final BinaryField.Initializable binaryField : binaryFieldsOfClass)
			{
				collector.accept(binaryField.name(), binaryField);
			}
		}
	}
	
	private void initializeBinaryFieldOffsets(final XGettingTable<String, BinaryField.Initializable> binaryFields)
	{
		validateVariableLengthLayout(binaryFields);
		
		long offset = 0;
		for(final BinaryField.Initializable binaryField : binaryFields.values())
		{
			binaryField.initializeOffset(offset);
			offset += binaryField.persistentMinimumLength();
		}
		// note: a trailing variable length field sets the offset to an invalid state, but that is never read.
	}
	
	/**
	 * Only the last field may have variable length, otherweise simple offsets can't be used.
	 * 
	 * @param binaryFields
	 */
	private static void validateVariableLengthLayout(final XGettingTable<String, ? extends BinaryField> binaryFields)
	{
		if(binaryFields.size() <= 1)
		{
			// no fields or a single field is implicitely valid.
			return;
		}
		
		final BinaryField lastBinaryField = binaryFields.values().peek();
		for(final BinaryField binaryField : binaryFields.values())
		{
			if(!binaryField.isVariableLength())
			{
				continue;
			}
			if(binaryField != lastBinaryField)
			{
				// (18.04.2019 TM)EXCP: proper exception
				throw new PersistenceException("Non-last binary field with variable length: " + binaryField.name());
			}
		}
	}
	
	@Override
	protected void internalInitialize()
	{
		this.initializeBinaryFieldsExplicitely(this.getClass());
	}

}
