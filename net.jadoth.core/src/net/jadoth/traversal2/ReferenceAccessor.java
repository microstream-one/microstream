package net.jadoth.traversal2;

import static net.jadoth.Jadoth.notNull;

import java.lang.reflect.Field;

import net.jadoth.reflect.JadothReflect;


public interface ReferenceAccessor
{
	public Object get(Object parent);
	
	public void set(Object parent, Object newValue);
	
	public default Object getSet(final Object parent, final Object newValue)
	{
		final Object oldValue = this.get(parent);
		this.set(parent, newValue);
		return oldValue;
	}
	
	
	
	public static ReferenceAccessor New(final Class<?> type, final Field field)
	{
		field.setAccessible(true);
		
		return new ByField(
			notNull(type),
			notNull(field)
		);
	}
	
	public final class ByField implements ReferenceAccessor
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final Class<?> type ;
		private final Field    field;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		ByField(final Class<?> type, final Field field)
		{
			super();
			this.type  = type;
			this.field = field;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final Object get(final Object parent)
		{
			return this.type.cast(JadothReflect.getFieldValue(this.field, parent));
		}

		@Override
		public final void set(final Object parent, final Object newValue)
		{
			JadothReflect.setFieldValue(this.field, parent, newValue);
		}
		
	}
	
	
}
