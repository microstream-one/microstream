package one.microstream.collections.types;

import java.util.function.Function;

public interface X2DMap<K1, K2, V> extends XGetting2DMap<K1, K2, V>
{
	public boolean add(K1 key1, K2 key2, V value);
	
	public boolean put(K1 key1, K2 key2, V value);
	
	public V ensure(K1 key1, K2 key2, Function<? super K2, V> valueSupplier);
	
}
