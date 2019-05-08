package one.microstream.persistence.binary.internal;

import static one.microstream.functional.XFunc.not;

import java.lang.reflect.Field;
import java.util.function.Consumer;
import java.util.function.Predicate;

import one.microstream.collections.BulkList;
import one.microstream.collections.ConstHashEnum;
import one.microstream.collections.EqConstHashEnum;
import one.microstream.collections.EqHashEnum;
import one.microstream.collections.HashTable;
import one.microstream.collections.types.XGettingEnum;
import one.microstream.collections.types.XGettingTable;
import one.microstream.collections.types.XImmutableEnum;
import one.microstream.exceptions.TypeCastException;
import one.microstream.functional.XFunc;
import one.microstream.memory.XMemory;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.BinaryPersistence;
import one.microstream.persistence.binary.types.BinaryTypeHandler;
import one.microstream.persistence.binary.types.BinaryValueFunctions;
import one.microstream.persistence.binary.types.BinaryValueSetter;
import one.microstream.persistence.binary.types.BinaryValueStorer;
import one.microstream.persistence.types.PersistenceEagerStoringFieldEvaluator;
import one.microstream.persistence.types.PersistenceFieldLengthResolver;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceObjectIdAcceptor;
import one.microstream.persistence.types.PersistenceStoreHandler;
import one.microstream.persistence.types.PersistenceTypeDefinitionMember;
import one.microstream.persistence.types.PersistenceTypeDefinitionMemberField;
import one.microstream.persistence.types.PersistenceTypeHandlerReflective;
import one.microstream.reflect.XReflect;

