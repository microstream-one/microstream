package one.microstream.collections.types;

public interface XRemovingSequence<E> extends XRemovingCollection<E>
{
	public XRemovingSequence<E> removeRange(long offset, long length);

	/**
	 * Removing all elements but the ones from the offset (basically start index)
	 * to the offset+length (end index).
	 * 
	 * @param offset is the index of the first element to retain
	 * @param length is the amount of elements to retain
	 * @return this
	 */
	public XRemovingSequence<E> retainRange(long offset, long length);

	public long removeSelection(long[] indices);


	public interface Factory<E> extends XRemovingCollection.Factory<E>
	{
		@Override
		public XRemovingSequence<E> newInstance();
	}

}
