package one.microstream.persistence.binary.internal;

import static one.microstream.X.notNull;

import one.microstream.math.XMath;
import one.microstream.persistence.exceptions.PersistenceException;
import one.microstream.persistence.types.PersistenceTypeDefinitionMember;
import one.microstream.persistence.types.PersistenceTypeDefinitionMemberCreator;
import one.microstream.persistence.types.PersistenceTypeDefinitionMemberFieldGeneric;
import one.microstream.persistence.types.PersistenceTypeDescriptionMemberAppender;

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
	
	
	public static BinaryField New(
		final Class<?> type
	)
	{
		return New(type, Defaults.defaultUninitializedName());
	}
	
	public static BinaryField New(
		final Class<?> type,
		final String   name
	)
	{
		final long offset = Defaults.defaultUninitializedOffset();
		final PersistenceTypeDefinitionMemberFieldGeneric field =
			AbstractBinaryHandlerCustom.CustomField(type, notNull(name))
		;
		
		return type == byte.class   ? new Default_byte(field, offset)
			: type == boolean.class ? new Default_boolean(field, offset)
			: type == short.class   ? new Default_short(field, offset)
			: type == char.class    ? new Default_char(field, offset)
			: type == int.class     ? new Default_int(field, offset)
			: type == float.class   ? new Default_float(field, offset)
			: type == long.class    ? new Default_long(field, offset)
			: type == double.class  ? new Default_double(field, offset)
			: type == Object.class  ? new DefaultReference(field, offset)
			: unhandledFieldType(type)
		;
	}
	
	public static BinaryField unhandledFieldType(final Class<?> type)
	{
		// (06.01.2020 TM)EXCP: proper exception
		throw new PersistenceException("Unsupported " + BinaryField.class.getSimpleName() + " type: " + type);
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
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private PersistenceTypeDefinitionMemberFieldGeneric actual;
		private long offset;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Abstract(final PersistenceTypeDefinitionMemberFieldGeneric actual, final long offset)
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
	
	public final class Default_byte<T> extends Abstract<T>
	{

		Default_byte(final PersistenceTypeDefinitionMemberFieldGeneric actual, final long offset)
		{
			super(actual, offset);
		}

		// (06.01.2020 TM)FIXME: priv#88: remove in all implementations below if really not needed
//		@Override
//		public final byte read_byte(final Binary data)
//		{
//			return data.read_byte(this.offset());
//		}
//
//		@Override
//		public final void store_byte(final Binary data, final byte value)
//		{
//			data.store_byte(this.offset(), value);
//		}
		
		@Override
		public final BinaryField<T> copyForName(final String qualifier, final String name)
		{
			return new Default_byte<>(
				this.actual().copyForName(qualifier, name),
				this.offset()
			);
		}
		
	}
	
	public final class Default_boolean<T> extends Abstract<T>
	{

		Default_boolean(final PersistenceTypeDefinitionMemberFieldGeneric actual, final long offset)
		{
			super(actual, offset);
		}
		
//		@Override
//		public final boolean read_boolean(final Binary data)
//		{
//			return data.read_boolean(this.offset());
//		}
//
//		@Override
//		public final void store_boolean(final Binary data, final boolean value)
//		{
//			data.store_boolean(this.offset(), value);
//		}
		
		@Override
		public final BinaryField<T> copyForName(final String qualifier, final String name)
		{
			return new Default_boolean<>(
				this.actual().copyForName(qualifier, name),
				this.offset()
			);
		}
		
	}
	
	public final class Default_short<T> extends Abstract<T>
	{

		Default_short(final PersistenceTypeDefinitionMemberFieldGeneric actual, final long offset)
		{
			super(actual, offset);
		}
		
//		@Override
//		public final short read_short(final Binary data)
//		{
//			return data.read_short(this.offset());
//		}
//
//		@Override
//		public final void store_short(final Binary data, final short value)
//		{
//			data.store_short(this.offset(), value);
//		}
		
		@Override
		public final BinaryField<T> copyForName(final String qualifier, final String name)
		{
			return new Default_short<>(
				this.actual().copyForName(qualifier, name),
				this.offset()
			);
		}
		
	}
	
	public final class Default_char<T> extends Abstract<T>
	{

		Default_char(final PersistenceTypeDefinitionMemberFieldGeneric actual, final long offset)
		{
			super(actual, offset);
		}
		
//		@Override
//		public final char read_char(final Binary data)
//		{
//			return data.read_char(this.offset());
//		}
//
//		@Override
//		public final void store_char(final Binary data, final char value)
//		{
//			data.store_char(this.offset(), value);
//		}
		
		@Override
		public final BinaryField<T> copyForName(final String qualifier, final String name)
		{
			return new Default_char<>(
				this.actual().copyForName(qualifier, name),
				this.offset()
			);
		}
		
	}
	
	public final class Default_int<T> extends Abstract<T>
	{

		Default_int(final PersistenceTypeDefinitionMemberFieldGeneric actual, final long offset)
		{
			super(actual, offset);
		}
		
//		@Override
//		public final int read_int(final Binary data)
//		{
//			return data.read_int(this.offset());
//		}
//
//		@Override
//		public final void store_int(final Binary data, final int value)
//		{
//			data.store_int(this.offset(), value);
//		}
		
		@Override
		public final BinaryField<T> copyForName(final String qualifier, final String name)
		{
			return new Default_int<>(
				this.actual().copyForName(qualifier, name),
				this.offset()
			);
		}
		
	}
	
	public final class Default_float<T> extends Abstract<T>
	{

		Default_float(final PersistenceTypeDefinitionMemberFieldGeneric actual, final long offset)
		{
			super(actual, offset);
		}
		
//		@Override
//		public final float read_float(final Binary data)
//		{
//			return data.read_float(this.offset());
//		}
//
//		@Override
//		public final void store_float(final Binary data, final float value)
//		{
//			data.store_float(this.offset(), value);
//		}
		
		@Override
		public final BinaryField<T> copyForName(final String qualifier, final String name)
		{
			return new Default_float<>(
				this.actual().copyForName(qualifier, name),
				this.offset()
			);
		}
		
	}
	
	public final class Default_long<T> extends Abstract<T>
	{

		Default_long(final PersistenceTypeDefinitionMemberFieldGeneric actual, final long offset)
		{
			super(actual, offset);
		}
		
//		@Override
//		public final long read_long(final Binary data)
//		{
//			return data.read_long(this.offset());
//		}
//
//		@Override
//		public final void store_long(final Binary data, final long value)
//		{
//			data.store_long(this.offset(), value);
//		}
		
		@Override
		public final BinaryField<T> copyForName(final String qualifier, final String name)
		{
			return new Default_long<>(
				this.actual().copyForName(qualifier, name),
				this.offset()
			);
		}
		
	}
	
	public final class Default_double<T> extends Abstract<T>
	{

		Default_double(final PersistenceTypeDefinitionMemberFieldGeneric actual, final long offset)
		{
			super(actual, offset);
		}
		
//		@Override
//		public final double read_double(final Binary data)
//		{
//			return data.read_double(this.offset());
//		}
//
//		@Override
//		public final void store_double(final Binary data, final double value)
//		{
//			data.store_double(this.offset(), value);
//		}
		
		@Override
		public final BinaryField<T> copyForName(final String qualifier, final String name)
		{
			return new Default_double<>(
				this.actual().copyForName(qualifier, name),
				this.offset()
			);
		}
		
	}
	
	public final class DefaultReference extends Abstract
	{

		DefaultReference(final PersistenceTypeDefinitionMemberFieldGeneric actual, final long offset)
		{
			super(actual, offset);
		}
		
//		@Override
//		public Object readReference(final Binary data, final PersistenceLoadHandler handler)
//		{
//			final long objectId = data.read_long(this.offset());
//
//			return handler.lookupObject(objectId);
//		}
//
//		@Override
//		public void storeReference(final Binary data, final Object instance, final PersistenceStoreHandler handler)
//		{
//			final long objectId = handler.apply(instance);
//			data.store_long(this.offset(), objectId);
//		}
		
		@Override
		public final BinaryField copyForName(final String qualifier, final String name)
		{
			return new DefaultReference(
				this.actual().copyForName(qualifier, name),
				this.offset()
			);
		}
		
	}
	
}
