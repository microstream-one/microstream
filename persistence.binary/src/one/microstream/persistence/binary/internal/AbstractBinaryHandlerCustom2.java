package one.microstream.persistence.binary.internal;

import static one.microstream.X.mayNull;
import static one.microstream.X.notNull;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import one.microstream.collections.BulkList;
import one.microstream.collections.EqHashTable;
import one.microstream.collections.HashTable;
import one.microstream.collections.types.XAddingCollection;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.collections.types.XGettingTable;
import one.microstream.collections.types.XImmutableEnum;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.exceptions.PersistenceException;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceReferenceLoader;
import one.microstream.persistence.types.PersistenceStoreHandler;
import one.microstream.persistence.types.PersistenceTypeDefinitionMember;
import one.microstream.persistence.types.PersistenceTypeInstantiator;
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


public abstract class AbstractBinaryHandlerCustom2<T>
extends AbstractBinaryHandlerCustom<T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	protected static final <T> BinaryField<T> Field_byte(
		final Getter_byte<T> getter,
		final Setter_byte<T> setter
	)
	{
		return new BinaryField.Default_byte<>(
			mayNull(getter),
			notNull(setter)
		);
	}
		
	protected static final <T> BinaryField<T> Field_boolean(
		final Getter_boolean<T> getter,
		final Setter_boolean<T> setter
	)
	{
		return new BinaryField.Default_boolean<>(
			mayNull(getter),
			notNull(setter)
		);
	}
		
	protected static final <T> BinaryField<T> Field_short(
		final Getter_short<T> getter,
		final Setter_short<T> setter
	)
	{
		return new BinaryField.Default_short<>(
			mayNull(getter),
			notNull(setter)
		);
	}
		
	protected static final <T> BinaryField<T> Field_char(
		final Getter_char<T> getter,
		final Setter_char<T> setter
	)
	{
		return new BinaryField.Default_char<>(
			mayNull(getter),
			notNull(setter)
		);
	}
		
	protected static final <T> BinaryField<T> Field_int(
		final Getter_int<T> getter,
		final Setter_int<T> setter
	)
	{
		return new BinaryField.Default_int<>(
			mayNull(getter),
			notNull(setter)
		);
	}
		
	protected static final <T> BinaryField<T> Field_float(
		final Getter_float<T> getter,
		final Setter_float<T> setter
	)
	{
		return new BinaryField.Default_float<>(
			mayNull(getter),
			notNull(setter)
		);
	}
		
	protected static final <T> BinaryField<T> Field_long(
		final Getter_long<T> getter,
		final Setter_long<T> setter
	)
	{
		return new BinaryField.Default_long<>(
			mayNull(getter),
			notNull(setter)
		);
	}
		
	protected static final <T> BinaryField<T> Field_double(
		final Getter_double<T> getter,
		final Setter_double<T> setter
	)
	{
		return new BinaryField.Default_double<>(
			mayNull(getter),
			notNull(setter)
		);
	}
		
	protected static final <T, R> BinaryField<T> Field(
		final Class<R>     referenceType,
		final Getter<T, R> getter       ,
		final Setter<T, R> setter
	)
	{
		return new BinaryField.DefaultReference<>(
			notNull(referenceType),
			mayNull(getter),
			notNull(setter)
		);
	}
	
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	// (06.01.2020 TM)FIXME: priv#88: remove #initializeBinaryFieldsExplicitely if this is really not needed
//	private Class<?> initializationInvokingClass;

	private final PersistenceTypeInstantiator<Binary, T> instantiator;
	
	// must be deferred-initialized since all fields have to be collected in a generic way
	private XGettingTable<String, BinaryField.Initializable<T>> binaryFields;
	
	// having no setting members effectively means the type is an immutable value type
	private boolean hasSettingMembers;
	
	private boolean hasPersistedReferences;
	private boolean hasVaryingPersistedLengthInstances;

	private BinaryField<T>[] storingFields      ;
	private BinaryField<T>[] allReferenceFields ;
	private BinaryField<T>[] settingRefrncFields;
	private BinaryField<T>[] settingNonRefFields;
	



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	protected AbstractBinaryHandlerCustom2(final Class<T> type)
	{
		this(type, (XGettingSequence<? extends PersistenceTypeDefinitionMember>)null);
	}

	protected AbstractBinaryHandlerCustom2(
		final Class<T>                                                    type   ,
		final XGettingSequence<? extends PersistenceTypeDefinitionMember> members
	)
	{
		this(type, deriveTypeName(type), null, members);
	}
	
	protected AbstractBinaryHandlerCustom2(
		final Class<T>                                                    type    ,
		final String                                                      typeName,
		final XGettingSequence<? extends PersistenceTypeDefinitionMember> members
	)
	{
		this(type, typeName, null, members);
	}
	
	protected AbstractBinaryHandlerCustom2(
		final Class<T>                               type        ,
		final PersistenceTypeInstantiator<Binary, T> instantiator
	)
	{
		this(type, instantiator, null);
	}

	protected AbstractBinaryHandlerCustom2(
		final Class<T>                                                    type        ,
		final PersistenceTypeInstantiator<Binary, T>                      instantiator,
		final XGettingSequence<? extends PersistenceTypeDefinitionMember> members
	)
	{
		this(type, deriveTypeName(type), members);
	}
	
	protected AbstractBinaryHandlerCustom2(
		final Class<T>                                                    type        ,
		final String                                                      typeName    ,
		final PersistenceTypeInstantiator<Binary, T>                      instantiator,
		final XGettingSequence<? extends PersistenceTypeDefinitionMember> members
	)
	{
		super(type, typeName, members);
		this.instantiator = mayNull(instantiator);
	}
	
	

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final boolean hasPersistedReferences()
	{
		this.ensureInitializeInstanceMembers();
		
		return this.hasPersistedReferences;
	}

	@Override
	public final boolean hasVaryingPersistedLengthInstances()
	{
		this.ensureInitializeInstanceMembers();
		
		return this.hasVaryingPersistedLengthInstances;
	}
		
	@Override
	protected XImmutableEnum<? extends PersistenceTypeDefinitionMember> initializeInstanceMembers()
	{
		// super class's on-demand logic guarantees that this method is only called once for every instance.
		final XGettingSequence<BinaryField.Initializable<T>> binaryFields = this.initializeBinaryFields();
		
		return validateAndImmure(binaryFields);
	}
	
	private long calculcateContentLength(final T instance)
	{
		// (20.01.2020 TM)FIXME: priv#88: AbstractBinaryHandlerCustom2#calculcateContentLength()
		throw new one.microstream.meta.NotImplementedYetError();
	}
	
	@Override
	public void store(final Binary data, final T instance, final long objectId, final PersistenceStoreHandler handler)
	{
		final long contentLength = this.calculcateContentLength(instance);

		data.storeEntityHeader(contentLength, this.typeId(), objectId);

		for(final BinaryField<T> storingField : this.storingFields)
		{
			storingField.storeFromInstance(instance, data, handler);
		}
	}
	
	
	protected T instantiate(final Binary data)
	{
		// if this method is not overwritten by the subclass, then instantiator must be non-null.
		return this.instantiator.instantiate(data);
	}

	@Override
	public T create(final Binary data, final PersistenceLoadHandler handler)
	{
		final T instance = this.instantiate(data);
		
		// reference values will get set later on in initializeState, when instances are guaranteed to be available
		this.setNonReferenceValues(instance, data, handler);
		
		return instance;
	}
	
	private void setNonReferenceValues(final T instance, final Binary data, final PersistenceLoadHandler handler)
	{
		for(final BinaryField<T> settingNonRefField : this.settingNonRefFields)
		{
			settingNonRefField.setToInstance(instance, data, handler);
		}
	}
	
	
	
	private void setReferenceValues(final T instance, final Binary data, final PersistenceLoadHandler handler)
	{
		for(final BinaryField<T> settingRefrncField : this.settingRefrncFields)
		{
			settingRefrncField.setToInstance(instance, data, handler);
		}
	}
	
	private void validateState(final T instance, final Binary data, final PersistenceLoadHandler handler)
	{
		for(final BinaryField<T> settingNonRefField : this.storingFields)
		{
			settingNonRefField.setToInstance(instance, data, handler);
		}
	}
	
	@Override
	public void initializeState(final Binary data, final T instance, final PersistenceLoadHandler handler)
	{
		// non-reference values were already set in #create
		this.setReferenceValues(instance, data, handler);
	}

	@Override
	public void updateState(final Binary data, final T instance, final PersistenceLoadHandler handler)
	{
		if(this.hasSettingMembers)
		{
			// update has to set both types of values
			this.setNonReferenceValues(instance, data, handler);
			this.setReferenceValues(instance, data, handler);
		}
		else
		{
			// immutable value types are validated, instead of updated. See native handlers (String etc.)
			this.validateState(instance, data, handler);
		}
	}

	@Override
	public void complete(final Binary data, final T instance, final PersistenceLoadHandler handler)
	{
		// no-op for normal implementation (see non-reference-hashing collections for other examples)
	}
	
	@Override
	public <C extends Consumer<? super Class<?>>> C iterateMemberTypes(final C logic)
	{
		for(final BinaryField<T> storingField : this.storingFields)
		{
			logic.accept(storingField.type());
		}
		
		return logic;
	}

