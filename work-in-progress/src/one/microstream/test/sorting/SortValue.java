package one.microstream.test.sorting;

import java.util.Comparator;

import one.microstream.chars.VarString;
import one.microstream.math.FastRandom;

public final class SortValue
{
	public static final Comparator<SortValue> compare = (final SortValue o1, final SortValue o2) ->
	{
			return unstableCompare(o1, o2);
	};

	public static final Comparator<SortValue> stableCompare = (final SortValue o1, final SortValue o2) ->
	{
			return stableCompare(o1, o2);
	};

	private static final int DEFAULT_PRINT_LENGTH = 100;
	private static final FastRandom RND = new FastRandom();
	private static final VarString  VS  = VarString.New(1000*1000*10);



	public static SortValue[] createArray(final int length)
	{
		final SortValue[] sortValues = new SortValue[length];
		for(int i = 0; i < length; i++)
		{
			sortValues[i] = new SortValue(0, 0);
		}
		return sortValues;
	}


	public static SortValue[] init(final int... values)
	{
		final SortValue[] sortValues = new SortValue[values.length];
		for(int i = 0; i < values.length;)
		{
			sortValues[i] = new SortValue(values[i], ++i);
		}
		return sortValues;
	}

	public static SortValue[] alreadySorted(final SortValue[] values)
	{
		for(int i = 0; i < values.length;)
		{
			values[i++].set(i, i);
		}
		return values;
	}
	public static SortValue[] reverseSorted(final SortValue[] values)
	{
		for(int i = 0; i < values.length;)
		{
			values[i].set(values.length - i, ++i);
		}
		return values;
	}

	public static SortValue[] randomUniques(final SortValue[] values)
	{
		alreadySorted(values);
		for(int i = 0; i < values.length; i++)
			swap(values, i, RND.nextInt(values.length));
		return values;
	}
	public static SortValue[] random(final SortValue[] values)
	{
		for(int i = 0; i < values.length;)
		{
			values[i++].set(RND.nextInt(values.length), i);
		}
		return values;
	}
	public static SortValue[] fewUnique(final SortValue[] values, final int distinctAmount)
	{
		for(int i = 0; i < values.length;)
		{
			values[i].set(1+RND.nextInt(distinctAmount), ++i);
		}
		return values;
	}
	public static SortValue[] fewUniqueBulk(final SortValue[] values, final int distinctAmount)
	{
		final int distinctSegment = values.length/distinctAmount;
		int value = 0;
		for(int i = 0, d = 0; i < values.length;)
		{
			if(d == 0) value = 1+RND.nextInt(distinctAmount);
			values[i].set(value, ++i);
			if(++d == distinctSegment) d = 0;
		}

		return values;
	}
	public static SortValue[] fewUniqueBulkAlreadySorted(final SortValue[] values, final int distinctAmount)
	{
		final int distinctSegment = values.length/distinctAmount;
		int value = 1;
		for(int i = 0, d = 0; i < values.length;)
		{
			if(d == 0) value++;
			values[i].set(value, ++i);
			if(++d == distinctSegment) d = 0;
		}

		return values;
	}
	public static SortValue[] uniques(final SortValue[] values)
	{
		alreadySorted(values);
		for(int i = 0; i < values.length; i++)
		{
			swap(values, i, RND.nextInt(values.length));
		}
		return values;
	}

	public static SortValue[] sawtoothForward(final SortValue[] values, final int toothCount)
	{
		final int max = values.length / toothCount;
		for(int i = 0, s = 0; i < values.length;)
		{
			values[i].set(++s == max ? s = 0 : s, ++i);
		}
		return values;
	}

	public static SortValue[] sawtoothForwardLifted(final SortValue[] values, final int toothCount)
	{
		final int max = values.length / toothCount;
		for(int i = 0, s = 0, offset=0; i < values.length; i++)
		{
			if(s == 0) offset = RND.nextInt(101);
			values[i].set((++s == max ? s = 0 : s) + offset, ++i);
		}
		return values;
	}

