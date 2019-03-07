package one.microstream.collections.types;

public interface XRemovingList<E> extends XRemovingSequence<E>, XRemovingBag<E>
{
	public interface Factory<E> extends XRemovingSequence.Factory<E>, XRemovingBag.Factory<E>
	{
		@Override
		public XProcessingList<E> newInstance();
	}

}
