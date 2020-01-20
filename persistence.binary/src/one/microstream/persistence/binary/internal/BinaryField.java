package one.microstream.persistence.binary.internal;

import one.microstream.math.XMath;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.exceptions.PersistenceException;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceReferenceLoader;
import one.microstream.persistence.types.PersistenceStoreHandler;
import one.microstream.persistence.types.PersistenceTypeDefinitionMember;
import one.microstream.persistence.types.PersistenceTypeDefinitionMemberCreator;
import one.microstream.persistence.types.PersistenceTypeDefinitionMemberFieldGeneric;
import one.microstream.persistence.types.PersistenceTypeDescriptionMemberAppender;
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

public interface BinaryField<T> extends PersistenceTypeDefinitionMemberFieldGeneric
{
	// (17.04.2019 TM)FIXME: priv#88: must have a connection to or even itself be a PersistenceTypeDefinitionMember.
	
	@Override
	public Class<?> type();
	
	@Override
	public String name();

	@Override
	public default BinaryField<T> copyForName(final String name)
	{
		return this.copyForName(null, name);
	}
	
	@Override
	public BinaryField<T> copyForName(String qualifier, String name);
	
	public void readValue(T instance, Binary data, PersistenceLoadHandler handler);
	
	public void storeValue(T instance, Binary data, PersistenceStoreHandler handler);
	
	public boolean canRead();
	
	public default <F extends PersistenceFunction> F iterateReferences(
		final Object instance,
		final F iterator
	)
	{
		// no-op in default implementation
		return iterator;
	}
	
	public default <L extends PersistenceReferenceLoader> L iterateLoadableReferences(
		final Binary data,
		final L loader
	)
	{
		// no-op in default implementation
		return loader;
	}
	
	
	// (06.01.2020 TM)FIXME: priv#88: remove if really not needed
//	public default void store_byte(final Binary data, final byte value)
//	{
//		throw new UnsupportedOperationException();
//	}
//
//	public default void store_boolean(final Binary data, final boolean value)
//	{
//		throw new UnsupportedOperationException();
//	}
//
//	public default void store_short(final Binary data, final short value)
//	{
//		throw new UnsupportedOperationException();
//	}
//
//	public default void store_char(final Binary data, final char value)
//	{
//		throw new UnsupportedOperationException();
//	}
//
//	public default void store_int(final Binary data, final int value)
//	{
//		throw new UnsupportedOperationException();
//	}
//
//	public default void store_float(final Binary data, final float value)
//	{
//		throw new UnsupportedOperationException();
//	}
//
//	public default void store_long(final Binary data, final long value)
//	{
//		throw new UnsupportedOperationException();
//	}
//
//	public default void store_double(final Binary data, final double value)
//	{
//		throw new UnsupportedOperationException();
//	}
//
//	public default void storeReference(final Binary data, final Object instance, final PersistenceStoreHandler handler)
//	{
//		throw new UnsupportedOperationException();
//	}
//
//
//
//	public default byte read_byte(final Binary data)
//	{
//		throw new UnsupportedOperationException();
//	}
//
//	public default boolean read_boolean(final Binary data)
//	{
//		throw new UnsupportedOperationException();
//	}
//
//	public default short read_short(final Binary data)
//	{
//		throw new UnsupportedOperationException();
//	}
//
//	public default char read_char(final Binary data)
//	{
//		throw new UnsupportedOperationException();
//	}
//
//	public default int read_int(final Binary data)
//	{
//		throw new UnsupportedOperationException();
//	}
//
//	public default float read_float(final Binary data)
//	{
//		throw new UnsupportedOperationException();
//	}
//
//	public default long read_long(final Binary data)
//	{
//		throw new UnsupportedOperationException();
//	}
//
//	public default double read_double(final Binary data)
//	{
//		throw new UnsupportedOperationException();
//	}
//
//	public default Object readReference(final Binary data, final PersistenceLoadHandler handler)
//	{
//		throw new UnsupportedOperationException();
//	}

	
	
	
	public interface Initializable<T> extends BinaryField<T>
	{
		public long initializeOffset(long offset);
		
