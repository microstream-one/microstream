package one.microstream.collections.types;

import java.util.function.Consumer;

import one.microstream.typing.KeyValue;

public interface XGetting2DMap<K1, K2, V> extends XIterable<KeyValue<K1, ? extends XGettingMap<K2, V>>>
{
	public XGettingMap<K1, ? extends XGettingMap<K2, V>> get();
	
	public XGettingMap<K2, V> get(K1 key1);
	
	public V get(K1 key1, K2 key2);
	
	public <PK1 extends Consumer<? super K1>> PK1 iterateKeys1(PK1 procedure);
	
	public <PK2 extends Consumer<? super K2>> PK2 iterateKeys2(PK2 procedure);
	
	public <PV extends Consumer<? super V>> PV iterateValues(PV procedure);
	
	public <PIE extends Consumer<? super KeyValue<K2, V>>> PIE iterateInnerEntries(PIE procedure);
		
}