	public static SortValue[] nearlySorted(final SortValue[] values)
	{
		alreadySorted(values);
		for(int i = 1, len = values.length - 3; i < len; i+=3)
		{
			final SortValue t = values[i];
			final int r = i+(RND.nextInt(100) < 50 ? -1 : +1);
			values[i] = values[r];
			values[r] = t;
		}
		return values;
	}

	private static void swap(final SortValue[] values, final int l, final int r)
	{
		final SortValue t = values[l];
		values[l] = values[r];
		values[r] = t;
	}



	static final int unstableCompare(final SortValue o1, final SortValue o2) {
		return o1.value > o2.value ? 1 : o1.value < o2.value ? -1 : 0;
	}
	static final int stableCompare(final SortValue o1, final SortValue o2) {
		return o1.value > o2.value
		? 1
			: o1.value < o2.value
			? -1
				: o1.order > o2.order
				? 1
					: o1.order < o2.order
					? -1
						: 0;
	}

	static final boolean isSorted(final SortValue[] values)
	{
		for(int i = 1; i < values.length; i++)
		{
			if(compare.compare(values[i-1], values[i]) > 0)
			{
				return false;
			}
		}
		return true;
	}
	static final boolean isStableSorted(final SortValue[] values)
	{
		for(int i = 1; i < values.length; i++)
		{
			if(stableCompare.compare(values[i-1], values[i]) > 0)
			{
				return false;
			}
		}
		return true;
	}


	public static final void print(final SortValue[] values)
	{
		print("", DEFAULT_PRINT_LENGTH, values, false);
	}
	public static final void print(final String s, final SortValue[] values)
	{
		print(s, DEFAULT_PRINT_LENGTH, values, false);
	}
	public static final void print(final int limit, final SortValue[] values)
	{
		print("", limit, values, false);
	}
	public static final void print(final String s, final int limit, final SortValue[] values)
	{
		print(s, limit, values, false);
	}

	public static final void printStable(final SortValue[] values)
	{
		print("", DEFAULT_PRINT_LENGTH, values, true);
	}
	public static final void printStable(final String s, final SortValue[] values)
	{
		print(s, DEFAULT_PRINT_LENGTH, values, true);
	}
	public static final void printStable(final int limit, final SortValue[] values)
	{
		print("", limit, values, true);
	}
	public static final void printStable(final String s, final int limit, final SortValue[] values)
	{
		print(s, limit, values, true);
	}



	public static final void printSimple(final String s, int limit, final SortValue[] values)
	{
		if(limit > values.length) limit = values.length;
		VS.clear();
		VS.add(s).append(' ');
		VS.append('[');
		for(int i = 0; i < limit; i++)
		{
			VS.add(values[i]).add(',', ' ');
		}
		(limit < values.length ? VS.add("... ").add(values[values.length-1]) : VS.deleteLast(2))
		.append(']')
		.append('(').add(values.length).append(')');
		System.out.println(VS.toString());
	}


	public static final void print(final String s, int limit, final SortValue[] values, final boolean stable)
	{
		if(limit > values.length) limit = values.length;
		VS.clear();
		VS.add(s).append(' ');

		if(isStableSorted(values)) VS.add(" STABLE\t");
		else if(isSorted(values))  VS.add(" sorted\t");
		else                       VS.add("       \t");


		VS.append('[');
		for(int i = 0; i < limit; i++)
		{
			VS.add(stable ? values[i].toOrderString() : values[i].toString()).add(',', ' ');
		}
		(limit < values.length ? VS.add("... ").add(values[values.length-1]) : VS.deleteLast(2))
		.append(']')
		.append('(').add(values.length).append(')');
		System.out.println(VS.toString());
	}



	private int value;
	private int order;

	public SortValue(final int value, final int order)
	{
		super();
		this.value = value;
		this.order = order;
	}

	@Override
	public String toString()
	{
		return Integer.toString(this.value, 10);
	}

	public String toOrderString()
	{
		return Integer.toString(this.value, 10)+'('+Integer.toString(this.order, 10)+')';
	}

	public void set(final int value, final int order)
	{
		this.value = value;
		this.order = order;
	}

}
