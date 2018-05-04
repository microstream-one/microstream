package net.jadoth.swizzling.types;

import java.util.function.Consumer;

import net.jadoth.typing.KeyValue;

public interface SwizzleTypeIterable
{
	public void iterateTypes(Consumer<KeyValue<Long, Class<?>>> iterator);
}
