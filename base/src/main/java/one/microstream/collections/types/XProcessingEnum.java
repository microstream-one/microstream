package one.microstream.collections.types;


public interface XProcessingEnum<E> extends XRemovingEnum<E>, XGettingEnum<E>, XProcessingSet<E>, XProcessingSequence<E>
{
	public interface Creator<E>
	extends XGettingEnum.Creator<E>, XProcessingSet.Factory<E>, XProcessingSequence.Factory<E>
	{
		@Override
		public XProcessingEnum<E> newInstance();
	}



	@Override
	public XProcessingEnum<E> copy();

	@Override
	public XProcessingEnum<E> toReversed();

}
