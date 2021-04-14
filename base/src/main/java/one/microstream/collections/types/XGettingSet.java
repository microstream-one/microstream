package one.microstream.collections.types;

import java.util.function.Consumer;


/**
 * 
 *
 */
public interface XGettingSet<E> extends XGettingCollection<E>
{
	public interface Creator<E> extends XGettingCollection.Creator<E>
	{
		@Override
		public XGettingSet<E> newInstance();
	}



	@Override
	public XImmutableSet<E> immure();

	@Override
	public XGettingSet<E> copy();

	@Override
	public <P extends Consumer<? super E>> P iterate(P procedure);

}