public abstract class AbstractBinaryHandlerReflective<T>
extends BinaryTypeHandler.Abstract<T>
implements PersistenceTypeHandlerReflective<Binary, T>
{
	// (21.05.2013)XXX: AbstractBinaryHandlerReflective clean up static handling massacre mess

	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SafeVarargs
	protected static final EqConstHashEnum<Field> filter(
		final XGettingEnum<Field> allFields,
		final Predicate<Field>... predicates
	)
	{
		return allFields.filterTo(EqHashEnum.<Field>New(), XFunc.all(predicates)).immure();
	}

	/**
	 * The persisted order of the fields differs from the declared order. For efficiency reasons, all
	 * reference fields come first, the primitive fields come second. The benefit from this is that the
	 * storage's garbage collector does not need to load primitive field data for traversing the entity graph.
	 * It might seem irrelevant if a few bytes more or less are loaded, but considering millions of entities
	 * have to be loaded, the savings can amass to a substantial amount.
	 * The relative order of reference fields and primitive fields respectively is maintained.
	 */
	protected static long fillArraysAndCalculateOffsets(
		final Class<?>                              entityType         ,
		final Field[]                               fieldsDeclaredOrder,
		final Field[]                               fieldsPersistdOrder,
		final BinaryValueStorer[]                   storers            ,
		final BinaryValueSetter[]                   setters            ,
		final PersistenceEagerStoringFieldEvaluator eagerStoreEvaluator,
		final boolean                               switchByteOrder
	)
	{
		final BinaryValueStorer[] refStorers = new BinaryValueStorer[   storers.length];
		final BinaryValueSetter[] refSetters = new BinaryValueSetter[   setters.length];
		final Field[]             refFields  = new Field[   fieldsPersistdOrder.length];
		final BinaryValueStorer[] prmStorers = new BinaryValueStorer[   storers.length];
		final BinaryValueSetter[] prmSetters = new BinaryValueSetter[   setters.length];
		final Field[]             prmFields  = new Field[   fieldsPersistdOrder.length];

		long primitiveTotalBinaryLength = 0;
		int r = 0, p = 0;
		for(int i = 0; i < fieldsDeclaredOrder.length; i++)
		{
			final Class<?> fieldType = fieldsDeclaredOrder[i].getType()                                     ;
			final boolean  isEager  = eagerStoreEvaluator.isEagerStoring(entityType, fieldsDeclaredOrder[i]);
			
			final BinaryValueStorer storer = BinaryValueFunctions.getObjectValueStorer(fieldType, isEager, switchByteOrder);
			final BinaryValueSetter setter = BinaryValueFunctions.getObjectValueSetter(fieldType, switchByteOrder)    ;
			
			if(fieldType.isPrimitive())
			{
				primitiveTotalBinaryLength += XMemory.byteSizePrimitive(fieldType);
				prmStorers[p] = storer;
				prmSetters[p] = setter;
				prmFields [p] = fieldsDeclaredOrder[i];
				p++;
			}
			else
			{
				refStorers[r] = storer;
				refSetters[r] = setter;
				refFields [r] = fieldsDeclaredOrder[i];
				r++;
			}
		}
		
		System.arraycopy(refStorers, 0, storers            , 0, r);
		System.arraycopy(refSetters, 0, setters            , 0, r);
		System.arraycopy(refFields , 0, fieldsPersistdOrder, 0, r);
		System.arraycopy(prmStorers, 0, storers            , r, p);
		System.arraycopy(prmSetters, 0, setters            , r, p);
		System.arraycopy(prmFields , 0, fieldsPersistdOrder, r, p);

		// the values' total length is the length of all references plus the accumulated length of all primitives.
		return Binary.referenceBinaryLength(r) + primitiveTotalBinaryLength;
	}
	
	protected static final XGettingTable<Field, PersistenceTypeDefinitionMemberField> createTypeDescriptionMembers(
		final Field[]                        persistentOrderFields,
		final PersistenceFieldLengthResolver lengthResolver
	)
	{
		final HashTable<Field, PersistenceTypeDefinitionMemberField> members = HashTable.New();
		
		for(final Field field : persistentOrderFields)
		{
			members.add(field, declaredField(field, lengthResolver));
		}
		
		return members;
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	// instance persistence context //
	private final EqConstHashEnum<Field>                               instanceFields         ;
	private final EqConstHashEnum<Field>                               instanceReferenceFields;
	private final EqConstHashEnum<Field>                               instancePrimitiveFields;
	private final long[]                                               allBinaryOffsets       ;
	private final long[]                                               refBinaryOffsets       ;
	private final long                                                 referenceOffsetStart   ;
	private final long                                                 referenceOffsetBound   ;
	private final long                                                 binaryContentLength    ;
	private final BinaryValueStorer[]                                  binaryStorers          ;
	private final BinaryValueSetter[]                                  memorySetters          ;
	private final XImmutableEnum<PersistenceTypeDefinitionMemberField> membersInPersistdOrder ;
	private final XImmutableEnum<PersistenceTypeDefinitionMemberField> membersInDeclaredOrder ;
	private final boolean                                              hasReferences          ;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	protected AbstractBinaryHandlerReflective(
		final Class<T>                              type                      ,
		final XGettingEnum<Field>                   allFields                 ,
		final PersistenceFieldLengthResolver        lengthResolver            ,
		final PersistenceEagerStoringFieldEvaluator eagerStoringFieldEvaluator,
		final boolean                               switchByteOrder
	)
	{
		super(type);
		
		// (17.05.2018 TM)TODO: why does this constructor contain so much logic? WTF ^^.

		// Unsafe JavaDoc says ensureClassInitialized is "often needed" for getting the field base, so better do it.
		XMemory.ensureClassInitialized(type);

		this.instanceFields          = filter(allFields, not(XReflect::isStatic)                            );
		this.instanceReferenceFields = filter(allFields, not(XReflect::isStatic), not(XReflect::isPrimitive));
		this.instancePrimitiveFields = filter(allFields, not(XReflect::isStatic),     XReflect::isPrimitive );
		this.hasReferences           = !this.instanceReferenceFields.isEmpty();
		
		final Field[]
			fieldsDeclaredOrder = this.instanceFields.toArray(Field.class)         ,
			refFieldsBothOrders = this.instanceReferenceFields.toArray(Field.class),
			fieldsPersistdOrder = new Field[fieldsDeclaredOrder.length]
		;
		
		this.binaryStorers       = new BinaryValueStorer[fieldsDeclaredOrder.length];
		this.memorySetters       = new BinaryValueSetter[fieldsDeclaredOrder.length];
		this.binaryContentLength = fillArraysAndCalculateOffsets(
			type                      ,
			fieldsDeclaredOrder       ,
			fieldsPersistdOrder       ,
			this.binaryStorers        ,
			this.memorySetters        ,
			eagerStoringFieldEvaluator,
			switchByteOrder
		);
		
		final XGettingTable<Field, PersistenceTypeDefinitionMemberField> typeDescriptionMembers =
			createTypeDescriptionMembers(fieldsPersistdOrder, lengthResolver)
		;
		
		// member fields are created in persistent order, collected, validated and immured.
		this.membersInPersistdOrder = validateAndImmure(typeDescriptionMembers.values());
		this.membersInDeclaredOrder = resolveMembersInDeclaredOrder(fieldsDeclaredOrder, typeDescriptionMembers);
		
		// reference field offsets fit either way, because the relative order of reference fields is maintained.
		this.allBinaryOffsets = XMemory.objectFieldOffsets(fieldsPersistdOrder);
		this.refBinaryOffsets = XMemory.objectFieldOffsets(refFieldsBothOrders);
		
		// references are always stored at the beginnnig of the content (0 bytes after header)
		this.referenceOffsetStart = 0;
		this.referenceOffsetBound = Binary.referenceBinaryLength(this.instanceReferenceFields.size());
	}

	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	protected static XImmutableEnum<PersistenceTypeDefinitionMemberField> resolveMembersInDeclaredOrder(
		final Field[]                                                    fieldsDeclaredOrder                 ,
		final XGettingTable<Field, PersistenceTypeDefinitionMemberField> typeDescriptionMembersPersistedOrder
	)
	{
		final BulkList<PersistenceTypeDefinitionMemberField> membersDeclaredOrder = BulkList.New(fieldsDeclaredOrder.length);
		
		for(final Field field : fieldsDeclaredOrder)
		{
			membersDeclaredOrder.add(typeDescriptionMembersPersistedOrder.get(field));
		}
		
		return ConstHashEnum.New(membersDeclaredOrder);
	}
	
	@Override
	public XGettingEnum<Field> instanceFields()
	{
		return this.instanceFields;
	}

	@Override
	public XGettingEnum<Field> instancePrimitiveFields()
	{
		return this.instancePrimitiveFields;
	}

	@Override
	public XGettingEnum<Field> instanceReferenceFields()
	{
		return this.instanceReferenceFields;
	}
	
	@Override
	public final boolean isPrimitiveType()
	{
		return false;
	}
	
	@Override
	public XGettingEnum<? extends PersistenceTypeDefinitionMemberField> members()
	{
		return this.membersInPersistdOrder;
	}
	
	@Override
	public XGettingEnum<? extends PersistenceTypeDefinitionMember> membersInDeclaredOrder()
	{
		return this.membersInDeclaredOrder;
	}

	@Override
	public final boolean hasInstanceReferences()
	{
		return this.hasReferences;
	}
	
	@Override
	public final boolean hasPersistedReferences()
	{
		return this.hasReferences;
	}
	
	@Override
	public final long membersPersistedLengthMinimum()
	{
		return this.binaryContentLength;
	}
	
	@Override
	public final long membersPersistedLengthMaximum()
	{
		return this.binaryContentLength;
	}
	
	@Override
	public final boolean hasPersistedVariableLength()
	{
		return false;
	}

	@Override
	public final boolean hasVaryingPersistedLengthInstances()
	{
		return false;
	}

	@Override
	public void store(final Binary bytes, final T instance, final long objectId, final PersistenceStoreHandler handler)
	{
		bytes.storeFixedSize(
			handler                  ,
			this.binaryContentLength,
			this.typeId()           ,
			objectId                ,
			instance                ,
			this.allBinaryOffsets   ,
			this.binaryStorers
		);
	}

	@Override
	public abstract T create(final Binary bytes, PersistenceLoadHandler handler);

	@Override
	public void update(final Binary bytes, final T instance, final PersistenceLoadHandler handler)
	{
		/*
		 * Explicit type check to avoid memory getting overwritten with bytes not fitting to the actual type.
		 * This can be especially critical if a custom roo resolver returns an instance that does not match
		 * the type defined by the typeId.
		 */
		if(!this.type().isInstance(instance))
		{
			throw new TypeCastException(this.type(), instance);
		}

		bytes.updateFixedSize(instance, this.memorySetters, this.allBinaryOffsets, handler);
	}

	@Override
	public final void complete(final Binary medium, final T instance, final PersistenceLoadHandler handler)
	{
		// no-op for normal implementation (see non-reference-hashing collections for other examples)
	}

	@Override
	public void iterateInstanceReferences(final T instance, final PersistenceFunction iterator)
	{
		BinaryPersistence.iterateInstanceReferences(iterator, instance, this.refBinaryOffsets);
	}

	@Override
	public void iteratePersistedReferences(final Binary bytes, final PersistenceObjectIdAcceptor iterator)
	{
		// "bytes" points to the entity content address, the offsets are relative to the content address.
		bytes.iterateReferenceRange(
			this.referenceOffsetStart,
			this.referenceOffsetBound,
			iterator
		);
	}
	
	@Override
	public <C extends Consumer<? super Class<?>>> C iterateMemberTypes(final C logic)
	{
		for(final Field field : this.instanceFields())
		{
			logic.accept(field.getType());
		}
		
		return logic;
	}

}
