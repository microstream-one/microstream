package one.microstream.persistence.binary.internal;

import static one.microstream.functional.XFunc.not;

import java.lang.reflect.Field;
import java.util.function.Consumer;
import java.util.function.Predicate;

import one.microstream.collections.BulkList;
import one.microstream.collections.EqConstHashEnum;
import one.microstream.collections.EqHashEnum;
import one.microstream.collections.types.XAddingSequence;
import one.microstream.collections.types.XGettingEnum;
import one.microstream.collections.types.XGettingSequence;
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
import one.microstream.persistence.types.PersistenceTypeDefinitionMemberFieldReflective;
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
		final XGettingEnum<Field> fields    ,
		final Predicate<Field>... predicates
	)
	{
		return fields.filterTo(EqHashEnum.New(), XFunc.all(predicates)).immure();
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
		final Class<?>                                                        entityType         ,
		final Iterable<Field>                                                 fieldsDeclaredOrder,
		final Field[]                                                         fieldsPersistdOrder,
		final BinaryValueStorer[]                                             storers            ,
		final BinaryValueSetter[]                                             setters            ,
		final PersistenceEagerStoringFieldEvaluator                           eagerStoreEvaluator,
		final PersistenceFieldLengthResolver                                  lengthResolver,
		final XAddingSequence<PersistenceTypeDefinitionMemberFieldReflective> membersInPersistdOrder,
		final XAddingSequence<PersistenceTypeDefinitionMemberFieldReflective> membersInDeclaredOrder,
		final boolean                                                         switchByteOrder
	)
	{
		final BinaryValueStorer[] refStorers = new BinaryValueStorer[      storers.length];
		final BinaryValueSetter[] refSetters = new BinaryValueSetter[      setters.length];
		final Field[]             refFields  = new Field[      fieldsPersistdOrder.length];
		final PersistenceTypeDefinitionMemberFieldReflective[] refMembers =
			new PersistenceTypeDefinitionMemberFieldReflective[fieldsPersistdOrder.length];
		
		final BinaryValueStorer[] prmStorers = new BinaryValueStorer[      storers.length];
		final BinaryValueSetter[] prmSetters = new BinaryValueSetter[      setters.length];
		final Field[]             prmFields  = new Field[      fieldsPersistdOrder.length];
		final PersistenceTypeDefinitionMemberFieldReflective[] prmMembers =
			new PersistenceTypeDefinitionMemberFieldReflective[fieldsPersistdOrder.length];

		long primitiveTotalBinaryLength = 0;
		int r = 0, p = 0;
		for(final Field field : fieldsDeclaredOrder)
		{
			final Class<?> fieldType = field.getType();
			final boolean  isEager   = eagerStoreEvaluator.isEagerStoring(entityType, field);
			
			final BinaryValueStorer storer = BinaryValueFunctions.getObjectValueStorer(fieldType, isEager, switchByteOrder);
			final BinaryValueSetter setter = BinaryValueFunctions.getObjectValueSetter(fieldType, switchByteOrder);
			final PersistenceTypeDefinitionMemberFieldReflective member = declaredField(field, lengthResolver);
			
			if(fieldType.isPrimitive())
			{
				primitiveTotalBinaryLength += XMemory.byteSizePrimitive(fieldType);
				prmStorers[p] = storer;
				prmSetters[p] = setter;
				prmFields [p] = field ;
				prmMembers[p] = member;
				p++;
			}
			else
			{
				refStorers[r] = storer;
				refSetters[r] = setter;
				refFields [r] = field ;
				refMembers[r] = member;
				r++;
			}
			membersInDeclaredOrder.add(member);
		}
		
		// persistent order is simply: first all reference fields in decl. order, then all primitives in decl. order.
		System.arraycopy(refStorers, 0, storers            , 0, r);
		System.arraycopy(refSetters, 0, setters            , 0, r);
		System.arraycopy(refFields , 0, fieldsPersistdOrder, 0, r);
		System.arraycopy(prmStorers, 0, storers            , r, p);
		System.arraycopy(prmSetters, 0, setters            , r, p);
		System.arraycopy(prmFields , 0, fieldsPersistdOrder, r, p);
		membersInPersistdOrder
			.addAll(refMembers, 0, r)
			.addAll(prmMembers, 0, p)
		;

		// the values' total length is the length of all references plus the accumulated length of all primitives.
		return Binary.referenceBinaryLength(r) + primitiveTotalBinaryLength;
	}
	
	protected static final XGettingSequence<PersistenceTypeDefinitionMemberFieldReflective> createTypeDescriptionMembers(
		final Field[]                        persistentOrderFields,
		final PersistenceFieldLengthResolver lengthResolver
	)
	{
		final BulkList<PersistenceTypeDefinitionMemberFieldReflective> members = BulkList.New();
		
		for(final Field field : persistentOrderFields)
		{
			members.add(declaredField(field, lengthResolver));
		}
		
		return members;
	}
	


	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	// instance persistence context //
	private final EqConstHashEnum<Field> fields              ;
	private final EqConstHashEnum<Field> referenceFields     ;
	private final EqConstHashEnum<Field> primitiveFields     ;
	private final long[]                 allBinaryOffsets    ;
	private final long[]                 refBinaryOffsets    ;
	private final long                   referenceOffsetStart;
	private final long                   referenceOffsetBound;
	private final long                   binaryContentLength ;
	private final BinaryValueStorer[]    binaryStorers       ;
	private final BinaryValueSetter[]    memorySetters       ;
	private final boolean                hasReferences       ;
	private final XImmutableEnum<PersistenceTypeDefinitionMemberFieldReflective> membersInPersistdOrder;
	private final XImmutableEnum<PersistenceTypeDefinitionMemberFieldReflective> membersInDeclaredOrder;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	protected AbstractBinaryHandlerReflective(
		final Class<T>                              type                      ,
		final XGettingEnum<Field>                   persistableFields         ,
		final PersistenceFieldLengthResolver        lengthResolver            ,
		final PersistenceEagerStoringFieldEvaluator eagerStoringFieldEvaluator,
		final boolean                               switchByteOrder
	)
	{
		super(type);
		
		// Unsafe JavaDoc says ensureClassInitialized is "often needed" for getting the field base, so better do it.
		XMemory.ensureClassInitialized(type);

		// filtering for static should not be necessary, but it is a simply done precaution, so why not.
		this.fields          = this.filterInstanceFields         (persistableFields);
		this.referenceFields = this.filterInstanceReferenceFields(persistableFields);
		this.primitiveFields = this.filterInstancePrimitiveFields(persistableFields);
		
		final int memberCount = this.fields.intSize();
		
		final Field[]                                                  fieldsPersistdOrder = new Field[memberCount];
		final BulkList<PersistenceTypeDefinitionMemberFieldReflective> collectorPersOrder  = BulkList.New();
		final BulkList<PersistenceTypeDefinitionMemberFieldReflective> collectorDeclOrder  = BulkList.New();
		
		this.binaryStorers       = new BinaryValueStorer[memberCount];
		this.memorySetters       = new BinaryValueSetter[memberCount];
		this.binaryContentLength = fillArraysAndCalculateOffsets(
			type                      ,
			this.fields       ,
			fieldsPersistdOrder       ,
			this.binaryStorers        ,
			this.memorySetters        ,
			eagerStoringFieldEvaluator,
			lengthResolver            ,
			collectorPersOrder        ,
			collectorDeclOrder        ,
			switchByteOrder
		);
		
		// member fields are created in persistent order, collected, validated and immured.
		this.membersInPersistdOrder = validateAndImmure(collectorPersOrder);
		this.membersInDeclaredOrder = validateAndImmure(collectorDeclOrder);
		
		// reference field offsets fit either way, because the relative order of reference fields is maintained.
		this.allBinaryOffsets = XMemory.objectFieldOffsets(fieldsPersistdOrder);
		this.refBinaryOffsets = XMemory.objectFieldOffsets(this.referenceFields.toArray(Field.class));
		
		// references are always stored at the beginnnig of the content (0 bytes after header)
		this.referenceOffsetStart = 0;
		this.referenceOffsetBound = Binary.referenceBinaryLength(this.referenceFields.size());
		this.hasReferences        = !this.referenceFields.isEmpty();
	}
	
	private EqConstHashEnum<Field> filterInstanceFields(final XGettingEnum<Field> persistableFields)
	{
		return filter(persistableFields, this::isValid);
	}
	
	private EqConstHashEnum<Field> filterInstanceReferenceFields(final XGettingEnum<Field> persistableFields)
	{
		return filter(persistableFields, this::isValid, not(XReflect::isPrimitive));
	}
	
	private EqConstHashEnum<Field> filterInstancePrimitiveFields(final XGettingEnum<Field> persistableFields)
	{
		return filter(persistableFields, this::isValid, XReflect::isPrimitive);
	}
	
	
	protected boolean isValid(final Field field)
	{
		// only safety-net instance filtering in default implementation. Enum fields special casing uses this more.
		return !XReflect.isStatic(field);
	}

	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
			
	@Override
	public XGettingEnum<Field> instanceFields()
	{
		return this.fields;
	}

	@Override
	public XGettingEnum<Field> instancePrimitiveFields()
	{
		return this.primitiveFields;
	}

	@Override
	public XGettingEnum<Field> instanceReferenceFields()
	{
		return this.referenceFields;
	}
	
	@Override
	public final boolean isPrimitiveType()
	{
		return false;
	}
	
	@Override
	public XGettingEnum<? extends PersistenceTypeDefinitionMember> allMembers()
	{
		// "normal" entity types (non-enums) only have instance members
		return this.instanceMembers();
	}
	
	@Override
	public XGettingEnum<? extends PersistenceTypeDefinitionMemberFieldReflective> instanceMembers()
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
		 * This can be especially critical if a custom root resolver returns an instance that does not match
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
	public void iterateLoadableReferences(final Binary bytes, final PersistenceObjectIdAcceptor iterator)
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
