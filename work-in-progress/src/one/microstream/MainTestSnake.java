/**
 *
 */
package one.microstream;

import one.microstream.experimental.collections.Snake;

/**
 * @author Thomas Muenz
 *
 */
public class MainTestSnake
{
	public static final <E> Snake<E> snake()
	{
		return new Snake.Default<>();
	}

	public static final <E> Snake<E> snake(final E value)
	{
		return new Snake.Default<>(value);
	}

	@SafeVarargs
	public static final <E> Snake<E> snake(final E... values)
	{
		Snake<E> snake = new Snake.Default<>();
		for(final E e : values)
		{
			snake = snake.add(e);
		}
		return snake;
	}

	public static void main(final String[] args)
	{
		final Snake<Integer> snake = snake(0).add(1).add(2).add(3).add(4).add(5).add(6).add(7).add(8).add(9);

		System.out.println(snake.head());
		System.out.println(snake.tail());
	}

}
