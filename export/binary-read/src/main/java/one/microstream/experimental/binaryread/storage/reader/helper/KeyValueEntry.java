package one.microstream.experimental.binaryread.storage.reader.helper;

import java.util.Map;

public class KeyValueEntry<K, V> implements Map.Entry<K, V>
{
    private final K key;
    private V value;

    public KeyValueEntry(final K key, final V value)
    {
        this.key = key;
        this.value = value;
    }

    @Override
    public K getKey()
    {
        return key;
    }

    @Override
    public V getValue()
    {
        return value;
    }

    @Override
    public V setValue(final V newValue)
    {
        V oldValue = value;
        value = newValue;
        return oldValue;
    }

    @Override
    public String toString()
    {
        return String.format("[%s,%s]", key, value);
    }
}
