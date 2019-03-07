package one.microstream.functional;

public final class KamikazeArrayAdder<E> implements Aggregator<E, Integer> // +1 for creative class name :D
{
	private final Object[] array;
	private int index;

	public KamikazeArrayAdder(final Object[] array, final int index)
	{
		super();
		this.array = array;
		this.index = index;
	}

	@Override
	public final void accept(final E element)
	{
		this.array[this.index++] = element;
	}

	@Override
	public final Integer yield()
	{
		return this.index;
	}

}
