package one.microstream.experimental.collections;

import one.microstream.chars.VarString;

/**
 * Experimental simple collection type
 *
 * @author Thomas Muenz
 *
 */
public interface Snake<E> //extends VarString.Appendable
{
	E value();

	Snake<E> tail();
	Snake<E> head();

	Snake<E> next();
	Snake<E> prev();

	boolean hasNext();
	boolean hasPrev();

	Snake<E> hop(int index);
	Snake<E> add(E value);
	E set(E value);






	class Default<E> implements Snake<E>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		Object           value;
		Snake.Default<E> next ;
		Snake.Default<E> prev ;

		

		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Default()
		{
			super();
			this.value = null;
		}
		
		public Default(final Object value)
		{
			super();
			this.value = value;
		}
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public Snake<E> head()
		{
			Snake.Default<E> link = this;
			while(link.next != null)
			{
				link = link.next;
			}
			return link;
		}

		@Override
		public Snake<E> hop(int index)
		{
			Snake.Default<E> link = this;
			if(index < 0)
			{
				while(index ++> 0)
				{
					link = link.prev;
				}
			}
			else
			{
				while(index --> 0)
				{
					link = link.next;
				}
			}
			return link;
		}

		@Override
		public Snake<E> tail()
		{
			Snake.Default<E> link = this;
			while(link.prev != null)
			{
				link = link.prev;
			}
			return link;
		}

		@SuppressWarnings("unchecked")
		@Override
		public E value()
		{
			return (E)this.value;
		}

		@Override
		public Snake<E> next()
		{
			return this.next();
		}

		@Override
		public Snake<E> prev()
		{
			return this.prev();
		}

		@Override
		public Snake<E> add(final E value)
		{
			final Snake.Default<E> newLink = new Snake.Default<>();
			newLink.value = value;

			newLink.prev = this;
			this.next = newLink;

			return newLink;
		}

		@Override
		public boolean hasNext()
		{
			return this.next != null;
		}

		@Override
		public boolean hasPrev()
		{
			return this.prev != null;
		}

		@SuppressWarnings("unchecked")
		@Override
		public E set(final E value)
		{
			final E oldValue = (E)this.value;
			this.value = value;
			return oldValue;
		}

		@Override
		public String toString()
		{

			if(this.next == null)
			{
				//head
				final VarString vc = VarString.New().append('�');
				Snake.Default<E> link = this;
				do {
					vc.append('(').add(link.value).append(')');
				}
				while((link = link.prev) != null);
				return vc.append('>').toString();
			}
			if(this.prev == null)
			{
				//tail
				final VarString vc = VarString.New().append('<');
				Snake.Default<E> link = this;
				do {
					vc.append('(').add(link.value).append(')');
				}
				while((link = link.next) != null);
				return vc.append('�').toString();
			}
			return String.valueOf(this.value);
		}
		
	}

}
