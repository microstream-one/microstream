package one.microstream.collections.types;

public interface XRemovingEnum<E> extends XRemovingSet<E>, XRemovingSequence<E>
{
	public interface Factory<E> extends XRemovingSet.Factory<E>, XRemovingSequence.Factory<E>
	{
		@Override
		public XRemovingEnum<E> newInstance();
	}

}
