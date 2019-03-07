package one.microstream.entity;


/**
 * An entity whose data can be read. It is not defined if the entity is mutable or immutable.
 * 
 * @author TM
 *
 * @param <E>
 */
public interface ReadableEntity<E extends ReadableEntity<E>>
{
	@SuppressWarnings("unchecked")
	public static <E> E identity(final E instance)
	{
		return instance instanceof Entity<?>
			? (E)((Entity<?>)instance).$entity()
			: instance
		;
	}
	
	@SuppressWarnings("unchecked")
	public static <E> E data(final E instance)
	{
		return instance instanceof Entity<?>
			? (E)((Entity<?>)instance).$data()
			: instance
		;
	}
	
	@SuppressWarnings("unchecked")
	public default E $entity()
	{
		return (E)this;
	}
		
	public E $data();
	
}
