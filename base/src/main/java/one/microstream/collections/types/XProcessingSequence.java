package one.microstream.collections.types;



import java.util.function.Consumer;

/**
 * 
 *
 */
public interface XProcessingSequence<E> extends XRemovingSequence<E>, XGettingSequence<E>, XProcessingCollection<E>
{
	public interface Factory<E>
	extends XRemovingSequence.Factory<E>, XGettingSequence.Factory<E>, XProcessingCollection.Factory<E>
	{
		@Override
		public XProcessingSequence<E> newInstance();
	}



	public E removeAt(long index); // remove and retrieve element at index or throw IndexOutOfBoundsException if invalid

//	@Override
//	public E fetch(); // remove and retrieve first or throw IndexOutOfBoundsException if empty (fetch ~= first)
	public E pop();   // remove and retrieve last  or throw IndexOutOfBoundsException if empty (stack conceptional pop)

//	@Override
//	public E pinch(); // remove and retrieve first or null if empty (like forcefull extraction from collection's base)
	public E pick();  // remove and retrieve last  or null if empty (like easy extraction from collection's end)


	@Override
	public XProcessingSequence<E> toReversed();

	public <C extends Consumer<? super E>> C moveSelection(C target, long... indices);

	@Override
	public XGettingSequence<E> view(long fromIndex, long toIndex);

}
