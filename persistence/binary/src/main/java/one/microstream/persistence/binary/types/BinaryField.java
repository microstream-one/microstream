package one.microstream.persistence.binary.types;

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

import one.microstream.chars.XChars;
import one.microstream.math.XMath;
import one.microstream.persistence.binary.exceptions.BinaryPersistenceException;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustom;
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
	@Override
	public Class<?> type();
	
	@Override
	public String name();

	@Override
	public default BinaryField<T> copyForName(final String name)
	{
		return this.copyForName(this.qualifier(), name);
	}
	
	@Override
	public BinaryField<T> copyForName(String qualifier, String name);

	public void storeFromInstance(T instance, Binary data, PersistenceStoreHandler<Binary> handler);
	
	public void setToInstance(T instance, Binary data, PersistenceLoadHandler handler);
	
	public void validateState(T instance, Binary data, PersistenceLoadHandler handler);
	
	public boolean canSet();
	
	public default <F extends PersistenceFunction> F iterateReferences(
		final Object instance,
		final F      iterator
	)
	{
		// no-op in default implementation
		return iterator;
	}
	
	public default <L extends PersistenceReferenceLoader> L iterateLoadableReferences(
		final Binary data  ,
		final L      loader
	)
	{
		// no-op in default implementation
		return loader;
	}
	
	public long calculateBinaryLength(T instance);
	
