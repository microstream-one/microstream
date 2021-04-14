package one.microstream.collections.types;

import java.util.function.BiConsumer;

public interface XJoinable<E>
{
	public <A> A join(BiConsumer<? super E, ? super A> joiner, A aggregate);
}
