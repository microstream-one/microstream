package one.microstream.reflect;

import java.lang.reflect.Field;

@FunctionalInterface
public interface CopyPredicate
{
	public <T, S extends T> boolean test(T source, S target, Field field, Object value);
	
	
	
	public static <T, S extends T> boolean all(final T source, final S target, final Field field, final Object value)
	{
		return true;
	}
	
	public static <T, S extends T> boolean none(final T source, final S target, final Field field, final Object value)
	{
		return false;
	}
}
