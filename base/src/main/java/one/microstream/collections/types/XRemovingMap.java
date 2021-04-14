package one.microstream.collections.types;

import one.microstream.collections.interfaces.ConsolidatableCollection;
import one.microstream.collections.interfaces.ExtendedMap;
import one.microstream.collections.interfaces.OptimizableCollection;
import one.microstream.collections.interfaces.Truncateable;

public interface XRemovingMap<K, V>
extends Truncateable, OptimizableCollection, ConsolidatableCollection, ExtendedMap<K, V>
{
	public interface Factory<K, V>
	{
		public XRemovingMap<K, V> newInstance();
	}

}
