package net.jadoth.collections;

import net.jadoth.functional.BiProcedure;

public interface XJoinable<E>
{
	public <A> A join(BiProcedure<? super E, ? super A> joiner, A aggregate);
}
