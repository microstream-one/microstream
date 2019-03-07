package one.microstream.collections.types;


public interface XPutGetBag<E> extends XPuttingBag<E>, XGettingBag<E>
{
	public interface Factory<E> extends XPuttingBag.Creator<E>, XGettingBag.Factory<E>
	{
		@Override
		public XPutGetBag<E> newInstance();
	}

}
