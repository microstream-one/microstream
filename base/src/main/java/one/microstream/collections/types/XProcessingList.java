
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

	@Override
	public XProcessingList<E> toReversed();

}
