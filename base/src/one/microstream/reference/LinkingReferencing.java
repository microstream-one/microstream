/**
 *
 */
package one.microstream.reference;

import java.util.Iterator;

import one.microstream.collections.Constant;


/**
 * @author Thomas Muenz
 *
 */
public interface LinkingReferencing<T> extends Referencing<T>, Iterable<T>
{
	public boolean hasNext();
	public boolean isNext(LinkingReferencing<T> linkedReference);
	public LinkingReferencing<T> next();

	public Object[] toArray();


	/**
	 * @return
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString();


	public String toChainString();


	public class Implementation<T> extends Constant<T> implements LinkingReferencing<T>
	{

		private final LinkingReferencing<T> next = null;


		/**
		 * @param ref
		 */
		public Implementation(final T ref)
		{
			super(ref);
		}


		/**
		 * @return
		 */
		@Override
		public LinkingReferencing<T> next()
		{
			return this.next;
		}

		/**
		 * @return
		 */
		@Override
		public boolean hasNext()
		{
			return this.next != null;
		}

		/**
		 * @param linkedReference
		 * @return
		 */
		@Override
		public boolean isNext(final LinkingReferencing<T> linkedReference)
		{
			return this.next == linkedReference;
		}

		/**
		 * @return
		 */
		@Override
		public Object[] toArray()
		{
			LinkingReferencing<T> loopNext = this;
			int i = 1;
			while((loopNext = loopNext.next()) != null)
			{
				i++; //this is presumable faster than using an ArrayList or LinkedList for collection
			}

			final Object[] array = new Object[i];
			loopNext = this;
			i = 0;
			do
			{
				array[i++] = loopNext.get();
			}
			while((loopNext = loopNext.next()) != null);

			return array;
		}


		/**
		 * @return
		 */
		@Override
		public Iterator<T> iterator()
		{
			return new ChainIterator<>(this);
		}

		/**
		 * @return
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			final String e = String.valueOf(this.get());
			final StringBuilder sb = new StringBuilder(e.length() + 3);
			return sb.append('(').append(e).append(')').append(this.hasNext() ? '-' : 'x').toString();
		}


		/**
		 * @return
		 * @see one.microstream.reference.LinkingReferencing#toChainString()
		 */
		@Override
		public String toChainString()
		{
			final StringBuilder sb = new StringBuilder(1024);
			sb.append('(').append(this.get()).append(')');
			for(LinkingReferencing<T> r = this.next; r != null; r = r.next())
			{
				sb.append('-').append('(').append(r.get()).append(')');
			}
			return sb.toString();
		}


	}


	final class ChainIterator<T> implements Iterator<T>
	{
		private LinkingReferencing<T> current;



		ChainIterator(final LinkingReferencing<T> current)
		{
			super();
			this.current = current;
		}

		@Override
		public boolean hasNext()
		{
			return this.current.next() != null;
		}

		@Override
		public T next()
		{
			final LinkingReferencing<T> currentCurrent = this.current;
			this.current = currentCurrent.next();
			return currentCurrent.get();
		}

		@Override
		public void remove() throws UnsupportedOperationException
		{
			throw new UnsupportedOperationException(
				"Can't remove current element in a one directional chain"
			);
		}
	}
}
