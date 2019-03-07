/**
 *
 */
package one.microstream.reference;



/**
 * @author Thomas Muenz
 *
 */
public interface LinkingReference<T> extends Reference<T>, LinkingReferencing<T>
{
	@Override
	public LinkingReference<T> next();
}
