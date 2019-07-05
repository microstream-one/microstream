package one.microstream.experimental;

import java.util.NoSuchElementException;

import one.microstream.chars.VarString;

public final class RingQueue<E>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	static final int MINIMUM_CAPACITY =  4;
	static final int DEFAULT_CAPACITY = 16;
	static final int SHIFTING_MAXIMUM = 1<<30; //the highest possible int value that can be reached by bit shifting



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	////////////////////

	private static int calculateCapacity(final int minimalCapacity)
	{
		if(minimalCapacity > SHIFTING_MAXIMUM)
		{
			return Integer.MAX_VALUE;
		}
		int capacity = MINIMUM_CAPACITY;
		while(capacity < minimalCapacity)
		{
			capacity <<= 1;
		}
		return capacity;
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private Object[] data;
	private int capacity;
	private int lastIndex;
	private int getIndex;
	private int setIndex;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public RingQueue()
	{
		super();
		this.data = new Object[this.capacity = DEFAULT_CAPACITY];
		this.lastIndex = DEFAULT_CAPACITY - 1;
		this.getIndex = 0;
		this.setIndex = 0;
	}

	public RingQueue(final int capacity)
	{
		super();
		this.data = new Object[this.capacity = calculateCapacity(capacity)];
		this.lastIndex = this.capacity - 1;
		this.getIndex = 0;
		this.setIndex = 0;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	public boolean add(final E element)
	{
		if(element == null)
		{
			throw new NullPointerException();
		}

		final int oi = this.setIndex, ni; // oldIndex, newIndex
		if((ni = oi == this.lastIndex ? 0 : oi + 1) != this.getIndex)
		{
			this.data[oi] = element;
			this.setIndex = ni;
			return true;
		}

		// storage has to be increased
		final int len, g;
		if((len = this.data.length) >= Integer.MAX_VALUE)
		{
			throw new IndexOutOfBoundsException("Reached maximum capacity.");
		}
		final Object[] data = new Object[this.capacity *= 2.0f];

		// handling the simpler corner case i == 0 is not worth doing the check every time
		System.arraycopy(this.data, g = this.getIndex, data, g, len - g);
		System.arraycopy(this.data, 0, data, len, oi);
		data[len + oi] = element;
		this.setIndex = len + ni;
		this.data = data;
		this.lastIndex = this.capacity - 1;
		return true;
	}

	public boolean offer(final E element)
	{
		if(element == null)
		{
			throw new NullPointerException();
		}

		final int oi = this.setIndex, ni;
		if((ni = oi == this.lastIndex ? 0 : oi + 1) != this.getIndex)
		{
			this.data[oi] = element;
			this.setIndex = ni;
			return true;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public E poll()
	{
		final int i;
		if((i = this.getIndex) == this.setIndex)
		{
			return null;
		}
		final E e = (E)this.data[i];
		this.data[i] = null;
		this.getIndex = i == this.lastIndex ? 0 : i + 1;
		return e;
	}

	@SuppressWarnings("unchecked")
	public E peek()
	{
		final int i;
		if((i = this.getIndex) == this.setIndex)
		{
			return null;
		}
		return (E)this.data[i];
	}

	@SuppressWarnings("unchecked")
	public E remove()
	{
		final int i;
		if((i = this.getIndex) == this.setIndex) throw new NoSuchElementException();
		final E e = (E)this.data[i];
		this.data[i] = null;
		this.getIndex = i == this.lastIndex ? 0 : i + 1;
		return e;
	}

	@SuppressWarnings("unchecked")
	public E element()
	{
		final int i;
		if((i = this.getIndex) == this.setIndex) throw new NoSuchElementException();
		return (E)this.data[i];
	}

	public int size()
	{
		final int len;
		return (len = this.setIndex - this.getIndex) >= 0 ? len :this.capacity + len + 1;
	}

	public boolean isEmpty()
	{
		return this.getIndex == this.setIndex;
	}

	public void clear()
	{
		final Object[] data = this.data;

		final int g, s;
		if((s = this.setIndex) > (g = this.getIndex))
		{
			// trivial case
			for(int i = g; i <= s; i++)
			{
				data[i] = null;
			}
		}
		else if(s < g){
			// overflow case
			for(int i = g, len = data.length; i < len; i++)
			{
				data[i] = null;
			}
			for(int i = 0; i <= s; i++)
			{
				data[i] = null;
			}
		}

		// data is now completely empty, reset indices to start of array
		this.getIndex = 0;
		this.setIndex = 0;
	}

	public boolean allowsNull()
	{
		return false;
	}

	public int getCapacity()
	{
		return this.capacity;
	}

	public int getMaximumCapacity()
	{
		return Integer.MAX_VALUE;
	}



	///////////////////////////////////////////////////////////////////////////
	// debug //
	//////////

	public int getGetIndex()
	{
		return this.getIndex;
	}

	public int getSetIndex()
	{
		return this.setIndex;
	}

	public void printStorage()
	{
		final VarString vc = VarString.New(this.size()*3);

		final Object[] data = this.data;
		for(int i = 0; i < data.length; i++)
		{
			vc.append('\t').add(data[i]);
		}
		vc.append('\n');

		vc.append('\t');
		if(this.setIndex >= this.getIndex)
		{
			int i = 0;
			while(i++ < this.getIndex)
			{
				vc.append('\t');
			}
			vc.append('g');
			while(i++ < this.setIndex)
			{
				vc.append('\t');
			}
			vc.append('s');
		}
		else
		{
			int i = 0;
			while(i++ < this.setIndex)
			{
				vc.append('\t');
			}
			vc.append('s');
			while(i++ < this.getIndex)
			{
				vc.append('\t');
			}
			vc.append('g');
		}

		System.out.println(vc);
	}

	@Override
	public String toString()
	{
		final VarString vc = VarString.New(this.size()*3);

		final Object[] data = this.data;
		final int last = this.lastIndex;
		for(int i = this.getIndex; i != this.setIndex; i = i == last ? 0 :i+1)
		{
			vc.add(data[i]).append(',');
		}
		vc.deleteLast();

		return vc.toString();
	}

}
