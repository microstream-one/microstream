package one.microstream.persistence.binary.internal;

import java.lang.reflect.Field;
import java.util.function.Consumer;
import java.util.function.Predicate;

import one.microstream.collections.BulkList;
import one.microstream.collections.EqConstHashEnum;
import one.microstream.collections.EqHashEnum;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.collections.types.XGettingEnum;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.exceptions.TypeCastException;
import one.microstream.memory.XMemory;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.BinaryPersistence;
import one.microstream.persistence.binary.types.BinaryTypeHandler;
import one.microstream.persistence.binary.types.BinaryValueFunctions;
import one.microstream.persistence.binary.types.BinaryValueSetter;
import one.microstream.persistence.binary.types.BinaryValueStorer;
import one.microstream.persistence.exceptions.PersistenceExceptionTypeConsistency;
import one.microstream.persistence.types.PersistenceEagerStoringFieldEvaluator;
import one.microstream.persistence.types.PersistenceFieldLengthResolver;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceObjectIdAcceptor;
import one.microstream.persistence.types.PersistenceObjectIdResolver;
import one.microstream.persistence.types.PersistenceStoreHandler;
import one.microstream.persistence.types.PersistenceTypeDefinitionMember;
import one.microstream.persistence.types.PersistenceTypeDefinitionMemberFieldReflective;
import one.microstream.persistence.types.PersistenceTypeDescriptionMember;
import one.microstream.persistence.types.PersistenceTypeHandlerReflective;
import one.microstream.reflect.XReflect;
import one.microstream.util.UtilStackTrace;