		public String initializeName(String name);
		
		public default String initializeNameOptional(final String name)
		{
			final String currentName = this.name();
			if(currentName != null)
			{
				return currentName;
			}
			
			return this.initializeName(name);
		}
	}
	
	
	public interface Defaults
	{
		public static String defaultUninitializedName()
		{
			return BinaryField.Abstract.NAME_UNINITIALIZED;
		}
		
		public static long defaultUninitializedOffset()
		{
			return -1;
		}
	}
		
//	public static BinaryField Complex(
//		final PersistenceTypeDefinitionMemberFieldGeneric... nestedFields
//	)
//	{
//		return Complex(Defaults.defaultUninitializedName(), nestedFields);
//	}
//
//	public static BinaryField Complex(
//		final String                                         name        ,
//		final PersistenceTypeDefinitionMemberFieldGeneric... nestedFields
//	)
//	{
//		return new BinaryField.Abstract(
//			AbstractBinaryHandlerCustom.Complex(notNull(name), nestedFields),
//			Defaults.defaultUninitializedOffset()
//		);
//	}
//
//	public static BinaryField Bytes()
//	{
//		return Chars(Defaults.defaultUninitializedName());
//	}
//
//	public static BinaryField Bytes(final String name)
//	{
//		return new BinaryField.Abstract(
//			AbstractBinaryHandlerCustom.bytes(name),
//			Defaults.defaultUninitializedOffset()
//		);
//	}
//
//	public static BinaryField Chars()
//	{
//		return Chars(Defaults.defaultUninitializedName());
//	}
//
//	public static BinaryField Chars(final String name)
//	{
//		return new BinaryField.Abstract(
//			AbstractBinaryHandlerCustom.chars(name),
//			Defaults.defaultUninitializedOffset()
//		);
//	}
	
	public abstract class Abstract<T> implements BinaryField.Initializable<T>
	{
		///////////////////////////////////////////////////////////////////////////
		// constants //
		//////////////
		
		static final String NAME_UNINITIALIZED = "[Uninitialized " + BinaryField.class.getSimpleName() + "]";
		
		
		static PersistenceTypeDefinitionMemberFieldGeneric defineField(final Class<?> type)
		{
			final PersistenceTypeDefinitionMemberFieldGeneric field =
				AbstractBinaryHandlerCustom.CustomField(type, Defaults.defaultUninitializedName())
			;
			
			return field;
		}
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private PersistenceTypeDefinitionMemberFieldGeneric actual;
		private long offset;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected Abstract(final Class<?> type)
		{
			this(defineField(type), Defaults.defaultUninitializedOffset());
		}
		
