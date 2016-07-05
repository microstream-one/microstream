/**
 *
 */
package net.jadoth.util.aspects;


/**
 * @author Thomas Muenz
 *
 */
public interface Aspect<C extends AspectContext>
{
	public Object invoke(C context);
}
