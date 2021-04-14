/**
 *
 */
package one.microstream.reference;



/**
 * 
 *
 */
public interface LinkingReference<T> extends Reference<T>, LinkingReferencing<T>
{
	@Override
	public LinkingReference<T> next();
}
