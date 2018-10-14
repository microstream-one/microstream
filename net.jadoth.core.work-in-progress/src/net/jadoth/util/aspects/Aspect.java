/**
 *
 */
package net.jadoth.util.aspects;

import net.jadoth.collections.DownwrapList;
import net.jadoth.collections.ListView;
import net.jadoth.collections.types.XGettingList;
import net.jadoth.collections.types.XList;

/**
 * @author Thomas Muenz
 *
 */
public interface Aspect<C extends AspectContext>
{
	public Object invoke(C context);
	
	/**
	 * Magically causes an instance of type {@code T} to be usable as if it was of type {@code S extends T}.
	 * It can best be seen as a reflective-wise "hard" downcast wrapper (hence the name).
	 * <p>
	 * <b>Caution: This technique is pure sin!</b>
	 * <p>
	 * It is the type-wise complementary to {@link #downwrap(Object, Class)} and a generic decorator version of
	 * explicit downwrapping implementations like {@link DownwrapList}. It's documentation applies to the mechanics
	 * of this method as well: it has to be seen as a workaround tool for special situations (e.g. compatibility to a
	 * foreign codebase API). Relying on it by design is nothing but bad and broken.
	 * <p>
	 * If this intentionally scarce documentation was not enough, do not use this method!
	 *
	 * @param <T>
	 * @param <S>
	 * @param subject the subject to be downwrapped to the given downwrap type.
	 * @param downwrapType the interface sub type the passed subject shall be downwrapped to.
	 * @return a reflection wise downwrapped instance of type {@literal S} of the passed subject.
	 */
	@SuppressWarnings("unchecked")
	public static <T, S extends T> S downwrap(final T subject, final Class<S> downwrapType)
	{
		if(!downwrapType.isInterface())
		{
			throw new IllegalArgumentException("downwrap type is not an interface");
		}
		return (S)java.lang.reflect.Proxy.newProxyInstance(
			subject.getClass().getClassLoader(),
			new Class<?>[]{downwrapType},
			new AspectWrapper<>(subject)
		);
	}

	/**
	 * Reduces the type of the passed instance to a super type interface. It can best be seen as a reflective-wise
	 * "hard" upcast wrapper (hence the name).
	 * <p>
	 * This is effectively a generic decorater implementation realized via dynamic proxy instantiation.
	 * <p>
	 * A very good example is a read-only access on a mutable collection instance:<br>
	 * The type {@link XList} extends the type {@link XGettingList} (and combines it with other aspects like
	 * adding, removing, etc. to create a full scale general purpose list type).<br>
	 * In certain situations, it is necessary that certain code (e.g. an external framework) can only read
	 * but never modify the collection's content. Just casting the {@link XList} instance won't suffice here,
	 * as the receiving code could still do an {@code instanceof } check and downcast the passed instance.<br>
	 * What is really needed is an actual decorator instance, wrapping the general purpose type instance and
	 * relaying only the reading procedures.<br>
	 * For this particular example, there's an explicit decorator type, {@link ListView}.<br>
	 * For other situations, where there is no explicit decorator type (or not yet), this method provides
	 * a solution to create a generic decorator instance.
	 * <p>
	 * Note that the genericity comes at the price of performance, as it purely consists of reflection calls.
	 *
	 * @param <T>
	 * @param <S>
	 * @param subject the subject to be upwrapped to the given upwrap type.
	 * @param upwrapType the interface super type the passed subject shall be upwrapped to.
	 * @return a reflection wise upwrapped instance of type {@literal T} of the passed subject.
	 * @throws IllegalArgumentException if the passed unwrap type is not an interface.
	 */
	@SuppressWarnings("unchecked")
	public static <T, S extends T> T upwrap(final S subject, final Class<T> upwrapType)
	{
		if(!upwrapType.isInterface())
		{
			throw new IllegalArgumentException("upwrap type is not an interface");
		}
		return (T)java.lang.reflect.Proxy.newProxyInstance(
			subject.getClass().getClassLoader(),
			new Class<?>[]{upwrapType},
			new AspectWrapper<T>(subject)
		);
	}
}