//	public long binaryOffset();
		
	public default byte read_byte(final Binary data)
	{
		throw new UnsupportedOperationException();
	}

	public default boolean read_boolean(final Binary data)
	{
		throw new UnsupportedOperationException();
	}

	public default short read_short(final Binary data)
	{
		throw new UnsupportedOperationException();
	}

	public default char read_char(final Binary data)
	{
		throw new UnsupportedOperationException();
	}

	public default int read_int(final Binary data)
	{
		throw new UnsupportedOperationException();
	}

	public default float read_float(final Binary data)
	{
		throw new UnsupportedOperationException();
	}

	public default long read_long(final Binary data)
	{
		throw new UnsupportedOperationException();
	}

	public default double read_double(final Binary data)
	{
		throw new UnsupportedOperationException();
	}

	public default Object readReference(final Binary data, final PersistenceLoadHandler handler)
	{
		throw new UnsupportedOperationException();
	}
	
	
	
	public interface Initializable<T> extends BinaryField<T>
	{
		public long initializeOffset(long offset);
		
		public String initializeIdentifier(String qualifier, String name);
		
		public default String initializeIdentifierOptional(final String qualifier, final String name)
		{
			final String currentName = this.name();
			if(!Defaults.defaultUninitializedName().equals(currentName))
			{
				return currentName;
			}
			
			return this.initializeIdentifier(qualifier, name);
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
		
	
	
	public abstract class Abstract<T> implements BinaryField.Initializable<T>
	{
		///////////////////////////////////////////////////////////////////////////
		// constants //
		//////////////
		
		static final String NAME_UNINITIALIZED = "[Uninitialized " + BinaryField.class.getSimpleName() + "]";
		
		static PersistenceTypeDefinitionMemberFieldGeneric defineField(final Class<?> type, final String name)
		{
			// note: field name may not be null, hence the "uninitialized" dummy.
			final PersistenceTypeDefinitionMemberFieldGeneric field =
				AbstractBinaryHandlerCustom.CustomField(type, name)
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
			this(type, Defaults.defaultUninitializedName());
		}
		
		protected Abstract(final Class<?> type, final String name)
		{
			this(defineField(type, name), Defaults.defaultUninitializedOffset());
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
				
				throw new BinaryPersistenceException("Offset already assigned: " + this.offset + ". Passed: " + offset);
			}
			
			return this.offset = XMath.notNegative(offset);
		}

		public final long binaryOffset()
		{
			return this.offset;
		}
		
		protected final PersistenceTypeDefinitionMemberFieldGeneric actual()
		{
			return this.actual;
		}
		
		@Override
		public final String initializeIdentifier(final String qualifier, final String name)
		{
			final String currentName = this.actual.name();
			if(!currentName.equals(Defaults.defaultUninitializedName()))
			{
				if(currentName.equals(name))
				{
					return currentName;
				}
				
				throw new BinaryPersistenceException(
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
		public final String typeName()
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
		public final void assembleTypeDescription(final PersistenceTypeDescriptionMemberAppender assembler)
		{
			this.actual.assembleTypeDescription(assembler);
		}

		@Override
		public final boolean isReference()
		{
			return this.actual.isReference();
		}

		@Override
		public final boolean isPrimitive()
		{
			return this.actual.isPrimitive();
		}

		@Override
		public final boolean isPrimitiveDefinition()
		{
			return this.actual.isPrimitiveDefinition();
		}
		
		@Override
		public final boolean isEnumConstant()
		{
			return this.actual.isEnumConstant();
		}

		@Override
		public final boolean hasReferences()
		{
			return this.actual.hasReferences();
		}

		@Override
		public final long persistentMinimumLength()
		{
			return this.actual.persistentMinimumLength();
		}

		@Override
		public final long persistentMaximumLength()
		{
			return this.actual.persistentMaximumLength();
		}

		@Override
		public final boolean isValidPersistentLength(final long persistentLength)
		{
			return this.actual.isValidPersistentLength(persistentLength);
		}

		@Override
		public final void validatePersistentLength(final long persistentLength)
		{
			this.actual.validatePersistentLength(persistentLength);
		}

		@Override
		public final PersistenceTypeDefinitionMember createDefinitionMember(
			final PersistenceTypeDefinitionMemberCreator creator
		)
		{
			return this.actual.createDefinitionMember(creator);
		}
		
		protected void throwValidationException(final String instanceValue, final String persistedValue)
		{
			throw new BinaryPersistenceException(
				"Invalid value change for field " + this.identifier()
				+ ": instance value " + instanceValue
				+ " != persisted value " + persistedValue
				+ "."
			);
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
		
		Default_byte(final String name, final Getter_byte<T> getter, final Setter_byte<T> setter)
		{
			super(byte.class, name);
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
		public final long calculateBinaryLength(final T instance)
		{
			return Byte.BYTES;
		}
		
		@Override
		public final BinaryField<T> copyForName(final String qualifier, final String name)
		{
			final PersistenceTypeDefinitionMemberFieldGeneric memberCopy = this.actual().copyForName(qualifier, name);
			return new Default_byte<>(memberCopy, this.binaryOffset(), this.getter, this.setter);
		}
		
		@Override
		public final void storeFromInstance(
			final T                               instance,
			final Binary                          data    ,
			final PersistenceStoreHandler<Binary> handler
		)
		{
			final byte value = this.getter.get_byte(instance);
			data.store_byte(this.binaryOffset(), value);
		}
		
		@Override
		public final byte read_byte(final Binary data)
		{
			return data.read_byte(this.binaryOffset());
		}
		
		@Override
		public final boolean canSet()
		{
			return this.setter != null;
		}
		
		@Override
		public final void setToInstance(final T instance, final Binary data, final PersistenceLoadHandler handler)
		{
			final byte value = this.read_byte(data);
			this.setter.set_byte(instance, value);
		}
		
		@Override
		public final void validateState(final T instance, final Binary data, final PersistenceLoadHandler handler)
		{
			final byte instanceValue  = this.getter.get_byte(instance);
			final byte persistedValue = this.read_byte(data);
			
			if(persistedValue == instanceValue)
			{
				return;
			}
			
			this.throwValidationException(
				String.valueOf(instanceValue),
				String.valueOf(persistedValue)
			);
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
		
		Default_boolean(final String name, final Getter_boolean<T> getter, final Setter_boolean<T> setter)
		{
			super(boolean.class, name);
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
		public final long calculateBinaryLength(final T instance)
		{
			return Byte.BYTES;
		}
		
		@Override
		public final BinaryField<T> copyForName(final String qualifier, final String name)
		{
			final PersistenceTypeDefinitionMemberFieldGeneric memberCopy = this.actual().copyForName(qualifier, name);
			return new Default_boolean<>(memberCopy, this.binaryOffset(), this.getter, this.setter);
		}
		
		@Override
		public final void storeFromInstance(
			final T                               instance,
			final Binary                          data    ,
			final PersistenceStoreHandler<Binary> handler
		)
		{
			final boolean value = this.getter.get_boolean(instance);
			data.store_boolean(this.binaryOffset(), value);
		}
		
		@Override
		public final boolean read_boolean(final Binary data)
		{
			return data.read_boolean(this.binaryOffset());
		}
		
		@Override
		public final boolean canSet()
		{
			return this.setter != null;
		}
		
		@Override
		public final void setToInstance(final T instance, final Binary data, final PersistenceLoadHandler handler)
		{
			final boolean value = this.read_boolean(data);
			this.setter.set_boolean(instance, value);
		}
		
		@Override
		public final void validateState(final T instance, final Binary data, final PersistenceLoadHandler handler)
		{
			final boolean instanceValue  = this.getter.get_boolean(instance);
			final boolean persistedValue = this.read_boolean(data);
			
			if(persistedValue == instanceValue)
			{
				return;
			}
			
			this.throwValidationException(
				String.valueOf(instanceValue),
				String.valueOf(persistedValue)
			);
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
		
		Default_short(final String name, final Getter_short<T> getter, final Setter_short<T> setter)
		{
			super(short.class, name);
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
		public final long calculateBinaryLength(final T instance)
		{
			return Short.BYTES;
		}
		
		@Override
		public final BinaryField<T> copyForName(final String qualifier, final String name)
		{
			final PersistenceTypeDefinitionMemberFieldGeneric memberCopy = this.actual().copyForName(qualifier, name);
			return new Default_short<>(memberCopy, this.binaryOffset(), this.getter, this.setter);
		}
		
		@Override
		public final void storeFromInstance(
			final T                               instance,
			final Binary                          data    ,
			final PersistenceStoreHandler<Binary> handler
		)
		{
			final short value = this.getter.get_short(instance);
			data.store_short(this.binaryOffset(), value);
		}

		@Override
		public final short read_short(final Binary data)
		{
			return data.read_short(this.binaryOffset());
		}
		
		@Override
		public final boolean canSet()
		{
			return this.setter != null;
		}
		
		@Override
		public final void setToInstance(final T instance, final Binary data, final PersistenceLoadHandler handler)
		{
			final short value = this.read_short(data);
			this.setter.set_short(instance, value);
		}
		
		@Override
		public final void validateState(final T instance, final Binary data, final PersistenceLoadHandler handler)
		{
			final short instanceValue  = this.getter.get_short(instance);
			final short persistedValue = this.read_short(data);
			
			if(persistedValue == instanceValue)
			{
				return;
			}
			
			this.throwValidationException(
				String.valueOf(instanceValue),
				String.valueOf(persistedValue)
			);
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
		
		Default_char(final String name, final Getter_char<T> getter, final Setter_char<T> setter)
		{
			super(char.class, name);
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
		public final long calculateBinaryLength(final T instance)
		{
			return Character.BYTES;
		}
		
		@Override
		public final BinaryField<T> copyForName(final String qualifier, final String name)
		{
			final PersistenceTypeDefinitionMemberFieldGeneric memberCopy = this.actual().copyForName(qualifier, name);
			return new Default_char<>(memberCopy, this.binaryOffset(), this.getter, this.setter);
		}
		
		@Override
		public final void storeFromInstance(
			final T                               instance,
			final Binary                          data    ,
			final PersistenceStoreHandler<Binary> handler
		)
		{
			final char value = this.getter.get_char(instance);
			data.store_char(this.binaryOffset(), value);
		}
		
		@Override
		public final char read_char(final Binary data)
		{
			return data.read_char(this.binaryOffset());
		}
		
		@Override
		public final boolean canSet()
		{
			return this.setter != null;
		}
		
		@Override
		public final void setToInstance(final T instance, final Binary data, final PersistenceLoadHandler handler)
		{
			final char value = this.read_char(data);
			this.setter.set_char(instance, value);
		}
		
		@Override
		public final void validateState(final T instance, final Binary data, final PersistenceLoadHandler handler)
		{
			final char instanceValue  = this.getter.get_char(instance);
			final char persistedValue = this.read_char(data);
			
			if(persistedValue == instanceValue)
			{
				return;
			}
			
			this.throwValidationException(
				String.valueOf(instanceValue),
				String.valueOf(persistedValue)
			);
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
		
		Default_int(final String name, final Getter_int<T> getter, final Setter_int<T> setter)
		{
			super(int.class, name);
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
		public final long calculateBinaryLength(final T instance)
		{
			return Integer.BYTES;
		}
		
		@Override
		public final BinaryField<T> copyForName(final String qualifier, final String name)
		{
			final PersistenceTypeDefinitionMemberFieldGeneric memberCopy = this.actual().copyForName(qualifier, name);
			return new Default_int<>(memberCopy, this.binaryOffset(), this.getter, this.setter);
		}
		
		@Override
		public final void storeFromInstance(
			final T                               instance,
			final Binary                          data    ,
			final PersistenceStoreHandler<Binary> handler
		)
		{
			final int value = this.getter.get_int(instance);
			data.store_int(this.binaryOffset(), value);
		}
		
		@Override
		public final int read_int(final Binary data)
		{
			return data.read_int(this.binaryOffset());
		}
		
		@Override
		public final boolean canSet()
		{
			return this.setter != null;
		}
		
		@Override
		public final void setToInstance(final T instance, final Binary data, final PersistenceLoadHandler handler)
		{
			final int value = this.read_int(data);
			this.setter.set_int(instance, value);
		}
		
		@Override
		public final void validateState(final T instance, final Binary data, final PersistenceLoadHandler handler)
		{
			final int instanceValue  = this.getter.get_int(instance);
			final int persistedValue = this.read_int(data);
			
			if(persistedValue == instanceValue)
			{
				return;
			}
			
			this.throwValidationException(
				String.valueOf(instanceValue),
				String.valueOf(persistedValue)
			);
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
		
		Default_float(final String name, final Getter_float<T> getter, final Setter_float<T> setter)
		{
			super(float.class, name);
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
		public final long calculateBinaryLength(final T instance)
		{
			return Float.BYTES;
		}
		
		@Override
		public final BinaryField<T> copyForName(final String qualifier, final String name)
		{
			final PersistenceTypeDefinitionMemberFieldGeneric memberCopy = this.actual().copyForName(qualifier, name);
			return new Default_float<>(memberCopy, this.binaryOffset(), this.getter, this.setter);
		}
		
		@Override
		public final void storeFromInstance(
			final T                               instance,
			final Binary                          data    ,
			final PersistenceStoreHandler<Binary> handler
		)
		{
			final float value = this.getter.get_float(instance);
			data.store_float(this.binaryOffset(), value);
		}
		
		@Override
		public final float read_float(final Binary data)
		{
			return data.read_float(this.binaryOffset());
		}
		
		@Override
		public final boolean canSet()
		{
			return this.setter != null;
		}
		
		@Override
		public final void setToInstance(final T instance, final Binary data, final PersistenceLoadHandler handler)
		{
			final float value = this.read_float(data);
			this.setter.set_float(instance, value);
		}
		
		@Override
		public final void validateState(final T instance, final Binary data, final PersistenceLoadHandler handler)
		{
			final float instanceValue  = this.getter.get_float(instance);
			final float persistedValue = this.read_float(data);
			
			if(persistedValue == instanceValue)
			{
				return;
			}
			
			this.throwValidationException(
				String.valueOf(instanceValue),
				String.valueOf(persistedValue)
			);
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
		
		Default_long(final String name, final Getter_long<T> getter, final Setter_long<T> setter)
		{
			super(long.class, name);
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
		public final long calculateBinaryLength(final T instance)
		{
			return Long.BYTES;
		}
		
		@Override
		public final BinaryField<T> copyForName(final String qualifier, final String name)
		{
			final PersistenceTypeDefinitionMemberFieldGeneric memberCopy = this.actual().copyForName(qualifier, name);
			return new Default_long<>(memberCopy, this.binaryOffset(), this.getter, this.setter);
		}
		
		@Override
		public final void storeFromInstance(
			final T                               instance,
			final Binary                          data    ,
			final PersistenceStoreHandler<Binary> handler
		)
		{
			final long value = this.getter.get_long(instance);
			data.store_long(this.binaryOffset(), value);
		}
		
		@Override
		public final long read_long(final Binary data)
		{
			return data.read_long(this.binaryOffset());
		}
		
		@Override
		public final boolean canSet()
		{
			return this.setter != null;
		}
		
		@Override
		public final void setToInstance(final T instance, final Binary data, final PersistenceLoadHandler handler)
		{
			final long value = this.read_long(data);
			this.setter.set_long(instance, value);
		}
		
		@Override
		public final void validateState(final T instance, final Binary data, final PersistenceLoadHandler handler)
		{
			final long instanceValue  = this.getter.get_long(instance);
			final long persistedValue = this.read_long(data);
			
			if(persistedValue == instanceValue)
			{
				return;
			}
			
			this.throwValidationException(
				String.valueOf(instanceValue),
				String.valueOf(persistedValue)
			);
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
		
		Default_double(final String name, final Getter_double<T> getter, final Setter_double<T> setter)
		{
			super(double.class, name);
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
		public final long calculateBinaryLength(final T instance)
		{
			return Double.BYTES;
		}
		
		@Override
		public final BinaryField<T> copyForName(final String qualifier, final String name)
		{
			final PersistenceTypeDefinitionMemberFieldGeneric memberCopy = this.actual().copyForName(qualifier, name);
			return new Default_double<>(memberCopy, this.binaryOffset(), this.getter, this.setter);
		}
		
		@Override
		public final void storeFromInstance(
			final T                               instance,
			final Binary                          data    ,
			final PersistenceStoreHandler<Binary> handler
		)
		{
			final double value = this.getter.get_double(instance);
			data.store_double(this.binaryOffset(), value);
		}
		
		@Override
		public final double read_double(final Binary data)
		{
			return data.read_double(this.binaryOffset());
		}
		
		@Override
		public final boolean canSet()
		{
			return this.setter != null;
		}
		
		@Override
		public final void setToInstance(final T instance, final Binary data, final PersistenceLoadHandler handler)
		{
			final double value = this.read_double(data);
			this.setter.set_double(instance, value);
		}
		
		@Override
		public final void validateState(final T instance, final Binary data, final PersistenceLoadHandler handler)
		{
			final double instanceValue  = this.getter.get_double(instance);
			final double persistedValue = this.read_double(data);
			
			if(persistedValue == instanceValue)
			{
				return;
			}
			
			this.throwValidationException(
				String.valueOf(instanceValue),
				String.valueOf(persistedValue)
			);
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
		
		DefaultReference(
			final Class<R>     referenceType,
			final String       name         ,
			final Getter<T, R> getter       ,
			final Setter<T, R> setter
		)
		{
			super(referenceType, name);
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
		public final long calculateBinaryLength(final T instance)
		{
			return Binary.objectIdByteLength();
		}
		
		@Override
		public final BinaryField<T> copyForName(final String qualifier, final String name)
		{
			final PersistenceTypeDefinitionMemberFieldGeneric memberCopy = this.actual().copyForName(qualifier, name);
			return new DefaultReference<>(this.type, memberCopy, this.binaryOffset(), this.getter, this.setter);
		}
		
		@Override
		public final void storeFromInstance(
			final T                               instance,
			final Binary                          data    ,
			final PersistenceStoreHandler<Binary> handler
		)
		{
			final Object reference = this.getter.get(instance);
			final long   objectId  = handler.apply(reference);
			data.store_long(this.binaryOffset(), objectId);
		}
		
		@Override
		public final R readReference(final Binary data, final PersistenceLoadHandler handler)
		{
			final long objectId  = data.read_long(this.binaryOffset());
			final R    reference = this.type.cast(handler.lookupObject(objectId));
			
			return reference;
		}
		
		@Override
		public final boolean canSet()
		{
			return this.setter != null;
		}
		
		@Override
		public final void setToInstance(final T instance, final Binary data, final PersistenceLoadHandler handler)
		{
			final R reference = this.readReference(data, handler);
			this.setter.set(instance, reference);
		}
		
		@Override
		public final <F extends PersistenceFunction> F iterateReferences(
			final Object instance,
			final F      iterator
		)
		{
			@SuppressWarnings("unchecked") // due to typing conflict with primitives
			final R reference = this.getter.get((T)instance);
			iterator.apply(reference);
			
			return iterator;
		}
		
		@Override
		public final <L extends PersistenceReferenceLoader> L iterateLoadableReferences(
			final Binary data  ,
			final L      loader
		)
		{
			final long objectId = data.read_long(this.binaryOffset());
			loader.acceptObjectId(objectId);

			return loader;
		}
		
		@Override
		public final void validateState(final T instance, final Binary data, final PersistenceLoadHandler handler)
		{
			final R instanceValue  = this.getter.get(instance);
			final R persistedValue = this.readReference(data, handler);
			
			if(persistedValue == instanceValue)
			{
				return;
			}
			
			this.throwValidationException(
				XChars.systemString(instanceValue),
				XChars.systemString(persistedValue)
			);
		}
		
	}
	
}