public abstract class AbstractBinaryHandlerReflective<T>
extends BinaryTypeHandler.Abstract<T>
implements PersistenceTypeHandlerReflective<Binary, T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	protected static <M extends PersistenceTypeDefinitionMember> EqHashEnum<M> MemberEnum()
	{
		return EqHashEnum.New(
			PersistenceTypeDescriptionMember.identityHashEqualator()
		);
	}
	
	protected static <M extends PersistenceTypeDefinitionMember> EqHashEnum<M> MemberEnum(
		final XGettingCollection<M> initialMembers
	)
	{
		return AbstractBinaryHandlerReflective.<M>MemberEnum().addAll(initialMembers);
	}
	
	protected static EqHashEnum<PersistenceTypeDefinitionMemberFieldReflective> deriveMembers(
		final XGettingEnum<Field>            fields        ,
		final PersistenceFieldLengthResolver lengthResolver
	)
	{
		final EqHashEnum<PersistenceTypeDefinitionMemberFieldReflective> members = MemberEnum();
		
		for(final Field field : fields)
		{
			// just a precaution
			if(XReflect.isStatic(field))
			{
				// (31.07.2019 TM)EXCP: proper exception
				throw new PersistenceExceptionTypeConsistency("static fields are not persistable.");
			}
			
			final PersistenceTypeDefinitionMemberFieldReflective member = declaredField(field, lengthResolver);
			
			if(!members.add(member))
			{
				// (07.09.2018 TM)EXCP: proper exception
				throw new PersistenceExceptionTypeConsistency("Duplicate member descriptions.");
			}
		}
		
		return members;
	}
	
	protected static final EqConstHashEnum<PersistenceTypeDefinitionMemberFieldReflective> filter(
		final XGettingCollection<? extends PersistenceTypeDefinitionMemberFieldReflective> fields    ,
		final Predicate<? super PersistenceTypeDefinitionMemberFieldReflective>            predicate
	)
	{
		return fields.filterTo(EqHashEnum.<PersistenceTypeDefinitionMemberFieldReflective>New(), predicate).immure();
	}
	
	protected static final <C extends Consumer<? super Field>> C unbox(
		final XGettingCollection<? extends PersistenceTypeDefinitionMemberFieldReflective> members,
		final C collector
	)
	{
		return PersistenceTypeDefinitionMemberFieldReflective.unbox(members, collector);
	}
	
	protected static final long equal(final long value1, final long value2) throws IllegalArgumentException
	{
		if(value1 != value2)
		{
			throw UtilStackTrace.cutStacktraceByOne(new IllegalArgumentException());
		}
		
		return value1;
	}
			
	protected static void createStorers(
		final Class<?>                                                 entityType     ,
		final Iterable<PersistenceTypeDefinitionMemberFieldReflective> storingMembers ,
		final BinaryValueStorer[]                                      storers        ,
		final PersistenceEagerStoringFieldEvaluator                    eagerEvaluator ,
		final boolean                                                  switchByteOrder
	)
	{
		int i = 0;
		for(final PersistenceTypeDefinitionMemberFieldReflective member : storingMembers)
		{
			final boolean isEager = eagerEvaluator.isEagerStoring(entityType, member.field());
			storers[i++] = BinaryValueFunctions.getObjectValueStorer(member.type(), isEager, switchByteOrder);
		}
	}
	
	protected static long calculcateBinaryContentLength(
		final Iterable<PersistenceTypeDefinitionMemberFieldReflective> storingMembers
	)
	{
		long binaryContentLength = 0;
		
		for(final PersistenceTypeDefinitionMemberFieldReflective member : storingMembers)
		{
			final long fixedBinaryLength = equal(member.persistentMinimumLength(), member.persistentMaximumLength());
			binaryContentLength += fixedBinaryLength;
		}
		
		return binaryContentLength;
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
	
	protected static final long[] objectFieldOffsets(
		final Class<?>                                                                   entityClass,
		final XGettingSequence<? extends PersistenceTypeDefinitionMemberFieldReflective> members
	)
	{
		// (11.11.2019 TM)NOTE: important for usage of MemoryAccessorGeneric to provide the fields' class context
		final Field[] fields             = unbox(members, BulkList.New()).toArray(Field.class);
		final long[]  objectFieldOffsets = XMemory.objectFieldOffsets(entityClass, fields);
		
		return objectFieldOffsets;
		
		// (11.11.2019 TM)NOTE: old logic without class context
//		final long[] offsets = new long[members.intSize()];
//
//		int i = 0;
//		for(final PersistenceTypeDefinitionMemberFieldReflective member : members)
//		{
//			offsets[i++] = XMemory.objectFieldOffset(member.field());
//		}
//
//		return offsets;
	}

	

	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	/*
	 * The persisted order of the fields differs from the declared order. For efficiency reasons, all
	 * reference fields come first, the primitive fields come second. The benefit from this is that the
	 * storage's garbage collector does not need to load primitive field data for traversing the entity graph.
	 * It might seem irrelevant if a few bytes more or less are loaded, but considering millions of entities
	 * have to be loaded, the savings can amass to a substantial amount.
	 * The relative order of reference fields and primitive fields respectively is maintained.
	 */
	private final EqConstHashEnum<PersistenceTypeDefinitionMember> membersInDeclaredOrder;
	
	private final EqConstHashEnum<PersistenceTypeDefinitionMemberFieldReflective>
		referenceMembers,
		primitiveMembers,
		storingMembers  ,
		settingMembers
	;
	private final long[]
		storingMemoryOffsets,
		settingMemoryOffsets,
		refrnceMemoryOffsets
	;
	private final long
		refBinaryOffsetStart,
		refBinaryOffsetBound,
		binaryContentLength
	;
	private final BinaryValueStorer[]
		storers
	;
	private final BinaryValueSetter[]
		setters
	;
	private final EqConstHashEnum<Field>
		declOrderFields,
		referenceFields,
		primitiveFields
	;
	
	private final boolean switchByteOrder;
	
	/* (28.10.2019 TM)TODO: encapsulate / abstract BinaryValue~ handling types.
	 * While the per-field handling via the BinaryValue~ handling types is perfectly fine for JDK
	 * and all fully Unsafe-compatible JVMs, it poses a considerable inefficiency for the generic
	 * memory handling implementation. There, every call with an object-based "offset" has to be
	 * translated to the corresponding field and then executed via that.
	 * A more efficient solution would be to encapsulate / abstract all BinaryValue~ handling type instances
	 * in a single BinaryObjectValues~ handling type and let its implementation use cached Fields directly.
	 */


	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	protected AbstractBinaryHandlerReflective(
		final Class<T>                              type             ,
		final String                                typeName         ,
		final XGettingEnum<Field>                   persistableFields,
		final PersistenceFieldLengthResolver        lengthResolver   ,
		final PersistenceEagerStoringFieldEvaluator eagerEvaluator   ,
		final boolean                               switchByteOrder
	)
	{
		super(type, typeName);
		
		this.switchByteOrder = switchByteOrder;
		
		/*
		 * Unsafe JavaDoc says ensureClassInitialized is "often needed" for getting the field base, so better do it.
		 * MemoryAccessor implementations that do not use the field base don't need to do anything here.
		 * They probably also can't do anything to ensure a class is initialized.
		 */
		XMemory.ensureClassInitialized(type, persistableFields);
		
		final EqHashEnum<PersistenceTypeDefinitionMemberFieldReflective> instMembersInDeclOrdr =
			deriveMembers(persistableFields, lengthResolver)
		;
		
		this.membersInDeclaredOrder = this.deriveAllMembers(instMembersInDeclOrdr);
		
		// member instances are created from the persistable fields and splitted into references and primitives
		this.referenceMembers = this.filterReferenceMembers(instMembersInDeclOrdr, MemberEnum()).immure();
		this.primitiveMembers = this.filterPrimitiveMembers(instMembersInDeclOrdr, MemberEnum()).immure();
		
		// persistent order is all reference fields in declared order, then all primitive fields in declared order.
		this.storingMembers = MemberEnum(this.referenceMembers).addAll(this.primitiveMembers).immure();
		this.settingMembers = this.filterSettingMembers(this.storingMembers);
		
		// storing/setting memory offsets initialization must be overridable for enum special casing
		this.storingMemoryOffsets = this.initializeStoringMemoryOffsets();
		this.settingMemoryOffsets = this.initializeSettingMemoryOffsets();
		this.refrnceMemoryOffsets = this.initializeStoringRefMemOffsets();
		
		// references are always stored at the beginnnig of the content (0 bytes after header)
		this.refBinaryOffsetStart = 0;
		this.refBinaryOffsetBound = Binary.referenceBinaryLength(this.referenceMembers.size());

		// storers set a field's value from the instance in memory to a buffered persistent form.
		this.storers = new BinaryValueStorer[this.storingMembers.intSize()];
		createStorers(type, this.storingMembers, this.storers, eagerEvaluator, switchByteOrder);
		
		// setters set a field's value from a buffered persistent form to the instance in memory.
		this.setters = this.deriveSetters();

		// binary content length (without the entity header) is calculated based on the storing ("all") members.
		this.binaryContentLength = calculcateBinaryContentLength(this.storingMembers);

		// lots of data copying detour, but only once per handler and nicely readable
		this.declOrderFields = unbox(instMembersInDeclOrdr, EqHashEnum.New()).immure();
		this.referenceFields = unbox(this.referenceMembers, EqHashEnum.New()).immure();
		this.primitiveFields = unbox(this.primitiveMembers, EqHashEnum.New()).immure();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// initializer logic //
	//////////////////////
		
	protected long[] initializeStoringMemoryOffsets()
	{
		return objectFieldOffsets(this.type(), this.storingMembers);
	}
	
	protected long[] initializeSettingMemoryOffsets()
	{
		// no difference by default (enums get skipping setters, but the offsets stay the same)
		return this.storingMemoryOffsets;
	}
	
	protected BinaryValueSetter[] deriveSetters()
	{
		final EqConstHashEnum<PersistenceTypeDefinitionMemberFieldReflective> members = this.settingMembers;
		
		final BinaryValueSetter[] setters = new BinaryValueSetter[members.intSize()];
		
		int i = 0;
		for(final PersistenceTypeDefinitionMemberFieldReflective member : members)
		{
			setters[i++] = this.deriveSetter(member);
		}
		
		return setters;
	}
	
	protected BinaryValueSetter deriveSetter(
		final PersistenceTypeDefinitionMemberFieldReflective member
	)
	{
		return BinaryValueFunctions.getObjectValueSetter(member.type(), this.isSwitchedByteOrder());
	}
	
	protected long[] initializeStoringRefMemOffsets()
	{
		return objectFieldOffsets(this.type(), this.referenceMembers);
	}
	
	protected EqConstHashEnum<PersistenceTypeDefinitionMemberFieldReflective> filterSettingMembers(
		final EqConstHashEnum<PersistenceTypeDefinitionMemberFieldReflective> members
	)
	{
		// by default, all members are settable (enums get skipping setters, but the fields stay the same)
		return members;
	}
	
	protected EqHashEnum<PersistenceTypeDefinitionMemberFieldReflective> filterReferenceMembers(
		final XGettingCollection<PersistenceTypeDefinitionMemberFieldReflective> members,
		final EqHashEnum<PersistenceTypeDefinitionMemberFieldReflective>         target
	)
	{
		return members.filterTo(target, m ->
			m.isReference()
		);
	}
	
	protected EqHashEnum<PersistenceTypeDefinitionMemberFieldReflective> filterPrimitiveMembers(
		final XGettingCollection<PersistenceTypeDefinitionMemberFieldReflective> members,
		final EqHashEnum<PersistenceTypeDefinitionMemberFieldReflective>         target
	)
	{
		return members.filterTo(target, m ->
			m.isPrimitive()
		);
	}
	
	protected EqConstHashEnum<PersistenceTypeDefinitionMember> deriveAllMembers(
		final XGettingSequence<? extends PersistenceTypeDefinitionMember> declaredOrderInstanceMembers
	)
	{
		return EqConstHashEnum.New(declaredOrderInstanceMembers);
	}
	
		
	
	///////////////////////////////////////////////////////////////////////////
	// getters //
	////////////
	
	public final boolean isSwitchedByteOrder()
	{
		return this.switchByteOrder;
	}
			
	@Override
	public XGettingEnum<Field> instanceFields()
	{
		return this.declOrderFields;
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
		// with the exception of some special types (primitive definition and enums), there are only instance members.
		return this.instanceMembers();
	}
	
	@Override
	public XGettingEnum<? extends PersistenceTypeDefinitionMemberFieldReflective> instanceMembers()
	{
		return this.storingMembers();
	}
	
	@Override
	public XGettingEnum<? extends PersistenceTypeDefinitionMemberFieldReflective> storingMembers()
	{
		return this.storingMembers;
	}

	@Override
	public XGettingEnum<? extends PersistenceTypeDefinitionMemberFieldReflective> settingMembers()
	{
		return this.settingMembers;
	}
	
	@Override
	public XGettingEnum<? extends PersistenceTypeDefinitionMember> membersInDeclaredOrder()
	{
		return this.membersInDeclaredOrder;
	}

	@Override
	public final boolean hasInstanceReferences()
	{
		return !this.referenceMembers.isEmpty();
	}
	
	@Override
	public final boolean hasPersistedReferences()
	{
		return !this.referenceMembers.isEmpty();
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
	
	
	///////////////////////////////////////////////////////////////////////////
	// actual operating logic //
	///////////////////////////

	@Override
	public void store(final Binary bytes, final T instance, final long objectId, final PersistenceStoreHandler handler)
	{
		bytes.storeFixedSize(
			handler                  ,
			this.binaryContentLength ,
			this.typeId()            ,
			objectId                 ,
			instance                 ,
			this.storingMemoryOffsets,
			this.storers
		);
	}

	@Override
	public abstract T create(final Binary bytes, PersistenceObjectIdResolver idResolver);

	@Override
	public void update(final Binary bytes, final T instance, final PersistenceObjectIdResolver idResolver)
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

		bytes.updateFixedSize(instance, this.setters, this.settingMemoryOffsets, idResolver);
	}

	@Override
	public final void complete(final Binary medium, final T instance, final PersistenceObjectIdResolver idResolver)
	{
		// no-op for normal implementation (see non-reference-hashing collections for other examples)
	}

	@Override
	public void iterateInstanceReferences(final T instance, final PersistenceFunction iterator)
	{
		BinaryPersistence.iterateInstanceReferences(iterator, instance, this.refrnceMemoryOffsets);
	}

	@Override
	public void iterateLoadableReferences(final Binary bytes, final PersistenceObjectIdAcceptor iterator)
	{
		// "bytes" points to the entity content address, the offsets are relative to the content address.
		bytes.iterateReferenceRange(
			this.refBinaryOffsetStart,
			this.refBinaryOffsetBound,
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
