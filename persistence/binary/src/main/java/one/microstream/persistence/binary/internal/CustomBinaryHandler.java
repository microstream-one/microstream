package one.microstream.persistence.binary.internal;

/*-
 * #%L
 * microstream-persistence-binary
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import static one.microstream.X.mayNull;
import static one.microstream.X.notNull;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import one.microstream.chars.XChars;
import one.microstream.collections.BulkList;
import one.microstream.collections.EqConstHashTable;
import one.microstream.collections.EqHashTable;
import one.microstream.collections.HashTable;
import one.microstream.collections.types.XAddingCollection;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.collections.types.XGettingTable;
import one.microstream.collections.types.XImmutableEnum;
import one.microstream.collections.types.XTable;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.BinaryField;
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
import one.microstream.reflect.XReflect;
import one.microstream.typing.KeyValue;


public class CustomBinaryHandler<T> extends AbstractBinaryHandlerCustom<T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	protected static final <T> BinaryField<T> Field_byte(final Getter_byte<T> getter)
	{
		return Field_byte(getter, null);
	}
	
	protected static final <T> BinaryField<T> Field_byte(
		final Getter_byte<T> getter,
		final Setter_byte<T> setter
	)
	{
		return Binary.Field_byte(BinaryField.Defaults.defaultUninitializedName(), getter, setter);
	}
	
	protected static final <T> BinaryField<T> Field_boolean(final Getter_boolean<T> getter)
	{
		return Field_boolean(getter, null);
	}
		
	protected static final <T> BinaryField<T> Field_boolean(
		final Getter_boolean<T> getter,
		final Setter_boolean<T> setter
	)
	{
		return Binary.Field_boolean(BinaryField.Defaults.defaultUninitializedName(), getter, setter);
	}
	
	protected static final <T> BinaryField<T> Field_short(final Getter_short<T> getter)
	{
		return Field_short(getter, null);
	}
		
	protected static final <T> BinaryField<T> Field_short(
		final Getter_short<T> getter,
		final Setter_short<T> setter
	)
	{
		return Binary.Field_short(BinaryField.Defaults.defaultUninitializedName(), getter, setter);
	}
	
	protected static final <T> BinaryField<T> Field_char(final Getter_char<T> getter)
	{
		return Field_char(getter, null);
	}
		
	protected static final <T> BinaryField<T> Field_char(
		final Getter_char<T> getter,
		final Setter_char<T> setter
	)
	{
		return Binary.Field_char(BinaryField.Defaults.defaultUninitializedName(), getter, setter);
	}
	
	protected static final <T> BinaryField<T> Field_int(final Getter_int<T> getter)
	{
		return Field_int(getter, null);
	}
		
	protected static final <T> BinaryField<T> Field_int(
		final Getter_int<T> getter,
		final Setter_int<T> setter
	)
	{
		return Binary.Field_int(BinaryField.Defaults.defaultUninitializedName(), getter, setter);
	}
	
	protected static final <T> BinaryField<T> Field_float(final Getter_float<T> getter)
	{
		return Field_float(getter, null);
	}
		
	protected static final <T> BinaryField<T> Field_float(
		final Getter_float<T> getter,
		final Setter_float<T> setter
	)
	{
		return Binary.Field_float(BinaryField.Defaults.defaultUninitializedName(), getter, setter);
	}
	
	protected static final <T> BinaryField<T> Field_long(final Getter_long<T> getter)
	{
		return Field_long(getter, null);
	}
		
	protected static final <T> BinaryField<T> Field_long(
		final Getter_long<T> getter,
		final Setter_long<T> setter
	)
	{
		return Binary.Field_long(BinaryField.Defaults.defaultUninitializedName(), getter, setter);
	}
	
	protected static final <T> BinaryField<T> Field_double(final Getter_double<T> getter)
	{
		return Field_double(getter, null);
	}
		
	protected static final <T> BinaryField<T> Field_double(
		final Getter_double<T> getter,
		final Setter_double<T> setter
	)
	{
		return Binary.Field_double(BinaryField.Defaults.defaultUninitializedName(), getter, setter);
	}
	
	protected static final <T, R> BinaryField<T> Field(final Class<R> referenceType, final Getter<T, R> getter)
	{
		return Field(referenceType, getter, null);
	}
		
	protected static final <T, R> BinaryField<T> Field(
		final Class<R>     referenceType,
		final Getter<T, R> getter       ,
		final Setter<T, R> setter
	)
	{
		return Binary.Field(referenceType, BinaryField.Defaults.defaultUninitializedName(), getter, setter);
	}
	
	// (22.01.2020 TM)TODO: priv#88: support variable length fields
	

	protected EqConstHashTable<String, ? extends BinaryField<? super T>> initializeDefinedFields(
		final XGettingSequence<? extends BinaryField<? super T>> binaryFields
	)
	{
		if(binaryFields == null)
		{
			// may be null for deferred on-demand initialization via reflection
			return null;
		}
		
		final EqHashTable<String, BinaryField.Initializable<? super T>> mappedBinaryFields = EqHashTable.New();
		
		for(final BinaryField<? super T> binaryField : binaryFields)
		{
			if(!(binaryField instanceof BinaryField.Initializable<?>))
			{
				throw new PersistenceException(
					BinaryField.class.getSimpleName() + "\"" + binaryField.name() + "\" is not "
					+ BinaryField.Initializable.class.getSimpleName() + "."
				);
			}
			
			final String identifier = binaryField.identifier();
			if(identifier == null)
			{
				throw new PersistenceException(
					"Unnamed " + BinaryField.class.getSimpleName() + " of type " + binaryField.type()
				);
			}
			
			if(!mappedBinaryFields.add(identifier, (BinaryField.Initializable<? super T>)binaryField))
			{
				throw new PersistenceException(
					"Duplicate " + BinaryField.class.getSimpleName() + " \"" + binaryField.type() + "\""
				);
			}
		}
				
		this.initializeBinaryFields(mappedBinaryFields);
		
		return mappedBinaryFields.immure();
	}
	
	
	
	public static <T> CustomBinaryHandler<T> New(
		final Class<T>                                   type        ,
		final PersistenceTypeInstantiator<Binary, T>     instantiator,
		final XGettingSequence<? extends BinaryField<? super T>> binaryFields
	)
	{
		return new CustomBinaryHandler<>(
			notNull(type),
			notNull(instantiator),
			notNull(binaryFields)
		);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final PersistenceTypeInstantiator<Binary, T> instantiator;
	
	// must be deferred-initialized since all fields have to be collected in a generic way
	private EqConstHashTable<String, ? extends BinaryField<? super T>> binaryFields;
	
	// having no setting members effectively means the type is an immutable value type
	private boolean hasSettingMembers;
	private boolean hasNonSettingMembers;
	private boolean hasPersistedReferences;

	private BinaryField<? super T>[] storingFields      ;
	private BinaryField<? super T>[] nonSettingFields   ;
	private BinaryField<? super T>[] allReferenceFields ;
	private BinaryField<? super T>[] settingRefrncFields;
	private BinaryField<? super T>[] settingNonRefFields;
	
	
	// may be null if no such field is present
	private BinaryField<? super T> trailingVariableLengthField;
	
	// all but trailing variable length field, if present.
	private int fixedLengthBinaryContent;
	
	

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	protected CustomBinaryHandler(final Class<T> type)
	{
		this(type, (XGettingSequence<? extends BinaryField<? super T>>)null);
	}

	protected CustomBinaryHandler(
		final Class<T>                                           type        ,
		final XGettingSequence<? extends BinaryField<? super T>> binaryFields
	)
	{
		this(type, deriveTypeName(type), null, binaryFields);
	}
	
	protected CustomBinaryHandler(
		final Class<T>                                           type        ,
		final String                                             typeName    ,
		final XGettingSequence<? extends BinaryField<? super T>> binaryFields
	)
	{
		this(type, typeName, null, binaryFields);
	}
	
	protected CustomBinaryHandler(
		final Class<T>                               type        ,
		final PersistenceTypeInstantiator<Binary, T> instantiator
	)
	{
		this(type, instantiator, null);
	}

	protected CustomBinaryHandler(
		final Class<T>                                           type        ,
		final PersistenceTypeInstantiator<Binary, T>             instantiator,
		final XGettingSequence<? extends BinaryField<? super T>> binaryFields
	)
	{
		this(type, deriveTypeName(type), instantiator, binaryFields);
	}
	
	protected CustomBinaryHandler(
		final Class<T>                                           type        ,
		final String                                             typeName    ,
		final PersistenceTypeInstantiator<Binary, T>             instantiator,
		final XGettingSequence<? extends BinaryField<? super T>> binaryFields
	)
	{
		super(type, typeName, binaryFields);
		this.instantiator = mayNull(instantiator);
		this.binaryFields = this.initializeDefinedFields(binaryFields);
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
		
		return this.trailingVariableLengthField != null;
	}
		
	@Override
	protected XImmutableEnum<? extends PersistenceTypeDefinitionMember> initializeInstanceMembers()
	{
		// super class's on-demand logic guarantees that this method is only called once for every instance.
		final XGettingSequence<? extends BinaryField<? super T>> binaryFields = this.reflectiveInitializeBinaryFields();
		
		return validateAndImmure(binaryFields);
	}
	
	private long calculcateContentLength(final T instance)
	{
		if(this.trailingVariableLengthField != null)
		{
			return this.fixedLengthBinaryContent + this.trailingVariableLengthField.calculateBinaryLength(instance);
		}
		
		return this.fixedLengthBinaryContent;
	}
	
	@Override
	public void store(
		final Binary                          data    ,
		final T                               instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		final long contentLength = this.calculcateContentLength(instance);

		data.storeEntityHeader(contentLength, this.typeId(), objectId);

		for(final BinaryField<? super T> storingField : this.storingFields)
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
	
	@Override
	public void initializeState(final Binary data, final T instance, final PersistenceLoadHandler handler)
	{
		// non-reference values were already set in #create
		this.setReferenceValues(instance, data, handler);
	}

	@Override
	public void updateState(final Binary data, final T instance, final PersistenceLoadHandler handler)
	{
		if(this.hasNonSettingMembers)
		{
			// read-only fields must be validated instead of updated. Of course BEFORE updating anything
			this.validateReadOnlyFields(instance, data, handler);
		}
		if(this.hasSettingMembers)
		{
			// update has to set both types of values
			this.setNonReferenceValues(instance, data, handler);
			this.setReferenceValues(instance, data, handler);
		}
	}
	
	protected void validateReadOnlyFields(final T instance, final Binary data, final PersistenceLoadHandler handler)
	{
		for(final BinaryField<? super T> nonSettingFieldField : this.nonSettingFields)
		{
			nonSettingFieldField.validateState(instance, data, handler);
		}
	}
	
	private void setNonReferenceValues(final T instance, final Binary data, final PersistenceLoadHandler handler)
	{
		for(final BinaryField<? super T> settingNonRefField : this.settingNonRefFields)
		{
			settingNonRefField.setToInstance(instance, data, handler);
		}
	}
	
	private void setReferenceValues(final T instance, final Binary data, final PersistenceLoadHandler handler)
	{
		for(final BinaryField<? super T> settingRefrncField : this.settingRefrncFields)
		{
			settingRefrncField.setToInstance(instance, data, handler);
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
		for(final BinaryField<? super T> storingField : this.storingFields)
		{
			logic.accept(storingField.type());
		}
		
		return logic;
	}
	
	protected final synchronized XGettingSequence<? extends BinaryField<? super T>> reflectiveInitializeBinaryFields()
	{
		final HashTable<Class<?>, XGettingSequence<BinaryField.Initializable<T>>> binaryFieldsPerClass = HashTable.New();
		
		this.collectBinaryFields(binaryFieldsPerClass);

		final EqHashTable<String, BinaryField.Initializable<? super T>> binaryFieldsInOrder = EqHashTable.New();
		this.defineBinaryFieldOrder(binaryFieldsPerClass, (identifier, field) ->
		{
			if(!binaryFieldsInOrder.add(identifier, field))
			{
				throw new PersistenceException(
					BinaryField.class.getSimpleName()
					+ " with the unique identifier \"" + identifier + "\" is already registered."
				);
			}
		});
		
		this.initializeBinaryFields(binaryFieldsInOrder);
		
		this.binaryFields = binaryFieldsInOrder.immure();
		
		return this.binaryFields.values();
	}
	
	private void collectBinaryFields(
		final HashTable<Class<?>, XGettingSequence<BinaryField.Initializable<T>>> binaryFieldsPerClass
	)
	{
		for(Class<?> c = this.getClass(); c != CustomBinaryHandler.class; c = c.getSuperclass())
		{
			// This construction is necessary to maintain the order even if a class overrides the collecting logic.
			final BulkList<BinaryField.Initializable<T>> binaryFieldsOfClass = BulkList.New();
			this.collectDeclaredBinaryFields(c, binaryFieldsOfClass);

			// Already existing entries (added by an extending class in an override of this method) are allowed.
			binaryFieldsPerClass.add(c, binaryFieldsOfClass);
		}
		
		// collection the fields "upwards" requires to reverse the collected class hierarchy in the end.
		binaryFieldsPerClass.reverse();
	}
	
	protected void validateBinaryFieldGenericType(final Field binaryFieldField)
	{
		// the cast is safe for BinaryField<T> since it is parameterized.
		final Type genericType = binaryFieldField.getGenericType();
		if(!(genericType instanceof ParameterizedType))
		{
			// omitted type parameter causes #getGenericType to return the primary type instead (which is idiotic).
			return;
		}
		
		final ParameterizedType parameterizedType = (ParameterizedType)genericType;
		
		// hardcoded array index is safe for BinaryField<T> since it has exactely one type parameter.
		final Type typeParameter = parameterizedType.getActualTypeArguments()[0];
		
		if(!(typeParameter instanceof Class))
		{
			// complex type parameters (WildCard etc.) are not analyzed (for now).
			return;
		}
		
		final Class<?> typeParameterClass = (Class<?>)typeParameter;
		if(XReflect.isActualClass(typeParameterClass) && typeParameterClass.isAssignableFrom(this.type()))
		{
			// same or field-layout-wise compatible class
			return;
		}

		throw new PersistenceException(
			BinaryField.class.getSimpleName()
			+ " type parameter \"" + typeParameterClass + "\""
			+ " of field \"" + binaryFieldField + "\""
			+ " is not compatible with this type handler's handled type \"" + this.type() + "\""
		);
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
				
				@SuppressWarnings("unchecked")
				final BinaryField<?> binaryField = (BinaryField<T>)field.get(this);
				if(!(binaryField instanceof BinaryField.Initializable))
				{
					continue;
				}
				
				this.validateBinaryFieldGenericType(field);
				
				@SuppressWarnings("unchecked")
				final BinaryField.Initializable<T> initializable = (BinaryField.Initializable<T>)binaryField;
				
				// the whole identifier must be initialized to ensure uniqueness.
				initializable.initializeIdentifierOptional(clazz.getName(), field.getName());
				target.add(initializable);
			}
			catch(final Exception e)
			{
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
		 * With the class hierarchy collection order guaranteed above, this loop does:
		 * - order binaryFields from most to least specific class ("upwards")
		 * - order binaryFields per class in declaration order
		 */
		for(final XGettingSequence<BinaryField.Initializable<T>> binaryFieldsOfClass : binaryFieldsPerClass.values())
		{
			for(final BinaryField.Initializable<T> binaryField : binaryFieldsOfClass)
			{
				collector.accept(binaryField.identifier(), binaryField);
			}
		}
	}
	
	private void initializeBinaryFields(
		final EqHashTable<String, BinaryField.Initializable<? super T>> binaryFields
	)
	{
		final BinaryField<? super T>   varLengthField      = checkVariableLengthLayout(binaryFields);
		final BulkList<BinaryField<? super T>> storingFields       = BulkList.New();
		final BulkList<BinaryField<? super T>> allReferenceFields  = BulkList.New();
		final BulkList<BinaryField<? super T>> settingNonRefFields = BulkList.New();
		final BulkList<BinaryField<? super T>> settingRefrncFields = BulkList.New();
		final BulkList<BinaryField<? super T>> nonSettingFields    = BulkList.New();
						
		int offset = 0;
		for(final BinaryField.Initializable<? super T> binaryField : binaryFields.values())
		{
			binaryField.initializeOffset(offset);
			storingFields.add(binaryField);
			
			if(binaryField.hasReferences())
			{
				allReferenceFields.add(binaryField);
			}
			
			// variable length field must be excluded as offset is co-used as the fixed content length
			if(binaryField != varLengthField)
			{
				offset += binaryField.persistentMinimumLength();
			}
			
			if(!binaryField.canSet())
			{
				nonSettingFields.add(binaryField);
				continue;
			}
			
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
		
		this.storingFields       = storingFields      .toArray(this.binaryFieldClass());
		this.nonSettingFields    = nonSettingFields   .toArray(this.binaryFieldClass());
		this.allReferenceFields  = allReferenceFields .toArray(this.binaryFieldClass());
		this.settingRefrncFields = settingRefrncFields.toArray(this.binaryFieldClass());
		this.settingNonRefFields = settingNonRefFields.toArray(this.binaryFieldClass());

		this.trailingVariableLengthField = varLengthField;
		this.hasPersistedReferences      = !allReferenceFields.isEmpty();
		this.hasSettingMembers           = !settingRefrncFields.isEmpty() || !settingNonRefFields.isEmpty();
		this.hasNonSettingMembers        = !nonSettingFields.isEmpty();
		this.fixedLengthBinaryContent    = offset;
	}
	
	@SuppressWarnings({"unchecked",  "rawtypes"})
	private Class<BinaryField<? super T>> binaryFieldClass()
	{
		// no idea how to get ".class" to work otherwise
		return (Class)BinaryField.class;
	}
	
	/**
	 * Only the last field may have variable length, otherwise simple offsets can't be used.
	 */
	private static <F extends BinaryField<?>> F checkVariableLengthLayout(
		final XTable<String, F> binaryFields
	)
	{
		if(binaryFields.isEmpty())
		{
			// empty fields is implicitly valid and, of course, yields null.
			return null;
		}
		
		KeyValue<String, F> varLengthEntry = null;
		for(final KeyValue<String, F> e : binaryFields)
		{
			if(e.value().isVariableLength())
			{
				if(varLengthEntry == null)
				{
					varLengthEntry = e;
					continue;
				}

				throw new PersistenceException(
					"Multiple variable length fields detected: "
					+ XChars.systemString(varLengthEntry.value()) + ": " + varLengthEntry.value().identifier()
					+ ", " + XChars.systemString(e.value()) + ": " + e.value().identifier()
				);
			}
		}
		
		if(varLengthEntry == null)
		{
			// no variable length field
			return null;
		}
		
		binaryFields.put(varLengthEntry);
		
		return varLengthEntry.value();
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
