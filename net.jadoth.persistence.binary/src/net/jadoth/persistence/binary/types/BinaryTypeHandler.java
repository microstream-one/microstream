package net.jadoth.persistence.binary.types;

import static net.jadoth.X.notNull;

import java.lang.reflect.Field;

import net.jadoth.persistence.types.PersistenceTypeDefinitionMemberField;
import net.jadoth.persistence.types.PersistenceTypeHandler;
import net.jadoth.reflect.XReflect;

public interface BinaryTypeHandler<T> extends PersistenceTypeHandler<Binary, T>
{

	
	public abstract class AbstractImplementation<T>
	extends PersistenceTypeHandler.AbstractImplementation<Binary, T>
	implements BinaryTypeHandler<T>
	{
		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////
		
		public static final PersistenceTypeDefinitionMemberField declaredField(
			final Class<?> declaringClass,
			final String   fieldName
		)
		{
			final Field field = XReflect.getDeclaredField(declaringClass, fieldName);
			return declaredField(field, BinaryPersistence.createFieldLengthResolver());
		}
		
		public static final PersistenceTypeDefinitionMemberField declaredField(final Field field)
		{
			return declaredField(field, BinaryPersistence.createFieldLengthResolver());
		}
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final BinaryValueAccessor binaryValueAccessor;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected AbstractImplementation(final Class<T> type, final BinaryValueAccessor binaryValueAccessor)
		{
			super(type);
			this.binaryValueAccessor = notNull(binaryValueAccessor);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		protected final byte get_byte(final long address)
		{
			return this.binaryValueAccessor.get_byte(address);
		}
		
		protected final boolean get_boolean(final long address)
		{
			return this.binaryValueAccessor.get_boolean(address);
		}
		
		protected final short get_short(final long address)
		{
			return this.binaryValueAccessor.get_short(address);
		}
		
		protected final char get_char(final long address)
		{
			return this.binaryValueAccessor.get_char(address);
		}
		
		protected final int get_int(final long address)
		{
			return this.binaryValueAccessor.get_int(address);
		}
		
		protected final float get_float(final long address)
		{
			return this.binaryValueAccessor.get_float(address);
		}
		
		protected final long get_long(final long address)
		{
			return this.binaryValueAccessor.get_long(address);
		}
		
		protected final double get_double(final long address)
		{
			return this.binaryValueAccessor.get_double(address);
		}
		
		
		
		protected final void set_byte(final long address, final byte value)
		{
			this.binaryValueAccessor.set_byte(address, value);
		}
		
		protected final void set_boolean(final long address, final boolean value)
		{
			this.binaryValueAccessor.set_boolean(address, value);
		}
		
		protected final void set_short(final long address, final short value)
		{
			this.binaryValueAccessor.set_short(address, value);
		}
		
		protected final void set_char(final long address, final char value)
		{
			this.binaryValueAccessor.set_char(address, value);
		}
		
		protected final void set_int(final long address, final int value)
		{
			this.binaryValueAccessor.set_int(address, value);
		}
		
		protected final void set_float(final long address, final float value)
		{
			this.binaryValueAccessor.set_float(address, value);
		}
		
		protected final void set_long(final long address, final long value)
		{
			this.binaryValueAccessor.set_long(address, value);
		}
		
		protected final void set_double(final long address, final double value)
		{
			this.binaryValueAccessor.set_double(address, value);
		}

	}

}