		protected Abstract(final PersistenceTypeDefinitionMemberFieldGeneric actual, final long offset)
		{
			super();
			this.actual = actual;
			this.offset = offset;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final long initializeOffset(final long offset)
		{
			if(this.offset >= 0)
			{
				if(this.offset == offset)
				{
					return offset;
				}
				
				// (04.04.2019 TM)EXCP: proper exception
				throw new PersistenceException("Offset already assigned: " + this.offset + ". Passed: " + offset);
			}
			
			return this.offset = XMath.notNegative(offset);
		}
		
		protected final long offset()
		{
			return this.offset;
		}
		
		protected final PersistenceTypeDefinitionMemberFieldGeneric actual()
		{
			return this.actual;
		}
		
		@Override
		public final String initializeName(final String name)
		{
			final String currentName = this.actual.name();
			if(!currentName.equals(Defaults.defaultUninitializedName()))
			{
				if(currentName.equals(name))
				{
					return currentName;
				}
				
				// (04.04.2019 TM)EXCP: proper exception
				throw new PersistenceException(
					"Name already initialized: current name \"" + currentName + "\" != \"" + name + "\""
				);
			}
			this.actual = this.actual.copyForName(name);
			
			return this.actual.name();
		}
		
		@Override
		public final Class<?> type()
		{
			return this.actual.type();
		}

		@Override
		public String typeName()
		{
			return this.actual.typeName();
		}
		
		@Override
		public final String qualifier()
		{
			return this.actual.qualifier();
		}
		
		@Override
		public final String name()
		{
			return this.actual.name();
		}
		
		@Override
		public final String identifier()
		{
			return this.actual.identifier();
		}

		@Override
		public void assembleTypeDescription(final PersistenceTypeDescriptionMemberAppender assembler)
		{
			this.actual.assembleTypeDescription(assembler);
		}

		@Override
		public boolean isReference()
		{
			return this.actual.isReference();
		}

		@Override
		public boolean isPrimitive()
		{
			return this.actual.isPrimitive();
		}

		@Override
		public boolean isPrimitiveDefinition()
		{
			return this.actual.isPrimitiveDefinition();
		}
		
		@Override
		public boolean isEnumConstant()
		{
			return this.actual.isEnumConstant();
		}

		@Override
		public boolean hasReferences()
		{
			return this.actual.hasReferences();
		}

		@Override
		public long persistentMinimumLength()
		{
			return this.actual.persistentMinimumLength();
		}

		@Override
		public long persistentMaximumLength()
		{
			return this.actual.persistentMaximumLength();
		}

		@Override
		public boolean isValidPersistentLength(final long persistentLength)
		{
			return this.actual.isValidPersistentLength(persistentLength);
		}

		@Override
		public void validatePersistentLength(final long persistentLength)
		{
			this.actual.validatePersistentLength(persistentLength);
		}

		@Override
		public PersistenceTypeDefinitionMember createDefinitionMember(
			final PersistenceTypeDefinitionMemberCreator creator
		)
		{
			return this.actual.createDefinitionMember(creator);
		}
		
	}
	
	public final class Default_byte<T> extends BinaryField.Abstract<T>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final Getter_byte<T> getter;
		private final Setter_byte<T> setter;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default_byte(final Getter_byte<T> getter, final Setter_byte<T> setter)
		{
			super(byte.class);
			this.getter = getter;
			this.setter = setter;
		}
		
		Default_byte(
			final PersistenceTypeDefinitionMemberFieldGeneric actual,
			final long                                        offset,
			final Getter_byte<T>                              getter,
			final Setter_byte<T>                              setter
		)
		{
			super(actual, offset);
			this.getter = getter;
			this.setter = setter;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final boolean canRead()
		{
			return this.getter != null;
		}
		
		@Override
		public final void readValue(final T instance, final Binary data, final PersistenceLoadHandler handler)
		{
			final byte value = data.read_byte(this.offset());
			this.setter.set_byte(instance, value);
		}
		
		@Override
		public final void storeValue(final T instance, final Binary data, final PersistenceStoreHandler handler)
		{
			final byte value = this.getter.get_byte(instance);
			data.store_byte(this.offset(), value);
		}
		
		@Override
		public final BinaryField<T> copyForName(final String qualifier, final String name)
		{
			final PersistenceTypeDefinitionMemberFieldGeneric memberCopy = this.actual().copyForName(qualifier, name);
			return new Default_byte<>(memberCopy, this.offset(), this.getter, this.setter);
		}
		
	}
		
	public final class Default_boolean<T> extends BinaryField.Abstract<T>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final Getter_boolean<T> getter;
		private final Setter_boolean<T> setter;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default_boolean(final Getter_boolean<T> getter, final Setter_boolean<T> setter)
		{
			super(boolean.class);
			this.getter = getter;
			this.setter = setter;
		}
		