//	protected final synchronized void initializeBinaryFieldsExplicitely(final Class<?> invokingClass)
//	{
//		if(this.initializationInvokingClass != null)
//		{
//			if(this.initializationInvokingClass == invokingClass)
//			{
//				// consistent no-op, abort.
//				return;
//			}
//
//			// (04.04.2019 TM)EXCP: proper exception
//			throw new PersistenceException(
//				XChars.systemString(this)
//				+ " already initialized by an invokation from class "
//				+ this.initializationInvokingClass.getName()
//			);
//		}
//
//		this.initializeBinaryFields();
//		this.initializationInvokingClass = invokingClass;
//	}

	protected final synchronized XGettingSequence<BinaryField.Initializable<T>> initializeBinaryFields()
	{
		final HashTable<Class<?>, XGettingSequence<BinaryField.Initializable<T>>> binaryFieldsPerClass = HashTable.New();
		
		this.collectBinaryFields(binaryFieldsPerClass);

		final EqHashTable<String, BinaryField.Initializable<T>> binaryFieldsInOrder = EqHashTable.New();
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
		
		this.initializeBinaryFields(binaryFieldsInOrder);
		// (18.04.2019 TM)FIXME: priv#88: assign to members field here or somewhere appropriate.
		
		this.binaryFields = binaryFieldsInOrder;
		
		return this.binaryFields.values();
	}
	
	private void collectBinaryFields(
		final HashTable<Class<?>, XGettingSequence<BinaryField.Initializable<T>>> binaryFieldsPerClass
	)
	{
		for(Class<?> c = this.getClass(); c != AbstractBinaryHandlerCustom2.class; c = c.getSuperclass())
		{
			/*
			 * This construction is necessary to maintain the collection order even if a class
			 * overrides the collecting logic
			 */
			final BulkList<BinaryField.Initializable<T>> binaryFieldsOfClass = BulkList.New();
			this.collectDeclaredBinaryFields(c, binaryFieldsOfClass);

			// already existing entries (added by an extending class in an override of this method) are allowed
			binaryFieldsPerClass.add(c, binaryFieldsOfClass);
		}
	}
	
	protected void validateBinaryFieldGenericType(final Field binaryFieldField)
	{
		final Type genericType = binaryFieldField.getGenericType();
		
		if(genericType == this.type())
		{
			
		}
	}

	protected void collectDeclaredBinaryFields(
		final Class<?>                                        clazz ,
		final XAddingCollection<BinaryField.Initializable<T>> target
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
				
				// (20.01.2020 TM)FIXME: priv#88: test if field's getGenericType() is correct
				
				@SuppressWarnings("unchecked")
				final BinaryField<?> binaryField = (BinaryField<T>)field.get(this);
				if(!(binaryField instanceof BinaryField.Initializable))
				{
					continue;
				}
				
				@SuppressWarnings("unchecked")
				final BinaryField.Initializable<T> initializable = (BinaryField.Initializable<T>)binaryField;
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
		final XGettingTable<Class<?>, XGettingSequence<BinaryField.Initializable<T>>> binaryFieldsPerClass,
		final BiConsumer<String, BinaryField.Initializable<T>>                        collector
	)
	{
		/*
		 * With the class hiararchy collection order guaranteed above, this loop does:
		 * - order binaryFields from most to least specific class ("upwards")
		 * - order binaryFields per class in declaration order
		 */
		for(final XGettingSequence<BinaryField.Initializable<T>> binaryFieldsOfClass : binaryFieldsPerClass.values())
		{
			for(final BinaryField.Initializable<T> binaryField : binaryFieldsOfClass)
			{
				collector.accept(binaryField.name(), binaryField);
			}
		}
	}
	
	private void initializeBinaryFields(final XGettingTable<String, BinaryField.Initializable<T>> binaryFields)
	{
		validateVariableLengthLayout(binaryFields);
		final BulkList<BinaryField<T>> storingFields       = BulkList.New();
		final BulkList<BinaryField<T>> allReferenceFields  = BulkList.New();
		final BulkList<BinaryField<T>> settingNonRefFields = BulkList.New();
		final BulkList<BinaryField<T>> settingRefrncFields = BulkList.New();
		
		boolean hasSettingMembers = false;
		
		// (14.01.2020 TM)FIXME: priv#88: hasInstanceReferences. See task in PersistenceTypeHandler.
		
		long offset = 0;
		for(final BinaryField.Initializable<T> binaryField : binaryFields.values())
		{
			binaryField.initializeOffset(offset);
			offset += binaryField.persistentMinimumLength();

			storingFields.add(binaryField);
			
			if(binaryField.hasReferences())
			{
				allReferenceFields.add(binaryField);
			}
			
			if(!binaryField.canSet())
			{
				continue;
			}
			
			hasSettingMembers = true;
			
			/*
			 * must use "hasReferences" instead of "isReference" as a variable list field
			 * is not a reference, but can contain references.
			 */
			if(binaryField.hasReferences())
			{
				settingRefrncFields.add(binaryField);
			}
			else
			{
				settingNonRefFields.add(binaryField);
			}
		}
		/* note:
		 * A variable length field sets the local offset variable to end up in an invalid state, but that is never read.
		 */
		
		// #validateVariableLengthLayout already ensured that only the last field can have variable length
		this.hasVaryingPersistedLengthInstances = !binaryFields.values().isEmpty()
			&& binaryFields.values().peek().isVariableLength()
		;
		
		this.hasPersistedReferences = !allReferenceFields.isEmpty();
		this.hasSettingMembers      = hasSettingMembers;
		
		this.storingFields       = storingFields      .toArray(this.binaryFieldClass());
		this.allReferenceFields  = allReferenceFields .toArray(this.binaryFieldClass());
		this.settingRefrncFields = settingRefrncFields.toArray(this.binaryFieldClass());
		this.settingNonRefFields = settingNonRefFields.toArray(this.binaryFieldClass());
	}
	
	
	@SuppressWarnings({"unchecked",  "rawtypes"})
	private Class<BinaryField<T>> binaryFieldClass()
	{
		// no idea how to get ".class" to work otherwise
		return (Class)BinaryField.class;
	}
	
	/**
	 * Only the last field may have variable length, otherweise simple offsets can't be used.
	 * 
	 * @param binaryFields
	 */
	private static void validateVariableLengthLayout(
		final XGettingTable<String, ? extends BinaryField<?>> binaryFields
	)
	{
		if(binaryFields.size() <= 1)
		{
			// no fields or a single field is implicitely valid.
			return;
		}
		
		final BinaryField<?> lastBinaryField = binaryFields.values().peek();
		for(final BinaryField<?> binaryField : binaryFields.values())
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
	public final void iterateInstanceReferences(final T instance, final PersistenceFunction iterator)
	{
		for(final BinaryField<?> referenceField : this.allReferenceFields)
		{
			referenceField.iterateReferences(instance, iterator);
		}
	}

	@Override
	public void iterateLoadableReferences(final Binary data, final PersistenceReferenceLoader loader)
	{
		for(final BinaryField<?> referenceField : this.allReferenceFields)
		{
			referenceField.iterateLoadableReferences(data, loader);
		}
	}

}
