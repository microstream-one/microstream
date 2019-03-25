package one.microstream.collections;

import one.microstream.chars.VarString;
import one.microstream.typing.KeyValue;


public abstract class AbstractChainEntryLinkedKV<K, V, EN extends AbstractChainEntryLinkedKV<K, V, EN>>
extends AbstractChainEntryLinked<KeyValue<K, V>, K, V, EN>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

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
