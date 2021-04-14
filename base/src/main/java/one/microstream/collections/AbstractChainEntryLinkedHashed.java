package one.microstream.collections;



/**
 * 
 *
 */
public abstract class AbstractChainEntryLinkedHashed<E, K, V, EN extends AbstractChainEntryLinkedHashed<E, K, V, EN>>
extends AbstractChainEntryLinked<E, K, V, EN>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	final int hash; // the hash value of the hash-related value contained in this entry



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	protected AbstractChainEntryLinkedHashed(final int hash, final EN link)
	{
		super(link);
		this.hash = hash;
	}

}
