package one.microstream.experimental;
import java.util.Collection;
import java.util.function.Consumer;

/**
 *
 */

/**
 * @author Thomas Muenz
 *
 */
public final class ExperimentalArrayVarList<E>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	private static final int MINIMAL_INDEX_SIZE = 2;
	private static final int MINIMAL_ENTRY_SIZE = 8;
	private static final int DEFAULT_INDEX_SIZE = 8;
	private static final int DEFAULT_ENTRY_SIZE = 8;

//	private static final Object REMOVE_MARKER = new Object();



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private E[][] slots;
	private int[] index;

	private int indexSize;
	private final int entrySize;

	private E[] lastEntry;
	private int nextSlotIndex;
	private int nextEntryIndex;
	private int size;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 *
	 */
	@SuppressWarnings("unchecked")
	public ExperimentalArrayVarList()
	{
		super();
		this.slots = (E[][])new Object[DEFAULT_INDEX_SIZE][];
		this.slots[0] = (E[])new Object[DEFAULT_ENTRY_SIZE];
		this.index = new int[DEFAULT_INDEX_SIZE];
		this.indexSize = DEFAULT_INDEX_SIZE;
		this.entrySize = DEFAULT_ENTRY_SIZE;
		this.lastEntry = this.slots[0];
		this.nextSlotIndex = 1;
		this.nextEntryIndex = 0;
		this.size = 0;
	}

	@SuppressWarnings("unchecked")
	public ExperimentalArrayVarList(final Collection<? extends E> elements)
	{
		super();
		final Object[] array = elements.toArray();
		this.slots = (E[][])new Object[DEFAULT_INDEX_SIZE][];
		this.slots[0] = (E[])array;
		this.slots[1] = (E[])new Object[DEFAULT_ENTRY_SIZE];
		this.index = new int[DEFAULT_INDEX_SIZE];
		this.index[1] = array.length;
		this.indexSize = DEFAULT_INDEX_SIZE;
		this.entrySize = DEFAULT_ENTRY_SIZE;
		this.lastEntry = this.slots[1];
		this.nextSlotIndex = 2; // 2 is no magic number here, but comes from "1++"
		this.nextEntryIndex = 0;
		this.size = array.length;
	}

	@SuppressWarnings("unchecked")
	public ExperimentalArrayVarList(final E... elements)
	{
		super();
		this.slots = (E[][])new Object[DEFAULT_INDEX_SIZE][];
		this.slots[0] = elements;
		this.slots[1] = (E[])new Object[DEFAULT_ENTRY_SIZE];
		this.index = new int[DEFAULT_INDEX_SIZE];
		this.index[1] = elements.length;
		this.indexSize = DEFAULT_INDEX_SIZE;
		this.entrySize = DEFAULT_ENTRY_SIZE;
		this.lastEntry = this.slots[1];
		this.nextSlotIndex = 2; // 2 is no magic number here, but comes from "1++"
		this.nextEntryIndex = 0;
		this.size = elements.length;
	}

	@SuppressWarnings("unchecked")
	public ExperimentalArrayVarList(int initialIndexSize, int defaultEntrySize)
	{
		super();
		if(initialIndexSize < MINIMAL_INDEX_SIZE)
		{
			initialIndexSize = MINIMAL_INDEX_SIZE;
		}
		if(defaultEntrySize < MINIMAL_ENTRY_SIZE)
		{
			defaultEntrySize = MINIMAL_ENTRY_SIZE;
		}
		this.slots = (E[][])new Object[initialIndexSize][];
		this.slots[0] = (E[])new Object[defaultEntrySize];
		this.index = new int[initialIndexSize];
		this.indexSize = initialIndexSize;
		this.entrySize = defaultEntrySize;
		this.lastEntry = this.slots[0];
		this.nextSlotIndex = 1;
		this.nextEntryIndex = 0;
		this.size = 0;
	}

	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

