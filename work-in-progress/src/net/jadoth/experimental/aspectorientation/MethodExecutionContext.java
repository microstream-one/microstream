package net.jadoth.experimental.aspectorientation;

/**
 *
 */

/**
 * @author Thomas Muenz
 *
 */
public interface MethodExecutionContext
{
	public <R> R executeMethod(MethodCall<R> methodhandle);
}