		Default_boolean(
			final PersistenceTypeDefinitionMemberFieldGeneric actual,
			final long                                        offset,
			final Getter_boolean<T>                           getter,
			final Setter_boolean<T>                           setter
		)
		{
			super(actual, offset);
			this.getter = getter;
			this.setter = setter;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final boolean canRead()
		{
			return this.getter != null;
		}
		
		@Override
		public final void readValue(final T instance, final Binary data, final PersistenceLoadHandler handler)
		{
			final boolean value = data.read_boolean(this.offset());
			this.setter.set_boolean(instance, value);
		}
		
		@Override
		public final void storeValue(final T instance, final Binary data, final PersistenceStoreHandler handler)
		{
			final boolean value = this.getter.get_boolean(instance);
			data.store_boolean(this.offset(), value);
		}
		
		@Override
		public final BinaryField<T> copyForName(final String qualifier, final String name)
		{
			final PersistenceTypeDefinitionMemberFieldGeneric memberCopy = this.actual().copyForName(qualifier, name);
			return new Default_boolean<>(memberCopy, this.offset(), this.getter, this.setter);
		}
		
	}
	
	public final class Default_short<T> extends BinaryField.Abstract<T>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final Getter_short<T> getter;
		private final Setter_short<T> setter;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default_short(final Getter_short<T> getter, final Setter_short<T> setter)
		{
			super(short.class);
			this.getter = getter;
			this.setter = setter;
		}
		
		Default_short(
			final PersistenceTypeDefinitionMemberFieldGeneric actual,
			final long                                        offset,
			final Getter_short<T>                             getter,
			final Setter_short<T>                             setter
		)
		{
			super(actual, offset);
			this.getter = getter;
			this.setter = setter;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final boolean canRead()
		{
			return this.getter != null;
		}
		
		@Override
		public final void readValue(final T instance, final Binary data, final PersistenceLoadHandler handler)
		{
			final short value = data.read_short(this.offset());
			this.setter.set_short(instance, value);
		}
		
		@Override
		public final void storeValue(final T instance, final Binary data, final PersistenceStoreHandler handler)
		{
			final short value = this.getter.get_short(instance);
			data.store_short(this.offset(), value);
		}
		
		@Override
		public final BinaryField<T> copyForName(final String qualifier, final String name)
		{
			final PersistenceTypeDefinitionMemberFieldGeneric memberCopy = this.actual().copyForName(qualifier, name);
			return new Default_short<>(memberCopy, this.offset(), this.getter, this.setter);
		}
		
	}
	
	public final class Default_char<T> extends BinaryField.Abstract<T>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final Getter_char<T> getter;
		private final Setter_char<T> setter;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default_char(final Getter_char<T> getter, final Setter_char<T> setter)
		{
			super(char.class);
			this.getter = getter;
			this.setter = setter;
		}
		
		Default_char(
			final PersistenceTypeDefinitionMemberFieldGeneric actual,
			final long                                        offset,
			final Getter_char<T>                              getter,
			final Setter_char<T>                              setter
		)
		{
			super(actual, offset);
			this.getter = getter;
			this.setter = setter;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final boolean canRead()
		{
			return this.getter != null;
		}
		
		@Override
		public final void readValue(final T instance, final Binary data, final PersistenceLoadHandler handler)
		{
			final char value = data.read_char(this.offset());
			this.setter.set_char(instance, value);
		}
		
		@Override
		public final void storeValue(final T instance, final Binary data, final PersistenceStoreHandler handler)
		{
			final char value = this.getter.get_char(instance);
			data.store_char(this.offset(), value);
		}
		
		@Override
		public final BinaryField<T> copyForName(final String qualifier, final String name)
		{
			final PersistenceTypeDefinitionMemberFieldGeneric memberCopy = this.actual().copyForName(qualifier, name);
			return new Default_char<>(memberCopy, this.offset(), this.getter, this.setter);
		}
		
	}
	
