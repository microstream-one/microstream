package one.microstream.collections.types;

public interface XRemovingSequence<E> extends XRemovingCollection<E>
{
	public XRemovingSequence<E> removeRange(long offset, long length);

	public XRemovingSequence<E> retainRange(long offset, long length);

	public long removeSelection(long[] indices);


	public interface Factory<E> extends XRemovingCollection.Factory<E>
	{
		@Override
		public XRemovingSequence<E> newInstance();
	}

}
