package one.microstream.experimental.aspectorientation;



/**
 *
 */

/**
 * @author Thomas Muenz
 *
 */
public abstract class MethodCall<R>
{
	public abstract R execute();

	@Override
	public String toString() {
		return this.getClass().getCanonicalName();
	}

}
