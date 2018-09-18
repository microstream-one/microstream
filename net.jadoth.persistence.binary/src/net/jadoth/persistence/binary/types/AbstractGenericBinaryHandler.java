package net.jadoth.persistence.binary.types;

import static net.jadoth.functional.XFunc.not;

import java.lang.reflect.Field;
import java.util.function.Predicate;

import net.jadoth.collections.BulkList;
import net.jadoth.collections.EqConstHashEnum;
import net.jadoth.collections.EqHashEnum;
import net.jadoth.collections.types.XGettingEnum;
import net.jadoth.collections.types.XImmutableEnum;
import net.jadoth.exceptions.TypeCastException;
import net.jadoth.functional.XFunc;
import net.jadoth.functional._longProcedure;
import net.jadoth.low.XVM;
import net.jadoth.persistence.types.PersistenceEagerStoringFieldEvaluator;
import net.jadoth.persistence.types.PersistenceFieldLengthResolver;
import net.jadoth.persistence.types.PersistenceTypeDescriptionMember;
import net.jadoth.persistence.types.PersistenceTypeDescriptionMemberField;
import net.jadoth.persistence.types.PersistenceTypeHandlerGeneric;
import net.jadoth.reflect.XReflect;
import net.jadoth.swizzling.types.PersistenceStoreFunction;
import net.jadoth.swizzling.types.SwizzleBuildLinker;
import net.jadoth.swizzling.types.SwizzleFunction;

