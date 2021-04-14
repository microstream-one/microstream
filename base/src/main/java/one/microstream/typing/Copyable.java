package one.microstream.typing;

/**
 * Copyable objects can create copies of themselves that will have the same type and behave exacely as they do.
 * <p>
 * This does not neccessarily mean that all data is copied. E.g. caching fields or ones that are set lazy on
 * demand could be left out in the copy process.
 *
 * 
 */
public interface Copyable
{
	public Copyable copy();

	public final class Static
	{
		/**
		 * Returns either {@code null} if the passed instance is {@code null}, otherwise returns the instance created by
		 * the call to {@link Copyable#copy()}.
		 *
		 * @param <T> The type of the {@link Copyable} instance.
		 * @param copyable the instance whose {@link Copyable#copy()} method shall be called to create the copy.
		 * @return the copy created by the call to the {@link Copyable#copy()} method from the passed instance.
		 */
		@SuppressWarnings("unchecked")
		public static <T extends Copyable> T copy(final T copyable)
		{
			return copyable == null ? null : (T)copyable.copy(); // cast must be valid as defined by contract.
		}

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		/**
		 * Dummy constructor to prevent instantiation of this static-only utility class.
		 * 
		 * @throws UnsupportedOperationException
		 */
		private Static()
		{
			// static only
			throw new UnsupportedOperationException();
		}
		
	}
	
}
