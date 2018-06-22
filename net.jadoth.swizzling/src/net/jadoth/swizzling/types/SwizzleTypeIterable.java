package net.jadoth.swizzling.types;

import java.util.function.Consumer;

public interface SwizzleTypeIterable
{
	public void iterateTypes(Consumer<? super SwizzleRegistry.Entry> iterator);
}
