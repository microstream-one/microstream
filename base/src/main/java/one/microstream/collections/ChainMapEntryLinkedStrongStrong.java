package one.microstream.collections;

import one.microstream.X;
import one.microstream.typing.KeyValue;

final class ChainMapEntryLinkedStrongStrong<K, V>
extends AbstractChainEntryLinkedKV<K, V, ChainMapEntryLinkedStrongStrong<K, V>>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	@SuppressWarnings("unchecked") // compensate java generics type system loophole
	static final <K, V> ChainMapEntryLinkedStrongStrong<K, V>[] array(final int length)
	{
		return new ChainMapEntryLinkedStrongStrong[length];
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	K key  ;
	V value;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	protected ChainMapEntryLinkedStrongStrong(
		final K                                     key  ,
		final V                                     value,
		final ChainMapEntryLinkedStrongStrong<K, V> link
	)
	{
		super(link);
		this.key   = key  ;
		this.value = value;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////
	
	@Override
	protected final void setElement0(final KeyValue<K, V> element)
	{
		this.key = element.key();
		this.value = element.value();
	}

	@Override
	protected final boolean hasNullElement()
	{
		return false; // element is this entry itself which obviously cannot be null.
	}

	@Override
	protected final KeyValue<K, V> element()
	{
		return this; // sneaky :-D
	}
	
	@Override
	protected final void set0(final K key, final V value)
	{
		this.key   = key  ;
		this.value = value;
	}

	@Override
	protected final boolean hasNullValue()
	{
		return this.value == null;
	}

	@Override
	protected final boolean sameKV(final KeyValue<K, V> other)
	{
		return other.key() == this.key && other.value() == this.value;
	}

	@Override
	public final K key()
	{
		return this.key;
	}

	@Override
	protected final K setKey(final K key)
	{
		final K oldkey = this.key;
		this.key = key;
		return oldkey;
	}
	
	@Override
	protected final void setKey0(final K key)
	{
		this.key = key;
	}

	@Override
	protected final boolean hasNullKey()
	{
		return this.key == null;
	}

	@Override
	public final V value()
	{
		return this.value;
	}

	@Override
	public final V setValue(final V value)
	{
		final V oldValue = this.value;
		this.value = value;
		return oldValue;
	}
	
	@Override
	public final void setValue0(final V value)
	{
		this.value = value;
	}
	
	@Deprecated
	@Override
	public final K getKey()
	{
		return this.key;
	}

	@Deprecated
	@Override
	public final V getValue()
	{
		return this.value;
	}
	
	@Override
	protected final KeyValue<K, V> setElement(final KeyValue<K, V> element)
	{
		final KeyValue<K, V> old = X.KeyValue(this.key, this.value);
		this.key   = element.key()  ;
		this.value = element.value();
		return old;
	}
	
}