//	@SuppressWarnings("unchecked")
//	private E removeMarker()
//	{
//		return (E)REMOVE_MARKER;
//	}

	@SuppressWarnings("unchecked")
	private E[] newArray()
	{
		return (E[])new Object[this.entrySize];
	}

//	private E get(final int slotIndex,, final int entryIndex,  final int baseIndex)
//	{
//		return this.slots[slotIndex][entryIndex];
//	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	public void execute(final Consumer<E> procedure)
	{
		final E[][] slots = this.slots;

		E[] array;
		for(int i = 0, size = this.nextSlotIndex-1; i < size; i++)
		{
			array = slots[i];
			for(final E e : array)
			{
				procedure.accept(e);
			}
		}
		array = this.lastEntry;
		for(int i = 0, len = this.nextEntryIndex; i < len; i++)
		{
			procedure.accept(array[i]);
		}
	}

	@SuppressWarnings("unchecked")
	private void increaseSlots()
	{
		final E[][] slots = this.slots;
		final int slotCount = this.indexSize;
		final E[][] newSlots = (E[][])new Object[slotCount<<1][];
		final int[] newIndex = new int[slotCount<<1];

		System.arraycopy(slots, 0, newSlots, 0, slotCount);
		System.arraycopy(this.index, 0, newIndex, 0, slotCount);

		this.indexSize = slotCount<<1;
		this.slots = newSlots;
		this.index = newIndex;
	}


	public E get(final int index)
	{
		if(index >= this.size)
		{
			throw new IndexOutOfBoundsException();
		}

		final int len = this.nextSlotIndex-1;
		if(len == 0)
		{
			return this.lastEntry[index]; // special case for single-entry list
		}

		final int[] idx = this.index;
		int i = 0;
		for(; i < len; i++)
		{
			if(idx[i] > index)
			{
				return this.slots[i-1][index - idx[i-1]];
			}
		}
		return this.lastEntry[index - idx[i]];


		// version without special case handling
//		if(index >= this.size)
//		{
//			throw new IndexOutOfBoundsException();
//		}
//		final int[] idx = this.index;
//		int i = 0;
//		for(int len = this.nextSlotIndex-1; i < len; i++)
//		{
//			if(idx[i] > index)
//			{
//				return this.slots[i-1][index - idx[i-1]];
//			}
//		}
//		return this.lastEntry[index - idx[i]];
	}





	public boolean add(final E element)
	{
		if(this.nextEntryIndex < this.lastEntry.length)
		{
			this.lastEntry[this.nextEntryIndex++] = element;
			this.size++;
			return true;
		}

		if(this.nextSlotIndex == this.indexSize)
		{
			this.increaseSlots();
		}
		this.lastEntry = this.newArray();
		this.index[this.nextSlotIndex] = this.size++;
		this.slots[this.nextSlotIndex++] = this.lastEntry;
		this.lastEntry[0] = element;
		this.nextEntryIndex = 1;
		return true;
	}



	@SuppressWarnings("unchecked")
	public boolean addAll(final Collection<? extends E> elements)
	{
		// create new entry array from collection
		E[] newEntry = (E[])elements.toArray();

		// finalize current lastEntry
		if(this.nextEntryIndex != this.lastEntry.length)
		{
			final E[] array = (E[])new Object[this.nextEntryIndex];
			System.arraycopy(this.lastEntry, 0, array, 0, this.nextEntryIndex);
			this.slots[this.nextSlotIndex-1] = array;
		}

		// ensure slots capacity
		if(this.nextSlotIndex == this.indexSize)
		{
			this.increaseSlots();
		}

		//
		this.index[this.nextSlotIndex] = this.size;
		this.slots[this.nextSlotIndex++] = newEntry;
		this.size += newEntry.length;

		newEntry = this.newArray();
		this.index[this.nextSlotIndex] = this.size;
		this.slots[this.nextSlotIndex++] = newEntry;
		this.lastEntry = newEntry;
		this.nextEntryIndex = 0;

		return true;
	}


}