	public final class Default_int<T> extends BinaryField.Abstract<T>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final Getter_int<T> getter;
		private final Setter_int<T> setter;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default_int(final Getter_int<T> getter, final Setter_int<T> setter)
		{
			super(int.class);
			this.getter = getter;
			this.setter = setter;
		}
		
		Default_int(
			final PersistenceTypeDefinitionMemberFieldGeneric actual,
			final long                                        offset,
			final Getter_int<T>                               getter,
			final Setter_int<T>                               setter
		)
		{
			super(actual, offset);
			this.getter = getter;
			this.setter = setter;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final boolean canRead()
		{
			return this.getter != null;
		}
		
		@Override
		public final void readValue(final T instance, final Binary data, final PersistenceLoadHandler handler)
		{
			final int value = data.read_int(this.offset());
			this.setter.set_int(instance, value);
		}
		
		@Override
		public final void storeValue(final T instance, final Binary data, final PersistenceStoreHandler handler)
		{
			final int value = this.getter.get_int(instance);
			data.store_int(this.offset(), value);
		}
		
		@Override
		public final BinaryField<T> copyForName(final String qualifier, final String name)
		{
			final PersistenceTypeDefinitionMemberFieldGeneric memberCopy = this.actual().copyForName(qualifier, name);
			return new Default_int<>(memberCopy, this.offset(), this.getter, this.setter);
		}
		
	}
	
	public final class Default_float<T> extends BinaryField.Abstract<T>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final Getter_float<T> getter;
		private final Setter_float<T> setter;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default_float(final Getter_float<T> getter, final Setter_float<T> setter)
		{
			super(float.class);
			this.getter = getter;
			this.setter = setter;
		}
		
		Default_float(
			final PersistenceTypeDefinitionMemberFieldGeneric actual,
			final long                                        offset,
			final Getter_float<T>                             getter,
			final Setter_float<T>                             setter
		)
		{
			super(actual, offset);
			this.getter = getter;
			this.setter = setter;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final boolean canRead()
		{
			return this.getter != null;
		}
		
		@Override
		public final void readValue(final T instance, final Binary data, final PersistenceLoadHandler handler)
		{
			final float value = data.read_float(this.offset());
			this.setter.set_float(instance, value);
		}
		
		@Override
		public final void storeValue(final T instance, final Binary data, final PersistenceStoreHandler handler)
		{
			final float value = this.getter.get_float(instance);
			data.store_float(this.offset(), value);
		}
		
		@Override
		public final BinaryField<T> copyForName(final String qualifier, final String name)
		{
			final PersistenceTypeDefinitionMemberFieldGeneric memberCopy = this.actual().copyForName(qualifier, name);
			return new Default_float<>(memberCopy, this.offset(), this.getter, this.setter);
		}
		
	}
	
	public final class Default_long<T> extends BinaryField.Abstract<T>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final Getter_long<T> getter;
		private final Setter_long<T> setter;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default_long(final Getter_long<T> getter, final Setter_long<T> setter)
		{
			super(long.class);
			this.getter = getter;
			this.setter = setter;
		}
		
		Default_long(
			final PersistenceTypeDefinitionMemberFieldGeneric actual,
			final long                                        offset,
			final Getter_long<T>                              getter,
			final Setter_long<T>                              setter
		)
		{
			super(actual, offset);
			this.getter = getter;
			this.setter = setter;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final boolean canRead()
		{
			return this.getter != null;
		}
		
		@Override
		public final void readValue(final T instance, final Binary data, final PersistenceLoadHandler handler)
		{
			final long value = data.read_long(this.offset());
			this.setter.set_long(instance, value);
		}
		
		@Override
		public final void storeValue(final T instance, final Binary data, final PersistenceStoreHandler handler)
		{
			final long value = this.getter.get_long(instance);
			data.store_long(this.offset(), value);
		}
		
		@Override
		public final BinaryField<T> copyForName(final String qualifier, final String name)
		{
			final PersistenceTypeDefinitionMemberFieldGeneric memberCopy = this.actual().copyForName(qualifier, name);
			return new Default_long<>(memberCopy, this.offset(), this.getter, this.setter);
		}
		
	}
	
