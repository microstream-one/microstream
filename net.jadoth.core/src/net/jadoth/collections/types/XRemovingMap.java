package net.jadoth.collections.types;

import net.jadoth.collections.interfaces.ConsolidatableCollection;
import net.jadoth.collections.interfaces.ExtendedMap;
import net.jadoth.collections.interfaces.OptimizableCollection;
import net.jadoth.collections.interfaces.Truncateable;

public interface XRemovingMap<K, V>
extends Truncateable, OptimizableCollection, ConsolidatableCollection, ExtendedMap<K, V>
{
	public interface Factory<K, V>
	{
		public XRemovingMap<K, V> newInstance();
	}

}
