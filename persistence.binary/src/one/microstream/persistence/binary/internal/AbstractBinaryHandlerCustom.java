package one.microstream.persistence.binary.internal;

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
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceObjectIdAcceptor;
import one.microstream.persistence.types.PersistenceStoreHandler;
import one.microstream.persistence.types.PersistenceTypeDefinitionMember;
import one.microstream.persistence.types.PersistenceTypeDefinitionMemberPseudoField;
import one.microstream.persistence.types.PersistenceTypeDefinitionMemberPseudoFieldComplex;
import one.microstream.persistence.types.PersistenceTypeDefinitionMemberPseudoFieldSimple;
import one.microstream.persistence.types.PersistenceTypeDefinitionMemberPseudoFieldVariableLength;
import one.microstream.persistence.types.PersistenceTypeDescriptionMember;
import one.microstream.persistence.types.PersistenceTypeDescriptionMemberPseudoField;
import one.microstream.reflect.XReflect;


public abstract class AbstractBinaryHandlerCustom<T>
extends BinaryTypeHandler.Abstract<T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static final XImmutableSequence<PersistenceTypeDefinitionMemberPseudoField>
	defineValueType(final Class<?> valueType)
	{
		return X.Constant(pseudoField(valueType, "value"));
	}
	
	public static final PersistenceTypeDefinitionMemberPseudoFieldSimple pseudoField(
		final Class<?> type,
		final String   name
	)
	{
		return PersistenceTypeDefinitionMemberPseudoFieldSimple.New(
			name,
			type.getName(),
			type,
			!type.isPrimitive(),
			BinaryPersistence.resolveFieldBinaryLength(type),
			BinaryPersistence.resolveFieldBinaryLength(type)
		);
	}
	
	public static final PersistenceTypeDefinitionMemberPseudoField chars(final String name)
	{
		return PersistenceTypeDefinitionMemberPseudoFieldVariableLength.Chars(
			name,
			Binary.binaryListMinimumLength(),
			Binary.binaryListMaximumLength()
		);
	}

	public static final PersistenceTypeDefinitionMemberPseudoField bytes(final String name)
	{
		return PersistenceTypeDefinitionMemberPseudoFieldVariableLength.Bytes(
			name,
			Binary.binaryListMinimumLength(),
			Binary.binaryListMaximumLength()
		);
	}

	public static final XImmutableSequence<PersistenceTypeDefinitionMemberPseudoField>
	pseudoFields(final PersistenceTypeDefinitionMemberPseudoField... pseudoFields)
	{
		return X.ConstList(pseudoFields);
	}

	public static final PersistenceTypeDefinitionMemberPseudoFieldComplex
	complex(
		final String                                         name        ,
		final PersistenceTypeDescriptionMemberPseudoField... pseudoFields
	)
	{
		return PersistenceTypeDefinitionMemberPseudoFieldComplex.New(
			name,
			X.ConstList(pseudoFields),
			Binary.binaryListMinimumLength(),
			Binary.binaryListMaximumLength()
		);
	}
	
	public static final XGettingSequence<? extends PersistenceTypeDefinitionMemberPseudoField> sizedArrayPseudoFields(
		final PersistenceTypeDefinitionMemberPseudoField... preHeaderFields
	)
	{
		return simpleArrayPseudoFields(
			XArrays.add(
				preHeaderFields,
				AbstractBinaryHandlerCustom.pseudoField(long.class, "capacity")
			)
		);
	}

	public static final XGettingSequence<? extends PersistenceTypeDefinitionMemberPseudoField> simpleArrayPseudoFields(
		final PersistenceTypeDefinitionMemberPseudoField... preHeaderFields
	)
	{
		return AbstractBinaryHandlerCustom.pseudoFields(
			XArrays.add(
				preHeaderFields,
				AbstractBinaryHandlerCustom.complex("elements",
					AbstractBinaryHandlerCustom.pseudoField(Object.class, "element")
				)
			)
		);
	}
	
	public static final XGettingSequence<? extends PersistenceTypeDefinitionMemberPseudoField> keyValuesPseudoFields(
		final PersistenceTypeDefinitionMemberPseudoField... preHeaderFields
	)
	{
		return AbstractBinaryHandlerCustom.pseudoFields(
			XArrays.add(
				preHeaderFields,
				AbstractBinaryHandlerCustom.complex("elements",
					AbstractBinaryHandlerCustom.pseudoField(Object.class, "key"),
					AbstractBinaryHandlerCustom.pseudoField(Object.class, "value")
				)
			)
		);
	}
	
	/* (04.04.2019 TM)TODO: MS-130 BinaryField value-get/set-support
	 * To get rid of explicit offsets altogether, BinaryField could provide
	 * 9 methods to store the 8 primitives and the reference case.
	 * That would require 2 subclasses of BinaryField.
	 * The primitive implementation could convert to and from every primitive type and throw an exception for the reference case.
	 * The reference case implementation accordingly.
	 */
	
	protected static final BinaryField Field(
		final Class<?> type
	)
	{
		return BinaryField.New(type);
	}
	
	protected static final BinaryField Field(
		final Class<?> type,
		final String   name
	)
	{
		return BinaryField.New(type, name);
	}
	
	protected static final BinaryField FieldComplex(
		final PersistenceTypeDefinitionMemberPseudoField... nestedPseudoFields
	)
	{
		return BinaryField.Complex(nestedPseudoFields);
	}
	
	protected static final BinaryField FieldComplex(
		final String                                        name              ,
		final PersistenceTypeDefinitionMemberPseudoField... nestedPseudoFields
	)
	{
		return BinaryField.Complex(name, nestedPseudoFields);
	}
	
	protected static final BinaryField FieldBytes()
	{
		return BinaryField.Bytes();
	}
	
	protected static final BinaryField FieldBytes(final String name)
	{
		return BinaryField.Bytes(name);
	}
	
	protected static final BinaryField FieldChars()
	{
		return BinaryField.Chars();
	}
	
	protected static final BinaryField FieldChars(final String name)
	{
		return BinaryField.Chars(name);
	}
	
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

	private final XImmutableEnum<? extends PersistenceTypeDefinitionMember> members;
	private final long binaryLengthMinimum;
	private final long binaryLengthMaximum;
	
	private Class<?> initializationInvokingClass;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	protected AbstractBinaryHandlerCustom(
		final Class<T>                                                    type   ,
		final XGettingSequence<? extends PersistenceTypeDefinitionMember> members
	)
	{
		super(type);
		// (18.04.2019 TM)FIXME: MS-130: replace by on-demand member-initialization
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
	public XGettingEnum<? extends PersistenceTypeDefinitionMember> members()
	{
		return this.members;
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
	public abstract void store(Binary bytes, T instance, long objectId, PersistenceStoreHandler handler);

	@Override
	public void iterateInstanceReferences(final T instance, final PersistenceFunction iterator)
	{
		// no-op, no references
	}

	@Override
	public void iteratePersistedReferences(final Binary offset, final PersistenceObjectIdAcceptor iterator)
	{
		// no-op, no references
	}

	@Override
	public abstract T create(Binary bytes, PersistenceLoadHandler handler);

	@Override
	public void update(final Binary bytes, final T instance, final PersistenceLoadHandler handler)
	{
		/* No-op update logic by default. This is useful for all immutable value types (String, Integer, etc.).
		 * Value types never get updated. The value is only set once at instance creation time.
		 * Subsequently provided (potentially different) values are ignored intentionally.
		 */
	}

	@Override
	public void complete(final Binary bytes, final T instance, final PersistenceLoadHandler handler)
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
			throw new RuntimeException(
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
			/* (17.04.2019 TM)FIXME: MS-130: name must be unique.
			 * Also see about PersistenceTypeDefinitionMember in BinaryField.
			 */
			if(!binaryFieldsInOrder.add(name, field))
			{
				// (04.04.2019 TM)EXCP: proper exception
				throw new RuntimeException(
					BinaryField.class.getSimpleName() + " with the name \"" + name + "\" is already registered."
				);
			}
		});
		
		this.initializeBinaryFieldOffsets(binaryFieldsInOrder);
		// (18.04.2019 TM)FIXME: MS-130: assign to members field here or somewhere appropriate.
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
				throw new RuntimeException(e);
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
				throw new RuntimeException("Non-last binary field with variable length: " + binaryField.name());
			}
		}
	}
	
	@Override
	protected void internalInitialize()
	{
		this.initializeBinaryFieldsExplicitely(this.getClass());
	}

}
