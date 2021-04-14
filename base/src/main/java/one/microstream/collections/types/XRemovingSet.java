package one.microstream.collections.types;

public interface XRemovingSet<E> extends XRemovingCollection<E>
{
	public interface Factory<E> extends XRemovingCollection.Factory<E>
	{
		@Override
		public XRemovingCollection<E> newInstance();
	}

}
