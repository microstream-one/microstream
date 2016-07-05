package net.jadoth.collections;

import net.jadoth.util.KeyValue;
import net.jadoth.util.chars.VarString;


public abstract class AbstractChainEntryLinkedKV<K, V, EN extends AbstractChainEntryLinkedKV<K, V, EN>>
extends AbstractChainEntryLinked<KeyValue<K, V>, K, V, EN>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	protected AbstractChainEntryLinkedKV(final EN link)
	{
		super(link);
	}

	@Override
	public String toString()
	{
		// only for debug
		return VarString.New().append('(').add(this.key()).append('=').add(this.value()).append(')').toString();
	}

}
