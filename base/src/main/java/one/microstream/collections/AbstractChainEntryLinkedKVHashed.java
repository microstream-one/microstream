package one.microstream.collections;


public abstract class AbstractChainEntryLinkedKVHashed<K, V, EN extends AbstractChainEntryLinkedKVHashed<K, V, EN>>
extends AbstractChainEntryLinkedKV<K, V, EN>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	final int hash; // the hash value of the hash-related value contained in this entry



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	protected AbstractChainEntryLinkedKVHashed(final int hash, final EN link)
	{
		super(link);
		this.hash = hash;
	}

}