public abstract class AbstractGenericBinaryHandler<T>
extends BinaryTypeHandler.AbstractImplementation<T>
implements PersistenceTypeHandlerGeneric<Binary, T>
{
	// (21.05.2013)XXX: BinaryHandlerGeneric cleanup static handling massacre mess

	///////////////////////////////////////////////////////////////////////////
	// static methods   //
	/////////////////////

	@SafeVarargs
	protected static final EqConstHashEnum<Field> filter(
		final XGettingEnum<Field> allFields,
		final Predicate<Field>... predicates
	)
	{
		return allFields.filterTo(EqHashEnum.<Field>New(), XFunc.all(predicates)).immure();
	}

	protected static long calculateOffsets(
		final Class<?>                                    entityType        ,
		final Field[]                                     allFieldsDeclOrder,
		final Field[]                                     fieldsBinOrder    ,
		final long[]                                      allBinOfs         ,
		final BinaryValueStorer[]                         storers           ,
		final BinaryValueSetter[]                         setters           ,
		final PersistenceEagerStoringFieldEvaluator isForcedPredicate
	)
	{
		final BinaryValueStorer[] refStorers = new BinaryValueStorer[   storers.length];
		final BinaryValueSetter[] refSetters = new BinaryValueSetter[   setters.length];
		final Field[]             refFields  = new Field[        fieldsBinOrder.length];
		final BinaryValueStorer[] prmStorers = new BinaryValueStorer[   storers.length];
		final BinaryValueSetter[] prmSetters = new BinaryValueSetter[   setters.length];
		final Field[]             prmFields  = new Field[        fieldsBinOrder.length];
		final long[]              prmBinOffs = new long[              allBinOfs.length];

		long primBinOffsets = 0;
		int r = 0, p = 0;
		for(int i = 0; i < allFieldsDeclOrder.length; i++)
		{
			final Class<?>          fieldType = allFieldsDeclOrder[i].getType()                                    ;
			final boolean           isForced  = isForcedPredicate.isEagerStoring(entityType, allFieldsDeclOrder[i]);
			final BinaryValueStorer storer    = BinaryPersistence.getObjectValueStorer(fieldType, isForced)        ;
			final BinaryValueSetter setter    = BinaryPersistence.getObjectValueSetter(fieldType)                  ;
			if(fieldType.isPrimitive())
			{
				primBinOffsets += XVM.byteSizePrimitive(fieldType);
				prmStorers[p] = storer;
				prmSetters[p] = setter;
				prmBinOffs[p] = primBinOffsets;
				prmFields [p] = allFieldsDeclOrder[i];
				p++;
			}
			else
			{
				refStorers[r] = storer;
				refSetters[r] = setter;
				refFields [r] = allFieldsDeclOrder[i];
				r++;
			}
		}

		for(int i = 0; i < r; i++)
		{
			allBinOfs[i] = i * BinaryPersistence.oidLength();
		}
		
		final long binPrimOffset = r * BinaryPersistence.oidLength();
		for(int i = 0; i < p; i++)
		{
			allBinOfs[r + i] = binPrimOffset + prmBinOffs[i];
		}

		System.arraycopy(refStorers, 0, storers       , 0, r);
		System.arraycopy(refSetters, 0, setters       , 0, r);
		System.arraycopy(refFields , 0, fieldsBinOrder, 0, r);
		System.arraycopy(prmStorers, 0, storers       , r, p);
		System.arraycopy(prmSetters, 0, setters       , r, p);
		System.arraycopy(prmFields , 0, fieldsBinOrder, r, p);

		return binPrimOffset + primBinOffsets; // the offset at the end is equal to the total binary length
	}
	
	protected static final void createTypeDescriptionMembers(
		final Field[]                                    persistentOrderFields,
		final PersistenceFieldLengthResolver             lengthResolver       ,
		final BulkList<PersistenceTypeDescriptionMember> members
	)
	{
		for(final Field field : persistentOrderFields)
		{
			members.add(PersistenceTypeDescriptionMemberField.New(
				field.getType().getName(),
				field.getName(),
				field.getDeclaringClass().getName(),
				!field.getType().isPrimitive(),
				lengthResolver.resolveMinimumLengthFromField(field),
				lengthResolver.resolveMaximumLengthFromField(field)
			));
		}
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	// instance persistence context //
	private final EqConstHashEnum<Field>                           allFields                  ;
	private final EqConstHashEnum<Field>                           referenceFields            ;
	private final EqConstHashEnum<Field>                           primitiveFields            ;
	private final long[]                                           allMemoryOffsets           ;
	private final long[]                                           refMemoryOffsets           ;
	private final long                                             refBinaryContentStartOffset;
	private final long                                             refBinaryContentBoundOffset;
	private final long                                             binaryContentLength               ;
	private final BinaryValueStorer[]                              binStorers                 ;
	private final BinaryValueSetter[]                              memSetters                 ;
	private final XImmutableEnum<PersistenceTypeDescriptionMember> members                    ;
	private final boolean                                          hasReferences              ;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	protected AbstractGenericBinaryHandler(
		final Class<T>                              type                   ,
		final XGettingEnum<Field>                   allFields              ,
		final PersistenceFieldLengthResolver        lengthResolver         ,
		final PersistenceEagerStoringFieldEvaluator eagerStoringFieldEvaluator
	)
	{
		super(type);
		
		// (17.05.2018 TM)TODO: why does this constructor contain so much logic? WTF ^^.

		// Unsafe JavaDoc says ensureClassInitialized is "often needed" for getting the field base, so better do it.
		XVM.ensureClassInitialized(type);

		this.allFields    =  filter(allFields, not(XReflect::isStatic)                                 );
		this.referenceFields    =  filter(allFields, not(XReflect::isStatic), not(XReflect::isPrimitive));
		this.primitiveFields    =  filter(allFields, not(XReflect::isStatic),     XReflect::isPrimitive );
		final Field[]
			allFieldsDeclOrder = this.allFields.toArray(Field.class),
			refFieldsDeclOrder = this.referenceFields.toArray(Field.class),
			allFieldsPersOrder = new Field[allFieldsDeclOrder.length]
//			refFieldsPersOrder = new Field[refFieldsDeclOrder.length]
		;
		
		this.hasReferences = !this.referenceFields.isEmpty();

		/* (15.12.2012)TODO: TypeHandler: Field strategy
		 * Must bring in the possibility for a ValueSkipSetter and ValueSkipStorer
		 * And for a ValueShallowStorer
		 * Probably via different Value handler lookup instances.
		 * However those would have to be located in the handler creator instance, not here
		 */
		this.binaryContentLength = calculateOffsets(
			type                                                              ,
			allFieldsDeclOrder                                                ,
			allFieldsPersOrder                                                ,
			                  new long[             allFieldsDeclOrder.length],
			this.binStorers = new BinaryValueStorer[allFieldsDeclOrder.length],
			this.memSetters = new BinaryValueSetter[allFieldsDeclOrder.length],
			eagerStoringFieldEvaluator
		);

		// memory offsets must correspond to other arrays
		this.allMemoryOffsets = XVM.objectFieldOffsets(allFieldsPersOrder);
		
		// reference field offsets fit either way
		this.refMemoryOffsets = XVM.objectFieldOffsets(refFieldsDeclOrder);
		
		// references are always stored at the beginnnig of the content (0 bytes after header)
		this.refBinaryContentStartOffset = 0;
		this.refBinaryContentBoundOffset = BinaryPersistence.referenceBinaryLength(this.referenceFields.size());

		final BulkList<PersistenceTypeDescriptionMember> members = BulkList.New(allFields.size());
		createTypeDescriptionMembers(allFieldsPersOrder, lengthResolver, members);

		/* (21.10.2014 TM)XXX: binary handler declaration order or persistent order?
		 * if pseudo field definitions are in persistent order, why does the generic handler use declaration order?
		 * Is it necessary (e.g. validation with actual class) or arbitrary?
		 * Maybe keep both orders?
		 *
		 * Would a change in declaration order that results in the same persistent order matter in the first place?
		 * Value handlers are derived dynamically. As long as it results in the same persistent order,
		 * everything is fine.
		 */
		this.members = PersistenceTypeDescriptionMember.validateAndImmure(members);
	}

	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public XGettingEnum<Field> getInstanceFields()
	{
		return this.allFields;
	}

	@Override
	public XGettingEnum<Field> getInstancePrimitiveFields()
	{
		return this.primitiveFields;
	}

	@Override
	public XGettingEnum<Field> getInstanceReferenceFields()
	{
		return this.referenceFields;
	}
	
	@Override
	public final boolean isPrimitiveType()
	{
		return false;
	}
	
	@Override
	public XGettingEnum<? extends PersistenceTypeDescriptionMember> members()
	{
		return this.members;
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
	public final long binaryContentLengthMinimum()
	{
		return this.binaryContentLength;
	}
	
	@Override
	public final long binaryContentLengthMaximum()
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
	public void store(final Binary bytes, final T instance, final long objectId, final PersistenceStoreFunction linker)
	{
		BinaryPersistence.storeFixedSize(
			bytes                   ,
			linker                  ,
			this.binaryContentLength,
			this.typeId()           ,
			objectId                ,
			instance                ,
			this.allMemoryOffsets   ,
			this.binStorers
		);
	}

	@Override
	public abstract T create(final Binary bytes);

	@Override
	public void update(final Binary bytes, final T instance, final SwizzleBuildLinker builder)
	{
		/* must explicitely check type to avoid memory getting overwritten with bytes not fitting to the actual type
		 * this can be especially critical and important if a custom PersistenceRootResolver returns an instance
		 * that does not match the type defined by the typeId.
		 */
		if(!this.type().isInstance(instance))
		{
			/*
			 * I wished they knew how to write proper exceptions (pass class and instance) instead of cramming
			 * everything into contextless and expensive plain strings. Noobs.
			 * So, as usual, a new and proper exception type is needed.
			 * Also, while I'm on it, since not all types are classes (there are interfaces, too, which the JDK
			 * guys apparently don't know, which is especially hilarious), the correct term for such an exception type
			 * is "TypeCastException", not "ClassCastException".
			 */
			throw new TypeCastException(this.type(), instance);
		}

		BinaryPersistence.updateFixedSize(
			instance,
			this.memSetters,
			this.allMemoryOffsets,
			bytes.buildItemAddress(),
			builder
		);
	}

	@Override
	public final void complete(final Binary medium, final T instance, final SwizzleBuildLinker builder)
	{
		// no-op for normal implementation (see non-reference-hashing collections for other examples)
	}

	@Override
	public void iterateInstanceReferences(final T instance, final SwizzleFunction iterator)
	{
		BinaryPersistence.iterateInstanceReferences(iterator, instance, this.refMemoryOffsets);
	}

	@Override
	public void iteratePersistedReferences(final Binary bytes, final _longProcedure iterator)
	{
		// "bytes" points to the entity content address, the offsets are relative to the content address.
		BinaryPersistence.iterateBinaryReferences(
			bytes                           ,
			this.refBinaryContentStartOffset,
			this.refBinaryContentBoundOffset,
			iterator
		);
	}

//	void validate(final Field describedField, final long index)
//	{
//		if(!this.allFields.at(index).equals(describedField))
//		{
//			throw new PersistenceExceptionTypeConsistencyDefinitionValidationFieldMismatch(
//				this.allFields.at(index),
//				describedField
//			);
//		}
//	}

}
