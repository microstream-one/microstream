/**
 *
 */
package one.microstream.reference;

import one.microstream.collections.Singleton;


/**
 * 
 *
 */
/**
 * Simple Reference class to handle mutable references. Handle with care!
 * <p>
 * Note: In most cases, a mutable reference object like this should not be neccessary if the program is well
 * structured (that's why no such class exists in the Java API).
 * Extensive use of this class where it would be better to restructure the program may end in even more structural
 * problems.<br>
 * Yet in some cases, a mutable reference really is needed or at least helps in creating cleaner structures.<br>
 * So again, use wisely.
 */
public interface Reference<T> extends Referencing<T>
{
	public void set(T object);



	public static <T> Reference<T> New(final T object)
	{
		return Singleton.New(object);
	}

}
