package accessing;

import java.lang.reflect.Field;

/*
 * - Reflective / Array / Memory
 * - Static: needs no source. Instance: needs a source
 * - Value Type: Reference or one of 8 primitives
 * 
 * Reflective_int
 * Reflective_double
 * Reflective_*6
 * Reflective_Reference
 *  
 * Memory_int
 * Memory_double
 * Memory_*6
 * Memory_Reference
 */
final class ValueGetterInstance_int implements ValueGetter
{
	private final Field field;

	public ValueGetterInstance_int(final Field field)
	{
		super();
		this.field = field;
	}

	@Override
	public int get_int(final Object source)
	{
		try
		{
			return this.field.getInt(source);
		}
		catch(final Exception e)
		{
			throw new Error(e);
		}
	}

	@Override
	public Object get(final Object source)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Class<?> valueType()
	{
		return int.class;
	}

	@Override
	public void copyTo(final Object source, final ValueSetter setter, final Object target)
	{
		setter.set_int(target, this.get_int(source));
	}

	@Override
	public long as_long(final Object source)
	{
		return this.get_int(source);
	}

	@Override
	public Object as_Object(final Object source)
	{
		return Integer.valueOf(this.get_int(source));
	}

	@Override
	public void transformTo(final Object source, final ValueSetter setter, final Object target)
	{
		if(setter.valueType() == int.class)
		{
			setter.set_int(target, this.get_int(source));
		}
		if(setter.valueType() == long.class)
		{
			setter.set_long(target, this.as_long(source));
		}
		
		// (06.09.2019 TM)FIXME: etc ...
	}
	
}