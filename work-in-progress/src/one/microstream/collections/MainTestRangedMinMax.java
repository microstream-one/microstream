package one.microstream.collections;

import static one.microstream.X.notNull;
import static one.microstream.math.XMath.notNegative;

import java.util.Comparator;

import one.microstream.X;
import one.microstream.chars.VarString;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.functional.Aggregator;
import one.microstream.typing.KeyValue;

public class MainTestRangedMinMax
{
	public static void main(final String[] args)
	{
		final EqHashTable<Integer, String> testTable = EqHashTable.New(
			X.KeyValue(1, "class" ),
			X.KeyValue(2, "am"    ),
			X.KeyValue(3, ""      ),
			X.KeyValue(4, "test"  ),
			X.KeyValue(5, ""      ),
			X.KeyValue(6, "I"     ),
			X.KeyValue(7, "result"),
			X.KeyValue(8, "the"   )
		);
		print("Initial elements:", testTable);
//		print("\nOrdered:", testTable.copy().values().sort(XSort::compareLength).parent());
		
		newLine();
		print("Testing Maximum ...");
		test("maxStringLengthEntry", "result", testTable, 0, 9, MainTestRangedMinMax::compareValueLength);
		test("maxStringLengthEntry", "the"   , testTable, 7, 9, MainTestRangedMinMax::compareValueLength);
		test("maxStringLengthEntry", "class" , testTable, 0, 6, MainTestRangedMinMax::compareValueLength);
		test("maxStringLengthEntry", "test"  , testTable, 1, 5, MainTestRangedMinMax::compareValueLength);
		
		newLine();
		print("Testing Minimum ...");
		test("maxStringLengthEntry", ""   , testTable, 0, 9, XSort.reverse(MainTestRangedMinMax::compareValueLength));
		test("maxStringLengthEntry", "am" , testTable, 0, 2, XSort.reverse(MainTestRangedMinMax::compareValueLength));
		test("maxStringLengthEntry", "I"  , testTable, 5, 9, XSort.reverse(MainTestRangedMinMax::compareValueLength));
		test("maxStringLengthEntry", "the", testTable, 6, 9, XSort.reverse(MainTestRangedMinMax::compareValueLength));
		
		// also see:
		testTable.iterate(Max.New(             MainTestRangedMinMax::compareValueLength)).yield();
		testTable.iterate(Max.InRange(      4, MainTestRangedMinMax::compareValueLength)).yield();
		testTable.iterate(Max.InRange(   2, 4, MainTestRangedMinMax::compareValueLength)).yield();
		testTable.iterate(Max.FromOffset(2   , MainTestRangedMinMax::compareValueLength)).yield();
		
		// also also see:
		Max.searchIn(          testTable,       MainTestRangedMinMax::compareValueLength);
		Max.searchInRangeOf(   testTable,    4, MainTestRangedMinMax::compareValueLength);
		Max.searchInRangeOf(   testTable, 2, 4, MainTestRangedMinMax::compareValueLength);
		Max.searchFromOffsetIn(testTable, 2,    MainTestRangedMinMax::compareValueLength);
	}
	
	static int compareValueLength(final KeyValue<Integer, String> kv1, final KeyValue<Integer, String> kv2)
	{
		return XSort.compareLength(
			kv1 == null ? null : kv1.value(),
			kv2 == null ? null : kv2.value()
		);
	}
	
	static <E> void test(
		final String                label   ,
		final String                expected,
		final XGettingCollection<E> elements,
		final long                  offset  ,
		final long                  length  ,
		final Comparator<? super E> order
	)
	{
		final E result = elements.iterate(
			Max.InRange(offset, length, order)
		).yield();
		print(label + " ("
			+ "offset = " + offset
			+ ", length = " + length
			+ ", expected = " + VarString.New().padRight('"' + expected + '"', 8, ' ')
			+ "): ",
			result
		);
	}
	
	static void newLine()
	{
		System.out.println();
	}
	
	static void print(final String label)
	{
		// well, lol
		System.out.println(label);
	}
		
	static void print(final String label, final Iterable<KeyValue<Integer, String>> elements)
	{
		if(label != null)
		{
			print(label);
		}
		elements.forEach(System.out::println);
	}
	
	static void print(final String label, final Object object)
	{
		if(label != null)
		{
			System.out.print(label);
		}
		System.out.println(object);
	}
	
}


final class Max<E> implements Aggregator<E, E>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static final <E> E searchIn(
		final XGettingCollection<? extends E> elements,
		final Comparator<? super E>           order
	)
	{
		return elements.iterate(New(order)).yield();
	}

	public static final <E> E searchFromOffsetIn(
		final XGettingCollection<? extends E> elements,
		final long                            offset  ,
		final Comparator<? super E>           order
	)
	{
		return elements.iterate(FromOffset(offset, order)).yield();
	}

	public static final <E> E searchInRangeOf(
		final XGettingCollection<? extends E> elements,
		final long                            length,
		final Comparator<? super E>           order
	)
	{
		return elements.iterate(InRange(length, order)).yield();
	}
	
	static <E> E searchInRangeOf(
		final XGettingCollection<? extends E> elements,
		final long offset,
		final long length,
		final Comparator<? super E> order)
	{
		return elements.iterate(InRange(offset, length, order)).yield();
	}
	
	public static final <E> Max<E> New(final Comparator<? super E> order)
	{
		return FromOffset(0, order);
	}

	public static final <E> Max<E> FromOffset(final long offset, final Comparator<? super E> order)
	{
		return InRange(offset, Long.MAX_VALUE, order);
	}

	public static final <E> Max<E> InRange(final long length, final Comparator<? super E> order)
	{
		return InRange(0, length, order);
	}
	
	static <E> Max<E> InRange(final long offset, final long length, final Comparator<? super E> order)
	{
		return new Max<>(
			notNegative(offset),
			notNegative(length),
			notNull(order)
		);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final long offset, length;
	
	private final Comparator<? super E> order;
	
	private long iterationOffset, iterationLength;
	
	private E iterationElement;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	Max(final long offset, final long length, final Comparator<? super E> order)
	{
		super();
		this.offset = offset;
		this.length = length;
		this.order  = order ;
		
		this.reset();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final Max<E> reset()
	{
		this.iterationElement = null;
		this.iterationOffset  = this.offset;
		this.iterationLength  = this.length;
		
		return this;
	}

	@Override
	public final void accept(final E element)
	{
		if(this.iterationOffset > 0)
		{
			this.iterationOffset--;
			return;
		}
		
		if(this.iterationLength <= 0)
		{
			throw X.BREAK();
		}
		
		/*
		 * Must initialize iterationElement to the first element since yielding a "null" result
		 * in a non-empty collection without contained nulls would be a bug.
		 */
		if(this.iterationLength-- == this.length)
		{
			this.iterationElement = element;
			return;
		}
				
		if(this.order.compare(element, this.iterationElement) > 0)
		{
			this.iterationElement = element;
		}
	}
	
	@Override
	public final E yield()
	{
		return this.iterationElement;
	}
	
}
