package one.microstream.collections.interfaces;

public interface Shuffleable<E>
{
	/**
	 * Randomizes the elements in this {@link Shuffleable}.
	 * @return this
	 */
	public Shuffleable<E> shuffle();
}