	public final class Default_double<T> extends BinaryField.Abstract<T>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final Getter_double<T> getter;
		private final Setter_double<T> setter;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default_double(final Getter_double<T> getter, final Setter_double<T> setter)
		{
			super(double.class);
			this.getter = getter;
			this.setter = setter;
		}
		
		Default_double(
			final PersistenceTypeDefinitionMemberFieldGeneric actual,
			final long                                        offset,
			final Getter_double<T>                            getter,
			final Setter_double<T>                            setter
		)
		{
			super(actual, offset);
			this.getter = getter;
			this.setter = setter;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final boolean canRead()
		{
			return this.getter != null;
		}
		
		@Override
		public final void readValue(final T instance, final Binary data, final PersistenceLoadHandler handler)
		{
			final double value = data.read_double(this.offset());
			this.setter.set_double(instance, value);
		}
		
		@Override
		public final void storeValue(final T instance, final Binary data, final PersistenceStoreHandler handler)
		{
			final double value = this.getter.get_double(instance);
			data.store_double(this.offset(), value);
		}
		
		@Override
		public final BinaryField<T> copyForName(final String qualifier, final String name)
		{
			final PersistenceTypeDefinitionMemberFieldGeneric memberCopy = this.actual().copyForName(qualifier, name);
			return new Default_double<>(memberCopy, this.offset(), this.getter, this.setter);
		}
		
	}
	
	public final class DefaultReference<T, R> extends BinaryField.Abstract<T>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final Class<R>     type  ;
		private final Getter<T, R> getter;
		private final Setter<T, R> setter;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		DefaultReference(final Class<R> referenceType, final Getter<T, R> getter, final Setter<T, R> setter)
		{
			super(referenceType);
			this.type   = referenceType;
			this.getter = getter;
			this.setter = setter;
		}
		
		DefaultReference(
			final Class<R>                                    type  ,
			final PersistenceTypeDefinitionMemberFieldGeneric actual,
			final long                                        offset,
			final Getter<T, R>                                getter,
			final Setter<T, R>                                setter
		)
		{
			super(actual, offset);
			this.type   = type  ;
			this.getter = getter;
			this.setter = setter;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final boolean canRead()
		{
			return this.getter != null;
		}
		
		@Override
		public final void readValue(final T instance, final Binary data, final PersistenceLoadHandler handler)
		{
			final long objectId  = data.read_long(this.offset());
			final R    reference = this.type.cast(handler.lookupObject(objectId));
			this.setter.set(instance, reference);
		}
		
		@Override
		public final void storeValue(final T instance, final Binary data, final PersistenceStoreHandler handler)
		{
			final Object reference = this.getter.get(instance);
			final long   objectId  = handler.apply(reference);
			data.store_long(this.offset(), objectId);
		}
		
		@Override
		public final BinaryField<T> copyForName(final String qualifier, final String name)
		{
			final PersistenceTypeDefinitionMemberFieldGeneric memberCopy = this.actual().copyForName(qualifier, name);
			return new DefaultReference<>(this.type, memberCopy, this.offset(), this.getter, this.setter);
		}
		
		@Override
		public final <F extends PersistenceFunction> F iterateReferences(
			final Object instance,
			final F      iterator
		)
		{
			@SuppressWarnings("unchecked") // due to typing conflict with primitives
			final Object reference = this.getter.get((T)instance);
			iterator.apply(reference);
			
			return iterator;
		}
		
		@Override
		public final <L extends PersistenceReferenceLoader> L iterateLoadableReferences(
			final Binary data  ,
			final L      loader
		)
		{
			final long objectId = data.read_long(this.offset());
			loader.acceptObjectId(objectId);

			return loader;
		}
		
	}
	
}
