
package one.microstream.collections.types;





/**
 * 
 *
 */
public interface XProcessingList<E> extends XRemovingList<E>, XGettingList<E>, XProcessingSequence<E>, XProcessingBag<E>
{
	public interface Factory<E>
	extends
	XRemovingList.Factory<E>,
	XGettingList.Factory<E>,
	XProcessingSequence.Factory<E>,
	XProcessingBag.Factory<E>
	{
		@Override
		public XProcessingList<E> newInstance();
	}



	@Override
	public XProcessingList<E> copy();

	/**
	 * Creates a new {@link XProcessingList} with the reversed order of elements.
	 * <p>
	 * 	This method creates a new collection and does <b>not</b> change the
	 * 	existing collection.
	 */
	@Override
	public XProcessingList<E> toReversed();

}
