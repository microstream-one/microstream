package net.jadoth.experimental.collections;

import net.jadoth.chars.VarString;

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






	class Implementation<E> implements Snake<E>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		Object value;
		Snake.Implementation<E> next = null;
		Snake.Implementation<E> prev = null;


		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		public Implementation()
		{
			super();
			this.value = null;
		}
		public Implementation(final Object value)
		{
			super();
			this.value = value;
		}

		/**
		 * @return
		 * @see net.jadoth.experimental.collections.Snake#head()
		 */
		@Override
		public Snake<E> head()
		{
			Snake.Implementation<E> link = this;
			while(link.next != null)
			{
				link = link.next;
			}
			return link;
		}
		/**
		 * @param index
		 * @return
		 * @see net.jadoth.experimental.collections.Snake#hop(int)
		 */
		@Override
		public Snake<E> hop(int index)
		{
			Snake.Implementation<E> link = this;
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
		/**
		 * @return
		 * @see net.jadoth.experimental.collections.Snake#tail()
		 */
		@Override
		public Snake<E> tail()
		{
			Snake.Implementation<E> link = this;
			while(link.prev != null)
			{
				link = link.prev;
			}
			return link;
		}
		/**
		 * @return
		 * @see net.jadoth.experimental.collections.Snake#value()
		 */
		@SuppressWarnings("unchecked")
		@Override
		public E value()
		{
			return (E)this.value;
		}
		/**
		 * @return
		 * @see net.jadoth.experimental.collections.Snake#next()
		 */
		@Override
		public Snake<E> next()
		{
			return this.next();
		}
		/**
		 * @return
		 * @see net.jadoth.experimental.collections.Snake#prev()
		 */
		@Override
		public Snake<E> prev()
		{
			return this.prev();
		}
		/**
		 * @param value
		 * @return
		 * @see net.jadoth.experimental.collections.Snake#add(java.lang.Object)
		 */
		@Override
		public Snake<E> add(final E value)
		{
			final Snake.Implementation<E> newLink = new Snake.Implementation<>();
			newLink.value = value;

			newLink.prev = this;
			this.next = newLink;

			return newLink;
		}
		/**
		 * @return
		 * @see net.jadoth.experimental.collections.Snake#hasNext()
		 */
		@Override
		public boolean hasNext()
		{
			return this.next != null;
		}
		/**
		 * @return
		 * @see net.jadoth.experimental.collections.Snake#hasPrev()
		 */
		@Override
		public boolean hasPrev()
		{
			return this.prev != null;
		}
		/**
		 * @param value
		 * @return
		 * @see net.jadoth.experimental.collections.Snake#set(java.lang.Object)
		 */
		@SuppressWarnings("unchecked")
		@Override
		public E set(final E value)
		{
			final E oldValue = (E)this.value;
			this.value = value;
			return oldValue;
		}



		/**
		 * @return
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{

			if(this.next == null)
			{
				//head
				final VarString vc = VarString.New().append('�');
				Snake.Implementation<E> link = this;
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
				Snake.Implementation<E> link = this;
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
