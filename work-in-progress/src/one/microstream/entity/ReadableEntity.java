package one.microstream.entity;


/**
 * An entity whose data can be read. It is not defined if the entity is mutable or immutable.
 * 
 * @author TM
 */
public interface ReadableEntity
{
	public default ReadableEntity $entity()
	{
		return this;
	}
		
	public ReadableEntity $data();
	
}
