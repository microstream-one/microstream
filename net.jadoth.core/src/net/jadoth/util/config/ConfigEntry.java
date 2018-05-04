package net.jadoth.util.config;


/* CHECKSTYLE.OFF: AbstractClassName: Should be named ~Abstract, but is used at such a lot of places
 *                 that renaming it would significantly harm readability
 */
public interface ConfigEntry<T>
{
	public String key();

	public T parse(String value);


	public abstract class AbstractImplementation<T> implements ConfigEntry<T>
	{
		final String key;

		protected AbstractImplementation(final String key)
		{
			super();
			this.key = key;
		}

		@Override
		public final String key()
		{
			return this.key;
		}

		@Override
		public abstract T parse(String value);
	}

}
